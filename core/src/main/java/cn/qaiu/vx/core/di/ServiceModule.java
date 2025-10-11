package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.*;
import cn.qaiu.vx.core.util.AnnotationNameGenerator;
import cn.qaiu.vx.core.util.ReflectionUtil;
import dagger.Module;
import dagger.Provides;
import org.reflections.Reflections;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dagger2模块 - 用于扫描和提供各种注解的类
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Module
public class ServiceModule {

    /**
     * 提供Service注解的类集合
     *
     * @return Set<Class<?>> 包含@Service注解的类集合
     */
    @Provides
    @Singleton
    public Set<Class<?>> provideServiceClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Service.class);
    }

    /**
     * 提供Dao注解的类集合
     *
     * @return Set<Class<?>> 包含@Dao注解的类集合
     */
    @Provides
    @Singleton
    @Named("Dao")
    public Set<Class<?>> provideDaoClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Dao.class);
    }

    /**
     * 提供Component注解的类集合
     *
     * @return Set<Class<?>> 包含@Component注解的类集合
     */
    @Provides
    @Singleton
    @Named("Component")
    public Set<Class<?>> provideComponentClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Component.class);
    }

    /**
     * 提供Repository注解的类集合
     *
     * @return Set<Class<?>> 包含@Repository注解的类集合
     */
    @Provides
    @Singleton
    @Named("Repository")
    public Set<Class<?>> provideRepositoryClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Repository.class);
    }

    /**
     * 提供Controller注解的类集合
     *
     * @return Set<Class<?>> 包含@Controller注解的类集合
     */
    @Provides
    @Singleton
    @Named("Controller")
    public Set<Class<?>> provideControllerClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Controller.class);
    }

    /**
     * 提供所有注解类的映射
     *
     * @return Map<String, Set<Class<?>>> 注解类型到类集合的映射
     */
    @Provides
    @Singleton
    public Map<String, Set<Class<?>>> provideAnnotatedClassesMap() {
        Map<String, Set<Class<?>>> annotatedClassesMap = new HashMap<>();
        Reflections reflections = ReflectionUtil.getReflections();
        
        annotatedClassesMap.put("Service", reflections.getTypesAnnotatedWith(Service.class));
        annotatedClassesMap.put("Dao", reflections.getTypesAnnotatedWith(Dao.class));
        annotatedClassesMap.put("Component", reflections.getTypesAnnotatedWith(Component.class));
        annotatedClassesMap.put("Repository", reflections.getTypesAnnotatedWith(Repository.class));
        annotatedClassesMap.put("Controller", reflections.getTypesAnnotatedWith(Controller.class));
        
        return annotatedClassesMap;
    }

    /**
     * 提供注解类名称映射
     *
     * @return Map<String, String> 类名到有效名称的映射
     */
    @Provides
    @Singleton
    public Map<String, String> provideAnnotatedClassNamesMap() {
        Map<String, String> classNamesMap = new HashMap<>();
        Reflections reflections = ReflectionUtil.getReflections();
        
        // 扫描所有注解类型
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> daoClasses = reflections.getTypesAnnotatedWith(Dao.class);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> repositoryClasses = reflections.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);
        
        // 为每个类生成有效名称
        serviceClasses.forEach(clazz -> {
            String effectiveName = AnnotationNameGenerator.getEffectiveName(clazz);
            classNamesMap.put(clazz.getName(), effectiveName);
        });
        
        daoClasses.forEach(clazz -> {
            String effectiveName = AnnotationNameGenerator.getEffectiveName(clazz);
            classNamesMap.put(clazz.getName(), effectiveName);
        });
        
        componentClasses.forEach(clazz -> {
            String effectiveName = AnnotationNameGenerator.getEffectiveName(clazz);
            classNamesMap.put(clazz.getName(), effectiveName);
        });
        
        repositoryClasses.forEach(clazz -> {
            String effectiveName = AnnotationNameGenerator.getEffectiveName(clazz);
            classNamesMap.put(clazz.getName(), effectiveName);
        });
        
        controllerClasses.forEach(clazz -> {
            String effectiveName = AnnotationNameGenerator.getEffectiveName(clazz);
            classNamesMap.put(clazz.getName(), effectiveName);
        });
        
        return classNamesMap;
    }

}
