package cn.qaiu.demo.di.service;

import cn.qaiu.vx.core.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * 计数器服务 - 具体类 (用于构造函数注入测试)
 * 
 * 用于验证构造函数 @Inject 注入是否工作
 */
@Service
@Singleton
public class CounterService {

    private static final Logger log = LoggerFactory.getLogger(CounterService.class);

    private final String instanceId;
    private int counter = 0;

    public CounterService() {
        this.instanceId = "CounterService@" + Integer.toHexString(System.identityHashCode(this));
        log.info("CounterService created: {}", instanceId);
    }

    public int increment() {
        counter++;
        log.info("increment: counter={}, instance={}", counter, instanceId);
        return counter;
    }

    public int getCounter() {
        return counter;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
