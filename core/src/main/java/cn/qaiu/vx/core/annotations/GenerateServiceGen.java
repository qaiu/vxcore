package cn.qaiu.vx.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateServiceGen {
    /**
     * The ID type for the entity, defaults to Long
     */
    Class<?> idType() default Long.class;
    
    /**
     * Whether to generate the ProxyGen interface
     */
    boolean generateProxy() default true;
    
    /**
     * Base package for generated classes
     */
    String basePackage() default "";
    
    /**
     * Additional methods to include, comma-separated
     */
    String extraMethods() default "";
    
    /**
     * Reference interface for entity classes to generate methods from
     * When annotated on entity classes, this specifies which generic interface
     * methods should be generated (e.g., JooqDao.class)
     */
    Class<?> referenceInterface() default Void.class;
}
