package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.util.ConfigUtil;
import cn.qaiu.vx.core.util.VertxHolder;
import cn.qaiu.vx.core.verticle.*;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 框架生命周期管理器
 * 使用组合模式管理框架的启动、配置、服务注册等生命周期
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class FrameworkLifecycleManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkLifecycleManager.class);
    
    private static final AtomicReference<FrameworkLifecycleManager> INSTANCE = new AtomicReference<>();
    
    private final List<LifecycleComponent> components = new ArrayList<>();
    private final List<Verticle> verticles = new ArrayList<>();
    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.INITIAL);
    
    private Vertx vertx;
    private JsonObject globalConfig;
    private Handler<JsonObject> userHandler;
    
    private FrameworkLifecycleManager() {
        initializeComponents();
    }
    
    /**
     * 获取单例实例
     */
    public static FrameworkLifecycleManager getInstance() {
        return INSTANCE.updateAndGet(manager -> 
            manager == null ? new FrameworkLifecycleManager() : manager);
    }
    
    /**
     * 重置实例（仅用于测试）
     */
    public static void resetInstance() {
        INSTANCE.set(null);
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 按依赖顺序添加组件
        components.add(new ConfigurationComponent());
        components.add(new DataSourceComponent());
        components.add(new ServiceRegistryComponent());
        components.add(new RouterComponent());
        components.add(new ProxyComponent());
    }
    
    /**
     * 启动框架
     */
    public Future<Void> start(String[] args, Handler<JsonObject> userHandler) {
        this.userHandler = userHandler;
        
        return Future.future(promise -> {
            if (!state.compareAndSet(LifecycleState.INITIAL, LifecycleState.STARTING)) {
                promise.fail("Framework is already starting or started");
                return;
            }
            
            LOGGER.info("Starting VXCore framework...");
            
            // 1. 创建Vertx实例
            createVertxInstance()
                .compose(v -> {
                    // 2. 加载配置
                    return loadConfiguration(args);
                })
                .compose(config -> {
                    // 3. 初始化所有组件
                    return initializeAllComponents(config);
                })
                .compose(v -> {
                    // 4. 部署Verticle
                    return deployVerticles();
                })
                .compose(v -> {
                    // 5. 执行用户回调
                    return executeUserCallback();
                })
                .onSuccess(v -> {
                    state.set(LifecycleState.STARTED);
                    LOGGER.info("VXCore framework started successfully");
                    promise.complete();
                })
                .onFailure(error -> {
                    state.set(LifecycleState.FAILED);
                    LOGGER.error("Failed to start VXCore framework", error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 停止框架
     */
    public Future<Void> stop() {
        return Future.future(promise -> {
            if (!state.compareAndSet(LifecycleState.STARTED, LifecycleState.STOPPING)) {
                promise.fail("Framework is not started");
                return;
            }
            
            LOGGER.info("Stopping VXCore framework...");
            
            // 1. 停止所有组件
            stopAllComponents()
                .compose(v -> {
                    // 2. 关闭Vertx实例
                    return closeVertxInstance();
                })
                .onSuccess(v -> {
                    state.set(LifecycleState.STOPPED);
                    LOGGER.info("VXCore framework stopped successfully");
                    promise.complete();
                })
                .onFailure(error -> {
                    state.set(LifecycleState.FAILED);
                    LOGGER.error("Failed to stop VXCore framework", error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 创建Vertx实例
     */
    private Future<Void> createVertxInstance() {
        return Future.future(promise -> {
            try {
                VertxOptions options = new VertxOptions();
                options.setAddressResolverOptions(
                    new io.vertx.core.dns.AddressResolverOptions()
                        .addServer("114.114.114.114")
                        .addServer("114.114.115.115")
                        .addServer("8.8.8.8")
                        .addServer("8.8.4.4")
                );
                
                this.vertx = Vertx.vertx(options);
                VertxHolder.init(vertx);
                
                LOGGER.info("Vertx instance created successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to create Vertx instance", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 加载配置
     */
    private Future<JsonObject> loadConfiguration(String[] args) {
        return Future.future(promise -> {
            String configFile = determineConfigFile(args);
            
            ConfigUtil.readYamlConfig(configFile, vertx)
                .onSuccess(config -> {
                    this.globalConfig = config;
                    LOGGER.info("Configuration loaded successfully");
                    promise.complete(config);
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to load configuration", error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 确定配置文件路径
     */
    private String determineConfigFile(String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            if (arg.startsWith("app-")) {
                return arg;
            } else if (arg.equals("dev") || arg.equals("prod")) {
                return "application";
            } else if (arg.equals("test")) {
                return "application-test";
            } else if (arg.equals("application")) {
                return "application";
            }
        }
        return "app";
    }
    
    /**
     * 初始化所有组件
     */
    private Future<Void> initializeAllComponents(JsonObject config) {
        return Future.future(promise -> {
            Future<Void> allFutures = Future.succeededFuture();
            
            for (LifecycleComponent component : components) {
                allFutures = allFutures.compose(v -> component.initialize(vertx, config));
            }
            
            allFutures.onComplete(promise);
        });
    }
    
    /**
     * 部署Verticle
     */
    private Future<Void> deployVerticles() {
        return Future.future(promise -> {
            List<Future<String>> deploymentFutures = new ArrayList<>();
            
            // 部署核心Verticle
            deploymentFutures.add(vertx.deployVerticle(RouterVerticle.class, getDeploymentOptions("Router")));
            deploymentFutures.add(vertx.deployVerticle(ServiceVerticle.class, getDeploymentOptions("Service")));
            
            // 根据配置决定是否部署代理Verticle
            JsonObject proxyConfig = globalConfig.getJsonObject("proxy");
            if (proxyConfig != null && proxyConfig.getBoolean("enabled", false)) {
                deploymentFutures.add(vertx.deployVerticle(ReverseProxyVerticle.class, getDeploymentOptions("Proxy")));
            }
            
            // 部署后置执行Verticle
            deploymentFutures.add(vertx.deployVerticle(PostExecVerticle.class, getDeploymentOptions("PostExec", 2)));
            
            Future.all(deploymentFutures)
                .onSuccess(results -> {
                    LOGGER.info("All verticles deployed successfully");
                    promise.complete();
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to deploy verticles", error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 执行用户回调
     */
    private Future<Void> executeUserCallback() {
        return Future.future(promise -> {
            if (userHandler != null) {
                vertx.createSharedWorkerExecutor("user-handler")
                    .executeBlocking(() -> {
                        userHandler.handle(globalConfig);
                        return "User handler completed";
                    })
                    .onSuccess(result -> {
                        LOGGER.info("User handler executed: {}", result);
                        promise.complete();
                    })
                    .onFailure(error -> {
                        LOGGER.error("User handler failed", error);
                        promise.fail(error);
                    });
            } else {
                promise.complete();
            }
        });
    }
    
    /**
     * 停止所有组件
     */
    private Future<Void> stopAllComponents() {
        return Future.future(promise -> {
            Future<Void> allFutures = Future.succeededFuture();
            
            for (LifecycleComponent component : components) {
                allFutures = allFutures.compose(v -> component.stop());
            }
            
            allFutures.onComplete(promise);
        });
    }
    
    /**
     * 关闭Vertx实例
     */
    private Future<Void> closeVertxInstance() {
        return Future.future(promise -> {
            if (vertx != null) {
                vertx.close()
                    .onSuccess(v -> {
                        LOGGER.info("Vertx instance closed successfully");
                        promise.complete();
                    })
                    .onFailure(error -> {
                        LOGGER.error("Failed to close Vertx instance", error);
                        promise.fail(error);
                    });
            } else {
                promise.complete();
            }
        });
    }
    
    /**
     * 获取部署选项
     */
    private DeploymentOptions getDeploymentOptions(String name) {
        return getDeploymentOptions(name, 4);
    }
    
    /**
     * 获取部署选项
     */
    private DeploymentOptions getDeploymentOptions(String name, int instances) {
        return new DeploymentOptions()
            .setWorkerPoolName(name)
            .setThreadingModel(ThreadingModel.WORKER)
            .setInstances(instances);
    }
    
    /**
     * 获取当前状态
     */
    public LifecycleState getState() {
        return state.get();
    }
    
    /**
     * 获取全局配置
     */
    public JsonObject getGlobalConfig() {
        return globalConfig;
    }
    
    /**
     * 获取Vertx实例
     */
    public Vertx getVertx() {
        return vertx;
    }
    
    /**
     * 获取所有组件
     */
    public List<LifecycleComponent> getComponents() {
        return new ArrayList<>(components);
    }
    
    /**
     * 生命周期状态枚举
     */
    public enum LifecycleState {
        INITIAL,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        FAILED
    }
}