package cn.qaiu.vx.core.config;

import cn.qaiu.vx.core.annotaions.config.ConfigurationProperties;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 * 支持多数据源配置和灵活的自定义配置类
 * 
 * @author QAIU
 */
public class ConfigurationManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private final Vertx vertx;
    private final JsonObject globalConfig;
    private final Map<String, Object> configCache = new HashMap<>();
    
    public ConfigurationManager(Vertx vertx, JsonObject globalConfig) {
        this.vertx = vertx;
        this.globalConfig = globalConfig;
    }
    
    /**
     * 获取配置对象
     * 支持 @ConfigurationProperties 注解的配置类
     * 
     * @param configClass 配置类
     * @param <T> 配置类型
     * @return 配置对象
     */
    public <T> T getConfig(Class<T> configClass) {
        String cacheKey = configClass.getName();
        
        // 检查缓存
        if (configCache.containsKey(cacheKey)) {
            return (T) configCache.get(cacheKey);
        }
        
        try {
            T config = configClass.getDeclaredConstructor().newInstance();
            
            // 检查是否有 @ConfigurationProperties 注解
            ConfigurationProperties annotation = configClass.getAnnotation(ConfigurationProperties.class);
            if (annotation != null) {
                String prefix = annotation.prefix();
                JsonObject configData = getConfigDataByPrefix(prefix);
                ConfigurationPropertyBinder.bind(config, configData);
            } else {
                // 默认绑定：使用类名作为前缀
                String defaultPrefix = getDefaultPrefix(configClass);
                JsonObject configData = getConfigDataByPrefix(defaultPrefix);
                ConfigurationPropertyBinder.bind(config, configData);
            }
            
            // 缓存配置对象
            configCache.put(cacheKey, config);
            LOGGER.debug("Created and cached config: {}", configClass.getSimpleName());
            
            return config;
        } catch (Exception e) {
            LOGGER.error("Failed to create config object: {}", configClass.getSimpleName(), e);
            throw new RuntimeException("Failed to create config object: " + configClass.getSimpleName(), e);
        }
    }
    
    /**
     * 获取多数据源配置
     * 
     * @param dataSourceName 数据源名称
     * @return 数据源配置
     */
    public JsonObject getDataSourceConfig(String dataSourceName) {
        JsonObject datasources = globalConfig.getJsonObject("datasources");
        if (datasources == null) {
            LOGGER.warn("No datasources configuration found");
            return new JsonObject();
        }
        
        JsonObject dataSourceConfig = datasources.getJsonObject(dataSourceName);
        if (dataSourceConfig == null) {
            LOGGER.warn("DataSource '{}' not found in configuration", dataSourceName);
            return new JsonObject();
        }
        
        return dataSourceConfig;
    }
    
    /**
     * 获取服务器配置
     * 
     * @return 服务器配置
     */
    public JsonObject getServerConfig() {
        return globalConfig.getJsonObject("server", new JsonObject());
    }
    
    /**
     * 获取代理配置
     * 
     * @return 代理配置
     */
    public JsonObject getProxyConfig() {
        return globalConfig.getJsonObject("proxy", new JsonObject());
    }
    
    /**
     * 获取应用配置
     * 
     * @return 应用配置
     */
    public JsonObject getAppConfig() {
        return globalConfig.getJsonObject("app", new JsonObject());
    }
    
    /**
     * 获取日志配置
     * 
     * @return 日志配置
     */
    public JsonObject getLoggingConfig() {
        return globalConfig.getJsonObject("logging", new JsonObject());
    }
    
    /**
     * 根据前缀获取配置数据
     * 
     * @param prefix 配置前缀
     * @return 配置数据
     */
    private JsonObject getConfigDataByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return globalConfig;
        }
        
        // 支持嵌套路径，如 "server.database"
        String[] parts = prefix.split("\\.");
        JsonObject current = globalConfig;
        
        for (String part : parts) {
            if (current.containsKey(part)) {
                JsonObject next = current.getJsonObject(part);
                if (next != null) {
                    current = next;
                } else {
                    LOGGER.warn("Configuration path '{}' is not a JSON object", prefix);
                    return new JsonObject();
                }
            } else {
                LOGGER.warn("Configuration path '{}' not found", prefix);
                return new JsonObject();
            }
        }
        
        return current;
    }
    
    /**
     * 获取默认前缀
     * 将类名转换为配置前缀
     * 
     * @param configClass 配置类
     * @return 默认前缀
     */
    private String getDefaultPrefix(Class<?> configClass) {
        String className = configClass.getSimpleName();
        
        // 移除 "Config" 后缀
        if (className.endsWith("Config")) {
            className = className.substring(0, className.length() - 6);
        }
        
        // 转换为小写
        return className.toLowerCase();
    }
    
    /**
     * 检查配置是否存在
     * 
     * @param key 配置键
     * @return 是否存在
     */
    public boolean hasConfig(String key) {
        return globalConfig.containsKey(key);
    }
    
    /**
     * 获取原始配置
     * 
     * @return 原始配置
     */
    public JsonObject getRawConfig() {
        return globalConfig;
    }
    
    /**
     * 清除配置缓存
     */
    public void clearCache() {
        configCache.clear();
        LOGGER.debug("Configuration cache cleared");
    }
}
