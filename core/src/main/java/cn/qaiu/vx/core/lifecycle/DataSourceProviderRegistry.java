package cn.qaiu.vx.core.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源提供者注册表
 * 使用SPI模式管理数据源提供者，支持动态加载和注册
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceProviderRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceProviderRegistry.class);
    
    private static final DataSourceProviderRegistry INSTANCE = new DataSourceProviderRegistry();
    
    private final Map<String, DataSourceProvider> providers = new ConcurrentHashMap<>();
    private final Map<String, String> typeToProviderMap = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    private DataSourceProviderRegistry() {
        // 私有构造函数，单例模式
    }
    
    /**
     * 获取单例实例
     * 
     * @return DataSourceProviderRegistry实例
     */
    public static DataSourceProviderRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化注册表，加载SPI提供者
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            LOGGER.info("Initializing DataSource provider registry...");
            
            // 使用ServiceLoader加载SPI提供者
            ServiceLoader<DataSourceProvider> serviceLoader = ServiceLoader.load(DataSourceProvider.class);
            
            for (DataSourceProvider provider : serviceLoader) {
                registerProvider(provider);
            }
            
            initialized = true;
            LOGGER.info("DataSource provider registry initialized with {} providers", providers.size());
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize DataSource provider registry", e);
            throw new RuntimeException("Failed to initialize DataSource provider registry", e);
        }
    }
    
    /**
     * 注册数据源提供者
     * 
     * @param provider 数据源提供者
     */
    public void registerProvider(DataSourceProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        
        String name = provider.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
        
        providers.put(name, provider);
        LOGGER.info("Registered DataSource provider: {}", name);
    }
    
    /**
     * 获取数据源提供者
     * 
     * @param name 提供者名称
     * @return 数据源提供者，如果不存在返回null
     */
    public DataSourceProvider getProvider(String name) {
        if (!initialized) {
            initialize();
        }
        return providers.get(name);
    }
    
    /**
     * 根据数据源类型获取提供者
     * 
     * @param type 数据源类型
     * @return 数据源提供者，如果不存在返回null
     */
    public DataSourceProvider getProviderByType(String type) {
        if (!initialized) {
            initialize();
        }
        
        // 首先检查缓存
        String providerName = typeToProviderMap.get(type);
        if (providerName != null) {
            return providers.get(providerName);
        }
        
        // 遍历所有提供者查找支持的类型
        for (DataSourceProvider provider : providers.values()) {
            if (provider.supports(type)) {
                typeToProviderMap.put(type, provider.getName());
                return provider;
            }
        }
        
        return null;
    }
    
    /**
     * 获取所有提供者
     * 
     * @return 提供者列表
     */
    public List<DataSourceProvider> getAllProviders() {
        if (!initialized) {
            initialize();
        }
        return new ArrayList<>(providers.values());
    }
    
    /**
     * 获取所有提供者名称
     * 
     * @return 提供者名称列表
     */
    public Set<String> getProviderNames() {
        if (!initialized) {
            initialize();
        }
        return new HashSet<>(providers.keySet());
    }
    
    /**
     * 检查是否有提供者支持指定类型
     * 
     * @param type 数据源类型
     * @return 是否有提供者支持
     */
    public boolean hasProviderForType(String type) {
        return getProviderByType(type) != null;
    }
    
    /**
     * 重置注册表（主要用于测试）
     */
    public synchronized void reset() {
        providers.clear();
        typeToProviderMap.clear();
        initialized = false;
        LOGGER.info("DataSource provider registry reset");
    }
}
