package cn.qaiu.vx.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类型转换器注册表
 * 管理所有类型转换器，支持自定义扩展
 * 
 * @author QAIU
 */
public class TypeConverterRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterRegistry.class);
    
    private static final Map<Class<?>, TypeConverter<?>> CONVERTERS = new ConcurrentHashMap<>();
    
    static {
        // 注册内置转换器
        registerBuiltinConverters();
    }
    
    /**
     * 注册类型转换器
     * 
     * @param converter 转换器
     */
    public static void register(TypeConverter<?> converter) {
        CONVERTERS.put(converter.getTargetType(), converter);
        LOGGER.debug("Registered type converter for: {}", converter.getTargetType().getSimpleName());
    }
    
    /**
     * 获取类型转换器
     * 
     * @param targetType 目标类型
     * @return 转换器，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeConverter<T> getConverter(Class<T> targetType) {
        return (TypeConverter<T>) CONVERTERS.get(targetType);
    }
    
    /**
     * 检查是否支持指定类型的转换
     * 
     * @param targetType 目标类型
     * @return 是否支持
     */
    public static boolean isSupported(Class<?> targetType) {
        return CONVERTERS.containsKey(targetType);
    }
    
    /**
     * 转换值到指定类型
     * 
     * @param value 字符串值
     * @param targetType 目标类型
     * @return 转换后的对象
     * @throws IllegalArgumentException 转换失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> targetType) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        TypeConverter<T> converter = getConverter(targetType);
        if (converter != null) {
            return converter.convert(value);
        }
        
        // 尝试使用反射进行基本类型转换
        return convertByReflection(value, targetType);
    }
    
    /**
     * 使用反射进行基本类型转换
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertByReflection(String value, Class<T> targetType) {
        try {
            if (targetType == String.class) {
                return (T) value;
            } else if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(value);
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(value);
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(value);
            } else if (targetType == Float.class || targetType == float.class) {
                return (T) Float.valueOf(value);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return (T) Boolean.valueOf(value);
            } else if (targetType == Character.class || targetType == char.class) {
                return (T) Character.valueOf(value.charAt(0));
            } else if (targetType == Short.class || targetType == short.class) {
                return (T) Short.valueOf(value);
            } else if (targetType == Byte.class || targetType == byte.class) {
                return (T) Byte.valueOf(value);
            } else if (Enum.class.isAssignableFrom(targetType)) {
                return (T) Enum.valueOf((Class<? extends Enum>) targetType, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert '" + value + "' to " + targetType.getSimpleName(), e);
        }
        
        throw new IllegalArgumentException("Unsupported type: " + targetType.getSimpleName());
    }
    
    /**
     * 注册内置转换器
     */
    private static void registerBuiltinConverters() {
        // LocalDate转换器
        register(new TypeConverter<LocalDate>() {
            @Override
            public LocalDate convert(String value) {
                return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            @Override
            public Class<LocalDate> getTargetType() {
                return LocalDate.class;
            }
        });
        
        // LocalTime转换器
        register(new TypeConverter<LocalTime>() {
            @Override
            public LocalTime convert(String value) {
                return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
            }
            
            @Override
            public Class<LocalTime> getTargetType() {
                return LocalTime.class;
            }
        });
        
        // LocalDateTime转换器
        register(new TypeConverter<LocalDateTime>() {
            @Override
            public LocalDateTime convert(String value) {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            @Override
            public Class<LocalDateTime> getTargetType() {
                return LocalDateTime.class;
            }
        });
        
        // Optional转换器
        register(new TypeConverter<Optional<String>>() {
            @Override
            public Optional<String> convert(String value) {
                return Optional.ofNullable(value);
            }
            
            @Override
            public Class<Optional<String>> getTargetType() {
                return (Class<Optional<String>>) (Class<?>) Optional.class;
            }
        });
        
        LOGGER.info("Registered {} builtin type converters", CONVERTERS.size());
    }
}