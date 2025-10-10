package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 方法匹配评分器
 * 用于在方法重载时选择最佳匹配的方法
 * 
 * @author QAIU
 */
public class MethodMatcher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodMatcher.class);
    
    /**
     * 计算方法的匹配分数
     * 分数越高表示匹配度越好
     * 
     * @param method 方法
     * @param pathParams 路径参数
     * @param queryParams 查询参数
     * @param hasRequestBody 是否有请求体
     * @return 匹配分数
     */
    public static int calculateScore(Method method, Map<String, String> pathParams, 
                                   MultiMap queryParams, boolean hasRequestBody) {
        Parameter[] parameters = method.getParameters();
        int score = 0;
        
        for (Parameter parameter : parameters) {
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();
            
            // 跳过特殊类型参数
            if (isSpecialType(paramType)) {
                continue;
            }
            
            // 检查路径参数注解
            PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
            if (pathVar != null) {
                String varName = pathVar.value().isEmpty() ? paramName : pathVar.value();
                if (pathParams.containsKey(varName)) {
                    score += 100; // 路径参数匹配得分最高
                } else if (pathVar.required()) {
                    return -1; // 必需的路径参数缺失，直接返回-1
                }
                continue;
            }
            
            // 检查请求体注解
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                if (hasRequestBody) {
                    score += 80; // 请求体匹配得分较高
                } else if (requestBody.required()) {
                    return -1; // 必需的请求体缺失
                }
                continue;
            }
            
            // 检查请求参数注解
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String paramKey = requestParam.value().isEmpty() ? paramName : requestParam.value();
                if (queryParams.contains(paramKey)) {
                    score += 50; // 查询参数匹配得分中等
                } else if (requestParam.required()) {
                    return -1; // 必需的查询参数缺失
                } else if (!requestParam.defaultValue().isEmpty()) {
                    score += 10; // 有默认值的参数给予少量分数
                }
                continue;
            }
            
            // 无注解的参数，尝试按名称匹配
            if (pathParams.containsKey(paramName)) {
                score += 30; // 路径参数按名称匹配
            } else if (queryParams.contains(paramName)) {
                score += 20; // 查询参数按名称匹配
            } else {
                // 参数未匹配，给予负分
                score -= 10;
            }
        }
        
        return score;
    }
    
    /**
     * 检查是否为特殊类型参数
     * 这些参数不需要从请求中获取值
     */
    private static boolean isSpecialType(Class<?> type) {
        return type == RoutingContext.class ||
               type == HttpServerRequest.class ||
               type == HttpServerResponse.class;
    }
    
    /**
     * 验证方法是否完全匹配
     * 检查所有必需参数是否都能提供值
     * 
     * @param method 方法
     * @param pathParams 路径参数
     * @param queryParams 查询参数
     * @param hasRequestBody 是否有请求体
     * @return 是否完全匹配
     */
    public static boolean isFullyMatched(Method method, Map<String, String> pathParams,
                                       MultiMap queryParams, boolean hasRequestBody) {
        Parameter[] parameters = method.getParameters();
        
        for (Parameter parameter : parameters) {
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();
            
            // 跳过特殊类型参数
            if (isSpecialType(paramType)) {
                continue;
            }
            
            // 检查路径参数注解
            PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
            if (pathVar != null) {
                String varName = pathVar.value().isEmpty() ? paramName : pathVar.value();
                if (pathVar.required() && !pathParams.containsKey(varName)) {
                    return false;
                }
                continue;
            }
            
            // 检查请求体注解
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                if (requestBody.required() && !hasRequestBody) {
                    return false;
                }
                continue;
            }
            
            // 检查请求参数注解
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String paramKey = requestParam.value().isEmpty() ? paramName : requestParam.value();
                if (requestParam.required() && !queryParams.contains(paramKey)) {
                    return false;
                }
                continue;
            }
            
            // 无注解的参数，检查是否有值
            if (!pathParams.containsKey(paramName) && !queryParams.contains(paramName)) {
                return false;
            }
        }
        
        return true;
    }
}
