package cn.qaiu.vx.core.config;

import io.vertx.core.json.JsonObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置绑定器 将JsonObject配置自动绑定到Java对象，使用策略模式处理不同类型
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigBinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBinder.class);

  private final ConfigAliasRegistry aliasRegistry;
  private final Map<Class<?>, TypeBindStrategy<?>> typeStrategies = new ConcurrentHashMap<>();

  public ConfigBinder() {
    this(ConfigAliasRegistry.getInstance());
  }

  public ConfigBinder(ConfigAliasRegistry aliasRegistry) {
    this.aliasRegistry = aliasRegistry;
    registerDefaultStrategies();
  }

  /** 注册默认类型绑定策略 */
  private final void registerDefaultStrategies() {
    // 基本类型策略
    registerStrategy(String.class, (value, targetType) -> value != null ? value.toString() : null);

    registerStrategy(
        Integer.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).intValue();
          if (value != null) {
            try {
              return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
              return null;
            }
          }
          return null;
        });

    registerStrategy(
        int.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).intValue();
          if (value != null) {
            try {
              return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
              return 0;
            }
          }
          return 0;
        });

    registerStrategy(
        Long.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).longValue();
          if (value != null) {
            try {
              return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
              return null;
            }
          }
          return null;
        });

    registerStrategy(
        long.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).longValue();
          if (value != null) {
            try {
              return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
              return 0L;
            }
          }
          return 0L;
        });

    registerStrategy(
        Double.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).doubleValue();
          if (value != null) {
            try {
              return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
              return null;
            }
          }
          return null;
        });

    registerStrategy(
        double.class,
        (value, targetType) -> {
          if (value instanceof Number) return ((Number) value).doubleValue();
          if (value != null) {
            try {
              return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
              return 0.0;
            }
          }
          return 0.0;
        });

    registerStrategy(
        Boolean.class,
        (value, targetType) -> {
          if (value instanceof Boolean) return (Boolean) value;
          if (value != null) return Boolean.parseBoolean(value.toString());
          return Boolean.FALSE;
        });

    registerStrategy(
        boolean.class,
        (value, targetType) -> {
          if (value instanceof Boolean) return (Boolean) value;
          if (value != null) return Boolean.parseBoolean(value.toString());
          return false;
        });
  }

  /**
   * 注册类型绑定策略
   *
   * @param type 目标类型
   * @param strategy 绑定策略
   */
  public <T> void registerStrategy(Class<T> type, TypeBindStrategy<T> strategy) {
    typeStrategies.put(type, strategy);
  }

  /**
   * 将配置绑定到目标对象
   *
   * @param config 配置对象
   * @param targetClass 目标类
   * @return 绑定后的对象
   */
  public <T> T bind(JsonObject config, Class<T> targetClass) {
    if (config == null) {
      return null;
    }

    try {
      T instance = targetClass.getDeclaredConstructor().newInstance();
      bindToInstance(config, instance);
      return instance;
    } catch (Exception e) {
      LOGGER.error("Failed to bind config to class: {}", targetClass.getName(), e);
      return null;
    }
  }

  /**
   * 将配置绑定到已存在的实例
   *
   * @param config 配置对象
   * @param instance 目标实例
   */
  public <T> void bindToInstance(JsonObject config, T instance) {
    if (config == null || instance == null) {
      return;
    }

    Class<?> targetClass = instance.getClass();
    ConfigResolver resolver = new ConfigResolver(config, aliasRegistry);

    for (Field field : targetClass.getDeclaredFields()) {
      try {
        bindField(resolver, instance, field);
      } catch (Exception e) {
        LOGGER.debug("Failed to bind field '{}': {}", field.getName(), e.getMessage());
      }
    }
  }

  /** 绑定单个字段 */
  private void bindField(ConfigResolver resolver, Object instance, Field field) throws Exception {
    String fieldName = field.getName();
    Class<?> fieldType = field.getType();

    // 尝试获取配置值
    Object value = getConfigValue(resolver, fieldName, fieldType);

    if (value != null) {
      // 使用setter方法设置值
      String setterName =
          "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
      try {
        Method setter = instance.getClass().getMethod(setterName, fieldType);
        setter.invoke(instance, value);
        LOGGER.debug("Bound config '{}' to field '{}'", fieldName, fieldName);
      } catch (NoSuchMethodException e) {
        // 尝试直接设置字段
        field.setAccessible(true);
        field.set(instance, value);
        LOGGER.debug("Directly set field '{}' (no setter found)", fieldName);
      }
    }
  }

  /** 获取配置值并转换为目标类型 */
  @SuppressWarnings("unchecked")
  private Object getConfigValue(ConfigResolver resolver, String fieldName, Class<?> fieldType) {
    // 检查配置是否存在
    if (!resolver.containsKey(fieldName)) {
      return null;
    }

    // 获取原始值
    Object rawValue;
    if (fieldType == JsonObject.class) {
      rawValue = resolver.getJsonObject(fieldName);
    } else if (fieldType == String.class) {
      rawValue = resolver.getString(fieldName);
    } else if (fieldType == Integer.class || fieldType == int.class) {
      rawValue = resolver.getInteger(fieldName);
    } else if (fieldType == Long.class || fieldType == long.class) {
      rawValue = resolver.getLong(fieldName);
    } else if (fieldType == Boolean.class || fieldType == boolean.class) {
      rawValue = resolver.getBoolean(fieldName);
    } else {
      rawValue = resolver.getRawConfig().getValue(fieldName);
    }

    // 使用策略转换
    TypeBindStrategy<?> strategy = typeStrategies.get(fieldType);
    if (strategy != null) {
      return ((TypeBindStrategy<Object>) strategy).convert(rawValue, fieldType);
    }

    return rawValue;
  }

  /** 类型绑定策略接口 */
  @FunctionalInterface
  public interface TypeBindStrategy<T> {
    /**
     * 将原始值转换为目标类型
     *
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    T convert(Object value, Class<?> targetType);
  }
}
