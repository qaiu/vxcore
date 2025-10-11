package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解名称生成器
 * 当注解的name属性为空时，自动生成类名首字母小写的名称
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AnnotationNameGenerator {

    /**
     * 生成注解类的名称
     *
     * @param clazz 类对象
     * @return 生成的名称
     */
    public static String generateName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        
        String className = clazz.getSimpleName();
        if (StringUtils.isEmpty(className)) {
            return "";
        }
        
        // 首字母小写
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * 获取注解类的有效名称（优先使用注解的name属性，为空时使用生成的名称）
     *
     * @param clazz 类对象
     * @return 有效名称
     */
    public static String getEffectiveName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }

        // 检查各种注解的name属性
        String annotationName = getAnnotationName(clazz);
        if (StringUtils.isNotEmpty(annotationName)) {
            return annotationName;
        }

        // 如果注解的name为空，使用生成的名称
        return generateName(clazz);
    }

    /**
     * 获取注解的name属性值
     *
     * @param clazz 类对象
     * @return 注解的name属性值
     */
    private static String getAnnotationName(Class<?> clazz) {
        // 检查 @Service 注解
        Service serviceAnnotation = clazz.getAnnotation(Service.class);
        if (serviceAnnotation != null && StringUtils.isNotEmpty(serviceAnnotation.name())) {
            return serviceAnnotation.name();
        }

        // 检查 @Dao 注解
        Dao daoAnnotation = clazz.getAnnotation(Dao.class);
        if (daoAnnotation != null && StringUtils.isNotEmpty(daoAnnotation.name())) {
            return daoAnnotation.name();
        }

        // 检查 @Component 注解
        Component componentAnnotation = clazz.getAnnotation(Component.class);
        if (componentAnnotation != null && StringUtils.isNotEmpty(componentAnnotation.name())) {
            return componentAnnotation.name();
        }

        // 检查 @Repository 注解
        Repository repositoryAnnotation = clazz.getAnnotation(Repository.class);
        if (repositoryAnnotation != null && StringUtils.isNotEmpty(repositoryAnnotation.name())) {
            return repositoryAnnotation.name();
        }

        // 检查 @Controller 注解
        Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
        if (controllerAnnotation != null && StringUtils.isNotEmpty(controllerAnnotation.name())) {
            return controllerAnnotation.name();
        }

        return "";
    }

    /**
     * 获取注解类型信息
     *
     * @param clazz 类对象
     * @return 注解类型信息
     */
    public static Map<String, Object> getAnnotationInfo(Class<?> clazz) {
        Map<String, Object> info = new HashMap<>();
        
        if (clazz == null) {
            return info;
        }

        info.put("className", clazz.getName());
        info.put("simpleName", clazz.getSimpleName());
        info.put("generatedName", generateName(clazz));
        info.put("effectiveName", getEffectiveName(clazz));

        // 获取注解类型
        String annotationType = getAnnotationType(clazz);
        info.put("annotationType", annotationType);

        // 获取注解属性
        Map<String, Object> annotationAttributes = getAnnotationAttributes(clazz);
        info.put("annotationAttributes", annotationAttributes);

        return info;
    }

    /**
     * 获取注解类型
     *
     * @param clazz 类对象
     * @return 注解类型名称
     */
    private static String getAnnotationType(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Service.class)) {
            return "Service";
        }
        if (clazz.isAnnotationPresent(Dao.class)) {
            return "Dao";
        }
        if (clazz.isAnnotationPresent(Component.class)) {
            return "Component";
        }
        if (clazz.isAnnotationPresent(Repository.class)) {
            return "Repository";
        }
        if (clazz.isAnnotationPresent(Controller.class)) {
            return "Controller";
        }
        return "Unknown";
    }

    /**
     * 获取注解属性
     *
     * @param clazz 类对象
     * @return 注解属性映射
     */
    private static Map<String, Object> getAnnotationAttributes(Class<?> clazz) {
        Map<String, Object> attributes = new HashMap<>();

        // 检查 @Service 注解
        Service serviceAnnotation = clazz.getAnnotation(Service.class);
        if (serviceAnnotation != null) {
            attributes.put("name", serviceAnnotation.name());
            attributes.put("annotation", "Service");
        }

        // 检查 @Dao 注解
        Dao daoAnnotation = clazz.getAnnotation(Dao.class);
        if (daoAnnotation != null) {
            attributes.put("name", daoAnnotation.name());
            attributes.put("cacheable", daoAnnotation.cacheable());
            attributes.put("annotation", "Dao");
        }

        // 检查 @Component 注解
        Component componentAnnotation = clazz.getAnnotation(Component.class);
        if (componentAnnotation != null) {
            attributes.put("name", componentAnnotation.name());
            attributes.put("priority", componentAnnotation.priority());
            attributes.put("lazy", componentAnnotation.lazy());
            attributes.put("annotation", "Component");
        }

        // 检查 @Repository 注解
        Repository repositoryAnnotation = clazz.getAnnotation(Repository.class);
        if (repositoryAnnotation != null) {
            attributes.put("name", repositoryAnnotation.name());
            attributes.put("datasource", repositoryAnnotation.datasource());
            attributes.put("transactional", repositoryAnnotation.transactional());
            attributes.put("annotation", "Repository");
        }

        // 检查 @Controller 注解
        Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
        if (controllerAnnotation != null) {
            attributes.put("name", controllerAnnotation.name());
            attributes.put("basePath", controllerAnnotation.basePath());
            attributes.put("cors", controllerAnnotation.cors());
            attributes.put("authenticated", controllerAnnotation.authenticated());
            attributes.put("annotation", "Controller");
        }

        return attributes;
    }
}
