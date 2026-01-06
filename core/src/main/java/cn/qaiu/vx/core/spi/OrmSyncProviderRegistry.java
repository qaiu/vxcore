package cn.qaiu.vx.core.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ORM同步提供者注册表
 * 使用ServiceLoader发现并管理OrmSyncProvider实现
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class OrmSyncProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrmSyncProviderRegistry.class);

    private static final OrmSyncProviderRegistry INSTANCE = new OrmSyncProviderRegistry();

    private final List<OrmSyncProvider> providers = new CopyOnWriteArrayList<>();
    private volatile boolean initialized = false;

    private OrmSyncProviderRegistry() {}

    public static OrmSyncProviderRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化，使用SPI发现所有OrmSyncProvider实现
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        LOGGER.info("Discovering OrmSyncProvider implementations via SPI...");

        try {
            ServiceLoader<OrmSyncProvider> loader = ServiceLoader.load(OrmSyncProvider.class);
            
            for (OrmSyncProvider provider : loader) {
                providers.add(provider);
                LOGGER.info("Discovered OrmSyncProvider: {} (priority={})", 
                        provider.getName(), provider.getPriority());
            }

            // 按优先级排序
            providers.sort(Comparator.comparingInt(OrmSyncProvider::getPriority));

            LOGGER.info("Found {} OrmSyncProvider implementation(s)", providers.size());
            initialized = true;

        } catch (Exception e) {
            LOGGER.warn("Failed to discover OrmSyncProvider implementations: {}", e.getMessage());
            initialized = true; // 标记为已初始化，避免重复尝试
        }
    }

    /**
     * 获取所有提供者
     *
     * @return 提供者列表（按优先级排序）
     */
    public List<OrmSyncProvider> getProviders() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableList(providers);
    }

    /**
     * 获取首选提供者（优先级最高的）
     *
     * @return 首选提供者，如果没有则返回null
     */
    public OrmSyncProvider getPreferredProvider() {
        List<OrmSyncProvider> all = getProviders();
        return all.isEmpty() ? null : all.get(0);
    }

    /**
     * 检查是否有可用的提供者
     *
     * @return 是否有提供者
     */
    public boolean hasProvider() {
        return !getProviders().isEmpty();
    }

    /**
     * 手动注册提供者（用于测试或程序化配置）
     *
     * @param provider 提供者实例
     */
    public void registerProvider(OrmSyncProvider provider) {
        providers.add(provider);
        providers.sort(Comparator.comparingInt(OrmSyncProvider::getPriority));
        LOGGER.info("Manually registered OrmSyncProvider: {}", provider.getName());
    }

    /**
     * 清除所有提供者（用于测试）
     */
    public void clear() {
        providers.clear();
        initialized = false;
    }
}
