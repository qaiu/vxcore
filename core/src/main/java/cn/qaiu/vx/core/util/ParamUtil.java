package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 参数工具类
 * 增强版本，支持更多类型转换和方法重载
 * <br>Create date 2021-04-30 09:22:18
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public final class ParamUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamUtil.class);

    public static Map<String, Object> multiMapToMap(MultiMap multiMap) {
        if (multiMap == null) return null;
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry : multiMap.entries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <T> T multiMapToEntity(MultiMap multiMap, Class<T> tClass) {
        Map<String, Object> map = multiMapToMap(multiMap);
        if (map == null) {
            return null;
        }
        return new JsonObject(map).mapTo(tClass);
    }

    public static MultiMap paramsToMap(String paramString) {
        MultiMap entries = MultiMap.caseInsensitiveMultiMap();
        if (paramString == null) return entries;
        String[] params = paramString.split("&");
        if (params.length == 0) return entries;
        for (String param : params) {
            String[] kv = param.split("=");
            if (kv.length == 2) {
                entries.set(kv[0], kv[1]);
            } else {
                entries.set(kv[0], "");
            }
        }
        return entries;
    }
    
    /**
     * 根据参数注解获取参数值
     * 
     * @param parameter 方法参数
     * @param pathParams 路径参数
     * @param queryParams 查询参数
     * @param requestBody 请求体JSON
     * @return 参数值
     */
    public static Object getParameterValue(Parameter parameter, Map<String, String> pathParams,
                                         MultiMap queryParams, JsonObject requestBody) {
        String paramName = parameter.getName();
        Class<?> paramType = parameter.getType();
        
        // 检查路径变量注解
        PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
        if (pathVar != null) {
            String varName = pathVar.value().isEmpty() ? paramName : pathVar.value();
            String value = pathParams.get(varName);
            if (value != null) {
                return convertValue(value, paramType);
            } else if (pathVar.required()) {
                throw new IllegalArgumentException("Required path variable '" + varName + "' is missing");
            }
            return null;
        }
        
        // 检查请求体注解
        RequestBody requestBodyAnn = parameter.getAnnotation(RequestBody.class);
        if (requestBodyAnn != null) {
            if (requestBody != null) {
                return requestBody.mapTo(paramType);
            } else if (requestBodyAnn.required()) {
                throw new IllegalArgumentException("Required request body is missing");
            }
            return null;
        }
        
        // 检查请求参数注解
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            String paramKey = requestParam.value().isEmpty() ? paramName : requestParam.value();
            String value = queryParams.get(paramKey);
            if (value != null) {
                return convertValue(value, paramType);
            } else if (requestParam.required()) {
                throw new IllegalArgumentException("Required parameter '" + paramKey + "' is missing");
            } else if (!requestParam.defaultValue().isEmpty()) {
                return convertValue(requestParam.defaultValue(), paramType);
            }
            return null;
        }
        
        // 无注解的参数，尝试按名称匹配
        String value = pathParams.get(paramName);
        if (value != null) {
            return convertValue(value, paramType);
        }
        
        value = queryParams.get(paramName);
        if (value != null) {
            return convertValue(value, paramType);
        }
        
        return null;
    }
    
    /**
     * 转换字符串值到指定类型
     * 
     * @param value 字符串值
     * @param targetType 目标类型
     * @return 转换后的对象
     */
    public static Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 基本类型转换
            if (targetType == String.class) {
                return value;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(value);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(value);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(value);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(value);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(value);
            } else if (targetType == Character.class || targetType == char.class) {
                return Character.valueOf(value.charAt(0));
            } else if (targetType == Short.class || targetType == short.class) {
                return Short.valueOf(value);
            } else if (targetType == Byte.class || targetType == byte.class) {
                return Byte.valueOf(value);
            } else if (Enum.class.isAssignableFrom(targetType)) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
                return Enum.valueOf(enumClass, value);
            }
            
            throw new IllegalArgumentException("Unsupported type: " + targetType.getSimpleName());
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert '" + value + "' to " + targetType.getSimpleName(), e);
        }
    }
    
    /**
     * 获取方法的所有候选方法（同名方法）
     * 
     * @param methods 所有方法
     * @param methodName 方法名
     * @return 候选方法列表
     */
    public static List<java.lang.reflect.Method> getCandidateMethods(java.lang.reflect.Method[] methods, String methodName) {
        return Arrays.stream(methods)
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());
    }
    
    /**
     * 选择最佳匹配的方法
     * 
     * @param candidates 候选方法
     * @param pathParams 路径参数
     * @param queryParams 查询参数
     * @param hasRequestBody 是否有请求体
     * @return 最佳匹配的方法，如果没有则返回null
     */
    public static java.lang.reflect.Method selectBestMatch(List<java.lang.reflect.Method> candidates,
                                                          Map<String, String> pathParams,
                                                          MultiMap queryParams,
                                                          boolean hasRequestBody) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        
        // 计算每个方法的匹配分数
        java.lang.reflect.Method bestMatch = null;
        int bestScore = -1;
        
        for (java.lang.reflect.Method method : candidates) {
            int score = MethodMatcher.calculateScore(method, pathParams, queryParams, hasRequestBody);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = method;
            }
        }
        
        // 如果最佳分数小于0，说明没有完全匹配的方法
        if (bestScore < 0) {
            return null;
        }
        
        return bestMatch;
    }

}
