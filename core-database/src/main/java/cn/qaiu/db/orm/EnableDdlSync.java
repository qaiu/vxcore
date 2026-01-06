package cn.qaiu.db.orm;

import java.lang.annotation.*;

/**
 * 启用DDL自动同步注解
 * 在应用主类上添加此注解以启用实体类自动建表/同步功能
 *
 * <p>使用示例：
 * <pre>
 * &#64;App
 * &#64;EnableDdlSync
 * public class AppMain {
 *     public static void main(String[] args) {
 *         VXCoreApplication.run(args);
 *     }
 * }
 * </pre>
 *
 * <p>自定义配置：
 * <pre>
 * &#64;App
 * &#64;EnableDdlSync(
 *     strategy = DdlSyncStrategy.UPDATE,
 *     entityPackages = {"cn.qaiu.example.entity", "cn.qaiu.other.entity"},
 *     failOnError = false
 * )
 * public class AppMain { }
 * </pre>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableDdlSync {
    
    /**
     * DDL同步策略
     *
     * @return 同步策略
     */
    DdlSyncStrategy strategy() default DdlSyncStrategy.AUTO;
    
    /**
     * 实体类所在包路径
     * 为空时使用 @App 注解的 baseScanPackage 或自动检测
     *
     * @return 实体包路径数组
     */
    String[] entityPackages() default {};
    
    /**
     * DDL同步失败时是否抛出异常终止启动
     *
     * @return 是否在错误时失败
     */
    boolean failOnError() default true;
    
    /**
     * 是否打印DDL语句
     *
     * @return 是否打印DDL
     */
    boolean showDdl() default false;
    
    /**
     * 是否在应用启动时执行同步
     * 为false时需要手动调用同步方法
     *
     * @return 是否自动执行
     */
    boolean autoExecute() default true;
    
    /**
     * 指定数据源名称
     * 为空时使用默认/主数据源
     *
     * @return 数据源名称
     */
    String dataSource() default "";
}
