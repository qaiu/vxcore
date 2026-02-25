package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotations.param.PathVariable;
import cn.qaiu.vx.core.annotations.param.RequestBody;
import cn.qaiu.vx.core.annotations.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 参数匹配器 用于在方法重载时选择最佳匹配的方法和参数
 *
 * @author QAIU
 */
public class ParameterMatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterMatcher.class);

  /** 匹配结果 */
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
        LOGGER.debug("Method {} failed parameter extraction: {}", method.getName(), e.getMessage());
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
        // 使用ParamUtil提取参数值，如果Java -parameters不可用，尝试使用Javassist
        String paramName = getParameterName(method, parameter, i);
        args[i] = getParameterValueWithName(parameter, paramName, pathParams, queryParams, requestBody);
      }
    }

    return args;
  }

  /**
   * 获取参数名称
   * 优先使用Java反射（需要-parameters编译选项），
   * 如果不可用则尝试使用Javassist
   * 
   * @param method 方法
   * @param parameter 参数
   * @param index 参数索引
   * @return 参数名称
   */
  private static String getParameterName(Method method, Parameter parameter, int index) {
    // 首先尝试使用Java反射获取参数名称（需要-parameters编译选项）
    if (parameter.isNamePresent()) {
      return parameter.getName();
    }
    
    // 如果不可用，尝试使用Javassist
    try {
      Map<String, ?> paramMap = ReflectionUtil.getMethodParameter(method);
      List<String> paramNames = new ArrayList<>(paramMap.keySet());
      if (index < paramNames.size()) {
        return paramNames.get(index);
      }
    } catch (Exception e) {
      LOGGER.debug("Failed to get parameter name via Javassist for method {}, param index {}", 
          method.getName(), index);
    }
    
    // 如果都失败了，返回默认名称
    return "arg" + index;
  }

  /**
   * 使用指定的参数名称获取参数值
   */
  private static Object getParameterValueWithName(
      Parameter parameter,
      String paramName,
      Map<String, String> pathParams,
      MultiMap queryParams,
      JsonObject requestBody) {
    
    Class<?> paramType = parameter.getType();

    // 检查路径变量注解
    PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
    if (pathVar != null) {
      String varName = pathVar.value().isEmpty() ? paramName : pathVar.value();
      String value = pathParams.get(varName);
      if (value != null) {
        return ParamUtil.convertValue(value, paramType);
      } else if (pathVar.required()) {
        throw new IllegalArgumentException("Required path variable '" + varName + "' is missing");
      }
      return null;
    }

    // 检查请求体注解
    RequestBody requestBodyAnn = parameter.getAnnotation(RequestBody.class);
    if (requestBodyAnn != null) {
      if (requestBody != null) {
        if (paramType == JsonObject.class) {
          return requestBody;
        }
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
        return ParamUtil.convertValue(value, paramType);
      } else if (requestParam.required()) {
        throw new IllegalArgumentException("Required parameter '" + paramKey + "' is missing");
      } else if (!requestParam.defaultValue().isEmpty()) {
        return ParamUtil.convertValue(requestParam.defaultValue(), paramType);
      }
      return null;
    }

    // 无注解的参数，尝试按名称匹配
    String value = pathParams.get(paramName);
    if (value != null) {
      return ParamUtil.convertValue(value, paramType);
    }

    value = queryParams.get(paramName);
    if (value != null) {
      return ParamUtil.convertValue(value, paramType);
    }

    return null;
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

  /** 检查是否为特殊类型参数 */
  private static boolean isSpecialType(Class<?> type) {
    return type == RoutingContext.class
        || type == HttpServerRequest.class
        || type == HttpServerResponse.class;
  }
}
