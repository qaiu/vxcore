package cn.qaiu.generator.model;

import cn.qaiu.vx.core.codegen.TableInfo;
import cn.qaiu.generator.config.*;
import io.vertx.core.Future;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码生成上下文
 * 包含生成代码所需的所有配置和状态信息
 * 
 * @author QAIU
 */
public class GeneratorContext {
    
    private final DatabaseConfig databaseConfig;
    private final PackageConfig packageConfig;
    private final TemplateConfig templateConfig;
    private final OutputConfig outputConfig;
    private final FeatureConfig featureConfig;
    
    private final Map<String, Object> customProperties = new ConcurrentHashMap<>();
    
    public GeneratorContext(DatabaseConfig databaseConfig, 
                          PackageConfig packageConfig,
                          TemplateConfig templateConfig,
                          OutputConfig outputConfig,
                          FeatureConfig featureConfig) {
        this.databaseConfig = databaseConfig;
        this.packageConfig = packageConfig;
        this.templateConfig = templateConfig;
        this.outputConfig = outputConfig;
        this.featureConfig = featureConfig;
    }
    
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
    
    public PackageConfig getPackageConfig() {
        return packageConfig;
    }
    
    public TemplateConfig getTemplateConfig() {
        return templateConfig;
    }
    
    public OutputConfig getOutputConfig() {
        return outputConfig;
    }
    
    public FeatureConfig getFeatureConfig() {
        return featureConfig;
    }
    
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }
    
    public void setCustomProperty(String key, Object value) {
        customProperties.put(key, value);
    }
    
    public Object getCustomProperty(String key) {
        return customProperties.get(key);
    }
    
    public String getCustomPropertyAsString(String key) {
        Object value = getCustomProperty(key);
        return value != null ? value.toString() : null;
    }
    
    public String getCustomPropertyAsString(String key, String defaultValue) {
        String value = getCustomPropertyAsString(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 验证上下文配置
     * 
     * @return 验证结果
     */
    public boolean isValid() {
        return databaseConfig != null &&
               packageConfig != null &&
               outputConfig != null;
    }
    
    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private DatabaseConfig databaseConfig;
        private PackageConfig packageConfig;
        private TemplateConfig templateConfig;
        private OutputConfig outputConfig;
        private FeatureConfig featureConfig;
        
        public Builder databaseConfig(DatabaseConfig databaseConfig) {
            this.databaseConfig = databaseConfig;
            return this;
        }
        
        public Builder packageConfig(PackageConfig packageConfig) {
            this.packageConfig = packageConfig;
            return this;
        }
        
        public Builder templateConfig(TemplateConfig templateConfig) {
            this.templateConfig = templateConfig;
            return this;
        }
        
        public Builder outputConfig(OutputConfig outputConfig) {
            this.outputConfig = outputConfig;
            return this;
        }
        
        public Builder featureConfig(FeatureConfig featureConfig) {
            this.featureConfig = featureConfig;
            return this;
        }
        
        public GeneratorContext build() {
            return new GeneratorContext(databaseConfig, packageConfig, templateConfig, outputConfig, featureConfig);
        }
    }
}
