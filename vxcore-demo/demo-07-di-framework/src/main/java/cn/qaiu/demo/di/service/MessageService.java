package cn.qaiu.demo.di.service;

/**
 * 消息服务接口
 * 
 * 用于验证接口类型的 @Inject 注入
 * 框架对接口类型使用 AsyncServiceUtil.getAsyncServiceInstance() (EventBus代理)
 * 对具体类使用 ReflectionUtil.newWithNoParam()
 * 
 * 预期: 接口注入可能失败，因为没有在 EventBus 上注册此服务
 */
public interface MessageService {

    String getMessage(String key);
}
