package cn.qaiu.vx.core.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标记类 - 用于标识应该使用内置 JooqDao 方法
 */
class TestReferenceInterface {
    // 标记类，用于标识参照接口
}

/**
 * 自定义服务生成注解处理器
 * 
 * 功能：
 * 1. 动态读取父接口的所有方法和泛型参数
 * 2. 根据方法和泛型参数生成具体的服务类
 * 3. 支持通用的 ProxyGen 注解异步服务类生成
 * 4. 生成的服务类支持 Vert.x 的异步编程模型
 * 
 * 使用方式：
 * 在实体类上添加 @GenerateServiceGen 注解，处理器会自动生成对应的服务接口和实现类
 * 
 * @author vxcore
 * @version 1.0
 */
@SupportedAnnotationTypes("cn.qaiu.vx.core.annotations.GenerateServiceGen")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CustomServiceGenProcessor extends AbstractProcessor {
    
    private Elements elementUtils;
    private Messager messager;
    private Filer filer;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateServiceGen.class)) {
            if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            
            TypeElement entityElement = (TypeElement) element;
            GenerateServiceGen annotation = entityElement.getAnnotation(GenerateServiceGen.class);
            
            try {
                generateServiceClasses(entityElement, annotation);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "Failed to generate service classes for " + entityElement.getSimpleName() + ": " + e.getMessage());
            }
        }
        
        return true;
    }
    
    /**
     * 生成服务类
     * 
     * @param entityElement 实体元素
     * @param annotation 注解信息
     * @throws IOException 文件操作异常
     */
    private void generateServiceClasses(TypeElement entityElement, GenerateServiceGen annotation) throws IOException {
        String entityPackage = elementUtils.getPackageOf(entityElement).getQualifiedName().toString();
        String entityName = entityElement.getSimpleName().toString();
        String serviceName = entityName + "Service";
        String implName = entityName + "ServiceGen";
        
        String basePackage = annotation.basePackage().isEmpty() ? entityPackage : annotation.basePackage();
        
        // 获取ID类型
        String idType = getIdType(annotation);
        
        // 判断是接口还是实体类
        if (entityElement.getKind() == ElementKind.INTERFACE) {
            // 处理接口：分析接口的所有方法
            List<MethodInfo> interfaceMethods = analyzeInterfaceMethods(entityElement);
            
            // 生成 ProxyGen 接口
            if (annotation.generateProxy()) {
                generateServiceInterfaceFromInterface(basePackage, serviceName, entityName, entityPackage, interfaceMethods);
            }
            
            // 生成基础实现类
            generateServiceImplFromInterface(basePackage, implName, serviceName, entityName, entityPackage, idType, interfaceMethods);
        } else {
            // 处理实体类：检查是否有参照接口
            Class<?> referenceInterface = getReferenceInterface(annotation);
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "Reference interface for " + entityName + ": " + referenceInterface.getName());
            
            if (referenceInterface != Void.class && referenceInterface != TestReferenceInterface.class) {
                // 使用参照接口生成方法
                List<MethodInfo> referenceMethods = analyzeReferenceInterfaceMethods(referenceInterface, entityElement);
                messager.printMessage(Diagnostic.Kind.NOTE, 
                    "Generated " + referenceMethods.size() + " methods from reference interface");
                
                // 生成 ProxyGen 接口
                if (annotation.generateProxy()) {
                    generateServiceInterfaceFromReferenceInterface(basePackage, serviceName, entityName, entityPackage, referenceMethods);
                }
                
                // 生成基础实现类
                generateServiceImplFromReferenceInterface(basePackage, implName, serviceName, entityName, entityPackage, idType, referenceMethods);
            } else if (referenceInterface == TestReferenceInterface.class) {
                // 使用内置 JooqDao 方法
                List<MethodInfo> builtInMethods = generateBuiltInJooqDaoMethods();
                messager.printMessage(Diagnostic.Kind.NOTE, 
                    "Using built-in JooqDao methods: " + builtInMethods.size() + " methods");
                
                // 生成 ProxyGen 接口
                if (annotation.generateProxy()) {
                    generateServiceInterfaceFromReferenceInterface(basePackage, serviceName, entityName, entityPackage, builtInMethods);
                }
                
                // 生成基础实现类
                generateServiceImplFromReferenceInterface(basePackage, implName, serviceName, entityName, entityPackage, idType, builtInMethods);
            } else {
                // 使用原有逻辑
                List<String> genericTypes = analyzeGenericTypes(entityElement);
                
                // 生成 ProxyGen 接口
                if (annotation.generateProxy()) {
                    generateServiceInterface(basePackage, serviceName, entityName, entityPackage, genericTypes);
                }
                
                // 生成基础实现类
                generateServiceImpl(basePackage, implName, serviceName, entityName, entityPackage, idType, genericTypes);
            }
        }
    }
    
    /**
     * 获取ID类型
     * 
     * @param annotation 注解
     * @return ID类型字符串
     */
    private String getIdType(GenerateServiceGen annotation) {
        try {
            return annotation.idType().getSimpleName();
        } catch (javax.lang.model.type.MirroredTypeException e) {
            String typeName = e.getTypeMirror().toString();
            if (typeName.contains(".")) {
                return typeName.substring(typeName.lastIndexOf('.') + 1);
            } else {
                return typeName;
            }
        }
    }
    
    /**
     * 获取参照接口
     * 
     * @param annotation 注解
     * @return 参照接口类
     */
    private Class<?> getReferenceInterface(GenerateServiceGen annotation) {
        try {
            return annotation.referenceInterface();
        } catch (javax.lang.model.type.MirroredTypeException e) {
            String typeName = e.getTypeMirror().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "Reference interface type name: " + typeName);
            
            // 检查是否是 Void.class（默认值）
            if ("void".equals(typeName)) {
                return Void.class;
            }
            
            // 对于其他类型，返回一个标记类，表示应该使用内置方法
            return TestReferenceInterface.class; // 使用一个标记类
        }
    }
    
    /**
     * 分析实体类的泛型参数
     * 
     * @param entityElement 实体元素
     * @return 泛型类型列表
     */
    private List<String> analyzeGenericTypes(TypeElement entityElement) {
        List<String> genericTypes = new ArrayList<>();
        
        // 获取实体类的所有接口
        for (TypeMirror interfaceType : entityElement.getInterfaces()) {
            if (interfaceType.getKind() == TypeKind.DECLARED) {
                DeclaredType declaredType = (DeclaredType) interfaceType;
                
                // 分析接口的泛型参数
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                for (TypeMirror typeArg : typeArguments) {
                    if (typeArg.getKind() == TypeKind.DECLARED) {
                        DeclaredType declaredTypeArg = (DeclaredType) typeArg;
                        TypeElement typeElement = (TypeElement) declaredTypeArg.asElement();
                        genericTypes.add(typeElement.getSimpleName().toString());
                    }
                }
            }
        }
        
        // 如果没有找到泛型参数，使用默认的实体类名
        if (genericTypes.isEmpty()) {
            genericTypes.add(entityElement.getSimpleName().toString());
        }
        
        return genericTypes;
    }
    
    /**
     * 分析接口的所有方法（包括父接口的方法）
     * 
     * @param interfaceElement 接口元素
     * @return 方法信息列表
     */
    private List<MethodInfo> analyzeInterfaceMethods(TypeElement interfaceElement) {
        List<MethodInfo> methods = new ArrayList<>();
        Set<String> methodSignatures = new HashSet<>(); // 避免重复方法
        
        // 递归收集所有父接口的方法
        collectMethodsFromInterfaces(interfaceElement, methods, methodSignatures);
        
        return methods;
    }
    
    /**
     * 分析参照接口的方法
     * 
     * @param referenceInterface 参照接口类
     * @param entityElement 实体元素
     * @return 方法信息列表
     */
    private List<MethodInfo> analyzeReferenceInterfaceMethods(Class<?> referenceInterface, TypeElement entityElement) {
        List<MethodInfo> methods = new ArrayList<>();
        
        try {
            // 获取参照接口的 TypeElement
            String interfaceName = referenceInterface.getName();
            TypeElement referenceElement = elementUtils.getTypeElement(interfaceName);
            if (referenceElement != null) {
                // 收集参照接口的所有方法
                Set<String> methodSignatures = new HashSet<>();
                collectMethodsFromInterfaces(referenceElement, methods, methodSignatures);
                
                // 替换泛型参数为实体类型
                methods = replaceGenericParametersInMethods(methods, entityElement);
            } else {
                // 如果找不到参照接口，使用内置的 JooqDao 方法
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "Reference interface not found: " + interfaceName + ", using built-in JooqDao methods");
                methods = generateBuiltInJooqDaoMethods();
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Failed to analyze reference interface " + referenceInterface.getName() + ": " + e.getMessage() + 
                ", using built-in JooqDao methods");
            methods = generateBuiltInJooqDaoMethods();
        }
        
        return methods;
    }
    
    /**
     * 生成内置的 JooqDao 方法
     * 
     * @return 方法信息列表
     */
    private List<MethodInfo> generateBuiltInJooqDaoMethods() {
        List<MethodInfo> methods = new ArrayList<>();
        
        // 插入实体
        methods.add(new MethodInfo("insert", "Future<JsonObject>", List.of("JsonObject entity")));
        
        // 更新实体
        methods.add(new MethodInfo("update", "Future<JsonObject>", List.of("JsonObject entity")));
        
        // 根据ID删除实体
        methods.add(new MethodInfo("delete", "Future<Boolean>", List.of("Long id")));
        
        // 根据ID查找实体
        methods.add(new MethodInfo("findById", "Future<JsonObject>", List.of("Long id")));
        
        // 查找所有实体
        methods.add(new MethodInfo("findAll", "Future<List<JsonObject>>", List.of()));
        
        // 统计数量
        methods.add(new MethodInfo("count", "Future<Long>", List.of()));
        
        // 检查实体是否存在
        methods.add(new MethodInfo("exists", "Future<Boolean>", List.of("Long id")));
        
        return methods;
    }
    
    /**
     * 替换方法中的泛型参数为实体类型
     * 
     * @param methods 方法列表
     * @param entityElement 实体元素
     * @return 替换后的方法列表
     */
    private List<MethodInfo> replaceGenericParametersInMethods(List<MethodInfo> methods, TypeElement entityElement) {
        List<MethodInfo> replacedMethods = new ArrayList<>();
        String entityName = entityElement.getSimpleName().toString();
        
        for (MethodInfo method : methods) {
            String returnType = method.getReturnType();
            List<String> parameters = new ArrayList<>();
            
            // 替换返回类型中的泛型参数
            returnType = replaceGenericInType(returnType, entityName);
            
            // 替换参数类型中的泛型参数
            for (String param : method.getParameters()) {
                String[] parts = param.split(" ", 2);
                if (parts.length == 2) {
                    String paramType = replaceGenericInType(parts[0], entityName);
                    parameters.add(paramType + " " + parts[1]);
                } else {
                    parameters.add(param);
                }
            }
            
            replacedMethods.add(new MethodInfo(method.getMethodName(), returnType, parameters));
        }
        
        return replacedMethods;
    }
    
    /**
     * 替换类型字符串中的泛型参数
     * 
     * @param typeString 类型字符串
     * @param entityName 实体名称
     * @return 替换后的类型字符串
     */
    private String replaceGenericInType(String typeString, String entityName) {
        // 替换常见的泛型参数
        typeString = typeString.replace("T", "JsonObject");
        typeString = typeString.replace("ID", "Long");
        typeString = typeString.replace("Optional<JsonObject>", "JsonObject");
        typeString = typeString.replace("List<JsonObject>", "List<JsonObject>");
        typeString = typeString.replace("Future<JsonObject>", "Future<JsonObject>");
        typeString = typeString.replace("Future<Boolean>", "Future<Boolean>");
        typeString = typeString.replace("Future<Long>", "Future<Long>");
        
        return typeString;
    }
    
    /**
     * 递归收集接口及其父接口的所有方法
     * 
     * @param interfaceElement 接口元素
     * @param methods 方法列表
     * @param methodSignatures 方法签名集合（用于去重）
     */
    private void collectMethodsFromInterfaces(TypeElement interfaceElement, List<MethodInfo> methods, Set<String> methodSignatures) {
        // 收集当前接口的方法
        for (Element enclosedElement : interfaceElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;
                MethodInfo methodInfo = createMethodInfo(methodElement, interfaceElement);
                String signature = methodInfo.getSignature();
                
                if (!methodSignatures.contains(signature)) {
                    methods.add(methodInfo);
                    methodSignatures.add(signature);
                }
            }
        }
        
        // 递归收集父接口的方法
        for (TypeMirror interfaceType : interfaceElement.getInterfaces()) {
            if (interfaceType.getKind() == TypeKind.DECLARED) {
                DeclaredType declaredType = (DeclaredType) interfaceType;
                TypeElement parentInterface = (TypeElement) declaredType.asElement();
                collectMethodsFromInterfaces(parentInterface, methods, methodSignatures);
            }
        }
    }
    
    /**
     * 创建方法信息
     * 
     * @param methodElement 方法元素
     * @param interfaceElement 接口元素
     * @return 方法信息
     */
    private MethodInfo createMethodInfo(ExecutableElement methodElement, TypeElement interfaceElement) {
        String methodName = methodElement.getSimpleName().toString();
        String returnType = methodElement.getReturnType().toString();
        
        // 处理泛型类型替换
        returnType = replaceGenericTypes(returnType, interfaceElement);
        
        // 对于接口方法，我们不保留参数，因为生成的 Future 方法是无参数的
        List<String> parameters = new ArrayList<>();
        
        return new MethodInfo(methodName, returnType, parameters);
    }
    
    /**
     * 替换泛型类型为实际类型
     * 
     * @param typeString 类型字符串
     * @param interfaceElement 接口元素
     * @return 替换后的类型字符串
     */
    private String replaceGenericTypes(String typeString, TypeElement interfaceElement) {
        // 获取接口的泛型参数映射
        Map<String, String> genericMapping = getGenericTypeMapping(interfaceElement);
        
        // 替换泛型类型
        for (Map.Entry<String, String> entry : genericMapping.entrySet()) {
            typeString = typeString.replace(entry.getKey(), entry.getValue());
        }
        
        return typeString;
    }
    
    /**
     * 获取泛型类型映射
     * 
     * @param interfaceElement 接口元素
     * @return 泛型类型映射
     */
    private Map<String, String> getGenericTypeMapping(TypeElement interfaceElement) {
        Map<String, String> mapping = new HashMap<>();
        
        // 获取接口的泛型参数
        List<? extends TypeParameterElement> typeParameters = interfaceElement.getTypeParameters();
        for (int i = 0; i < typeParameters.size(); i++) {
            TypeParameterElement typeParam = typeParameters.get(i);
            String genericName = typeParam.getSimpleName().toString();
            // 使用实际类型替换，这里简化处理，使用 JsonObject
            mapping.put(genericName, "JsonObject");
        }
        
        return mapping;
    }
    
    /**
     * 方法信息类
     */
    private static class MethodInfo {
        private final String methodName;
        private final String returnType;
        private final List<String> parameters;
        
        public MethodInfo(String methodName, String returnType, List<String> parameters) {
            this.methodName = methodName;
            this.returnType = returnType;
            this.parameters = parameters;
        }
        
        public String getMethodName() { return methodName; }
        public String getReturnType() { return returnType; }
        public List<String> getParameters() { return parameters; }
        
        public String getSignature() {
            return methodName + "(" + String.join(", ", parameters) + ")";
        }
        
    }
    
    /**
     * 从参照接口生成服务接口
     * 
     * @param basePackage 基础包名
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param referenceMethods 参照接口方法列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceInterfaceFromReferenceInterface(String basePackage, String serviceName, String entityName, 
                                                               String entityPackage, List<MethodInfo> referenceMethods) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + serviceName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import io.vertx.codegen.annotations.ProxyGen;");
            writer.println("import io.vertx.codegen.annotations.VertxGen;");
            writer.println("import io.vertx.codegen.annotations.Fluent;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            writer.println("/**");
            writer.println(" * 基于参照接口动态生成的服务接口");
            writer.println(" * 包含参照接口的所有方法，泛型参数已替换为实际类型");
            writer.println(" * 所有方法都转换为 Future 异步模式");
            writer.println(" */");
            writer.println("@ProxyGen");
            writer.println("@VertxGen");
            writer.println("public interface " + serviceName + " {");
            writer.println();
            
            // 生成参照接口方法（转换为 Future 模式）
            generateReferenceInterfaceMethods(writer, referenceMethods);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成参照接口方法（转换为 Future 模式）
     * 
     * @param writer 写入器
     * @param referenceMethods 参照接口方法列表
     */
    private void generateReferenceInterfaceMethods(PrintWriter writer, List<MethodInfo> referenceMethods) {
        for (MethodInfo method : referenceMethods) {
            writer.println("    /**");
            writer.println("     * " + method.getMethodName() + " 方法（基于参照接口生成）");
            writer.println("     * @return Future 包装的结果");
            writer.println("     */");
            
            // 将方法转换为 Future 模式
            String futureReturnType = convertToFutureType(method.getReturnType());
            writer.println("    Future<" + futureReturnType + "> " + method.getMethodName() + "();");
            writer.println();
        }
    }
    
    /**
     * 从参照接口生成服务实现类
     * 
     * @param basePackage 基础包名
     * @param implName 实现类名称
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param idType ID类型
     * @param referenceMethods 参照接口方法列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceImplFromReferenceInterface(String basePackage, String implName, String serviceName, 
                                                          String entityName, String entityPackage, String idType, 
                                                          List<MethodInfo> referenceMethods) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + implName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import " + basePackage + "." + serviceName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            writer.println("/**");
            writer.println(" * 基于参照接口动态生成的服务实现类");
            writer.println(" * 提供参照接口方法的默认实现");
            writer.println(" * 用户可以继承此类并重写方法来实现具体的业务逻辑");
            writer.println(" */");
            writer.println("@javax.annotation.Generated(\"cn.qaiu.vx.core.processor.CustomServiceGenProcessor\")");
            writer.println("public abstract class " + implName + " implements " + serviceName + " {");
            writer.println();
            
            // 生成参照接口方法的实现
            generateReferenceInterfaceMethodImplementations(writer, referenceMethods);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成参照接口方法的实现
     * 
     * @param writer 写入器
     * @param referenceMethods 参照接口方法列表
     */
    private void generateReferenceInterfaceMethodImplementations(PrintWriter writer, List<MethodInfo> referenceMethods) {
        for (MethodInfo method : referenceMethods) {
            writer.println("    @Override");
            writer.println("    public Future<" + convertToFutureType(method.getReturnType()) + "> " + method.getMethodName() + "() {");
            writer.println("        // TODO: 实现 " + method.getMethodName() + " 方法（基于参照接口）");
            writer.println("        // 示例：实现具体的业务逻辑");
            writer.println("        return Future.succeededFuture(null); // 占位符实现");
            writer.println("    }");
            writer.println();
        }
    }
    
    /**
     * 从接口生成服务接口
     * 
     * @param basePackage 基础包名
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param interfaceMethods 接口方法列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceInterfaceFromInterface(String basePackage, String serviceName, String entityName, 
                                                      String entityPackage, List<MethodInfo> interfaceMethods) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + serviceName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import io.vertx.codegen.annotations.ProxyGen;");
            writer.println("import io.vertx.codegen.annotations.VertxGen;");
            writer.println("import io.vertx.codegen.annotations.Fluent;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            writer.println("/**");
            writer.println(" * 从接口动态生成的服务接口");
            writer.println(" * 包含所有父接口方法和当前接口方法");
            writer.println(" * 所有方法都转换为 Future 异步模式");
            writer.println(" */");
            writer.println("@ProxyGen");
            writer.println("@VertxGen");
            writer.println("public interface " + serviceName + " {");
            writer.println();
            
            // 生成接口方法（转换为 Future 模式）
            generateInterfaceMethods(writer, interfaceMethods);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成接口方法（转换为 Future 模式）
     * 
     * @param writer 写入器
     * @param interfaceMethods 接口方法列表
     */
    private void generateInterfaceMethods(PrintWriter writer, List<MethodInfo> interfaceMethods) {
        for (MethodInfo method : interfaceMethods) {
            writer.println("    /**");
            writer.println("     * " + method.getMethodName() + " 方法（转换为异步模式）");
            writer.println("     * @return Future 包装的结果");
            writer.println("     */");
            
            // 将方法转换为 Future 模式
            String futureReturnType = convertToFutureType(method.getReturnType());
            writer.println("    Future<" + futureReturnType + "> " + method.getMethodName() + "();");
            writer.println();
        }
    }
    
    /**
     * 将返回类型转换为 Future 类型
     * 
     * @param returnType 原始返回类型
     * @return Future 包装的类型
     */
    private String convertToFutureType(String returnType) {
        // 如果已经是 Future 类型，直接返回内部类型
        if (returnType.startsWith("Future<")) {
            return returnType.substring(7, returnType.length() - 1); // 去掉 Future< 和 >
        }
        
        // 如果是 void，返回 Void
        if ("void".equals(returnType)) {
            return "Void";
        }
        
        // 如果是 JsonObject，直接返回
        if ("JsonObject".equals(returnType)) {
            return "JsonObject";
        }
        
        // 如果是 List<JsonObject>，直接返回
        if ("List<JsonObject>".equals(returnType)) {
            return "List<JsonObject>";
        }
        
        // 如果是基本类型，转换为 JsonObject
        if (isPrimitiveType(returnType)) {
            return "JsonObject";
        }
        
        // 其他类型转换为 JsonObject
        return "JsonObject";
    }
    
    /**
     * 判断是否为基本类型
     * 
     * @param type 类型字符串
     * @return 是否为基本类型
     */
    private boolean isPrimitiveType(String type) {
        return "int".equals(type) || "long".equals(type) || "double".equals(type) || 
               "float".equals(type) || "boolean".equals(type) || "char".equals(type) ||
               "byte".equals(type) || "short".equals(type) ||
               "Integer".equals(type) || "Long".equals(type) || "Double".equals(type) ||
               "Float".equals(type) || "Boolean".equals(type) || "Character".equals(type) ||
               "Byte".equals(type) || "Short".equals(type) || "String".equals(type);
    }
    
    /**
     * 生成服务接口
     * 
     * @param basePackage 基础包名
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param genericTypes 泛型类型列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceInterface(String basePackage, String serviceName, String entityName, 
                                        String entityPackage, List<String> genericTypes) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + serviceName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import io.vertx.codegen.annotations.ProxyGen;");
            writer.println("import io.vertx.codegen.annotations.VertxGen;");
            writer.println("import io.vertx.codegen.annotations.Fluent;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            // 生成泛型参数
            if (!genericTypes.isEmpty()) {
                String genericParams = genericTypes.stream()
                    .map(type -> "T" + genericTypes.indexOf(type))
                    .collect(Collectors.joining(", "));
                writer.println("/**");
                writer.println(" * 动态生成的服务接口");
                writer.println(" * 泛型参数: " + genericParams);
                writer.println(" */");
                writer.println("@ProxyGen");
                writer.println("@VertxGen");
                writer.println("public interface " + serviceName + " {");
            } else {
                writer.println("/**");
                writer.println(" * 动态生成的服务接口");
                writer.println(" */");
                writer.println("@ProxyGen");
                writer.println("@VertxGen");
                writer.println("public interface " + serviceName + " {");
            }
            
            writer.println();
            
            // 生成基础CRUD方法
            generateBasicCrudMethods(writer, serviceName, entityName, genericTypes);
            
            // 生成自定义查询方法
            generateCustomQueryMethods(writer, serviceName, genericTypes);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成基础CRUD方法
     * 
     * @param writer 写入器
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param genericTypes 泛型类型列表
     */
    private void generateBasicCrudMethods(PrintWriter writer, String serviceName, String entityName, List<String> genericTypes) {
        // 创建方法 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 创建实体");
        writer.println("     * @param entity 实体对象");
        writer.println("     * @return Future 包装的创建结果");
        writer.println("     */");
        writer.println("    Future<JsonObject> create(JsonObject entity);");
        writer.println();
        
        // 根据ID查找 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 根据ID查找实体");
        writer.println("     * @param id 实体ID");
        writer.println("     * @return Future 包装的查找结果");
        writer.println("     */");
        writer.println("    Future<JsonObject> findById(Long id);");
        writer.println();
        
        // 查找所有 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 查找所有实体");
        writer.println("     * @return Future 包装的查找结果");
        writer.println("     */");
        writer.println("    Future<List<JsonObject>> findAll();");
        writer.println();
        
        // 根据状态查找 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 根据状态查找实体");
        writer.println("     * @param status 状态值");
        writer.println("     * @return Future 包装的查找结果");
        writer.println("     */");
        writer.println("    Future<List<JsonObject>> findByStatus(String status);");
        writer.println();
        
        // 条件查询 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 条件查询实体");
        writer.println("     * @param query 查询条件");
        writer.println("     * @return Future 包装的查找结果");
        writer.println("     */");
        writer.println("    Future<JsonObject> findOne(JsonObject query);");
        writer.println();
        
        // 更新 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 更新实体");
        writer.println("     * @param entity 实体对象");
        writer.println("     * @return Future 包装的更新结果");
        writer.println("     */");
        writer.println("    Future<Integer> update(JsonObject entity);");
        writer.println();
        
        // 删除 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 根据ID删除实体");
        writer.println("     * @param id 实体ID");
        writer.println("     * @return Future 包装的删除结果");
        writer.println("     */");
        writer.println("    Future<Integer> deleteById(Long id);");
        writer.println();
        
        // 计数 - 使用 Future 返回类型
        writer.println("    /**");
        writer.println("     * 统计实体数量");
        writer.println("     * @return Future 包装的计数结果");
        writer.println("     */");
        writer.println("    Future<Long> count();");
        writer.println();
    }
    
    /**
     * 生成自定义查询方法
     * 
     * @param writer 写入器
     * @param serviceName 服务名称
     * @param genericTypes 泛型类型列表
     */
    private void generateCustomQueryMethods(PrintWriter writer, String serviceName, List<String> genericTypes) {
        writer.println("    /**");
        writer.println("     * 自定义查询方法");
        writer.println("     * @param param 查询参数");
        writer.println("     * @return Future 包装的查询结果");
        writer.println("     */");
        writer.println("    Future<List<JsonObject>> customQuery(String param);");
        writer.println();
        
        // 根据泛型类型生成特定的查询方法
        for (String genericType : genericTypes) {
            writer.println("    /**");
            writer.println("     * 根据" + genericType + "类型查询");
            writer.println("     * @param " + genericType.toLowerCase() + " " + genericType + "对象");
            writer.println("     * @return Future 包装的查询结果");
            writer.println("     */");
            writer.println("    Future<List<JsonObject>> findBy" + genericType + "(JsonObject " + genericType.toLowerCase() + ");");
            writer.println();
        }
    }
    
    /**
     * 从接口生成服务实现类
     * 
     * @param basePackage 基础包名
     * @param implName 实现类名称
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param idType ID类型
     * @param interfaceMethods 接口方法列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceImplFromInterface(String basePackage, String implName, String serviceName, 
                                                  String entityName, String entityPackage, String idType, 
                                                  List<MethodInfo> interfaceMethods) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + implName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import " + basePackage + "." + serviceName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            writer.println("/**");
            writer.println(" * 从接口动态生成的服务实现类");
            writer.println(" * 提供接口方法的默认实现");
            writer.println(" * 用户可以继承此类并重写方法来实现具体的业务逻辑");
            writer.println(" */");
            writer.println("@javax.annotation.Generated(\"cn.qaiu.vx.core.processor.CustomServiceGenProcessor\")");
            writer.println("public abstract class " + implName + " implements " + serviceName + " {");
            writer.println();
            
            // 生成接口方法的实现
            generateInterfaceMethodImplementations(writer, interfaceMethods);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成接口方法的实现
     * 
     * @param writer 写入器
     * @param interfaceMethods 接口方法列表
     */
    private void generateInterfaceMethodImplementations(PrintWriter writer, List<MethodInfo> interfaceMethods) {
        for (MethodInfo method : interfaceMethods) {
            writer.println("    @Override");
            writer.println("    public Future<" + convertToFutureType(method.getReturnType()) + "> " + method.getMethodName() + "() {");
            writer.println("        // TODO: 实现 " + method.getMethodName() + " 方法");
            writer.println("        // 示例：实现具体的业务逻辑");
            writer.println("        return Future.succeededFuture(null); // 占位符实现");
            writer.println("    }");
            writer.println();
        }
    }
    
    /**
     * 生成服务实现类
     * 
     * @param basePackage 基础包名
     * @param implName 实现类名称
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param entityPackage 实体包名
     * @param idType ID类型
     * @param genericTypes 泛型类型列表
     * @throws IOException 文件操作异常
     */
    private void generateServiceImpl(String basePackage, String implName, String serviceName, 
                                   String entityName, String entityPackage, String idType, 
                                   List<String> genericTypes) throws IOException {
        JavaFileObject file = filer.createSourceFile(basePackage + "." + implName);
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + basePackage + ";");
            writer.println();
            writer.println("import io.vertx.core.Future;");
            writer.println("import io.vertx.core.json.JsonObject;");
            writer.println("import " + entityPackage + "." + entityName + ";");
            writer.println("import " + basePackage + "." + serviceName + ";");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            // 生成泛型参数
            String genericParams = "";
            String genericBounds = "";
            if (!genericTypes.isEmpty()) {
                genericParams = "<" + genericTypes.stream()
                    .map(type -> "T" + genericTypes.indexOf(type))
                    .collect(Collectors.joining(", ")) + ">";
                genericBounds = " implements " + serviceName;
            } else {
                genericBounds = " implements " + serviceName;
            }
            
            writer.println("/**");
            writer.println(" * 动态生成的服务实现类");
            writer.println(" * 提供基础CRUD操作和自定义查询方法的默认实现");
            writer.println(" * 用户可以继承此类并重写方法来实现具体的业务逻辑");
            writer.println(" */");
            writer.println("@javax.annotation.Generated(\"cn.qaiu.vx.core.processor.CustomServiceGenProcessor\")");
            writer.println("public abstract class " + implName + genericParams + genericBounds + " {");
            writer.println();
            
            // 生成基础CRUD方法实现
            generateBasicCrudMethodImplementations(writer, serviceName, entityName, genericTypes);
            
            // 生成自定义查询方法实现
            generateCustomQueryMethodImplementations(writer, serviceName, genericTypes);
            
            writer.println("}");
        }
    }
    
    /**
     * 生成基础CRUD方法实现
     * 
     * @param writer 写入器
     * @param serviceName 服务名称
     * @param entityName 实体名称
     * @param genericTypes 泛型类型列表
     */
    private void generateBasicCrudMethodImplementations(PrintWriter writer, String serviceName, 
                                                       String entityName, List<String> genericTypes) {
        // 创建方法实现
        writer.println("    @Override");
        writer.println("    public Future<JsonObject> create(JsonObject entity) {");
        writer.println("        // TODO: 实现创建逻辑");
        writer.println("        // 示例：将JsonObject转换为实体对象并保存");
        writer.println("        return Future.succeededFuture(entity);");
        writer.println("    }");
        writer.println();
        
        // 根据ID查找实现
        writer.println("    @Override");
        writer.println("    public Future<JsonObject> findById(Long id) {");
        writer.println("        // TODO: 实现根据ID查找逻辑");
        writer.println("        // 示例：从数据库查询并转换为JsonObject");
        writer.println("        JsonObject result = new JsonObject().put(\"id\", id);");
        writer.println("        return Future.succeededFuture(result);");
        writer.println("    }");
        writer.println();
        
        // 查找所有实现
        writer.println("    @Override");
        writer.println("    public Future<List<JsonObject>> findAll() {");
        writer.println("        // TODO: 实现查找所有逻辑");
        writer.println("        // 示例：从数据库查询所有记录并转换为JsonObject列表");
        writer.println("        return Future.succeededFuture(List.of());");
        writer.println("    }");
        writer.println();
        
        // 根据状态查找实现
        writer.println("    @Override");
        writer.println("    public Future<List<JsonObject>> findByStatus(String status) {");
        writer.println("        // TODO: 实现根据状态查找逻辑");
        writer.println("        // 示例：根据状态字段查询");
        writer.println("        return Future.succeededFuture(List.of());");
        writer.println("    }");
        writer.println();
        
        // 条件查询实现
        writer.println("    @Override");
        writer.println("    public Future<JsonObject> findOne(JsonObject query) {");
        writer.println("        // TODO: 实现条件查询逻辑");
        writer.println("        // 示例：根据查询条件查找单个记录");
        writer.println("        return Future.succeededFuture(null);");
        writer.println("    }");
        writer.println();
        
        // 更新实现
        writer.println("    @Override");
        writer.println("    public Future<Integer> update(JsonObject entity) {");
        writer.println("        // TODO: 实现更新逻辑");
        writer.println("        // 示例：更新数据库记录");
        writer.println("        return Future.succeededFuture(0);");
        writer.println("    }");
        writer.println();
        
        // 删除实现
        writer.println("    @Override");
        writer.println("    public Future<Integer> deleteById(Long id) {");
        writer.println("        // TODO: 实现删除逻辑");
        writer.println("        // 示例：根据ID删除数据库记录");
        writer.println("        return Future.succeededFuture(0);");
        writer.println("    }");
        writer.println();
        
        // 计数实现
        writer.println("    @Override");
        writer.println("    public Future<Long> count() {");
        writer.println("        // TODO: 实现计数逻辑");
        writer.println("        // 示例：统计数据库记录数量");
        writer.println("        return Future.succeededFuture(0L);");
        writer.println("    }");
        writer.println();
    }
    
    /**
     * 生成自定义查询方法实现
     * 
     * @param writer 写入器
     * @param serviceName 服务名称
     * @param genericTypes 泛型类型列表
     */
    private void generateCustomQueryMethodImplementations(PrintWriter writer, String serviceName, 
                                                        List<String> genericTypes) {
        // 自定义查询方法实现
        writer.println("    @Override");
        writer.println("    public Future<List<JsonObject>> customQuery(String param) {");
        writer.println("        // TODO: 实现自定义查询逻辑");
        writer.println("        // 示例：根据参数执行自定义查询");
        writer.println("        return Future.succeededFuture(List.of());");
        writer.println("    }");
        writer.println();
        
        // 根据泛型类型生成特定的查询方法实现
        for (String genericType : genericTypes) {
            writer.println("    @Override");
            writer.println("    public Future<List<JsonObject>> findBy" + genericType + "(JsonObject " + genericType.toLowerCase() + ") {");
            writer.println("        // TODO: 实现根据" + genericType + "类型查询逻辑");
            writer.println("        // 示例：根据" + genericType + "对象查询相关记录");
            writer.println("        return Future.succeededFuture(List.of());");
            writer.println("    }");
            writer.println();
        }
    }
}