package cn.qaiu.vx.core.verticle.conf;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;

import java.util.UUID;

/**
 * HTTP代理配置类
 * 用于配置HTTP代理服务器的相关参数
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject
@JsonGen(publicConverter = false)
public class HttpProxyConf {

    /** 默认用户名 */
    public static final String DEFAULT_USERNAME = UUID.randomUUID().toString();

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = UUID.randomUUID().toString();

    /** 默认端口 */
    public static final Integer DEFAULT_PORT = 6402;

    /** 默认超时时间 */
    public static final Integer DEFAULT_TIMEOUT = 15000;

    /** 超时时间 */
    Integer timeout;

    /** 用户名 */
    String username;

    /** 密码 */
    String password;

    /** 端口 */
    Integer port;

    /** 前置代理选项 */
    ProxyOptions preProxyOptions;

    public HttpProxyConf() {
        this.username = DEFAULT_USERNAME;
        this.password = DEFAULT_PASSWORD;
        this.timeout = DEFAULT_PORT;
        this.timeout = DEFAULT_TIMEOUT;
        this.preProxyOptions = new ProxyOptions();
    }

    public HttpProxyConf(JsonObject json) {
        this();
    }


    /**
     * 获取超时时间
     * 
     * @return 超时时间
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * 设置超时时间
     * 
     * @param timeout 超时时间
     * @return 当前对象
     */
    public HttpProxyConf setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * 
     * @param username 用户名
     * @return 当前对象
     */
    public HttpProxyConf setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * 获取密码
     * 
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * 
     * @param password 密码
     * @return 当前对象
     */
    public HttpProxyConf setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * 获取端口
     * 
     * @return 端口
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置端口
     * 
     * @param port 端口
     * @return 当前对象
     */
    public HttpProxyConf setPort(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * 获取前置代理选项
     * 
     * @return 前置代理选项
     */
    public ProxyOptions getPreProxyOptions() {
        return preProxyOptions;
    }

    /**
     * 设置前置代理选项
     * 
     * @param preProxyOptions 前置代理选项
     * @return 当前对象
     */
    public HttpProxyConf setPreProxyOptions(ProxyOptions preProxyOptions) {
        this.preProxyOptions = preProxyOptions;
        return this;
    }
}
