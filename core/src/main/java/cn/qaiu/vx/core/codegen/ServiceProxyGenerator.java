package cn.qaiu.vx.core.codegen;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 自定义Service代理生成器 基于@CustomProxyGen注解，生成适合当前项目的Service代理类 支持接口继承树分析，生成所有父接口方法的代理
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@SupportedAnnotationTypes("cn.qaiu.vx.core.codegen.CustomProxyGen")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ServiceProxyGenerator extends AbstractProcessor {

  private Messager messager;
  private Filer filer;
  private Elements elementUtils;
  private Types typeUtils;

  // 当前处理的接口信息
  private TypeElement currentInterface;
  private String currentConcreteType;
  private String currentConcreteIdType;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(CustomProxyGen.class)) {
      if (element.getKind() == ElementKind.INTERFACE) {
        try {
          generateServiceProxy((TypeElement) element);
        } catch (Exception e) {
          messager.printMessage(
              Diagnostic.Kind.ERROR,
              "Failed to generate service proxy for "
                  + element.getSimpleName()
                  + ": "
                  + e.getMessage());
        }
      }
    }
    return true;
  }

  /** 生成Service代理类 - 生成完整的代理套件 */
  private void generateServiceProxy(TypeElement serviceInterface) throws IOException {
    String packageName = getPackageName(serviceInterface);
    String className = serviceInterface.getSimpleName().toString();
    String proxyClassName = className + "VertxEBProxy";
    String handlerClassName = className + "VertxProxyHandler";

    // 设置当前接口信息，用于类型替换
    setCurrentInterfaceInfo(serviceInterface);

    // 生成客户端代理类
    JavaFileObject proxyFile = filer.createSourceFile(packageName + "." + proxyClassName);
    try (Writer writer = proxyFile.openWriter()) {
      generateProxyClass(writer, serviceInterface, packageName, className, proxyClassName);
    }

    // 生成服务端处理器类
    JavaFileObject handlerFile = filer.createSourceFile(packageName + "." + handlerClassName);
    try (Writer writer = handlerFile.openWriter()) {
      generateProxyHandlerClass(writer, serviceInterface, packageName, className, handlerClassName);
    }

    messager.printMessage(
        Diagnostic.Kind.NOTE,
        "Generated service proxy suite: "
            + packageName
            + "."
            + proxyClassName
            + " and "
            + handlerClassName);
  }

  /** 生成代理类代码 - 完全兼容Vert.x ProxyGen格式 */
  private void generateProxyClass(
      Writer writer,
      TypeElement serviceInterface,
      String packageName,
      String className,
      String proxyClassName)
      throws IOException {

    // 生成文件头注释 - 完全匹配Vert.x格式
    writer.write("/*\n");
    writer.write("* Copyright 2014 Red Hat, Inc.\n");
    writer.write("*\n");
    writer.write("* Red Hat licenses this file to you under the Apache License, version 2.0\n");
    writer.write("* (the \"License\"); you may not use this file except in compliance with the\n");
    writer.write("* License. You may obtain a copy of the License at:\n");
    writer.write("*\n");
    writer.write("* http://www.apache.org/licenses/LICENSE-2.0\n");
    writer.write("*\n");
    writer.write("* Unless required by applicable law or agreed to in writing, software\n");
    writer.write("* distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT\n");
    writer.write("* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the\n");
    writer.write("* License for the specific language governing permissions and limitations\n");
    writer.write("* under the License.\n");
    writer.write("*/\n\n");

    // 包声明
    writer.write("package " + packageName + ";\n\n");

    // 导入语句
    generateImports(writer, serviceInterface);

    // 生成注释
    writer.write("/*\n");
    writer.write("  Generated Proxy code - DO NOT EDIT\n");
    writer.write("  @author Roger the Robot\n");
    writer.write("*/\n\n");

    // 类声明
    writer.write("@SuppressWarnings({\"unchecked\", \"rawtypes\"})\n");
    writer.write("public class " + proxyClassName + " implements " + className + " {\n");

    // 字段
    writer.write("  private Vertx _vertx;\n");
    writer.write("  private String _address;\n");
    writer.write("  private DeliveryOptions _options;\n");
    writer.write("  private boolean closed;\n\n");

    // 构造函数
    generateConstructors(writer, className);

    // 方法实现 - 分析所有接口方法
    generateAllMethods(writer, serviceInterface);

    writer.write("}\n");
  }

  /** 生成导入语句 */
  private void generateImports(Writer writer, TypeElement serviceInterface) throws IOException {
    Set<String> imports = new LinkedHashSet<>();

    // 基础导入 - 按Vert.x格式排序
    imports.add("io.vertx.core.eventbus.DeliveryOptions");
    imports.add("io.vertx.core.Vertx");
    imports.add("io.vertx.core.Future");
    imports.add("io.vertx.core.json.JsonObject");
    imports.add("io.vertx.core.json.JsonArray");
    imports.add("java.util.ArrayList");
    imports.add("java.util.HashSet");
    imports.add("java.util.List");
    imports.add("java.util.Map");
    imports.add("java.util.Set");
    imports.add("java.util.stream.Collectors");
    imports.add("java.util.function.Function");
    imports.add("io.vertx.serviceproxy.ServiceException");
    imports.add("io.vertx.serviceproxy.ServiceExceptionMessageCodec");
    imports.add("io.vertx.serviceproxy.ProxyUtils");

    // 从所有接口方法中收集类型
    Set<ExecutableElement> allMethods = getAllMethodsFromInterfaceHierarchy(serviceInterface);
    for (ExecutableElement method : allMethods) {
      collectTypesFromMethod(method, imports);
    }

    // 写入导入语句
    for (String importName : imports) {
      writer.write("import " + importName + ";\n");
    }
    writer.write("\n");
  }

  /** 获取接口继承树中的所有方法 */
  private Set<ExecutableElement> getAllMethodsFromInterfaceHierarchy(TypeElement interfaceElement) {
    Set<ExecutableElement> allMethods = new LinkedHashSet<>();
    Set<TypeElement> processedInterfaces = new HashSet<>();

    collectMethodsRecursively(interfaceElement, allMethods, processedInterfaces);

    return allMethods;
  }

  /** 递归收集接口方法 */
  private void collectMethodsRecursively(
      TypeElement interfaceElement,
      Set<ExecutableElement> allMethods,
      Set<TypeElement> processedInterfaces) {

    if (processedInterfaces.contains(interfaceElement)) {
      return; // 避免循环依赖
    }
    processedInterfaces.add(interfaceElement);

    // 收集当前接口的直接方法
    for (Element element : interfaceElement.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) element;
        allMethods.add(method);
      }
    }

    // 递归处理父接口
    for (TypeMirror superInterface : interfaceElement.getInterfaces()) {
      if (superInterface instanceof DeclaredType) {
        DeclaredType declaredType = (DeclaredType) superInterface;
        Element superElement = declaredType.asElement();
        if (superElement instanceof TypeElement) {
          TypeElement superInterfaceElement = (TypeElement) superElement;
          collectMethodsRecursively(superInterfaceElement, allMethods, processedInterfaces);
        }
      }
    }
  }

  /** 从方法中收集类型 */
  private void collectTypesFromMethod(ExecutableElement method, Set<String> imports) {
    // 返回类型
    TypeMirror returnType = method.getReturnType();
    collectTypesFromType(returnType, imports);

    // 参数类型
    for (VariableElement param : method.getParameters()) {
      collectTypesFromType(param.asType(), imports);
    }
  }

  /** 从类型中收集导入 */
  private void collectTypesFromType(TypeMirror type, Set<String> imports) {
    String typeString = type.toString();

    // 处理Future<T>类型
    if (typeString.startsWith("io.vertx.core.Future<")) {
      String innerType =
          typeString.substring("io.vertx.core.Future<".length(), typeString.length() - 1);
      String concreteInnerType = replaceGenericTypes(innerType);
      if (!isPrimitiveOrBasicType(concreteInnerType)
          && !isGenericTypeParameter(concreteInnerType)) {
        imports.add(concreteInnerType);
      }
    }
    // 处理List<T>类型
    else if (typeString.startsWith("java.util.List<")) {
      String innerType = typeString.substring("java.util.List<".length(), typeString.length() - 1);
      String concreteInnerType = replaceGenericTypes(innerType);
      if (!isPrimitiveOrBasicType(concreteInnerType)
          && !isGenericTypeParameter(concreteInnerType)) {
        imports.add(concreteInnerType);
      }
    }
    // 处理LambdaPageResult<T>类型
    else if (typeString.startsWith("cn.qaiu.db.dsl.lambda.LambdaPageResult<")) {
      String innerType =
          typeString.substring(
              "cn.qaiu.db.dsl.lambda.LambdaPageResult<".length(), typeString.length() - 1);
      String concreteInnerType = replaceGenericTypes(innerType);
      if (!isPrimitiveOrBasicType(concreteInnerType)
          && !isGenericTypeParameter(concreteInnerType)) {
        imports.add(concreteInnerType);
      }
    }
    // 处理其他非基本类型
    else if (!isPrimitiveOrBasicType(typeString) && !isGenericTypeParameter(typeString)) {
      String concreteType = replaceGenericTypes(typeString);
      if (!isGenericTypeParameter(concreteType) && !isPrimitiveOrBasicType(concreteType)) {
        imports.add(concreteType);
      }
    }
  }

  /** 判断是否为泛型类型参数 */
  private boolean isGenericTypeParameter(String typeName) {
    // 检查是否为单个大写字母（泛型类型参数）
    return typeName.length() == 1 && Character.isUpperCase(typeName.charAt(0));
  }

  /** 替换泛型类型参数为具体类型 */
  private String replaceGenericTypes(String typeString) {
    // 获取当前接口的具体类型参数
    String concreteType = getConcreteTypeFromInterface();
    String concreteIdType = getConcreteIdTypeFromInterface();

    // 替换泛型类型参数
    String result = typeString;
    result = result.replaceAll("\\bT\\b", concreteType);
    result = result.replaceAll("\\bID\\b", concreteIdType);

    return result;
  }

  /** 设置当前接口信息 */
  private void setCurrentInterfaceInfo(TypeElement serviceInterface) {
    this.currentInterface = serviceInterface;

    // 从接口的泛型参数中获取具体类型
    List<? extends TypeParameterElement> typeParams = serviceInterface.getTypeParameters();
    if (typeParams.size() >= 2) {
      // 假设接口定义为 Service<T, ID>，T是第一个参数，ID是第二个参数
      // 我们需要从接口的父接口中获取具体的类型参数
      this.currentConcreteType = extractConcreteTypeFromSuperInterface(serviceInterface, 0);
      this.currentConcreteIdType = extractConcreteTypeFromSuperInterface(serviceInterface, 1);
    } else {
      // 默认值
      this.currentConcreteType = "Object";
      this.currentConcreteIdType = "Long";
    }
  }

  /** 从父接口中提取具体类型 */
  private String extractConcreteTypeFromSuperInterface(
      TypeElement serviceInterface, int typeParamIndex) {
    // 查找SimpleJService接口
    for (TypeMirror superInterface : serviceInterface.getInterfaces()) {
      if (superInterface instanceof DeclaredType) {
        DeclaredType declaredType = (DeclaredType) superInterface;
        Element superElement = declaredType.asElement();
        if (superElement instanceof TypeElement) {
          TypeElement superTypeElement = (TypeElement) superElement;
          if (superTypeElement.getSimpleName().toString().equals("SimpleJService")) {
            // 获取SimpleJService的具体类型参数
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if (typeArgs.size() > typeParamIndex) {
              return typeArgs.get(typeParamIndex).toString();
            }
          }
        }
      }
    }

    // 默认值
    return typeParamIndex == 0 ? "Object" : "Long";
  }

  /** 从当前接口获取具体类型 */
  private String getConcreteTypeFromInterface() {
    return currentConcreteType != null ? currentConcreteType : "Object";
  }

  /** 从当前接口获取具体ID类型 */
  private String getConcreteIdTypeFromInterface() {
    return currentConcreteIdType != null ? currentConcreteIdType : "Long";
  }

  /** 判断是否为基本类型 */
  private boolean isPrimitiveOrBasicType(String typeName) {
    return typeName.equals("void")
        || typeName.equals("boolean")
        || typeName.equals("int")
        || typeName.equals("long")
        || typeName.equals("double")
        || typeName.equals("float")
        || typeName.equals("char")
        || typeName.equals("byte")
        || typeName.equals("short")
        || typeName.startsWith("java.lang.")
        || typeName.startsWith("java.util.")
        || typeName.startsWith("io.vertx.core.");
  }

  /** 生成构造函数 - 完全匹配Vert.x格式 */
  private void generateConstructors(Writer writer, String className) throws IOException {
    // 第一个构造函数
    writer.write("  public " + className + "VertxEBProxy(Vertx vertx, String address) {\n");
    writer.write("    this(vertx, address, null);\n");
    writer.write("  }\n\n");

    // 第二个构造函数
    writer.write(
        "  public "
            + className
            + "VertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {\n");
    writer.write("    this._vertx = vertx;\n");
    writer.write("    this._address = address;\n");
    writer.write("    this._options = options;\n");
    writer.write("    try {\n");
    writer.write(
        "      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());\n");
    writer.write("    } catch (IllegalStateException ex) {\n");
    writer.write("    }\n");
    writer.write("  }\n\n");
  }

  /** 生成所有方法实现 */
  private void generateAllMethods(Writer writer, TypeElement serviceInterface) throws IOException {
    Set<ExecutableElement> allMethods = getAllMethodsFromInterfaceHierarchy(serviceInterface);

    for (ExecutableElement method : allMethods) {
      generateMethod(writer, method);
    }
  }

  /** 生成单个方法实现 - 完全匹配Vert.x格式 */
  private void generateMethod(Writer writer, ExecutableElement method) throws IOException {
    String methodName = method.getSimpleName().toString();
    String returnType = replaceGenericTypes(method.getReturnType().toString());

    // 方法签名
    writer.write("  @Override\n");
    writer.write("  public " + returnType + " " + methodName + "(");

    // 参数
    List<? extends VariableElement> parameters = method.getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      VariableElement param = parameters.get(i);
      if (i > 0) writer.write(", ");
      String paramType = replaceGenericTypes(param.asType().toString());
      writer.write(paramType + " " + param.getSimpleName());
    }
    writer.write("){\n");

    // 方法体 - 完全匹配Vert.x格式
    writer.write(
        "    if (closed) return io.vertx.core.Future.failedFuture(\"Proxy is closed\");\n");
    writer.write("    JsonObject _json = new JsonObject();\n");

    // 添加参数到JSON
    for (VariableElement param : parameters) {
      writer.write(
          "    _json.put(\"" + param.getSimpleName() + "\", " + param.getSimpleName() + ");\n");
    }
    writer.write("\n");

    // 设置DeliveryOptions
    writer.write(
        "    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();\n");
    writer.write("    _deliveryOptions.addHeader(\"action\", \"" + methodName + "\");\n");
    writer.write("    _deliveryOptions.getHeaders().set(\"action\", \"" + methodName + "\");\n");

    // 处理返回值 - 完全匹配Vert.x格式
    if (returnType.startsWith("io.vertx.core.Future<")) {
      String innerType =
          returnType.substring("io.vertx.core.Future<".length(), returnType.length() - 1);

      if (innerType.equals("java.util.List")) {
        writer.write(
            "    return _vertx.eventBus().<JsonArray>request(_address, _json, _deliveryOptions).map(msg -> {\n");
        writer.write("      return ProxyUtils.convertList(msg.body().getList());\n");
      } else if (innerType.equals("io.vertx.core.json.JsonObject")) {
        writer.write(
            "    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {\n");
        writer.write("      return msg.body();\n");
      } else if (innerType.equals("io.vertx.core.json.JsonArray")) {
        writer.write(
            "    return _vertx.eventBus().<JsonArray>request(_address, _json, _deliveryOptions).map(msg -> {\n");
        writer.write("      return msg.body();\n");
      } else if (isPrimitiveOrBasicType(innerType)) {
        writer.write(
            "    return _vertx.eventBus().<"
                + innerType
                + ">request(_address, _json, _deliveryOptions).map(msg -> {\n");
        writer.write("      return msg.body();\n");
      } else {
        // 实体类型
        writer.write(
            "    return _vertx.eventBus().<"
                + innerType
                + ">request(_address, _json, _deliveryOptions).map(msg -> {\n");
        writer.write("      return msg.body();\n");
      }

      writer.write("    });\n");
    } else {
      writer.write(
          "    return io.vertx.core.Future.failedFuture(\"Unsupported return type: "
              + returnType
              + "\");\n");
    }

    writer.write("  }\n");
  }

  /** 生成ProxyHandler类 - 完全兼容Vert.x ProxyGen格式 */
  private void generateProxyHandlerClass(
      Writer writer,
      TypeElement serviceInterface,
      String packageName,
      String className,
      String handlerClassName)
      throws IOException {

    // 生成文件头注释 - 完全匹配Vert.x格式
    writer.write("/*\n");
    writer.write("* Copyright 2014 Red Hat, Inc.\n");
    writer.write("*\n");
    writer.write("* Red Hat licenses this file to you under the Apache License, version 2.0\n");
    writer.write("* (the \"License\"); you may not use this file except in compliance with the\n");
    writer.write("* License. You may obtain a copy of the License at:\n");
    writer.write("*\n");
    writer.write("* http://www.apache.org/licenses/LICENSE-2.0\n");
    writer.write("*\n");
    writer.write("* Unless required by applicable law or agreed to in writing, software\n");
    writer.write("* distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT\n");
    writer.write("* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the\n");
    writer.write("* License for the specific language governing permissions and limitations\n");
    writer.write("* under the License.\n");
    writer.write("*/\n\n");

    // 包声明
    writer.write("package " + packageName + ";\n\n");

    // 导入语句
    generateHandlerImports(writer, serviceInterface);

    // 生成注释
    writer.write("/*\n");
    writer.write("  Generated Proxy code - DO NOT EDIT\n");
    writer.write("  @author Roger the Robot\n");
    writer.write("*/\n\n");

    // 类声明
    writer.write("@SuppressWarnings({\"unchecked\", \"rawtypes\"})\n");
    writer.write("public class " + handlerClassName + " extends ProxyHandler {\n\n");

    // 字段
    writer.write("  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes \n");
    writer.write("  private final Vertx vertx;\n");
    writer.write("  private final " + className + " service;\n");
    writer.write("  private final long timerID;\n");
    writer.write("  private long lastAccessed;\n");
    writer.write("  private final long timeoutSeconds;\n");
    writer.write("  private final boolean includeDebugInfo;\n\n");

    // 构造函数
    generateHandlerConstructors(writer, className, handlerClassName);

    // 超时检查方法
    generateTimeoutMethods(writer);

    // 消息处理方法
    generateHandlerMethods(writer, serviceInterface);

    writer.write("}\n");
  }

  /** 生成Handler导入语句 */
  private void generateHandlerImports(Writer writer, TypeElement serviceInterface)
      throws IOException {
    Set<String> imports = new LinkedHashSet<>();

    // 基础导入
    imports.add("cn.qaiu.example.service." + serviceInterface.getSimpleName());
    imports.add("io.vertx.core.Vertx");
    imports.add("io.vertx.core.Handler");
    imports.add("io.vertx.core.AsyncResult");
    imports.add("io.vertx.core.eventbus.EventBus");
    imports.add("io.vertx.core.eventbus.Message");
    imports.add("io.vertx.core.eventbus.MessageConsumer");
    imports.add("io.vertx.core.eventbus.DeliveryOptions");
    imports.add("io.vertx.core.eventbus.ReplyException");
    imports.add("io.vertx.core.json.JsonObject");
    imports.add("io.vertx.core.json.JsonArray");
    imports.add("java.util.Collection");
    imports.add("java.util.ArrayList");
    imports.add("java.util.HashSet");
    imports.add("java.util.List");
    imports.add("java.util.Map");
    imports.add("java.util.Set");
    imports.add("java.util.UUID");
    imports.add("java.util.stream.Collectors");
    imports.add("io.vertx.serviceproxy.ProxyHandler");
    imports.add("io.vertx.serviceproxy.ServiceException");
    imports.add("io.vertx.serviceproxy.ServiceExceptionMessageCodec");
    imports.add("io.vertx.serviceproxy.HelperUtils");
    imports.add("io.vertx.serviceproxy.ServiceBinder");

    // 从所有接口方法中收集类型
    Set<ExecutableElement> allMethods = getAllMethodsFromInterfaceHierarchy(serviceInterface);
    for (ExecutableElement method : allMethods) {
      collectTypesFromMethod(method, imports);
    }

    // 写入导入语句
    for (String importName : imports) {
      writer.write("import " + importName + ";\n");
    }
    writer.write("\n");
  }

  /** 生成Handler构造函数 */
  private void generateHandlerConstructors(Writer writer, String className, String handlerClassName)
      throws IOException {
    // 第一个构造函数
    writer.write("  public " + handlerClassName + "(Vertx vertx, " + className + " service){\n");
    writer.write("    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);\n");
    writer.write("  }\n\n");

    // 第二个构造函数
    writer.write(
        "  public "
            + handlerClassName
            + "(Vertx vertx, "
            + className
            + " service, long timeoutInSecond){\n");
    writer.write("    this(vertx, service, true, timeoutInSecond);\n");
    writer.write("  }\n\n");

    // 第三个构造函数
    writer.write(
        "  public "
            + handlerClassName
            + "(Vertx vertx, "
            + className
            + " service, boolean topLevel, long timeoutInSecond){\n");
    writer.write("    this(vertx, service, true, timeoutInSecond, false);\n");
    writer.write("  }\n\n");

    // 第四个构造函数
    writer.write(
        "  public "
            + handlerClassName
            + "(Vertx vertx, "
            + className
            + " service, boolean topLevel, long timeoutSeconds, boolean includeDebugInfo) {\n");
    writer.write("      this.vertx = vertx;\n");
    writer.write("      this.service = service;\n");
    writer.write("      this.includeDebugInfo = includeDebugInfo;\n");
    writer.write("      this.timeoutSeconds = timeoutSeconds;\n");
    writer.write("      try {\n");
    writer.write("        this.vertx.eventBus().registerDefaultCodec(ServiceException.class,\n");
    writer.write("            new ServiceExceptionMessageCodec());\n");
    writer.write("      } catch (IllegalStateException ex) {}\n");
    writer.write("      if (timeoutSeconds != -1 && !topLevel) {\n");
    writer.write("        long period = timeoutSeconds * 1000 / 2;\n");
    writer.write("        if (period > 10000) {\n");
    writer.write("          period = 10000;\n");
    writer.write("        }\n");
    writer.write("        this.timerID = vertx.setPeriodic(period, this::checkTimedOut);\n");
    writer.write("      } else {\n");
    writer.write("        this.timerID = -1;\n");
    writer.write("      }\n");
    writer.write("      accessed();\n");
    writer.write("    }\n\n");
  }

  /** 生成超时检查方法 */
  private void generateTimeoutMethods(Writer writer) throws IOException {
    writer.write("  private void checkTimedOut(long id) {\n");
    writer.write("    long now = System.nanoTime();\n");
    writer.write("    if (now - lastAccessed > timeoutSeconds * 1000000000) {\n");
    writer.write("      close();\n");
    writer.write("    }\n");
    writer.write("  }\n\n");

    writer.write("    @Override\n");
    writer.write("    public void close() {\n");
    writer.write("      if (timerID != -1) {\n");
    writer.write("        vertx.cancelTimer(timerID);\n");
    writer.write("      }\n");
    writer.write("      super.close();\n");
    writer.write("    }\n\n");

    writer.write("    private void accessed() {\n");
    writer.write("      this.lastAccessed = System.nanoTime();\n");
    writer.write("    }\n\n");
  }

  /** 生成Handler方法 */
  private void generateHandlerMethods(Writer writer, TypeElement serviceInterface)
      throws IOException {
    writer.write("  public void handle(Message<JsonObject> msg) {\n");
    writer.write("    try{\n");
    writer.write("      JsonObject json = msg.body();\n");
    writer.write("      String action = msg.headers().get(\"action\");\n");
    writer.write(
        "      if (action == null) throw new IllegalStateException(\"action not specified\");\n");
    writer.write("      accessed();\n");
    writer.write("      switch (action) {\n");

    Set<ExecutableElement> allMethods = getAllMethodsFromInterfaceHierarchy(serviceInterface);
    for (ExecutableElement method : allMethods) {
      generateHandlerMethodCase(writer, method);
    }

    writer.write(
        "        default: throw new IllegalStateException(\"Invalid action: \" + action);\n");
    writer.write("      }\n");
    writer.write("    } catch (Throwable t) {\n");
    writer.write(
        "      if (includeDebugInfo) msg.reply(new ServiceException(500, t.getMessage(), HelperUtils.generateDebugInfo(t)));\n");
    writer.write("      else msg.reply(new ServiceException(500, t.getMessage()));\n");
    writer.write("      throw t;\n");
    writer.write("    }\n");
    writer.write("  }\n");
  }

  /** 生成Handler方法case */
  private void generateHandlerMethodCase(Writer writer, ExecutableElement method)
      throws IOException {
    String methodName = method.getSimpleName().toString();
    String returnType = replaceGenericTypes(method.getReturnType().toString());

    writer.write("        case \"" + methodName + "\": {\n");

    // 生成参数解析
    List<? extends VariableElement> parameters = method.getParameters();

    // 生成方法调用开始
    writer.write("          service." + methodName + "(");

    for (int i = 0; i < parameters.size(); i++) {
      VariableElement param = parameters.get(i);
      String paramName = param.getSimpleName().toString();
      String paramType = replaceGenericTypes(param.asType().toString());

      if (i > 0) {
        writer.write(",\n                                  ");
      }

      // 根据参数类型生成解析代码
      if (paramType.equals("java.lang.String")) {
        writer.write("(java.lang.String)json.getValue(\"" + paramName + "\")");
      } else if (paramType.equals("java.lang.Long") || paramType.equals("long")) {
        writer.write(
            "json.getValue(\""
                + paramName
                + "\") == null ? null : (json.getLong(\""
                + paramName
                + "\").longValue())");
      } else if (paramType.equals("java.lang.Integer") || paramType.equals("int")) {
        writer.write(
            "json.getValue(\""
                + paramName
                + "\") == null ? null : (json.getLong(\""
                + paramName
                + "\").intValue())");
      } else if (paramType.equals("java.lang.Boolean") || paramType.equals("boolean")) {
        writer.write(
            "json.getValue(\""
                + paramName
                + "\") == null ? null : (json.getBoolean(\""
                + paramName
                + "\"))");
      } else {
        writer.write("(" + paramType + ")json.getValue(\"" + paramName + "\")");
      }
    }

    // 生成onComplete调用
    writer.write(").onComplete(HelperUtils.createHandler(msg, includeDebugInfo));\n");

    writer.write("          break;\n");
    writer.write("        }\n");
  }

  /** 获取包名 */
  private String getPackageName(TypeElement typeElement) {
    Element enclosingElement = typeElement.getEnclosingElement();
    while (enclosingElement != null && enclosingElement.getKind() != ElementKind.PACKAGE) {
      enclosingElement = enclosingElement.getEnclosingElement();
    }
    return enclosingElement != null ? enclosingElement.toString() : "";
  }
}
