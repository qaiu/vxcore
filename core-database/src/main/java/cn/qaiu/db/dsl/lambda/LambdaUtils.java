package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.dsl.core.FieldNameConverter;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
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
                
                // 处理getter方法名，获取Java字段名
                String javaFieldName;
                if (methodName.startsWith("get")) {
                    String fieldName = methodName.substring(3);
                    javaFieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                } else if (methodName.startsWith("is")) {
                    String fieldName = methodName.substring(2);
                    javaFieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                } else {
                    javaFieldName = methodName;
                }
                
                // 尝试获取对应的Field对象，检查是否有@DdlColumn注解
                try {
                    String className = serializedLambda.getImplClass().replace('/', '.');
                    Class<?> clazz = Class.forName(className);
                    Field field = clazz.getDeclaredField(javaFieldName);
                    
                    // 检查是否有@DdlColumn注解
                    DdlColumn ddlColumn = field.getAnnotation(DdlColumn.class);
                    if (ddlColumn != null) {
                        // 优先使用value字段，如果为空则使用name字段
                        if (!ddlColumn.value().isEmpty()) {
                            return ddlColumn.value();
                        } else if (!ddlColumn.name().isEmpty()) {
                            return ddlColumn.name();
                        }
                    }
                } catch (Exception e) {
                    // 如果无法获取Field或注解，继续使用默认转换
                }
                
                // 转换为数据库字段名（下划线格式）
                return FieldNameConverter.toDatabaseFieldName(javaFieldName);
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
     * 清空缓存
     */
    public static void clearCache() {
        FIELD_NAME_CACHE.clear();
        FIELD_TYPE_CACHE.clear();
    }
}
