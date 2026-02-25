package cn.qaiu.demo.di.service;

import cn.qaiu.vx.core.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * 问候服务 - 具体类
 * 
 * 验证 07-di-framework.md 中的 @Service 注解
 * 文档描述: 标注 @Service 的类会被框架自动扫描和注册
 * 
 * 同时标注 @Singleton 验证单例管理是否生效
 */
@Service
@Singleton
public class GreetingService {

    private static final Logger log = LoggerFactory.getLogger(GreetingService.class);

    private final String instanceId;
    private int callCount = 0;

    public GreetingService() {
        this.instanceId = "GreetingService@" + Integer.toHexString(System.identityHashCode(this));
        log.info("GreetingService created: {}", instanceId);
    }

    public String greet(String name) {
        callCount++;
        String msg = "Hello, " + (name != null ? name : "World") + "! [instance=" + instanceId + ", calls=" + callCount + "]";
        log.info("greet called: {}", msg);
        return msg;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getCallCount() {
        return callCount;
    }
}
