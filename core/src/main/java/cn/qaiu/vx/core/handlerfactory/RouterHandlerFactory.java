package cn.qaiu.vx.core.handlerfactory;

import cn.qaiu.vx.core.annotaions.DateFormat;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.SockRouteMapper;
import cn.qaiu.vx.core.annotaions.websocket.WebSocketHandler;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.interceptor.BeforeInterceptor;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.exception.ExceptionHandlerManager;
import cn.qaiu.vx.core.exception.BuiltinExceptionHandler;
import cn.qaiu.vx.core.exception.BusinessException;
import cn.qaiu.vx.core.exception.ValidationException;
import cn.qaiu.vx.core.exception.SystemException;
import cn.qaiu.vx.core.util.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import javassist.CtClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.qaiu.vx.core.util.ConfigConstant.ROUTE_TIME_OUT;
import static cn.qaiu.vx.core.verticle.ReverseProxyVerticle.REROUTE_PATH_PREFIX;
import static io.vertx.core.http.HttpHeaders.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * 路由映射, 参数绑定
 * <br>Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterHandlerFactory implements BaseHttpApi {
    
    private final WebSocketHandlerFactory webSocketHandlerFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterHandlerFactory.class);

    private static final Set<HttpMethod> httpMethods = new HashSet<>() {{
        add(HttpMethod.GET);
        add(HttpMethod.POST);
        add(HttpMethod.OPTIONS);
        add(HttpMethod.PUT);
        add(HttpMethod.DELETE);
        add(HttpMethod.HEAD);
    }};

    private final String gatewayPrefix;
    private final ExceptionHandlerManager exceptionHandlerManager;

    public RouterHandlerFactory(String gatewayPrefix) {
        this.webSocketHandlerFactory = new WebSocketHandlerFactory(VertxHolder.getVertxInstance());
        Objects.requireNonNull(gatewayPrefix, "The gateway prefix is empty.");
        this.gatewayPrefix = gatewayPrefix;
        this.exceptionHandlerManager = new ExceptionHandlerManager();
        initBuiltinExceptionHandlers();
    }
    
    /**
     * 初始化内置异常处理器
     */
    private void initBuiltinExceptionHandlers() {
        // 注册内置异常处理器
        exceptionHandlerManager.registerGlobalHandler(BusinessException.class, new BuiltinExceptionHandler());
        exceptionHandlerManager.registerGlobalHandler(ValidationException.class, new BuiltinExceptionHandler());
        exceptionHandlerManager.registerGlobalHandler(SystemException.class, new BuiltinExceptionHandler());
        exceptionHandlerManager.registerGlobalHandler(IllegalArgumentException.class, new BuiltinExceptionHandler());
        exceptionHandlerManager.registerGlobalHandler(NullPointerException.class, new BuiltinExceptionHandler());
        exceptionHandlerManager.registerGlobalHandler(RuntimeException.class, new BuiltinExceptionHandler());
    }

    /**
     * 开始扫描并注册handler
     */
    public Router createRouter() {
        Router mainRouter = createMainRouter();
        setupGlobalHandlers(mainRouter);
        registerRouteHandlers(mainRouter);
        registerWebSocketHandlers(mainRouter);
        setupErrorHandlers(mainRouter);
        return mainRouter;
    }

    /**
     * 创建主路由器
     */
    private Router createMainRouter() {
        return Router.router(VertxHolder.getVertxInstance());
    }

    /**
     * 设置全局处理器
     */
    private void setupGlobalHandlers(Router router) {
        // 请求重写处理
        router.route().handler(this::handleRequestRewrite);
        
        // CORS处理
        router.route().handler(CorsHandler.create()
                .addRelativeOrigin(".*")
                .allowCredentials(true)
                .allowedMethods(httpMethods));
        
        // 文件上传处理
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));
        
        // 拦截器
        setupInterceptors(router);
    }

    /**
     * 处理请求重写
     */
    private void handleRequestRewrite(RoutingContext ctx) {
        String realPath = ctx.request().uri();
        if (realPath.startsWith(REROUTE_PATH_PREFIX)) {
            String rePath = realPath.replace(REROUTE_PATH_PREFIX, "");
            ctx.reroute(rePath);
            return;
        }

        LOGGER.debug("New request:{}, {}, {}",
                ctx.request().path(), ctx.request().absoluteURI(), ctx.request().method());
        
        // 设置CORS头
        setCorsHeaders(ctx);
        ctx.next();
    }

    /**
     * 设置CORS头
     */
    private void setCorsHeaders(RoutingContext ctx) {
        ctx.response().headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        ctx.response().headers().add(DATE, LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
        ctx.response().headers().add(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        ctx.response().headers().add(ACCESS_CONTROL_ALLOW_HEADERS, 
                "X-PINGOTHER, Origin,Content-Type, Accept, X-Requested-With, Dev, Authorization, Version, Token");
        ctx.response().headers().add(ACCESS_CONTROL_MAX_AGE, "1728000");
    }

    /**
     * 设置拦截器
     */
    private void setupInterceptors(Router router) {
        Set<Handler<RoutingContext>> interceptorSet = getInterceptorSet();
        Route route = router.route("/*");
        interceptorSet.forEach(route::handler);
    }

    /**
     * 注册路由处理器
     */
    private void registerRouteHandlers(Router router) {
        try {
            Set<Class<?>> handlers = reflections.getTypesAnnotatedWith(RouteHandler.class);
            List<Class<?>> sortedHandlers = sortHandlersByOrder(handlers);
            
            for (Class<?> handler : sortedHandlers) {
                try {
                    registerHandler(router, handler);
                } catch (Throwable e) {
                    LOGGER.error("Error register {}, Error details：", handler, e.getCause());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Manually Register Handler Fail, Error details：" + e.getMessage());
        }
    }

    /**
     * 按优先级排序处理器
     */
    private List<Class<?>> sortHandlersByOrder(Set<Class<?>> handlers) {
        Comparator<Class<?>> comparator = (c1, c2) -> {
            RouteHandler routeHandler1 = c1.getAnnotation(RouteHandler.class);
            RouteHandler routeHandler2 = c2.getAnnotation(RouteHandler.class);
            return Integer.compare(routeHandler2.order(), routeHandler1.order());
        };
        return handlers.stream().sorted(comparator).toList();
    }

    /**
     * 设置错误处理器
     */
    private void setupErrorHandlers(Router router) {
        router.errorHandler(405, ctx -> 
                doFireJsonResultResponse(ctx, JsonResult.error("Method Not Allowed", 405)));
        router.errorHandler(404, ctx -> 
                ctx.response().setStatusCode(404).setChunked(true).end("Internal server error: 404 not found"));
    }

    /**
     * 注册单个处理器
     */
    private void registerHandler(Router router, Class<?> handler) throws Throwable {
        String root = getRootPath(handler);
        Object instance = ReflectionUtil.newWithNoParam(handler);
        Method[] methods = handler.getMethods();
        
        // 获取路由方法
        List<Method> routeMethods = getRouteMethods(methods);
        
        // 注册异常处理器
        registerExceptionHandlers(handler, methods);
        
        // 按路由分组并注册
        Map<String, List<Method>> routeGroups = groupMethodsByRoute(routeMethods, root);
        registerRouteGroups(router, instance, routeGroups);
        
        // 注册WebSocket处理器
        if (instance.getClass().isAnnotationPresent(WebSocketHandler.class)) {
            webSocketHandlerFactory.registerWebSocketHandler(instance.getClass());
        }
    }

    /**
     * 获取路由方法
     */
    private List<Method> getRouteMethods(Method[] methods) {
        Comparator<Method> comparator = (m1, m2) -> {
            RouteMapping mapping1 = m1.getAnnotation(RouteMapping.class);
            RouteMapping mapping2 = m2.getAnnotation(RouteMapping.class);
            return Integer.compare(mapping2.order(), mapping1.order());
        };
        
        List<Method> methodList = Stream.of(methods)
                .filter(method -> method.isAnnotationPresent(RouteMapping.class))
                .sorted(comparator)
                .collect(Collectors.toList());

        methodList.addAll(Stream.of(methods)
                .filter(method -> method.isAnnotationPresent(SockRouteMapper.class))
                .toList());
        
        return methodList;
    }

    /**
     * 注册异常处理器
     */
    private void registerExceptionHandlers(Class<?> handler, Method[] methods) {
        for (Method method : methods) {
            if (method.isAnnotationPresent(cn.qaiu.vx.core.annotaions.exception.ExceptionHandler.class)) {
                exceptionHandlerManager.registerLocalHandler(handler, method);
            }
        }
    }

    /**
     * 注册路由组
     */
    private void registerRouteGroups(Router router, Object instance, Map<String, List<Method>> routeGroups) {
        for (Map.Entry<String, List<Method>> entry : routeGroups.entrySet()) {
            String routeKey = entry.getKey();
            List<Method> routeMethods = entry.getValue();
            
            if (routeMethods.isEmpty()) continue;
            
            Method firstMethod = routeMethods.get(0);
            if (firstMethod.isAnnotationPresent(RouteMapping.class)) {
                registerHttpRoute(router, instance, routeKey, routeMethods);
            } else if (firstMethod.isAnnotationPresent(SockRouteMapper.class)) {
                registerWebSocketRoute(router, instance, firstMethod);
            }
        }
    }

    /**
     * 注册HTTP路由
     */
    private void registerHttpRoute(Router router, Object instance, String routeKey, List<Method> routeMethods) {
        Method firstMethod = routeMethods.get(0);
        RouteMapping mapping = firstMethod.getAnnotation(RouteMapping.class);
        
        // 解析路由键
        String[] parts = routeKey.split(":");
        HttpMethod method = HttpMethod.valueOf(parts[0]);
        String path = parts[1];
        
        // 转换路径变量格式：{variable} -> :variable
        String vertxPath = convertPathVariables(path);
        
        // 创建路由
        Route route = router.route(method, vertxPath);
        configureHttpRoute(route, mapping, routeMethods, method, path);
        
        // 设置处理器
        route.handler(ctx -> handlerMethodWithOverload(instance, routeMethods, ctx))
             .failureHandler(ctx -> handleRouteFailure(ctx));
    }
    
    /**
     * 转换路径变量格式
     * 将Spring风格的{userId}转换为Vert.x风格的:userId
     * 同时支持Vert.x原生的:userId格式
     * 
     * @param path 原始路径
     * @return 转换后的路径
     */
    private String convertPathVariables(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        
        // 转换{userId}格式为:userId格式
        String converted = path.replaceAll("\\{([^}]+)\\}", ":$1");
        
        LOGGER.debug("Path variable conversion: {} -> {}", path, converted);
        return converted;
    }

    /**
     * 配置HTTP路由
     */
    private void configureHttpRoute(Route route, RouteMapping mapping, List<Method> routeMethods, HttpMethod method, String path) {
        String mineType = mapping.requestMIMEType().getValue();
        LOGGER.info("route -> {}:{} -> {} ({} methods)", 
                method.name(), path, mineType, routeMethods.size());
        
        if (StringUtils.isNotEmpty(mineType)) {
            route.consumes(mineType);
        }
        
        // 设置默认超时和响应时间
        route.handler(TimeoutHandler.create(SharedDataUtil.getCustomConfig().getInteger(ROUTE_TIME_OUT)));
        route.handler(ResponseTimeHandler.create());
    }

    /**
     * 注册WebSocket路由
     */
    private void registerWebSocketRoute(Router router, Object instance, Method method) {
        SockRouteMapper mapping = method.getAnnotation(SockRouteMapper.class);
        String routeUrl = getRouteUrl(mapping.value());
        String root = getRootPath(method.getDeclaringClass());
        String url = root.concat(routeUrl);
        
        LOGGER.info("Register New Websocket Handler -> {}", url);
        
        SockJSHandlerOptions options = new SockJSHandlerOptions()
                .setHeartbeatInterval(2000)
                .setRegisterWriteHandler(true);

        SockJSHandler sockJSHandler = SockJSHandler.create(VertxHolder.getVertxInstance(), options);
        Router route = sockJSHandler.socketHandler(sock -> {
            try {
                ReflectionUtil.invokeWithArguments(method, instance, sock);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        
        if (url.endsWith("*")) {
            throw new IllegalArgumentException("Don't include * when mounting a sub router");
        }
        router.route(url + "*").subRouter(route);
    }

    /**
     * 注册WebSocket处理器
     */
    private void registerWebSocketHandlers(Router router) {
        webSocketHandlerFactory.registerToRouter(router, gatewayPrefix);
    }
    
    /**
     * 处理路由失败
     */
    private void handleRouteFailure(RoutingContext ctx) {
        if (ctx.response().ended()) return;
        
        Throwable failure = ctx.failure();
        if (failure != null) {
            exceptionHandlerManager.handleException(failure, ctx, null);
        } else {
            doFireJsonResultResponse(ctx, JsonResult.error("请求处理超时", 503), 503);
        }
    }


    /**
     * 获取并处理路由URL分隔符
     *
     * @return String
     */
    private String getRouteUrl(String mapperValue) {
        String routeUrl;
        if ("/".equals(mapperValue)) {
            routeUrl = mapperValue;
        } else if (mapperValue.startsWith("/")) {
            routeUrl = mapperValue.substring(1);
        } else {
            routeUrl = mapperValue;
        }
        return routeUrl;
    }

    /**
     * 配置拦截
     *
     * @return Handler
     */
    private Set<Handler<RoutingContext>> getInterceptorSet() {
        // 配置拦截
        return getBeforeInterceptor().stream().map(BeforeInterceptor::doHandle).collect(Collectors.toSet());
    }

    /**
     * 按路由分组方法，支持方法重载
     * 
     * @param methods 方法列表
     * @return 按路由键分组的方法映射
     */
    private Map<String, List<Method>> groupMethodsByRoute(List<Method> methods, String root) {
        Map<String, List<Method>> groups = new LinkedHashMap<>();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(RouteMapping.class)) {
                RouteMapping mapping = method.getAnnotation(RouteMapping.class);
                String routeKey = mapping.method().name() + ":" + root + getRouteUrl(mapping.value());
                groups.computeIfAbsent(routeKey, k -> new ArrayList<>()).add(method);
            } else if (method.isAnnotationPresent(SockRouteMapper.class)) {
                SockRouteMapper mapping = method.getAnnotation(SockRouteMapper.class);
                String routeKey = "SOCKJS:" + root + getRouteUrl(mapping.value());
                groups.computeIfAbsent(routeKey, k -> new ArrayList<>()).add(method);
            }
        }
        
        return groups;
    }
    
    /**
     * 获取请求根路径
     *
     * @param handler handler
     * @return 根路径
     */
    private String getRootPath(Class<?> handler) {
        // 处理请求路径前缀和后缀
        String root = gatewayPrefix;
        if (!root.startsWith("/")) {
            root = "/" + root;
        }
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        // 子路径
        if (handler.isAnnotationPresent(RouteHandler.class)) {
            RouteHandler routeHandler = handler.getAnnotation(RouteHandler.class);
            String value = routeHandler.value();
            root += (value.startsWith("/") ? value.substring(1) : value);
        }
        if (!root.endsWith("/")) {
            root = root + "/";
        }

        return root;
    }

    
    /**
     * 处理请求-支持方法重载的参数绑定
     *
     * @param instance 类实例
     * @param methods  候选方法列表
     * @param ctx      路由上下文
     */
    private void handlerMethodWithOverload(Object instance, List<Method> methods, RoutingContext ctx) {
        // 使用参数匹配器选择最佳方法
        ParameterMatcher.MatchResult matchResult = ParameterMatcher.findBestMatch(methods, ctx);
        
        if (matchResult == null) {
            doFireJsonResultResponse(ctx, JsonResult.error("No suitable method found for the request"), 400);
            return;
        }
        
        // 使用选中的方法和参数
        handlerMethodWithParams(instance, matchResult.getMethod(), matchResult.getParameters(), ctx);
    }
    
    /**
     * 处理请求-使用预提取的参数
     */
    private void handlerMethodWithParams(Object instance, Method method, Object[] params, RoutingContext ctx) {
        try {
            Object data = ReflectionUtil.invokeWithArguments(method, instance, params);
            handleMethodResult(data, ctx);
        } catch (Throwable e) {
            handleMethodException(e, ctx);
        }
    }
    
    /**
     * 处理请求-使用预计算参数的参数绑定
     */
    private void handlerMethodWithParams(Object instance, Method method, Map<String, Object> parameters, RoutingContext ctx) {
        Object[] args = parameters.values().toArray();
        
        try {
            Object result = ReflectionUtil.invokeWithArguments(method, instance, args);
            if (result instanceof Future) {
                ((Future<?>) result).onComplete(ar -> {
                    if (ar.succeeded()) {
                        doFireJsonResultResponse(ctx, JsonResult.data(ar.result()));
                    } else {
                        doFireJsonResultResponse(ctx, JsonResult.error(ar.cause().getMessage()));
                    }
                });
            } else {
                doFireJsonResultResponse(ctx, JsonResult.data(result));
            }
        } catch (Throwable e) {
            exceptionHandlerManager.handleException(e, ctx, null);
        }
    }

    /**
     * 处理方法结果
     */
    private void handleMethodResult(Object data, RoutingContext ctx) {
        if (data == null) return;
        
        if (data instanceof JsonResult jsonResult) {
            doFireJsonResultResponse(ctx, jsonResult, jsonResult.getCode());
        } else if (data instanceof JsonObject) {
            doFireJsonObjectResponse(ctx, ((JsonObject) data));
        } else if (data instanceof Future) {
            handleAsyncResult((Future<?>) data, ctx);
        } else {
            doFireJsonResultResponse(ctx, JsonResult.data(data));
        }
    }

    /**
     * 处理异步结果
     */
    private void handleAsyncResult(Future<?> future, RoutingContext ctx) {
        future.onSuccess(res -> {
            if (res instanceof JsonResult jsonResult) {
                doFireJsonResultResponse(ctx, jsonResult, jsonResult.getCode());
            } else if (res instanceof JsonObject) {
                doFireJsonObjectResponse(ctx, ((JsonObject) res));
            } else if (res != null) {
                doFireJsonResultResponse(ctx, JsonResult.data(res));
            } else {
                doFireJsonResultResponse(ctx, JsonResult.data(null));
            }
        }).onFailure(e -> doFireJsonResultResponse(ctx, JsonResult.error(e.getMessage()), 500));
    }

    /**
     * 处理方法异常
     */
    private void handleMethodException(Throwable e, RoutingContext ctx) {
        e.printStackTrace();
        String err = e.getMessage();
        if (e.getCause() != null) {
            if (e.getCause() instanceof InvocationTargetException) {
                err = ((InvocationTargetException) e.getCause()).getTargetException().getMessage();
            } else {
                err = e.getCause().getMessage();
            }
        }
        doFireJsonResultResponse(ctx, JsonResult.error(err), 500);
    }

    /**
     * 获取DateFormat注解值
     */
    private String getFmt(Annotation[] parameterAnnotations, CtClass v) {
        String fmt = "";
        if (Date.class.getUsername().equals(v.getUsername())) {
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof DateFormat) {
                    fmt = ((DateFormat) annotation).value();
                }
            }
        }
        return fmt;
    }

    private Set<BeforeInterceptor> getBeforeInterceptor() {
        Set<Class<? extends BeforeInterceptor>> interceptorClassSet =
                reflections.getSubTypesOf(BeforeInterceptor.class);
        if (interceptorClassSet == null) {
            return new HashSet<>();
        }
        return CommonUtil.sortClassSet(interceptorClassSet);
    }
}
