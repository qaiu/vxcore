package cn.qaiu.demo.di.service;

import cn.qaiu.vx.core.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * 消息服务实现
 */
@Service
@Singleton
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final String instanceId;

    public MessageServiceImpl() {
        this.instanceId = "MessageServiceImpl@" + Integer.toHexString(System.identityHashCode(this));
        log.info("MessageServiceImpl created: {}", instanceId);
    }

    @Override
    public String getMessage(String key) {
        String msg = "Message for '" + key + "' [instance=" + instanceId + "]";
        log.info("getMessage: {}", msg);
        return msg;
    }
}
