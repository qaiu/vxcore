package cn.qaiu.vx.core.handlerfactory;

import static cn.qaiu.vx.core.util.ConfigConstant.ROUTE_TIME_OUT;
import static cn.qaiu.vx.core.verticle.ReverseProxyVerticle.REROUTE_PATH_PREFIX;
import static io.vertx.core.http.HttpHeaders.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.annotations.SockRouteMapper;
import cn.qaiu.vx.core.annotations.websocket.WebSocketHandler;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.exception.BuiltinExceptionHandler;
import cn.qaiu.vx.core.exception.BusinessException;
import cn.qaiu.vx.core.exception.ExceptionHandlerManager;
import cn.qaiu.vx.core.exception.SystemException;
import cn.qaiu.vx.core.exception.ValidationException;
import cn.qaiu.vx.core.interceptor.BeforeInterceptor;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.security.SecurityInterceptor;
import cn.qaiu.vx.core.util.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由映射, 参数绑定 <br>
 * Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterHandlerFactory implements BaseHttpApi {

  private final WebSocketHandlerFactory webSocketHandlerFactory;
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterHandlerFactory.class);

  private static final Set<HttpMethod> httpMethods =
      new HashSet<>() {
        {
          add(HttpMethod.GET);
          add(HttpMethod.POST);
          add(HttpMethod.OPTIONS);
          add(HttpMethod.PUT);
          add(HttpMethod.DELETE);
          add(HttpMethod.HEAD);
        }
      };

  private final String gatewayPrefix;
  private final ExceptionHandlerManager exceptionHandlerManager;
  private SecurityInterceptor securityInterceptor;
  private cn.qaiu.vx.core.registry.ServiceRegistry serviceRegistry;

  public RouterHandlerFactory(String gatewayPrefix) {
    this.webSocketHandlerFactory = new WebSocketHandlerFactory(VertxHolder.getVertxInstance());
    Objects.requireNonNull(gatewayPrefix, "The gateway prefix is empty.");
    this.gatewayPrefix = gatewayPrefix;
    this.exceptionHandlerManager = new ExceptionHandlerManager();
    initBuiltinExceptionHandlers();
    initSecurityInterceptor();
  }

  /** 设置服务注册表，用于 Controller 依赖注入时查找已注册的服务实例 */
  public void setServiceRegistry(cn.qaiu.vx.core.registry.ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  /** 初始化安全拦截器 */
  private void initSecurityInterceptor() {
    try {
      // 从全局配置获取安全配置
      JsonObject globalConfig = SharedDataUtil.getGlobalConfig();
      if (globalConfig != null) {
        JsonObject securityConfig = globalConfig.getJsonObject("security");
        if (securityConfig != null && securityConfig.getBoolean("enabled", false)) {
          this.securityInterceptor = new SecurityInterceptor(securityConfig);
          LOGGER.info("Security interceptor initialized");
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to initialize security interceptor: {}", e.getMessage());
    }
  }

  /** 初始化内置异常处理器 */
  private void initBuiltinExceptionHandlers() {
    // 注册内置异常处理器
    exceptionHandlerManager.registerGlobalHandler(
        BusinessException.class, new BuiltinExceptionHandler());
    exceptionHandlerManager.registerGlobalHandler(
        ValidationException.class, new BuiltinExceptionHandler());
    exceptionHandlerManager.registerGlobalHandler(
        SystemException.class, new BuiltinExceptionHandler());
    exceptionHandlerManager.registerGlobalHandler(
        IllegalArgumentException.class, new BuiltinExceptionHandler());
    exceptionHandlerManager.registerGlobalHandler(
        NullPointerException.class, new BuiltinExceptionHandler());
    exceptionHandlerManager.registerGlobalHandler(
        RuntimeException.class, new BuiltinExceptionHandler());
  }

  /** 开始扫描并注册handler */
  public Router createRouter() {
    Router mainRouter = createMainRouter();
    setupGlobalHandlers(mainRouter);
    registerRouteHandlers(mainRouter);
    registerWebSocketHandlers(mainRouter);
    setupErrorHandlers(mainRouter);
    return mainRouter;
  }

  /** 创建主路由器 */
  private Router createMainRouter() {
    return Router.router(VertxHolder.getVertxInstance());
  }

  /** 设置全局处理器 */
  private void setupGlobalHandlers(Router router) {
    // 请求重写处理
    router.route().handler(this::handleRequestRewrite);

    // CORS处理
    router
        .route()
        .handler(
            CorsHandler.create()
                .addOriginWithRegex(".*")
                .allowCredentials(true)
                .allowedMethods(httpMethods));

    // 文件上传处理
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

    // 拦截器
    setupInterceptors(router);
  }

  /** 处理请求重写 */
  private void handleRequestRewrite(RoutingContext ctx) {
    String realPath = ctx.request().uri();
    if (realPath.startsWith(REROUTE_PATH_PREFIX)) {
      String rePath = realPath.replace(REROUTE_PATH_PREFIX, "");
      ctx.reroute(rePath);
      return;
    }

    LOGGER.debug(
        "New request:{}, {}, {}",
        ctx.request().path(),
        ctx.request().absoluteURI(),
        ctx.request().method());

    // 设置CORS头
    setCorsHeaders(ctx);
    ctx.next();
  }

  /** 设置CORS头 */
  private void setCorsHeaders(RoutingContext ctx) {
    ctx.response().headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    ctx.response().headers().add(DATE, LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
    ctx.response()
        .headers()
        .add(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    ctx.response()
        .headers()
        .add(
            ACCESS_CONTROL_ALLOW_HEADERS,
            "X-PINGOTHER, Origin,Content-Type, Accept, X-Requested-With, Dev, Authorization, Version, Token");
    ctx.response().headers().add(ACCESS_CONTROL_MAX_AGE, "1728000");
  }

  /** 设置拦截器 */
  private void setupInterceptors(Router router) {
    Set<Handler<RoutingContext>> interceptorSet = getInterceptorSet();
    Route route = router.route("/*");
    interceptorSet.forEach(route::handler);
  }

  /** 注册路由处理器 */
  private void registerRouteHandlers(Router router) {
    try {
      Set<Class<?>> handlers = reflections.getTypesAnnotatedWith(RouteHandler.class);
      List<Class<?>> sortedHandlers = sortHandlersByOrder(handlers);

      for (Class<?> handler : sortedHandlers) {
        try {
          registerHandler(router, handler);
        } catch (Throwable e) {
          LOGGER.error("Error register {}, Error details：", handler, e.getCause());
        }
      }
    } catch (Exception e) {
      LOGGER.error("Manually Register Handler Fail, Error details：" + e.getMessage());
    }
  }

  /** 按优先级排序处理器 */
  private List<Class<?>> sortHandlersByOrder(Set<Class<?>> handlers) {
    Comparator<Class<?>> comparator =
        (c1, c2) -> {
          RouteHandler routeHandler1 = c1.getAnnotation(RouteHandler.class);
          RouteHandler routeHandler2 = c2.getAnnotation(RouteHandler.class);
          return Integer.compare(routeHandler2.order(), routeHandler1.order());
        };
    return handlers.stream().sorted(comparator).toList();
  }

  /** 设置错误处理器 */
  private void setupErrorHandlers(Router router) {
    router.errorHandler(
        405, ctx -> doFireJsonResultResponse(ctx, JsonResult.error("Method Not Allowed", 405)));
    router.errorHandler(
        404,
        ctx ->
            ctx.response()
                .setStatusCode(404)
                .setChunked(true)
                .end("Internal server error: 404 not found"));
  }

  /** 注册单个处理器 */
  private void registerHandler(Router router, Class<?> handler) throws Throwable {
    String root = getRootPath(handler);
    Object instance = createControllerInstance(handler);
    Method[] methods = handler.getMethods();

    // 获取路由方法
    List<Method> routeMethods = getRouteMethods(methods);

    // 注册异常处理器
    registerExceptionHandlers(handler, methods);

    // 按路由分组并注册
    Map<String, List<Method>> routeGroups = groupMethodsByRoute(routeMethods, root);
    registerRouteGroups(router, instance, routeGroups);

    // 注册WebSocket处理器
    if (instance.getClass().isAnnotationPresent(WebSocketHandler.class)) {
      webSocketHandlerFactory.registerWebSocketHandler(instance.getClass());
    }
  }

  /** 创建Controller实例并自动注入依赖 支持构造器注入和字段注入 */
  private Object createControllerInstance(Class<?> handlerClass) throws Throwable {
    // 1. 尝试构造器注入
    Object instance = tryConstructorInjection(handlerClass);

    if (instance != null) {
      // 构造器注入成功,返回实例
      return instance;
    }

    // 2. 使用无参构造器创建实例
    instance = ReflectionUtil.newWithNoParam(handlerClass);

    // 3. 执行字段注入
    injectFields(instance);

    return instance;
  }

  /**
   * 尝试使用带@Inject注解的构造器进行依赖注入
   *
   * @return 成功返回实例,失败返回null
   */
  private Object tryConstructorInjection(Class<?> handlerClass) {
    try {
      // 查找带@Inject注解的构造器
      java.lang.reflect.Constructor<?>[] constructors = handlerClass.getDeclaredConstructors();
      for (java.lang.reflect.Constructor<?> constructor : constructors) {
        if (constructor.isAnnotationPresent(javax.inject.Inject.class)) {
          // 找到@Inject构造器,获取参数并注入
          Class<?>[] paramTypes = constructor.getParameterTypes();
          Object[] args = new Object[paramTypes.length];

          for (int i = 0; i < paramTypes.length; i++) {
            args[i] = resolveService(paramTypes[i]);
            if (args[i] == null) {
              LOGGER.warn(
                  "Failed to resolve dependency for parameter type: {}", paramTypes[i].getName());
              return null;
            }
          }

          constructor.setAccessible(true);
          return constructor.newInstance(args);
        }
      }
    } catch (Exception e) {
      LOGGER.warn(
          "Constructor injection failed for {}: {}", handlerClass.getName(), e.getMessage());
    }
    return null;
  }

  /** 为实例的字段注入依赖 */
  private void injectFields(Object instance) {
    Class<?> clazz = instance.getClass();

    // 遍历所有字段
    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
    for (java.lang.reflect.Field field : fields) {
      // 检查是否有@Inject注解
      if (field.isAnnotationPresent(javax.inject.Inject.class)) {
        try {
          Class<?> fieldType = field.getType();
          Object service = resolveService(fieldType);

          if (service != null) {
            field.setAccessible(true);
            field.set(instance, service);
            LOGGER.debug(
                "Injected {} into {}.{}",
                fieldType.getSimpleName(),
                clazz.getSimpleName(),
                field.getName());
          } else {
            LOGGER.warn(
                "Failed to inject field {}.{}: service not found",
                clazz.getSimpleName(),
                field.getName());
          }
        } catch (Exception e) {
          LOGGER.error("Failed to inject field {}.{}", clazz.getSimpleName(), field.getName(), e);
        }
      }
    }
  }

  /**
   * 解析Service依赖。 优先从 ServiceRegistry 查找已注册的实例（支持接口和具体类）， 找不到时再 fallback 到 EventBus
   * 代理（接口）或反射创建（具体类）。
   */
  private Object resolveService(Class<?> serviceType) {
    try {
      if (serviceType.isPrimitive()
          || serviceType == String.class
          || serviceType.getName().startsWith("java.lang")
          || serviceType.getName().startsWith("java.util")) {
        LOGGER.debug("Skipping basic type: {}", serviceType.getName());
        return null;
      }

      // 1. 优先从 ServiceRegistry 查找已注册的服务实例
      if (serviceRegistry != null) {
        Object registered = serviceRegistry.getServiceByType(serviceType);
        if (registered != null) {
          LOGGER.debug(
              "Resolved {} from ServiceRegistry: {}",
              serviceType.getSimpleName(),
              registered.getClass().getSimpleName());
          return registered;
        }
      }

      // 2. ServiceRegistry 中未找到，按类型 fallback
      if (serviceType.isInterface()) {
        try {
          return cn.qaiu.vx.core.util.AsyncServiceUtil.getAsyncServiceInstance(serviceType);
        } catch (Exception proxyEx) {
          LOGGER.warn(
              "EventBus proxy not available for {}: {}",
              serviceType.getName(),
              proxyEx.getMessage());
          return null;
        }
      }

      // 3. 具体类：反射创建新实例
      try {
        return ReflectionUtil.newWithNoParam(serviceType);
      } catch (Exception createEx) {
        LOGGER.debug(
            "Failed to create instance for {}: {}", serviceType.getName(), createEx.getMessage());
        return null;
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to resolve service {}: {}", serviceType.getName(), e.getMessage());
      return null;
    }
  }

  /** 获取路由方法 */
  private List<Method> getRouteMethods(Method[] methods) {
    Comparator<Method> comparator =
        (m1, m2) -> {
          RouteMapping mapping1 = m1.getAnnotation(RouteMapping.class);
          RouteMapping mapping2 = m2.getAnnotation(RouteMapping.class);

          // 首先按照用户配置的order排序（降序）
          int orderCompare = Integer.compare(mapping2.order(), mapping1.order());
          if (orderCompare != 0) {
            return orderCompare;
          }

          // 如果order相同（都为默认值0），则按路径复杂度自动排序
          return Integer.compare(
              calculatePathPriority(mapping2.value()), calculatePathPriority(mapping1.value()));
        };

    List<Method> methodList =
        Stream.of(methods)
            .filter(method -> method.isAnnotationPresent(RouteMapping.class))
            .sorted(comparator)
            .collect(Collectors.toList());

    methodList.addAll(
        Stream.of(methods)
            .filter(method -> method.isAnnotationPresent(SockRouteMapper.class))
            .toList());

    return methodList;
  }

  /**
   * 计算路径优先级分数 优先级原则：具体路径 > 路径变量路径 评分规则： - 每个固定路径段: +100分 - 每个路径变量({var}或:var): +10分 - 路径深度（段数）: 基础分
   *
   * <p>示例： /a/b/c -> 300分（3个固定段） /a/b/:param -> 210分（2个固定段 + 1个变量） /a/:id -> 110分（1个固定段 + 1个变量）
   *
   * @param path 路径
   * @return 优先级分数，分数越高优先级越高
   */
  private int calculatePathPriority(String path) {
    if (path == null || path.isEmpty() || "/".equals(path)) {
      return 0;
    }

    // 移除开头和结尾的斜杠
    String cleanPath = path.replaceAll("^/+|/+$", "");
    if (cleanPath.isEmpty()) {
      return 0;
    }

    // 分割路径段
    String[] segments = cleanPath.split("/");
    int score = 0;

    for (String segment : segments) {
      if (segment.isEmpty()) {
        continue;
      }

      // 检查是否是路径变量
      if (segment.startsWith(":") || segment.matches("^\\{.+\\}$")) {
        // 路径变量段，得分较低
        score += 10;
      } else {
        // 固定路径段，得分较高
        score += 100;
      }
    }

    return score;
  }

  /** 注册异常处理器 */
  private void registerExceptionHandlers(Class<?> handler, Method[] methods) {
    for (Method method : methods) {
      if (method.isAnnotationPresent(
          cn.qaiu.vx.core.annotations.exception.ExceptionHandler.class)) {
        exceptionHandlerManager.registerLocalHandler(handler, method);
      }
    }
  }

  /** 注册路由组 */
  private void registerRouteGroups(
      Router router, Object instance, Map<String, List<Method>> routeGroups) {
    for (Map.Entry<String, List<Method>> entry : routeGroups.entrySet()) {
      String routeKey = entry.getKey();
      List<Method> routeMethods = entry.getValue();

      if (routeMethods.isEmpty()) continue;

      Method firstMethod = routeMethods.get(0);
      if (firstMethod.isAnnotationPresent(RouteMapping.class)) {
        registerHttpRoute(router, instance, routeKey, routeMethods);
      } else if (firstMethod.isAnnotationPresent(SockRouteMapper.class)) {
        registerWebSocketRoute(router, instance, firstMethod);
      }
    }
  }

  /** 注册HTTP路由 */
  private void registerHttpRoute(
      Router router, Object instance, String routeKey, List<Method> routeMethods) {
    Method firstMethod = routeMethods.get(0);
    RouteMapping mapping = firstMethod.getAnnotation(RouteMapping.class);
    Class<?> handlerClass = firstMethod.getDeclaringClass();

    // 解析路由键
    String[] parts = routeKey.split(":");
    HttpMethod method = HttpMethod.valueOf(parts[0]);
    String path = parts[1];

    // 转换路径变量格式：{variable} -> :variable
    String vertxPath = convertPathVariables(path);

    // 创建路由
    Route route = router.route(method, vertxPath);
    configureHttpRoute(route, mapping, routeMethods, method, vertxPath);

    // 添加安全拦截器（如果启用）
    if (securityInterceptor != null) {
      route.handler(
          ctx -> {
            // 将方法和类信息存入上下文，供安全拦截器使用
            ctx.put("_handler_method", firstMethod);
            ctx.put("_handler_class", handlerClass);
            securityInterceptor.handle(ctx);
          });
    }

    // 设置处理器
    route
        .handler(ctx -> handlerMethodWithOverload(instance, routeMethods, ctx))
        .failureHandler(ctx -> handleRouteFailure(ctx));
  }

  /**
   * 转换路径变量格式 将Spring风格的{userId}转换为Vert.x风格的:userId 同时支持Vert.x原生的:userId格式
   *
   * @param path 原始路径
   * @return 转换后的路径
   */
  private String convertPathVariables(String path) {
    if (path == null || path.isEmpty()) {
      return path;
    }

    // 转换{userId}格式为:userId格式
    String converted = path.replaceAll("\\{([^}]+)\\}", ":$1");

    // 只在实际发生转换时才记录日志
    if (!path.equals(converted)) {
      LOGGER.debug("Path variable conversion: {} -> {}", path, converted);
    }
    return converted;
  }

  /** 配置HTTP路由 */
  private void configureHttpRoute(
      Route route,
      RouteMapping mapping,
      List<Method> routeMethods,
      HttpMethod method,
      String vertxPath) {
    String mineType = mapping.requestMIMEType().getValue();
    LOGGER.info(
        "route -> {}:{} -> {} ({} methods)",
        method.name(),
        vertxPath,
        mineType,
        routeMethods.size());

    if (StringUtils.isNotEmpty(mineType)) {
      route.consumes(mineType);
    }

    // 设置默认超时和响应时间
    route.handler(
        TimeoutHandler.create(SharedDataUtil.getCustomConfig().getInteger(ROUTE_TIME_OUT)));
    route.handler(ResponseTimeHandler.create());
  }

  /** 注册WebSocket路由 */
  private void registerWebSocketRoute(Router router, Object instance, Method method) {
    SockRouteMapper mapping = method.getAnnotation(SockRouteMapper.class);
    String routeUrl = getRouteUrl(mapping.value());
    String root = getRootPath(method.getDeclaringClass());
    String url = root.concat(routeUrl);

    LOGGER.info("Register New Websocket Handler -> {}", url);

    SockJSHandlerOptions options =
        new SockJSHandlerOptions().setHeartbeatInterval(2000).setRegisterWriteHandler(true);

    SockJSHandler sockJSHandler = SockJSHandler.create(VertxHolder.getVertxInstance(), options);
    Router route =
        sockJSHandler.socketHandler(
            sock -> {
              try {
                ReflectionUtil.invokeWithArguments(method, instance, sock);
              } catch (Throwable e) {
                LOGGER.error("SockJS handler error", e);
              }
            });

    if (url.endsWith("*")) {
      throw new IllegalArgumentException("Don't include * when mounting a sub router");
    }
    router.route(url + "*").subRouter(route);
  }

  /** 注册WebSocket处理器 */
  private void registerWebSocketHandlers(Router router) {
    webSocketHandlerFactory.registerToRouter(router, gatewayPrefix);
  }

  /** 处理路由失败 */
  private void handleRouteFailure(RoutingContext ctx) {
    if (ctx.response().ended()) return;

    Throwable failure = ctx.failure();
    if (failure != null) {
      exceptionHandlerManager.handleException(failure, ctx, null);
    } else {
      doFireJsonResultResponse(ctx, JsonResult.error("请求处理超时", 503), 503);
    }
  }

  /**
   * 获取并处理路由URL分隔符
   *
   * @return String
   */
  private String getRouteUrl(String mapperValue) {
    String routeUrl;
    if ("/".equals(mapperValue)) {
      routeUrl = mapperValue;
    } else if (mapperValue.startsWith("/")) {
      routeUrl = mapperValue.substring(1);
    } else {
      routeUrl = mapperValue;
    }
    return routeUrl;
  }

  /**
   * 配置拦截
   *
   * @return Handler
   */
  private Set<Handler<RoutingContext>> getInterceptorSet() {
    // 配置拦截
    return getBeforeInterceptor().stream()
        .map(BeforeInterceptor::doHandle)
        .collect(Collectors.toSet());
  }

  /**
   * 按路由分组方法，支持方法重载
   *
   * @param methods 方法列表
   * @return 按路由键分组的方法映射
   */
  private Map<String, List<Method>> groupMethodsByRoute(List<Method> methods, String root) {
    Map<String, List<Method>> groups = new LinkedHashMap<>();

    for (Method method : methods) {
      if (method.isAnnotationPresent(RouteMapping.class)) {
        RouteMapping mapping = method.getAnnotation(RouteMapping.class);
        String routeKey = mapping.method().name() + ":" + root + getRouteUrl(mapping.value());
        groups.computeIfAbsent(routeKey, k -> new ArrayList<>()).add(method);
      } else if (method.isAnnotationPresent(SockRouteMapper.class)) {
        SockRouteMapper mapping = method.getAnnotation(SockRouteMapper.class);
        String routeKey = "SOCKJS:" + root + getRouteUrl(mapping.value());
        groups.computeIfAbsent(routeKey, k -> new ArrayList<>()).add(method);
      }
    }

    return groups;
  }

  /**
   * 获取请求根路径
   *
   * @param handler handler
   * @return 根路径
   */
  private String getRootPath(Class<?> handler) {
    // 处理请求路径前缀和后缀
    String root = gatewayPrefix;
    if (!root.startsWith("/")) {
      root = "/" + root;
    }
    if (!root.endsWith("/")) {
      root = root + "/";
    }
    // 子路径
    if (handler.isAnnotationPresent(RouteHandler.class)) {
      RouteHandler routeHandler = handler.getAnnotation(RouteHandler.class);
      String value = routeHandler.value();
      root += (value.startsWith("/") ? value.substring(1) : value);
    }
    if (!root.endsWith("/")) {
      root = root + "/";
    }

    return root;
  }

  /**
   * 处理请求-支持方法重载的参数绑定
   *
   * @param instance 类实例
   * @param methods 候选方法列表
   * @param ctx 路由上下文
   */
  private void handlerMethodWithOverload(
      Object instance, List<Method> methods, RoutingContext ctx) {
    // 使用参数匹配器选择最佳方法
    ParameterMatcher.MatchResult matchResult = ParameterMatcher.findBestMatch(methods, ctx);

    if (matchResult == null) {
      doFireJsonResultResponse(
          ctx, JsonResult.error("No suitable method found for the request"), 400);
      return;
    }

    // 使用选中的方法和参数
    handlerMethodWithParams(instance, matchResult.getMethod(), matchResult.getParameters(), ctx);
  }

  /** 处理请求-使用预提取的参数 */
  private void handlerMethodWithParams(
      Object instance, Method method, Object[] params, RoutingContext ctx) {
    try {
      Object data = ReflectionUtil.invokeWithArguments(method, instance, params);
      handleMethodResult(data, ctx);
    } catch (Throwable e) {
      handleMethodException(e, ctx);
    }
  }

  /** 处理方法结果 */
  private void handleMethodResult(Object data, RoutingContext ctx) {
    if (data == null) return;

    if (data instanceof JsonResult<?> jsonResult) {
      doFireJsonResultResponse(ctx, jsonResult, jsonResult.getCode());
    } else if (data instanceof JsonObject) {
      doFireJsonObjectResponse(ctx, ((JsonObject) data));
    } else if (data instanceof Future) {
      handleAsyncResult((Future<?>) data, ctx);
    } else {
      doFireJsonResultResponse(ctx, JsonResult.data(data));
    }
  }

  /** 处理异步结果 */
  private void handleAsyncResult(Future<?> future, RoutingContext ctx) {
    future
        .onSuccess(
            res -> {
              if (res instanceof JsonResult<?> jsonResult) {
                doFireJsonResultResponse(ctx, jsonResult, jsonResult.getCode());
              } else if (res instanceof JsonObject) {
                doFireJsonObjectResponse(ctx, ((JsonObject) res));
              } else if (res != null) {
                doFireJsonResultResponse(ctx, JsonResult.data(res));
              } else {
                doFireJsonResultResponse(ctx, JsonResult.data(null));
              }
            })
        .onFailure(e -> doFireJsonResultResponse(ctx, JsonResult.error(e.getMessage()), 500));
  }

  /** 处理方法异常 */
  private void handleMethodException(Throwable e, RoutingContext ctx) {
    LOGGER.error("Method execution error", e);
    String err = e.getMessage();
    if (e.getCause() != null) {
      if (e.getCause() instanceof InvocationTargetException) {
        err = ((InvocationTargetException) e.getCause()).getTargetException().getMessage();
      } else {
        err = e.getCause().getMessage();
      }
    }
    doFireJsonResultResponse(ctx, JsonResult.error(err), 500);
  }

  private Set<BeforeInterceptor> getBeforeInterceptor() {
    Set<Class<? extends BeforeInterceptor>> interceptorClassSet =
        reflections.getSubTypesOf(BeforeInterceptor.class);
    if (interceptorClassSet == null) {
      return new HashSet<>();
    }
    return CommonUtil.sortClassSet(interceptorClassSet);
  }
}
