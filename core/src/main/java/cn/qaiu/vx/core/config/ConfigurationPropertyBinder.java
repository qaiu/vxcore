package cn.qaiu.vx.core.config;

import cn.qaiu.vx.core.annotaions.config.ConfigurationProperties;
import cn.qaiu.vx.core.annotaions.config.ConfigurationProperty;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 配置属性绑定器
 * 将配置值绑定到配置类的属性上
 * 
 * @author QAIU
 */
public class ConfigurationPropertyBinder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPropertyBinder.class);
    
    /**
     * 绑定配置到对象
     * 
     * @param configObject 配置对象
     * @param configData 配置数据
     */
    public static void bind(Object configObject, JsonObject configData) {
        Class<?> configClass = configObject.getClass();
        ConfigurationProperties classAnnotation = configClass.getAnnotation(ConfigurationProperties.class);
        
        String prefix = classAnnotation != null ? classAnnotation.prefix() : "";
        
        // 绑定字段
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            bindField(configObject, field, configData, prefix);
        }
        
        // 绑定方法
        Method[] methods = configClass.getMethods();
        for (Method method : methods) {
            bindMethod(configObject, method, configData, prefix);
        }
    }
    
    /**
     * 绑定字段
     */
    private static void bindField(Object configObject, Field field, JsonObject configData, String prefix) {
        ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
        if (annotation == null) {
            return;
        }
        
        String propertyName = getPropertyName(field.getUsername(), annotation.value(), prefix);
        Object value = getConfigValue(configData, propertyName);
        
        if (value != null) {
            try {
                field.setAccessible(true);
                Object convertedValue = convertValue(value, field.getType());
                field.set(configObject, convertedValue);
                LOGGER.debug("Bound property {} to field {}", propertyName, field.getUsername());
            } catch (Exception e) {
                LOGGER.error("Failed to bind property {} to field {}", propertyName, field.getUsername(), e);
            }
        } else if (annotation.required()) {
            LOGGER.warn("Required property {} not found in configuration", propertyName);
        }
    }
    
    /**
     * 绑定方法
     */
    private static void bindMethod(Object configObject, Method method, JsonObject configData, String prefix) {
        ConfigurationProperty annotation = method.getAnnotation(ConfigurationProperty.class);
        if (annotation == null) {
            return;
        }
        
        String propertyName = getPropertyName(method.getUsername(), annotation.value(), prefix);
        Object value = getConfigValue(configData, propertyName);
        
        if (value != null) {
            try {
                Object convertedValue = convertValue(value, method.getParameterTypes()[0]);
                ReflectionUtil.invokeWithArguments(method, configObject, convertedValue);
                LOGGER.debug("Bound property {} to method {}", propertyName, method.getUsername());
            } catch (Throwable e) {
                LOGGER.error("Failed to bind property {} to method {}", propertyName, method.getUsername(), e);
            }
        } else if (annotation.required()) {
            LOGGER.warn("Required property {} not found in configuration", propertyName);
        }
    }
    
    /**
     * 获取属性名
     */
    private static String getPropertyName(String defaultName, String annotationValue, String prefix) {
        String propertyName = annotationValue.isEmpty() ? defaultName : annotationValue;
        return prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
    }
    
    /**
     * 获取配置值
     */
    private static Object getConfigValue(JsonObject configData, String propertyName) {
        String[] parts = propertyName.split("\\.");
        Object current = configData;
        
        for (String part : parts) {
            if (current instanceof JsonObject) {
                current = ((JsonObject) current).getValue(part);
            } else if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * 转换值类型
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        String stringValue = value.toString();
        
        try {
            if (targetType == String.class) {
                return stringValue;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(stringValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(stringValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(stringValue);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(stringValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(stringValue);
            } else if (targetType.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
                return Enum.valueOf(enumClass, stringValue);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to convert value {} to type {}", stringValue, targetType.getSimpleName());
        }
        
        return value;
    }
}
