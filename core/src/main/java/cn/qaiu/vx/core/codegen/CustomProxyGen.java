package cn.qaiu.vx.core.codegen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义代理生成注解 用于生成Service代理类，支持接口继承树分析
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CustomProxyGen {

  /** 代理类名称后缀 默认为"VertxEBProxy" */
  String value() default "VertxEBProxy";
}
