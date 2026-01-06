package cn.qaiu.vx.core.aop;

import cn.qaiu.vx.core.aop.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 切面元数据
 * <p>
 * 存储单个切面类的所有通知方法和切点信息
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
public class AspectMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectMetadata.class);

    private final Class<?> aspectClass;
    private final Object aspectInstance;
    private final int order;
    private final List<AdviceMetadata> advices = new ArrayList<>();

    public AspectMetadata(Class<?> aspectClass, Object aspectInstance) {
        this.aspectClass = aspectClass;
        this.aspectInstance = aspectInstance;
        this.order = aspectClass.isAnnotationPresent(Order.class) 
                ? aspectClass.getAnnotation(Order.class).value() 
                : Integer.MAX_VALUE;
        parseAdvices();
    }

    private void parseAdvices() {
        // 先解析所有的 @Pointcut 方法
        Map<String, String> pointcutMap = new HashMap<>();
        for (Method method : aspectClass.getDeclaredMethods()) {
            Pointcut pointcut = method.getAnnotation(Pointcut.class);
            if (pointcut != null) {
                pointcutMap.put(method.getName() + "()", pointcut.value());
            }
        }

        // 解析通知方法
        for (Method method : aspectClass.getDeclaredMethods()) {
            method.setAccessible(true);

            Before before = method.getAnnotation(Before.class);
            if (before != null) {
                String expression = resolvePointcut(before.value(), pointcutMap);
                advices.add(new AdviceMetadata(AdviceType.BEFORE, method, expression));
            }

            After after = method.getAnnotation(After.class);
            if (after != null) {
                String expression = resolvePointcut(after.value(), pointcutMap);
                advices.add(new AdviceMetadata(AdviceType.AFTER, method, expression));
            }

            Around around = method.getAnnotation(Around.class);
            if (around != null) {
                String expression = resolvePointcut(around.value(), pointcutMap);
                advices.add(new AdviceMetadata(AdviceType.AROUND, method, expression));
            }

            AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
            if (afterReturning != null) {
                String expression = resolvePointcut(afterReturning.value(), pointcutMap);
                AdviceMetadata metadata = new AdviceMetadata(AdviceType.AFTER_RETURNING, method, expression);
                metadata.setReturningParam(afterReturning.returning());
                advices.add(metadata);
            }

            AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
            if (afterThrowing != null) {
                String expression = resolvePointcut(afterThrowing.value(), pointcutMap);
                AdviceMetadata metadata = new AdviceMetadata(AdviceType.AFTER_THROWING, method, expression);
                metadata.setThrowingParam(afterThrowing.throwing());
                advices.add(metadata);
            }
        }

        LOGGER.debug("解析切面 {} 完成，发现 {} 个通知方法", aspectClass.getSimpleName(), advices.size());
    }

    /**
     * 解析切点引用
     * 如果表达式是一个方法引用（如 "serviceMethod()"），则替换为实际的切点表达式
     */
    private String resolvePointcut(String expression, Map<String, String> pointcutMap) {
        if (pointcutMap.containsKey(expression)) {
            return pointcutMap.get(expression);
        }
        return expression;
    }

    public Class<?> getAspectClass() {
        return aspectClass;
    }

    public Object getAspectInstance() {
        return aspectInstance;
    }

    public int getOrder() {
        return order;
    }

    public List<AdviceMetadata> getAdvices() {
        return Collections.unmodifiableList(advices);
    }

    /**
     * 获取匹配指定方法的所有通知
     *
     * @param targetMethod 目标方法
     * @return 匹配的通知列表
     */
    public List<AdviceMetadata> getMatchingAdvices(Method targetMethod) {
        List<AdviceMetadata> result = new ArrayList<>();
        for (AdviceMetadata advice : advices) {
            if (advice.matches(targetMethod)) {
                result.add(advice);
            }
        }
        return result;
    }

    /**
     * 通知类型
     */
    public enum AdviceType {
        BEFORE, AFTER, AROUND, AFTER_RETURNING, AFTER_THROWING
    }

    /**
     * 通知元数据
     */
    public static class AdviceMetadata {
        private final AdviceType type;
        private final Method adviceMethod;
        private final PointcutMatcher pointcutMatcher;
        private String returningParam;
        private String throwingParam;

        public AdviceMetadata(AdviceType type, Method adviceMethod, String pointcutExpression) {
            this.type = type;
            this.adviceMethod = adviceMethod;
            this.pointcutMatcher = new PointcutMatcher(pointcutExpression);
        }

        public boolean matches(Method targetMethod) {
            return pointcutMatcher.matches(targetMethod);
        }

        public AdviceType getType() {
            return type;
        }

        public Method getAdviceMethod() {
            return adviceMethod;
        }

        public PointcutMatcher getPointcutMatcher() {
            return pointcutMatcher;
        }

        public String getReturningParam() {
            return returningParam;
        }

        public void setReturningParam(String returningParam) {
            this.returningParam = returningParam;
        }

        public String getThrowingParam() {
            return throwingParam;
        }

        public void setThrowingParam(String throwingParam) {
            this.throwingParam = throwingParam;
        }
    }
}
