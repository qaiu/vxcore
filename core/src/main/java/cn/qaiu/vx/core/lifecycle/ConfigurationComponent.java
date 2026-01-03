package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.util.AutoScanPathDetector;
import cn.qaiu.vx.core.util.ConfigUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static cn.qaiu.vx.core.util.ConfigConstant.*;

/**
 * 配置管理组件
 * 负责配置的加载、验证和分发
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigurationComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationComponent.class);
    
    private Vertx vertx;
    private JsonObject globalConfig;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.globalConfig = config;
        
        return Future.future(promise -> {
            try {
                // 1. 自动检测扫描路径
                autoDetectScanPaths(config);
                
                // 2. 验证配置
                validateConfiguration(config);
                
                // 3. 存储配置到共享数据
                storeConfiguration(config);
                
                LOGGER.info("Configuration component initialized successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize configuration component", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 自动检测扫描路径
     */
    private void autoDetectScanPaths(JsonObject config) {
        try {
            Set<String> autoDetectedPaths = AutoScanPathDetector.detectScanPathsFromStackTrace();
            
            JsonObject customConfig = config.getJsonObject(CUSTOM);
            if (customConfig != null && customConfig.containsKey(BASE_LOCATIONS)) {
                String configuredPaths = customConfig.getString(BASE_LOCATIONS);
                LOGGER.info("Using configured baseLocations: {}", configuredPaths);
                LOGGER.info("Auto-detected paths (not used): {}", AutoScanPathDetector.formatScanPaths(autoDetectedPaths));
            } else {
                String autoPaths = AutoScanPathDetector.formatScanPaths(autoDetectedPaths);
                
                if (customConfig == null) {
                    customConfig = new JsonObject();
                    config.put(CUSTOM, customConfig);
                }
                
                customConfig.put(BASE_LOCATIONS, autoPaths);
                
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
            
            JsonObject customConfig = config.getJsonObject(CUSTOM);
            if (customConfig == null) {
                customConfig = new JsonObject();
                config.put(CUSTOM, customConfig);
            }
            customConfig.put(BASE_LOCATIONS, "cn.qaiu");
        }
    }
    
    /**
     * 检查是否使用了@App注解
     */
    private boolean isAppAnnotationUsed(Set<String> scanPaths) {
        try {
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
    
    /**
     * 验证配置
     */
    private void validateConfiguration(JsonObject config) {
        // 验证服务器配置
        JsonObject serverConfig = config.getJsonObject("server");
        if (serverConfig == null) {
            throw new IllegalArgumentException("Server configuration is required");
        }
        
        if (serverConfig.getInteger("port") == null) {
            throw new IllegalArgumentException("Server port is required");
        }
        
        // 验证数据源配置
        JsonObject datasources = config.getJsonObject("datasources");
        if (datasources == null || datasources.isEmpty()) {
            LOGGER.warn("No datasource configuration found");
        }
        
        LOGGER.info("Configuration validation completed");
    }
    
    /**
     * 存储配置到共享数据
     */
    private void storeConfiguration(JsonObject config) {
        LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, config);
        localMap.put(SERVER, config.getJsonObject("server"));
        
        JsonObject customConfig = config.getJsonObject(CUSTOM);
        if (customConfig != null) {
            localMap.put(CUSTOM_CONFIG, customConfig);
        }
        
        LOGGER.info("Configuration stored in shared data");
    }
    
    @Override
    public int getPriority() {
        return 10; // 最高优先级
    }
}