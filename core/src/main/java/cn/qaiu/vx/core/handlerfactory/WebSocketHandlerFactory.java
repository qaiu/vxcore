package cn.qaiu.vx.core.handlerfactory;

import cn.qaiu.vx.core.annotaions.websocket.*;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器工厂
 * 负责扫描和注册WebSocket处理器
 * 
 * @author QAIU
 */
public class WebSocketHandlerFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandlerFactory.class);
    
    private final Vertx vertx;
    private final Map<String, WebSocketHandlerInfo> handlers = new ConcurrentHashMap<>();
    
    public WebSocketHandlerFactory(Vertx vertx) {
        this.vertx = vertx;
    }
    
    /**
     * 注册WebSocket处理器
     */
    public void registerWebSocketHandler(Class<?> handlerClass) {
        if (!handlerClass.isAnnotationPresent(WebSocketHandler.class)) {
            return;
        }
        
        WebSocketHandler annotation = handlerClass.getAnnotation(WebSocketHandler.class);
        if (!annotation.enabled()) {
            LOGGER.debug("WebSocket handler {} is disabled, skipping", handlerClass.getSimpleName());
            return;
        }
        
        try {
            Object instance = handlerClass.getDeclaredConstructor().newInstance();
            WebSocketHandlerInfo handlerInfo = new WebSocketHandlerInfo();
            handlerInfo.setInstance(instance);
            handlerInfo.setPath(annotation.value());
            handlerInfo.setDescription(annotation.description());
            handlerInfo.setOrder(annotation.order());
            
            // 扫描事件处理方法
            scanEventMethods(handlerClass, handlerInfo);
            
            String path = normalizePath(handlerInfo.getPath());
            handlers.put(path, handlerInfo);
            
            LOGGER.info("Registered WebSocket handler: {} -> {}", handlerClass.getSimpleName(), path);
            
        } catch (Exception e) {
            LOGGER.error("Failed to register WebSocket handler: {}", handlerClass.getSimpleName(), e);
        }
    }
    
    /**
     * 扫描事件处理方法
     */
    private void scanEventMethods(Class<?> handlerClass, WebSocketHandlerInfo handlerInfo) {
        Method[] methods = handlerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnOpen.class)) {
                handlerInfo.setOnOpenMethod(method);
                LOGGER.debug("Found @OnOpen method: {}", method.getName());
            } else if (method.isAnnotationPresent(OnMessage.class)) {
                handlerInfo.addOnMessageMethod(method);
                LOGGER.debug("Found @OnMessage method: {}", method.getName());
            } else if (method.isAnnotationPresent(OnClose.class)) {
                handlerInfo.setOnCloseMethod(method);
                LOGGER.debug("Found @OnClose method: {}", method.getName());
            } else if (method.isAnnotationPresent(OnError.class)) {
                handlerInfo.addOnErrorMethod(method);
                LOGGER.debug("Found @OnError method: {}", method.getName());
            }
        }
    }
    
    /**
     * 注册所有WebSocket处理器到路由器
     */
    public void registerToRouter(Router router, String gatewayPrefix) {
        // 按order排序
        List<WebSocketHandlerInfo> sortedHandlers = new ArrayList<>(handlers.values());
        sortedHandlers.sort(Comparator.comparingInt(WebSocketHandlerInfo::getOrder));
        
        for (WebSocketHandlerInfo handlerInfo : sortedHandlers) {
            registerHandlerToRouter(router, handlerInfo, gatewayPrefix);
        }
    }
    
    /**
     * 注册单个处理器到路由器
     */
    private void registerHandlerToRouter(Router router, WebSocketHandlerInfo handlerInfo, String gatewayPrefix) {
        String path = buildFullPath(gatewayPrefix, handlerInfo.getPath());
        
        LOGGER.info("Registering WebSocket handler at path: {}", path);
        
        SockJSHandlerOptions options = new SockJSHandlerOptions()
                .setHeartbeatInterval(2000)
                .setRegisterWriteHandler(true);
        
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        Router subRouter = sockJSHandler.socketHandler(sock -> {
            handleWebSocketConnection(sock, handlerInfo);
        });
        
        router.route(path + "*").subRouter(subRouter);
    }
    
    /**
     * 处理WebSocket连接
     */
    private void handleWebSocketConnection(io.vertx.ext.web.handler.sockjs.SockJSSocket socket, WebSocketHandlerInfo handlerInfo) {
        LOGGER.debug("New WebSocket connection: {}", socket.uri());
        
        // 处理连接建立事件
        if (handlerInfo.getOnOpenMethod() != null) {
            invokeEventMethod(handlerInfo.getOnOpenMethod(), handlerInfo.getInstance(), socket, null);
        }
        
        // 设置消息处理器
        socket.handler(message -> {
            handleMessage(socket, handlerInfo, message.toString(), OnMessage.MessageType.TEXT);
        });
        
        // 设置关闭处理器
        socket.closeHandler(v -> {
            if (handlerInfo.getOnCloseMethod() != null) {
                invokeEventMethod(handlerInfo.getOnCloseMethod(), handlerInfo.getInstance(), socket, null);
            }
        });
        
        // 设置异常处理器
        socket.exceptionHandler(throwable -> {
            handleError(socket, handlerInfo, throwable);
        });
    }
    
    /**
     * 处理消息
     */
    private void handleMessage(io.vertx.ext.web.handler.sockjs.SockJSSocket socket, WebSocketHandlerInfo handlerInfo, 
                              String message, OnMessage.MessageType messageType) {
        List<Method> messageMethods = handlerInfo.getOnMessageMethods();
        
        for (Method method : messageMethods) {
            OnMessage annotation = method.getAnnotation(OnMessage.class);
            if (annotation.type() == messageType) {
                invokeEventMethod(method, handlerInfo.getInstance(), socket, message);
                break; // 只调用第一个匹配的方法
            }
        }
    }
    
    /**
     * 处理错误
     */
    private void handleError(io.vertx.ext.web.handler.sockjs.SockJSSocket socket, WebSocketHandlerInfo handlerInfo, Throwable error) {
        List<Method> errorMethods = handlerInfo.getOnErrorMethods();
        
        for (Method method : errorMethods) {
            OnError annotation = method.getAnnotation(OnError.class);
            Class<? extends Throwable>[] errorTypes = annotation.value();
            
            for (Class<? extends Throwable> errorType : errorTypes) {
                if (errorType.isAssignableFrom(error.getClass())) {
                    invokeEventMethod(method, handlerInfo.getInstance(), socket, error);
                    return; // 只调用第一个匹配的方法
                }
            }
        }
        
        // 如果没有匹配的错误处理器，记录错误
        LOGGER.error("Unhandled WebSocket error", error);
    }
    
    /**
     * 调用事件方法
     */
    private void invokeEventMethod(Method method, Object instance, io.vertx.ext.web.handler.sockjs.SockJSSocket socket, Object data) {
        try {
            method.setAccessible(true);
            
            // 根据方法参数类型调用
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i] == io.vertx.ext.web.handler.sockjs.SockJSSocket.class) {
                    args[i] = socket;
                } else if (paramTypes[i] == String.class && data instanceof String) {
                    args[i] = data;
                } else if (paramTypes[i] == Throwable.class && data instanceof Throwable) {
                    args[i] = data;
                } else {
                    args[i] = null;
                }
            }
            
            method.invoke(instance, args);
            
        } catch (Exception e) {
            LOGGER.error("Failed to invoke WebSocket event method: {}", method.getName(), e);
        }
    }
    
    /**
     * 构建完整路径
     */
    private String buildFullPath(String gatewayPrefix, String handlerPath) {
        StringBuilder fullPath = new StringBuilder();
        
        // 添加网关前缀
        if (StringUtils.isNotEmpty(gatewayPrefix)) {
            fullPath.append(gatewayPrefix.startsWith("/") ? gatewayPrefix : "/" + gatewayPrefix);
            if (!fullPath.toString().endsWith("/")) {
                fullPath.append("/");
            }
        }
        
        // 添加处理器路径
        if (StringUtils.isNotEmpty(handlerPath)) {
            String path = handlerPath.startsWith("/") ? handlerPath.substring(1) : handlerPath;
            fullPath.append(path);
        }
        
        return fullPath.toString();
    }
    
    /**
     * 标准化路径
     */
    private String normalizePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "/";
        }
        
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        return path;
    }
    
    /**
     * 获取所有注册的处理器
     */
    public Map<String, WebSocketHandlerInfo> getHandlers() {
        return new HashMap<>(handlers);
    }
    
    /**
     * WebSocket处理器信息
     */
    public static class WebSocketHandlerInfo {
        private Object instance;
        private String path;
        private String description;
        private int order;
        private Method onOpenMethod;
        private List<Method> onMessageMethods = new ArrayList<>();
        private Method onCloseMethod;
        private List<Method> onErrorMethods = new ArrayList<>();
        
        // Getters and Setters
        public Object getInstance() { return instance; }
        public void setInstance(Object instance) { this.instance = instance; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        
        public Method getOnOpenMethod() { return onOpenMethod; }
        public void setOnOpenMethod(Method onOpenMethod) { this.onOpenMethod = onOpenMethod; }
        
        public List<Method> getOnMessageMethods() { return onMessageMethods; }
        public void addOnMessageMethod(Method method) { this.onMessageMethods.add(method); }
        
        public Method getOnCloseMethod() { return onCloseMethod; }
        public void setOnCloseMethod(Method onCloseMethod) { this.onCloseMethod = onCloseMethod; }
        
        public List<Method> getOnErrorMethods() { return onErrorMethods; }
        public void addOnErrorMethod(Method method) { this.onErrorMethods.add(method); }
    }
}
