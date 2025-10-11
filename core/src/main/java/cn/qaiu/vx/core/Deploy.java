package cn.qaiu.vx.core;

import cn.qaiu.vx.core.util.AutoScanPathDetector;
import cn.qaiu.vx.core.util.CommonUtil;
import cn.qaiu.vx.core.util.ConfigUtil;
import cn.qaiu.vx.core.util.VertxHolder;
import cn.qaiu.vx.core.verticle.HttpProxyVerticle;
import cn.qaiu.vx.core.verticle.PostExecVerticle;
import cn.qaiu.vx.core.verticle.ReverseProxyVerticle;
import cn.qaiu.vx.core.verticle.RouterVerticle;
import cn.qaiu.vx.core.verticle.ServiceVerticle;
import io.vertx.core.*;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

import static cn.qaiu.vx.core.util.ConfigConstant.*;

/**
 * vertx启动类 需要在主启动类完成回调
 * <br>Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public final class Deploy {

    private static final Deploy INSTANCE = new Deploy();
    private static final Logger LOGGER = LoggerFactory.getLogger(Deploy.class);
    private static final long startTime = System.currentTimeMillis();

    private final Vertx tempVertx = Vertx.vertx();
    StringBuilder path = new StringBuilder("app");

    private JsonObject customConfig;
    private JsonObject globalConfig;
    private Handler<JsonObject> handle;

    private Thread mainThread;

    public static Deploy instance() {
        return INSTANCE;
    }
    
    public static void run(String[] args, Handler<JsonObject> handle) {
        INSTANCE.start(args, handle);;
    }

    /**
     *
     * @param args 启动参数
     * @param handle 启动完成后回调处理函数
     */
    public void start(String[] args, Handler<JsonObject> handle) {
        this.mainThread = Thread.currentThread();
        this.handle = handle;

        // 支持新的配置格式：application.yml 或 app-dev.yml
        String configFile = determineConfigFile(args);
        
        // 读取yml配置
        ConfigUtil.readYamlConfig(configFile, tempVertx)
                .onSuccess(this::readConf)
                .onFailure(Throwable::printStackTrace);
        LockSupport.park();
        deployVerticle();
    }
    
    /**
     * 确定配置文件路径
     * 支持新的 application.yml 格式和旧的 app-dev.yml 格式
     */
    private String determineConfigFile(String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            if (arg.startsWith("app-")) {
                // 旧格式：app-dev, app-prod
                return arg;
            } else if (arg.equals("dev") || arg.equals("prod") || arg.equals("test")) {
                // 新格式：dev, prod, test -> application.yml
                return "application";
            } else if (arg.equals("application")) {
                // 直接指定 application.yml
                return "application";
            }
        }
        
        // 默认使用 app.yml（旧格式）
        return "app";
    }

    /**
     * 自动检测扫描路径
     *
     * @param conf 配置对象
     */
    private void autoDetectScanPaths(JsonObject conf) {
        try {
            // 通过堆栈跟踪获取启动类
            Set<String> autoDetectedPaths = AutoScanPathDetector.detectScanPathsFromStackTrace();
            
            // 检查配置中是否已设置baseLocations
            JsonObject customConfig = conf.getJsonObject(CUSTOM);
            if (customConfig != null && customConfig.containsKey(BASE_LOCATIONS)) {
                String configuredPaths = customConfig.getString(BASE_LOCATIONS);
                LOGGER.info("Using configured baseLocations: {}", configuredPaths);
                LOGGER.info("Auto-detected paths (not used): {}", AutoScanPathDetector.formatScanPaths(autoDetectedPaths));
            } else {
                // 如果没有配置，使用自动检测的路径
                String autoPaths = AutoScanPathDetector.formatScanPaths(autoDetectedPaths);
                
                // 确保custom配置存在
                if (customConfig == null) {
                    customConfig = new JsonObject();
                    conf.put(CUSTOM, customConfig);
                }
                
                // 设置自动检测的路径
                customConfig.put(BASE_LOCATIONS, autoPaths);
                
                // 检查是否使用了@App注解
                if (isAppAnnotationUsed(autoDetectedPaths)) {
                    LOGGER.info("App-annotated baseLocations: {}", autoPaths);
                    LOGGER.info("Using @App annotation configuration for scan paths.");
                } else {
                    LOGGER.info("Auto-configured baseLocations: {}", autoPaths);
                    LOGGER.info("You can override this by setting 'baseLocations' in your config file or using @App annotation.");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to auto-detect scan paths, using default: cn.qaiu", e);
            
            // 设置默认路径
            JsonObject customConfig = conf.getJsonObject(CUSTOM);
            if (customConfig == null) {
                customConfig = new JsonObject();
                conf.put(CUSTOM, customConfig);
            }
            customConfig.put(BASE_LOCATIONS, "cn.qaiu");
        }
    }

    /**
     * 检查是否使用了@App注解
     *
     * @param scanPaths 扫描路径
     * @return 是否使用了@App注解
     */
    private boolean isAppAnnotationUsed(Set<String> scanPaths) {
        try {
            // 通过堆栈跟踪获取启动类
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ("main".equals(element.getMethodName())) {
                    String className = element.getClassName();
                    Class<?> mainClass = Class.forName(className);
                    return mainClass.isAnnotationPresent(cn.qaiu.vx.core.annotaions.App.class);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to check @App annotation", e);
        }
        return false;
    }

    private void readConf(JsonObject conf) {
        outLogo(conf);
        
        // 自动检测扫描路径
        autoDetectScanPaths(conf);
        
        // 支持新的配置格式：application.yml 直接使用，无需额外的环境配置文件
        if (conf.containsKey("server") && conf.containsKey("datasources")) {
            // 新格式：application.yml
            LOGGER.info("---------------> Using new application.yml configuration <--------------\n");
            this.globalConfig = conf;
            LockSupport.unpark(mainThread);
        } else {
            // 旧格式：app.yml + app-dev.yml
            var activeMode = conf.getString("active");
            if ("dev".equals(activeMode)) {
                LOGGER.info("---------------> development environment <--------------\n");
                System.setProperty("vertxweb.environment", "dev");
            } else {
                LOGGER.info("---------------> Production environment <--------------\n");
            }
            ConfigUtil.readYamlConfig(path + "-" + activeMode, tempVertx).onSuccess(res -> {
                this.globalConfig = res;
                LockSupport.unpark(mainThread);
            });
        }
    }

    /**
     * 打印logo
     */
    private void outLogo(JsonObject conf) {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        var year = calendar.get(Calendar.YEAR);
        var logoTemplate = """

                Web Server powered by:\s
                 ____   ____              _              _    _   \s
                |_^^_| |_^^_|            / |_           | |  | |  \s
                  \\ \\   / /.---.  _ .--.`| |-'   _   __ | |__| |_ \s
                   \\ \\ / // /__\\\\[ `/'`\\]| |    [ \\ [  ]|____   _|\s
                    \\ V / | \\__., | |    | |, _  > '  <     _| |_ \s
                     \\_/   '.__.'[___]   \\__/(_)[__]`\\_]   |_____|\s
                                                      Version: %s; Framework version: %s; %s©%d.

                """;

        System.out.printf(logoTemplate,
                CommonUtil.getAppVersion(),
                "4x",
                conf.getString("copyright"),
                year
        );
    }

    /**
     * 部署Verticle
     */
    private void deployVerticle() {
        tempVertx.close();
        LOGGER.info("配置读取成功");
        
        // 支持新的配置格式：application.yml
        if (globalConfig.containsKey("server") && globalConfig.containsKey("datasources")) {
            deployWithNewConfig();
        } else {
            deployWithOldConfig();
        }
    }
    
    /**
     * 使用新配置格式部署（application.yml）
     */
    private void deployWithNewConfig() {
        LOGGER.info("使用新配置格式部署...");
        
        // 创建 Vertx 实例
        var vertxOptions = new VertxOptions();
        vertxOptions.setAddressResolverOptions(
                new AddressResolverOptions().
                        addServer("114.114.114.114").
                        addServer("114.114.115.115").
                        addServer("8.8.8.8").
                        addServer("8.8.4.4"));
        
        var vertx = Vertx.vertx(vertxOptions);
        VertxHolder.init(vertx);
        
        // 配置保存在共享数据中
        var sharedData = vertx.sharedData();
        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, globalConfig);
        localMap.put(SERVER, globalConfig.getJsonObject("server"));
        
        // 执行用户自定义处理
        var future0 = vertx.createSharedWorkerExecutor("other-handle")
                .executeBlocking(() -> {
                    handle.handle(globalConfig);
                    return "Other handle complete";
                });

        future0.onSuccess(res -> {
            LOGGER.info(res);
            // 部署基础服务
            var future1 = vertx.deployVerticle(RouterVerticle.class, getWorkDeploymentOptions("Router"));
            var future2 = vertx.deployVerticle(ServiceVerticle.class, getWorkDeploymentOptions("Service"));
            
            // 检查是否需要部署反向代理
            JsonObject proxyConfig = globalConfig.getJsonObject("proxy");
            if (proxyConfig != null && proxyConfig.getBoolean("enabled", false)) {
                var future3 = vertx.deployVerticle(ReverseProxyVerticle.class, getWorkDeploymentOptions("proxy"));
                Future.all(future1, future2, future3)
                        .onSuccess(this::deployWorkVerticalSuccess)
                        .onFailure(this::deployVerticalFailed);
            } else {
                Future.all(future1, future2)
                        .onSuccess(this::deployWorkVerticalSuccess)
                        .onFailure(this::deployVerticalFailed);
            }
        }).onFailure(e -> LOGGER.error("Other handle error", e));
    }
    
    /**
     * 使用旧配置格式部署（app.yml + app-dev.yml）
     */
    private void deployWithOldConfig() {
        LOGGER.info("使用旧配置格式部署...");
        
        customConfig = globalConfig.getJsonObject(CUSTOM);

        JsonObject vertxConfig = globalConfig.getJsonObject(VERTX);
        Integer vertxConfigELPS = vertxConfig.getInteger(EVENT_LOOP_POOL_SIZE);
        var vertxOptions = vertxConfigELPS == 0 ?
                new VertxOptions() : new VertxOptions(vertxConfig);

        vertxOptions.setAddressResolverOptions(
                new AddressResolverOptions().
                        addServer("114.114.114.114").
                        addServer("114.114.115.115").
                        addServer("8.8.8.8").
                        addServer("8.8.4.4"));
        LOGGER.info("vertxConfigEventLoopPoolSize: {}, eventLoopPoolSize: {}, workerPoolSize: {}", vertxConfigELPS,
                vertxOptions.getEventLoopPoolSize(),
                vertxOptions.getWorkerPoolSize());
        var vertx = Vertx.vertx(vertxOptions);
        VertxHolder.init(vertx);
        //配置保存在共享数据中
        var sharedData = vertx.sharedData();
        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, globalConfig);
        localMap.put(CUSTOM_CONFIG, customConfig);
        localMap.put(SERVER, globalConfig.getJsonObject(SERVER));
        var future0 = vertx.createSharedWorkerExecutor("other-handle")
                .executeBlocking(() -> {
                    handle.handle(globalConfig);
                    return "Other handle complete";
                });

        future0.onSuccess(res -> {
            LOGGER.info(res);
            // 部署 路由、异步service、反向代理 服务
            var future1 = vertx.deployVerticle(RouterVerticle.class, getWorkDeploymentOptions("Router"));
            var future2 = vertx.deployVerticle(ServiceVerticle.class, getWorkDeploymentOptions("Service"));
            var future3 = vertx.deployVerticle(ReverseProxyVerticle.class, getWorkDeploymentOptions("proxy"));


            JsonObject jsonObject = ((JsonObject) localMap.get(GLOBAL_CONFIG)).getJsonObject("proxy-server");
            if (jsonObject != null) {
                genPwd(jsonObject);
                var future4 = vertx.deployVerticle(HttpProxyVerticle.class, getWorkDeploymentOptions("proxy"));
                future4.onSuccess(LOGGER::info);
                future4.onFailure(e -> LOGGER.error("Other handle error", e));
                Future.all(future1, future2, future3, future4)
                        .onSuccess(this::deployWorkVerticalSuccess)
                        .onFailure(this::deployVerticalFailed);
            } else {
                Future.all(future1, future2, future3)
                        .onSuccess(this::deployWorkVerticalSuccess)
                        .onFailure(this::deployVerticalFailed);
            }

        }).onFailure(e -> LOGGER.error("Other handle error", e));
    }

    private static void genPwd(JsonObject jsonObject) {
        if (jsonObject.getBoolean("randUserPwd")) {
            var username = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            var password = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        }
        LOGGER.info("=============server info=================");
        LOGGER.info("\nport: {}\nusername: {}\npassword: {}",
                jsonObject.getString("port"),
                jsonObject.getString("username"),
                jsonObject.getString("password"));
        LOGGER.info("==============server info================");
    }
    /**
     * 部署失败
     *
     * @param throwable Exception信息
     */
    private void deployVerticalFailed(Throwable throwable) {
        LOGGER.error(throwable.getClass().getName() + ": " + throwable.getMessage());
        System.exit(-1);
    }

    /**
     * 启动时间信息
     *
     * @param compositeFuture future wraps a list
     */
    private void deployWorkVerticalSuccess(CompositeFuture compositeFuture) {
        // 在所有verticle部署成功后，部署PostExecVerticle执行AppRun实现
        var vertx = VertxHolder.getVertxInstance();
        var postExecFuture = vertx.deployVerticle(PostExecVerticle.class, getWorkDeploymentOptions("postExec", 2));
        
        postExecFuture.onSuccess(postExecResult -> {
            LOGGER.info("PostExecVerticle 部署成功，AppRun实现执行完成");
        }).onFailure(e -> {
            LOGGER.error("PostExecVerticle deployment error", e);
        });
        var t1 = ((double) (System.currentTimeMillis() - startTime)) / 1000;
        var t2 = ((double) System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000;
        LOGGER.info("web服务启动成功 -> 用时: {}s, jvm启动用时: {}s", t1, t2);
        
    }

    /**
     * deploy Verticle Options
     *
     * @param name the worker pool name
     * @return Deployment Options
     */
    private DeploymentOptions getWorkDeploymentOptions(String name) {
        // 支持新配置格式
        if (globalConfig.containsKey("server") && globalConfig.containsKey("datasources")) {
            // 新格式：使用默认配置
            return getWorkDeploymentOptions(name, 4);
        } else {
            // 旧格式：使用 customConfig
            return getWorkDeploymentOptions(name, customConfig.getInteger(ASYNC_SERVICE_INSTANCES));
        }
    }

    private DeploymentOptions getWorkDeploymentOptions(String name, int ins) {
        return new DeploymentOptions()
                .setWorkerPoolName(name)
                .setThreadingModel(ThreadingModel.WORKER)
                .setInstances(ins);
    }

}
