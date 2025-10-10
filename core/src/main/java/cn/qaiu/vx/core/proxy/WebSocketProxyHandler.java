package cn.qaiu.vx.core.proxy;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket代理处理器
 * 实现WebSocket连接的双向代理转发
 * 
 * @author QAIU
 */
public class WebSocketProxyHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketProxyHandler.class);
    
    private final HttpClient httpClient;
    
    public WebSocketProxyHandler(Vertx vertx) {
        this.httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setConnectTimeout(10000)
                .setIdleTimeout(30)
                .setKeepAlive(true));
    }
    
    /**
     * 处理WebSocket代理连接
     * 
     * @param clientSocket 客户端WebSocket连接
     * @param targetOrigin 目标服务器地址
     * @param targetPath 目标路径
     */
    public void handleWebSocketProxy(ServerWebSocket clientSocket, String targetOrigin, String targetPath) {
        LOGGER.info("Starting WebSocket proxy: {} -> {}:{}", 
                clientSocket.path(), targetOrigin, targetPath);
        
        // 解析目标地址
        URI targetUri;
        try {
            targetUri = new URI("ws://" + targetOrigin + targetPath);
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid target URI: {}:{}", targetOrigin, targetPath, e);
            clientSocket.close((short) 1002, "Invalid target URI");
            return;
        }
        
        // 构建WebSocket连接选项
        WebSocketConnectOptions connectOptions = new WebSocketConnectOptions()
                .setURI(targetPath)
                .setHost(targetUri.getHost())
                .setPort(targetUri.getPort() == -1 ? 80 : targetUri.getPort())
                .setTimeout(10000);
        
        // 复制请求头
        clientSocket.headers().forEach(entry -> {
            if (!isWebSocketUpgradeHeader(entry.getKey())) {
                connectOptions.addHeader(entry.getKey(), entry.getValue());
            }
        });
        
        // 连接到目标服务器
        httpClient.webSocket(connectOptions)
                .onSuccess(targetSocket -> {
                    LOGGER.debug("WebSocket proxy connection established: {} -> {}", 
                            clientSocket.path(), targetUri);
                    
                    // 设置双向消息转发
                    setupBidirectionalProxy(clientSocket, targetSocket);
                    
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to connect to target WebSocket: {}", targetUri, error);
                    clientSocket.close((short) 1006, "Failed to connect to target server");
                });
    }
    
    /**
     * 设置双向代理转发
     */
    private void setupBidirectionalProxy(ServerWebSocket clientSocket, WebSocket targetSocket) {
        // 客户端 -> 目标服务器
        clientSocket.textMessageHandler(message -> {
            LOGGER.debug("Forwarding text message: client -> target");
            targetSocket.writeTextMessage(message);
        });
        
        clientSocket.binaryMessageHandler(message -> {
            LOGGER.debug("Forwarding binary message: client -> target");
            targetSocket.writeBinaryMessage(message);
        });
        
        clientSocket.pongHandler(message -> {
            LOGGER.debug("Forwarding pong: client -> target");
            targetSocket.writePong(message);
        });
        
        // 目标服务器 -> 客户端
        targetSocket.textMessageHandler(message -> {
            LOGGER.debug("Forwarding text message: target -> client");
            clientSocket.writeTextMessage(message);
        });
        
        targetSocket.binaryMessageHandler(message -> {
            LOGGER.debug("Forwarding binary message: target -> client");
            clientSocket.writeBinaryMessage(message);
        });
        
        targetSocket.pongHandler(message -> {
            LOGGER.debug("Forwarding pong: target -> client");
            clientSocket.writePong(message);
        });
        
        // 处理连接关闭
        clientSocket.closeHandler(code -> {
            LOGGER.debug("Client WebSocket closed, closing target connection");
            targetSocket.close();
        });
        
        targetSocket.closeHandler(code -> {
            LOGGER.debug("Target WebSocket closed, closing client connection");
            clientSocket.close();
        });
        
        // 处理异常
        clientSocket.exceptionHandler(error -> {
            LOGGER.error("Client WebSocket error", error);
            targetSocket.close();
        });
        
        targetSocket.exceptionHandler(error -> {
            LOGGER.error("Target WebSocket error", error);
            clientSocket.close();
        });
        
        // 处理ping/pong - 注意：Vert.x WebSocket API中没有pingHandler
        // Ping/Pong消息通过pongHandler处理
    }
    
    /**
     * 检查是否为WebSocket升级相关头部
     */
    private boolean isWebSocketUpgradeHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("connection") || 
               lowerName.equals("upgrade") || 
               lowerName.equals("sec-websocket-key") ||
               lowerName.equals("sec-websocket-version") ||
               lowerName.equals("sec-websocket-protocol") ||
               lowerName.equals("sec-websocket-extensions");
    }
    
    /**
     * 关闭代理处理器
     */
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
