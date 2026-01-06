package cn.qaiu.vx.core.config;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 配置解析器
 * 使用别名机制自动解析配置，支持多种配置格式兼容
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigResolver.class);

    private final ConfigAliasRegistry aliasRegistry;
    private final JsonObject config;

    public ConfigResolver(JsonObject config) {
        this(config, ConfigAliasRegistry.getInstance());
    }

    public ConfigResolver(JsonObject config, ConfigAliasRegistry aliasRegistry) {
        this.config = config != null ? config : new JsonObject();
        this.aliasRegistry = aliasRegistry;
    }

    /**
     * 获取字符串值，支持别名查找
     *
     * @param key 配置键（可以是任意别名）
     * @return 配置值
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * 获取字符串值，支持别名查找和默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getString(String key, String defaultValue) {
        String value = findValueByAlias(key, String.class);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数值，支持别名查找
     *
     * @param key 配置键
     * @return 配置值
     */
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    /**
     * 获取整数值，支持别名查找和默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Integer getInteger(String key, Integer defaultValue) {
        Integer value = findValueByAlias(key, Integer.class);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔值，支持别名查找
     *
     * @param key 配置键
     * @return 配置值
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    /**
     * 获取布尔值，支持别名查找和默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = findValueByAlias(key, Boolean.class);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取长整数值，支持别名查找
     *
     * @param key 配置键
     * @return 配置值
     */
    public Long getLong(String key) {
        return getLong(key, null);
    }

    /**
     * 获取长整数值，支持别名查找和默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Long getLong(String key, Long defaultValue) {
        Long value = findValueByAlias(key, Long.class);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取JsonObject值，支持别名查找
     *
     * @param key 配置键
     * @return 配置值
     */
    public JsonObject getJsonObject(String key) {
        return findValueByAlias(key, JsonObject.class);
    }

    /**
     * 获取JsonObject值，如果不存在则返回空JsonObject
     *
     * @param key 配置键
     * @return 配置值或空JsonObject
     */
    public JsonObject getJsonObjectOrEmpty(String key) {
        JsonObject value = getJsonObject(key);
        return value != null ? value : new JsonObject();
    }

    /**
     * 检查配置键是否存在（包括别名）
     *
     * @param key 配置键
     * @return 是否存在
     */
    public boolean containsKey(String key) {
        Set<String> aliases = aliasRegistry.getAliases(aliasRegistry.getCanonicalName(key));
        for (String alias : aliases) {
            if (config.containsKey(alias)) {
                return true;
            }
        }
        return config.containsKey(key);
    }

    /**
     * 通过别名查找值
     *
     * @param key 配置键
     * @param type 值类型
     * @return 配置值
     */
    private <T> T findValueByAlias(String key, Class<T> type) {
        // 1. 先直接查找
        T directValue = getValueFromConfig(key, type);
        if (directValue != null) {
            return directValue;
        }

        // 2. 通过别名查找
        String canonicalName = aliasRegistry.getCanonicalName(key);
        Set<String> aliases = aliasRegistry.getAliases(canonicalName);
        
        for (String alias : aliases) {
            T value = getValueFromConfig(alias, type);
            if (value != null) {
                if (!alias.equals(key)) {
                    LOGGER.debug("Config key '{}' resolved via alias '{}'", key, alias);
                }
                return value;
            }
        }

        return null;
    }

    /**
     * 从配置中获取指定类型的值
     */
    @SuppressWarnings("unchecked")
    private <T> T getValueFromConfig(String key, Class<T> type) {
        if (!config.containsKey(key)) {
            return null;
        }

        Object value = config.getValue(key);
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return (T) value.toString();
        } else if (type == Integer.class) {
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            try {
                return (T) Integer.valueOf(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (type == Long.class) {
            if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            try {
                return (T) Long.valueOf(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (type == Boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) Boolean.valueOf(value.toString());
        } else if (type == JsonObject.class) {
            if (value instanceof JsonObject) {
                return (T) value;
            }
        }

        return null;
    }

    /**
     * 创建子配置解析器
     *
     * @param key 子配置键
     * @return 子配置解析器
     */
    public ConfigResolver getSubResolver(String key) {
        JsonObject subConfig = getJsonObject(key);
        return new ConfigResolver(subConfig, aliasRegistry);
    }

    /**
     * 获取原始配置对象
     *
     * @return JsonObject配置
     */
    public JsonObject getRawConfig() {
        return config;
    }
}
