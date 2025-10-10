package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * 参数匹配器
 * 用于在方法重载时选择最佳匹配的方法和参数
 * 
 * @author QAIU
 */
public class ParameterMatcher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterMatcher.class);
    
    /**
     * 匹配结果
     */
    public static class MatchResult {
        private final Method method;
        private final Object[] parameters;
        private final int score;
        
        public MatchResult(Method method, Object[] parameters, int score) {
            this.method = method;
            this.parameters = parameters;
            this.score = score;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public Object[] getParameters() {
            return parameters;
        }
        
        public int getScore() {
            return score;
        }
    }
    
    /**
     * 查找最佳匹配的方法
     * 
     * @param methods 候选方法列表
     * @param ctx 路由上下文
     * @return 匹配结果，如果没有找到则返回null
     */
    public static MatchResult findBestMatch(List<Method> methods, RoutingContext ctx) {
        if (methods.isEmpty()) {
            return null;
        }
        
        if (methods.size() == 1) {
            Method method = methods.get(0);
            Object[] params = extractParameters(method, ctx);
            return new MatchResult(method, params, 100);
        }
        
        // 计算每个方法的匹配分数
        MatchResult bestMatch = null;
        int bestScore = -1;
        
        for (Method method : methods) {
            try {
                Object[] params = extractParameters(method, ctx);
                int score = calculateMatchScore(method, params, ctx);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = new MatchResult(method, params, score);
                }
            } catch (Exception e) {
                LOGGER.debug("Method {} failed parameter extraction: {}", method.getUsername(), e.getMessage());
                // 继续尝试其他方法
            }
        }
        
        return bestMatch;
    }
    
    /**
     * 提取方法参数
     * 
     * @param method 方法
     * @param ctx 路由上下文
     * @return 参数数组
     */
    private static Object[] extractParameters(Method method, RoutingContext ctx) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        Map<String, String> pathParams = ctx.pathParams();
        MultiMap queryParams = ctx.queryParams();
        JsonObject requestBody = null;
        
        // 检查是否有请求体
        if (ctx.body() != null && ctx.body().asJsonObject() != null) {
            requestBody = ctx.body().asJsonObject();
        }
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            // 特殊类型参数
            if (paramType == RoutingContext.class) {
                args[i] = ctx;
            } else if (paramType == HttpServerRequest.class) {
                args[i] = ctx.request();
            } else if (paramType == HttpServerResponse.class) {
                args[i] = ctx.response();
            } else {
                // 使用ParamUtil提取参数值
                args[i] = ParamUtil.getParameterValue(parameter, pathParams, queryParams, requestBody);
            }
        }
        
        return args;
    }
    
    /**
     * 计算匹配分数
     * 
     * @param method 方法
     * @param params 参数数组
     * @param ctx 路由上下文
     * @return 匹配分数
     */
    private static int calculateMatchScore(Method method, Object[] params, RoutingContext ctx) {
        Parameter[] parameters = method.getParameters();
        int score = 0;
        
        Map<String, String> pathParams = ctx.pathParams();
        MultiMap queryParams = ctx.queryParams();
        boolean hasRequestBody = ctx.body() != null && ctx.body().asJsonObject() != null;
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object paramValue = params[i];
            Class<?> paramType = parameter.getType();
            
            // 跳过特殊类型参数
            if (isSpecialType(paramType)) {
                continue;
            }
            
            // 检查路径参数注解
            PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
            if (pathVar != null) {
                if (paramValue != null) {
                    score += 100; // 路径参数匹配得分最高
                } else if (pathVar.required()) {
                    return -1; // 必需的路径参数缺失
                }
                continue;
            }
            
            // 检查请求体注解
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                if (hasRequestBody && paramValue != null) {
                    score += 80; // 请求体匹配得分较高
                } else if (requestBody.required() && !hasRequestBody) {
                    return -1; // 必需的请求体缺失
                }
                continue;
            }
            
            // 检查请求参数注解
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                if (paramValue != null) {
                    score += 50; // 查询参数匹配得分中等
                } else if (requestParam.required()) {
                    return -1; // 必需的查询参数缺失
                } else if (!requestParam.defaultValue().isEmpty()) {
                    score += 10; // 有默认值的参数给予少量分数
                }
                continue;
            }
            
            // 无注解的参数
            if (paramValue != null) {
                score += 20; // 按名称匹配的参数
            } else {
                score -= 10; // 未匹配的参数给予负分
            }
        }
        
        return score;
    }
    
    /**
     * 检查是否为特殊类型参数
     */
    private static boolean isSpecialType(Class<?> type) {
        return type == RoutingContext.class ||
               type == HttpServerRequest.class ||
               type == HttpServerResponse.class;
    }
}