package cn.qaiu.vx.core.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 切点匹配器
 * <p>
 * 解析切点表达式并判断方法是否匹配。
 * 支持的表达式格式:
 * <ul>
 *   <li>execution(modifiers? return-type declaring-type? method-name(params))</li>
 *   <li>@annotation(fully.qualified.AnnotationName)</li>
 *   <li>within(type-pattern)</li>
 * </ul>
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
public class PointcutMatcher {

    // execution 表达式模式: execution(* cn.qaiu..*.*(..))
    private static final Pattern EXECUTION_PATTERN = Pattern.compile(
            "execution\\s*\\(\\s*" +
                    "([\\w*]+)\\s+" +                        // 返回类型
                    "([\\w.*]+)\\.([\\w*]+)" +               // 类路径.方法名
                    "\\s*\\((.*)\\)" +                       // 参数
                    "\\s*\\)"
    );

    // @annotation 表达式模式: @annotation(cn.qaiu.vx.core.aop.Loggable)
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
            "@annotation\\s*\\(\\s*([\\w.]+)\\s*\\)"
    );

    // within 表达式模式: within(cn.qaiu.*.service.*)
    private static final Pattern WITHIN_PATTERN = Pattern.compile(
            "within\\s*\\(\\s*([\\w.*]+)\\s*\\)"
    );

    private final String expression;
    private final MatchType matchType;
    private final String returnTypePattern;
    private final String classPattern;
    private final String methodPattern;
    private final String paramsPattern;
    private final String annotationClassName;
    private final String withinPattern;

    private enum MatchType {
        EXECUTION, ANNOTATION, WITHIN
    }

    /**
     * 构造函数
     *
     * @param expression 切点表达式
     */
    public PointcutMatcher(String expression) {
        this.expression = expression.trim();

        // 解析 execution 表达式
        Matcher executionMatcher = EXECUTION_PATTERN.matcher(this.expression);
        if (executionMatcher.matches()) {
            this.matchType = MatchType.EXECUTION;
            this.returnTypePattern = executionMatcher.group(1);
            String classAndMethod = executionMatcher.group(2);
            this.methodPattern = executionMatcher.group(3);
            this.classPattern = classAndMethod;
            this.paramsPattern = executionMatcher.group(4);
            this.annotationClassName = null;
            this.withinPattern = null;
            return;
        }

        // 解析 @annotation 表达式
        Matcher annotationMatcher = ANNOTATION_PATTERN.matcher(this.expression);
        if (annotationMatcher.matches()) {
            this.matchType = MatchType.ANNOTATION;
            this.annotationClassName = annotationMatcher.group(1);
            this.returnTypePattern = null;
            this.classPattern = null;
            this.methodPattern = null;
            this.paramsPattern = null;
            this.withinPattern = null;
            return;
        }

        // 解析 within 表达式
        Matcher withinMatcher = WITHIN_PATTERN.matcher(this.expression);
        if (withinMatcher.matches()) {
            this.matchType = MatchType.WITHIN;
            this.withinPattern = withinMatcher.group(1);
            this.returnTypePattern = null;
            this.classPattern = null;
            this.methodPattern = null;
            this.paramsPattern = null;
            this.annotationClassName = null;
            return;
        }

        throw new IllegalArgumentException("无法解析切点表达式: " + expression);
    }

    /**
     * 判断方法是否匹配切点表达式
     *
     * @param method 要检查的方法
     * @return 如果匹配返回 true
     */
    public boolean matches(Method method) {
        return switch (matchType) {
            case EXECUTION -> matchesExecution(method);
            case ANNOTATION -> matchesAnnotation(method);
            case WITHIN -> matchesWithin(method);
        };
    }

    /**
     * 判断类是否匹配切点表达式
     *
     * @param clazz 要检查的类
     * @return 如果匹配返回 true
     */
    public boolean matchesClass(Class<?> clazz) {
        return switch (matchType) {
            case EXECUTION -> matchesClassPattern(clazz.getName());
            case ANNOTATION -> hasAnnotatedMethods(clazz);
            case WITHIN -> matchesWithinPattern(clazz.getName());
        };
    }

    private boolean matchesExecution(Method method) {
        // 检查返回类型
        if (!matchesReturnType(method)) {
            return false;
        }

        // 检查类路径
        if (!matchesClassPattern(method.getDeclaringClass().getName())) {
            return false;
        }

        // 检查方法名
        if (!matchesMethodName(method.getName())) {
            return false;
        }

        // 检查参数（简化处理，.. 匹配任意参数）
        return matchesParams(method);
    }

    private boolean matchesAnnotation(Method method) {
        try {
            // 尝试原始类名
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = loadAnnotationClass(annotationClassName);
            return method.isAnnotationPresent(annotationClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 加载注解类，支持内部类
     * 如果类名使用 . 分隔内部类名，会尝试将最后的 . 替换为 $ 来加载内部类
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> loadAnnotationClass(String className) throws ClassNotFoundException {
        try {
            // 首先尝试原始类名
            return (Class<? extends Annotation>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 如果失败，尝试将内部类分隔符 . 替换为 $
            // 例如: com.example.Outer.Inner -> com.example.Outer$Inner
            int lastDot = className.lastIndexOf('.');
            if (lastDot > 0) {
                String modifiedName = className.substring(0, lastDot) + "$" + className.substring(lastDot + 1);
                try {
                    return (Class<? extends Annotation>) Class.forName(modifiedName);
                } catch (ClassNotFoundException e2) {
                    // 继续尝试更多的替换
                }
            }
            throw e;
        }
    }

    private boolean matchesWithin(Method method) {
        return matchesWithinPattern(method.getDeclaringClass().getName());
    }

    private boolean matchesReturnType(Method method) {
        if ("*".equals(returnTypePattern)) {
            return true;
        }
        String returnTypeName = method.getReturnType().getName();
        String simpleReturnTypeName = method.getReturnType().getSimpleName();
        return returnTypeName.equals(returnTypePattern) || 
               simpleReturnTypeName.equals(returnTypePattern);
    }

    private boolean matchesClassPattern(String className) {
        // 将通配符模式转换为正则表达式
        String regex = patternToRegex(classPattern);
        return className.matches(regex);
    }

    private boolean matchesMethodName(String methodName) {
        if ("*".equals(methodPattern)) {
            return true;
        }
        String regex = methodPattern.replace("*", ".*");
        return methodName.matches(regex);
    }

    private boolean matchesParams(Method method) {
        if ("..".equals(paramsPattern) || paramsPattern.isEmpty()) {
            return true; // 匹配任意参数
        }
        // 可以扩展更复杂的参数匹配逻辑
        return true;
    }

    private boolean matchesWithinPattern(String className) {
        String regex = patternToRegex(withinPattern);
        return className.matches(regex);
    }

    private boolean hasAnnotatedMethods(Class<?> clazz) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = 
                    (Class<? extends Annotation>) Class.forName(annotationClassName);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 将通配符模式转换为正则表达式
     * <ul>
     *   <li>* 匹配任意字符（不包括.）</li>
     *   <li>.. 匹配任意包路径</li>
     *   <li>支持内部类名（. 会同时匹配 . 和 $）</li>
     * </ul>
     */
    private String patternToRegex(String pattern) {
        if (pattern == null) {
            return ".*";
        }
        // 首先处理 .. 替换为占位符
        String temp = pattern.replace("..", "##DOUBLE_DOT##");
        
        // 转义正则特殊字符，但保留 *
        // 注意：. 在模式中应该同时匹配 . 和 $ (用于内部类)
        String escaped = temp
                .replace(".", "[.$]")  // . 匹配 . 或 $（内部类）
                .replace("*", "[^.$]*")
                .replace("##DOUBLE_DOT##", ".*");  // .. 变成 .* 匹配任意包
        return escaped;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "PointcutMatcher{expression='" + expression + "'}";
    }
}
