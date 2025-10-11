package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.verticle.ServiceVerticle;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Dagger2组件接口 - 用于依赖注入
 * 专注于核心框架的注解扫描和基础服务提供
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {

    /**
     * 注入ServiceVerticle
     *
     * @param serviceVerticle ServiceVerticle实例
     */
    void inject(ServiceVerticle serviceVerticle);

    // =================== 传统注解类提供 ===================

    /**
     * 获取Service注解的类集合
     *
     * @return 包含@Service注解的类集合
     */
    Set<Class<?>> serviceClasses();

    /**
     * 获取Dao注解的类集合
     *
     * @return 包含@Dao注解的类集合
     */
    @Named("Dao")
    Set<Class<?>> daoClasses();

    /**
     * 获取Component注解的类集合
     *
     * @return 包含@Component注解的类集合
     */
    @Named("Component")
    Set<Class<?>> componentClasses();

    /**
     * 获取Repository注解的类集合
     *
     * @return 包含@Repository注解的类集合
     */
    @Named("Repository")
    Set<Class<?>> repositoryClasses();

    /**
     * 获取Controller注解的类集合
     *
     * @return 包含@Controller注解的类集合
     */
    @Named("Controller")
    Set<Class<?>> controllerClasses();

    /**
     * 获取所有注解类的映射
     *
     * @return 注解类型到类集合的映射
     */
    Map<String, Set<Class<?>>> annotatedClassesMap();

    /**
     * 获取注解类名称映射
     *
     * @return 类名到有效名称的映射
     */
    Map<String, String> annotatedClassNamesMap();

}
