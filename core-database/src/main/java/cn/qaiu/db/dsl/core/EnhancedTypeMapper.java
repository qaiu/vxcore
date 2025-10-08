package cn.qaiu.db.dsl.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 增强的类型映射器
 * 
 * 提供完善的类型转换功能，包括：
 * - 数据库类型到Java类型的映射
 * - 日期时间格式处理
 * - 数值类型精度处理
 * - 字符串编码处理
 * - 自定义类型转换器
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class EnhancedTypeMapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedTypeMapper.class);
    
    // 日期时间格式器
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter ISO_DATETIME_WITH_MICROS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter SQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQL_DATETIME_WITH_MICROS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    
    // 类型转换器映射
    private static final Map<Class<?>, Function<Object, Object>> TYPE_CONVERTERS = new HashMap<>();
    
    static {
        // 初始化类型转换器
        initializeTypeConverters();
    }
    
    /**
     * 初始化类型转换器
     */
    private static void initializeTypeConverters() {
        // String 转换器
        TYPE_CONVERTERS.put(String.class, obj -> {
            if (obj == null) return null;
            return obj.toString();
        });
        
        // Long 转换器
        TYPE_CONVERTERS.put(Long.class, obj -> {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            }
            if (obj instanceof String) {
                try {
                    return Long.parseLong((String) obj);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to convert string to Long: {}", obj);
                    return null;
                }
            }
            return null;
        });
        
        // Integer 转换器
        TYPE_CONVERTERS.put(Integer.class, obj -> {
            if (obj == null) return null;
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            if (obj instanceof String) {
                try {
                    return Integer.parseInt((String) obj);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to convert string to Integer: {}", obj);
                    return null;
                }
            }
            return null;
        });
        
        // BigDecimal 转换器
        TYPE_CONVERTERS.put(BigDecimal.class, obj -> {
            if (obj == null) return null;
            if (obj instanceof BigDecimal) {
                return obj;
            }
            if (obj instanceof Number) {
                return BigDecimal.valueOf(((Number) obj).doubleValue());
            }
            if (obj instanceof String) {
                try {
                    return new BigDecimal((String) obj);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to convert string to BigDecimal: {}", obj);
                    return null;
                }
            }
            return null;
        });
        
        // Boolean 转换器
        TYPE_CONVERTERS.put(Boolean.class, obj -> {
            if (obj == null) return null;
            if (obj instanceof Boolean) {
                return obj;
            }
            if (obj instanceof String) {
                String str = ((String) obj).toLowerCase();
                if ("true".equals(str) || "1".equals(str) || "yes".equals(str)) {
                    return true;
                } else if ("false".equals(str) || "0".equals(str) || "no".equals(str)) {
                    return false;
                }
                return null; // 无效的布尔字符串
            }
            if (obj instanceof Number) {
                return ((Number) obj).intValue() != 0;
            }
            return null;
        });
        
        // LocalDateTime 转换器
        TYPE_CONVERTERS.put(LocalDateTime.class, obj -> {
            if (obj == null) return null;
            if (obj instanceof LocalDateTime) {
                return obj;
            }
            if (obj instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) obj).toLocalDateTime();
            }
            if (obj instanceof String) {
                return parseDateTime((String) obj);
            }
            return null;
        });
    }
    
    /**
     * 解析日期时间字符串
     */
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateTimeStr.trim();
        
        // 尝试不同的格式
        DateTimeFormatter[] formatters = {
            ISO_DATETIME_WITH_MICROS_FORMATTER,
            ISO_DATETIME_FORMATTER,
            SQL_DATETIME_WITH_MICROS_FORMATTER,
            SQL_DATETIME_FORMATTER
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，尝试处理特殊情况
        try {
            // 处理带T的ISO格式
            if (trimmed.contains("T")) {
                String normalized = trimmed.replace("T", " ");
                return LocalDateTime.parse(normalized, SQL_DATETIME_FORMATTER);
            }
            
            // 处理微秒精度
            if (trimmed.contains(".")) {
                String[] parts = trimmed.split("\\.");
                if (parts.length == 2) {
                    String baseTime = parts[0];
                    String micros = parts[1];
                    
                    // 确保微秒部分有6位
                    while (micros.length() < 6) {
                        micros += "0";
                    }
                    if (micros.length() > 6) {
                        micros = micros.substring(0, 6);
                    }
                    
                    String normalized = baseTime + "." + micros;
                    return LocalDateTime.parse(normalized, SQL_DATETIME_WITH_MICROS_FORMATTER);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse datetime string: {}", dateTimeStr, e);
        }
        
        return null;
    }
    
    /**
     * 转换对象到指定类型
     */
    public static <T> T convertToType(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        
        // 如果已经是目标类型，直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return targetType.cast(value);
        }
        
        // 使用类型转换器
        Function<Object, Object> converter = TYPE_CONVERTERS.get(targetType);
        if (converter != null) {
            try {
                Object converted = converter.apply(value);
                return targetType.cast(converted);
            } catch (Exception e) {
                LOGGER.warn("Failed to convert {} to {}", value.getClass().getSimpleName(), targetType.getSimpleName(), e);
                return null;
            }
        }
        
        // 处理枚举类型
        if (targetType.isEnum()) {
            if (value instanceof String) {
                try {
                    @SuppressWarnings("unchecked")
                    T enumValue = (T) Enum.valueOf((Class<? extends Enum>) targetType, (String) value);
                    return enumValue;
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to convert string to enum {}: {}", targetType.getSimpleName(), value);
                    return null;
                }
            }
        }
        
        // 默认尝试直接转换
        try {
            return targetType.cast(value);
        } catch (ClassCastException e) {
            LOGGER.warn("Cannot cast {} to {}", value.getClass().getSimpleName(), targetType.getSimpleName());
            return null;
        }
    }
    
    /**
     * 转换对象为数据库兼容的值
     */
    public static Object convertToDatabaseValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // LocalDateTime 转换为 Timestamp
        if (value instanceof LocalDateTime) {
            return java.sql.Timestamp.valueOf((LocalDateTime) value);
        }
        
        // BigDecimal 确保精度
        if (value instanceof BigDecimal) {
            return value;
        }
        
        // String 处理
        if (value instanceof String) {
            String str = (String) value;
            
            // 检查是否是日期时间格式
            if (isDateTimeString(str)) {
                LocalDateTime dateTime = parseDateTime(str);
                if (dateTime != null) {
                    return java.sql.Timestamp.valueOf(dateTime);
                }
            }
            
            // 检查是否是数字格式
            if (isNumericString(str)) {
                try {
                    return new BigDecimal(str);
                } catch (NumberFormatException e) {
                    // 不是有效的数字，返回原字符串
                }
            }
            
            return str;
        }
        
        return value;
    }
    
    /**
     * 检查字符串是否是日期时间格式
     */
    private static boolean isDateTimeString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = str.trim();
        
        // 检查是否包含日期时间特征
        return trimmed.matches("\\d{4}-\\d{2}-\\d{2}.*\\d{2}:\\d{2}:\\d{2}.*") ||
               trimmed.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }
    
    /**
     * 检查字符串是否是数字格式
     */
    private static boolean isNumericString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = str.trim();
        
        // 检查是否是数字格式（包括小数）
        return trimmed.matches("-?\\d+(\\.\\d+)?");
    }
    
    /**
     * 添加自定义类型转换器
     */
    public static void addTypeConverter(Class<?> targetType, Function<Object, Object> converter) {
        TYPE_CONVERTERS.put(targetType, converter);
    }
    
    /**
     * 移除类型转换器
     */
    public static void removeTypeConverter(Class<?> targetType) {
        TYPE_CONVERTERS.remove(targetType);
        
        // 如果是String类型，恢复默认转换器
        if (targetType == String.class) {
            TYPE_CONVERTERS.put(String.class, obj -> {
                if (obj == null) return null;
                return obj.toString();
            });
        }
    }
    
    /**
     * 获取所有支持的转换器类型
     */
    public static Map<Class<?>, Function<Object, Object>> getSupportedConverters() {
        return new HashMap<>(TYPE_CONVERTERS);
    }
}
