package cn.qaiu.db.dsl.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda表达式工具类
 * 用于从Lambda表达式中提取字段名和类型信息
 * 
 * @author qaiu
 */
public class LambdaUtils {
    
    private static final Map<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 从Lambda表达式中获取字段名
     */
    public static <T, R> String getFieldName(SFunction<T, R> column) {
        String cacheKey = column.getClass().getName();
        return FIELD_NAME_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Method method = column.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(column);
                String methodName = serializedLambda.getImplMethodName();
                
                // 处理getter方法名
                if (methodName.startsWith("get")) {
                    return toSnakeCase(methodName.substring(3));
                } else if (methodName.startsWith("is")) {
                    return toSnakeCase(methodName.substring(2));
                } else {
                    return toSnakeCase(methodName);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract field name from lambda expression", e);
            }
        });
    }
    
    /**
     * 从Lambda表达式中获取字段类型
     */
    public static <T, R> Class<R> getFieldType(SFunction<T, R> column) {
        String cacheKey = column.getClass().getName();
        @SuppressWarnings("unchecked")
        Class<R> type = (Class<R>) FIELD_TYPE_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Method method = column.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(column);
                String methodName = serializedLambda.getImplMethodName();
                String className = serializedLambda.getImplClass().replace('/', '.');
                Class<?> clazz = Class.forName(className);
                
                // 查找对应的方法
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName)) {
                        return m.getReturnType();
                    }
                }
                
                // 如果没找到，尝试从公共方法中查找
                methods = clazz.getMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName)) {
                        return m.getReturnType();
                    }
                }
                
                throw new RuntimeException("Method not found: " + methodName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract field type from lambda expression", e);
            }
        });
        return type;
    }
    
    /**
     * 将驼峰命名转换为下划线命名
     */
    private static String toSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * 将下划线命名转换为驼峰命名
     */
    public static String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
    
    /**
     * 清空缓存
     */
    public static void clearCache() {
        FIELD_NAME_CACHE.clear();
        FIELD_TYPE_CACHE.clear();
    }
}
