package cn.qaiu.vx.core.util;

/**
 * 配置常量接口
 * 定义框架中使用的配置键名常量
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface ConfigConstant {
    /** 自定义配置键 */
    String CUSTOM = "custom";
    /** Vert.x配置键 */
    String VERTX = "vertx";
    /** 事件循环池大小配置键 */
    String EVENT_LOOP_POOL_SIZE = "eventLoopPoolSize";
    /** 本地配置键 */
    String LOCAL = "local";
    /** 服务器配置键 */
    String SERVER = "server";
    /** 缓存配置键 */
    String CACHE = "cache";

    /** 代理服务器配置键 */
    String PROXY_SERVER = "proxy-server";

    /** 代理配置键 */
    String PROXY = "proxy";

    /** 认证配置键 */
    String AUTHS = "auths";
    /** 全局配置键 */
    String GLOBAL_CONFIG = "globalConfig";
    /** 自定义配置键 */
    String CUSTOM_CONFIG = "customConfig";
    /** 异步服务实例配置键 */
    String ASYNC_SERVICE_INSTANCES = "asyncServiceInstances";
    /** 忽略正则配置键 */
    String IGNORES_REG="ignoresReg";
    /** 基础位置配置键 */
    String BASE_LOCATIONS="baseLocations";

    /** 路由超时配置键 */
    String ROUTE_TIME_OUT="routeTimeOut";
}
