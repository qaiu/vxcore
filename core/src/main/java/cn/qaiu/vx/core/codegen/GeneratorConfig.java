package cn.qaiu.vx.core.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器配置
 * 
 * @author QAIU
 */
public class GeneratorConfig {
    
    private String templatePath;
    private String outputPath;
    private String packageName;
    private String className;
    private String tableName;
    private String description;
    private boolean overwriteExisting = false;
    private boolean generateComments = true;
    private boolean generateToString = true;
    private boolean generateEquals = true;
    private boolean generateHashCode = true;
    private boolean generateGetters = true;
    private boolean generateSetters = true;
    private boolean generateConstructors = true;
    private String encoding = "UTF-8";
    private Map<String, Object> customProperties = new HashMap<>();
    
    public GeneratorConfig() {
    }
    
    public GeneratorConfig(String outputPath, String packageName) {
        this.outputPath = outputPath;
        this.packageName = packageName;
    }
    
    public String getTemplatePath() {
        return templatePath;
    }
    
    public GeneratorConfig setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public GeneratorConfig setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public GeneratorConfig setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }
    
    public String getClassName() {
        return className;
    }
    
    public GeneratorConfig setClassName(String className) {
        this.className = className;
        return this;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public GeneratorConfig setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public GeneratorConfig setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public GeneratorConfig setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
        return this;
    }
    
    public boolean isGenerateComments() {
        return generateComments;
    }
    
    public GeneratorConfig setGenerateComments(boolean generateComments) {
        this.generateComments = generateComments;
        return this;
    }
    
    public boolean isGenerateToString() {
        return generateToString;
    }
    
    public GeneratorConfig setGenerateToString(boolean generateToString) {
        this.generateToString = generateToString;
        return this;
    }
    
    public boolean isGenerateEquals() {
        return generateEquals;
    }
    
    public GeneratorConfig setGenerateEquals(boolean generateEquals) {
        this.generateEquals = generateEquals;
        return this;
    }
    
    public boolean isGenerateHashCode() {
        return generateHashCode;
    }
    
    public GeneratorConfig setGenerateHashCode(boolean generateHashCode) {
        this.generateHashCode = generateHashCode;
        return this;
    }
    
    public boolean isGenerateGetters() {
        return generateGetters;
    }
    
    public GeneratorConfig setGenerateGetters(boolean generateGetters) {
        this.generateGetters = generateGetters;
        return this;
    }
    
    public boolean isGenerateSetters() {
        return generateSetters;
    }
    
    public GeneratorConfig setGenerateSetters(boolean generateSetters) {
        this.generateSetters = generateSetters;
        return this;
    }
    
    public boolean isGenerateConstructors() {
        return generateConstructors;
    }
    
    public GeneratorConfig setGenerateConstructors(boolean generateConstructors) {
        this.generateConstructors = generateConstructors;
        return this;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public GeneratorConfig setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }
    
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }
    
    public GeneratorConfig setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
        return this;
    }
    
    public GeneratorConfig addCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }
    
    public Object getCustomProperty(String key) {
        return this.customProperties.get(key);
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
     * 获取输出目录
     * 
     * @return 输出目录
     */
    public String getOutputDirectory() {
        if (outputPath == null) {
            return null;
        }
        
        int lastSlash = outputPath.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = outputPath.lastIndexOf('\\');
        }
        
        if (lastSlash != -1) {
            return outputPath.substring(0, lastSlash);
        }
        
        return ".";
    }
    
    /**
     * 获取输出文件名
     * 
     * @return 输出文件名
     */
    public String getOutputFileName() {
        if (outputPath == null) {
            return null;
        }
        
        int lastSlash = outputPath.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = outputPath.lastIndexOf('\\');
        }
        
        if (lastSlash != -1) {
            return outputPath.substring(lastSlash + 1);
        }
        
        return outputPath;
    }
    
    /**
     * 获取包路径
     * 
     * @return 包路径
     */
    public String getPackagePath() {
        if (packageName == null) {
            return null;
        }
        
        return packageName.replace('.', '/');
    }
    
    /**
     * 验证配置
     * 
     * @return 验证结果
     */
    public boolean isValid() {
        return outputPath != null && !outputPath.trim().isEmpty() &&
               packageName != null && !packageName.trim().isEmpty();
    }
    
    /**
     * 创建副本
     * 
     * @return 配置副本
     */
    public GeneratorConfig copy() {
        GeneratorConfig copy = new GeneratorConfig();
        copy.templatePath = this.templatePath;
        copy.outputPath = this.outputPath;
        copy.packageName = this.packageName;
        copy.className = this.className;
        copy.tableName = this.tableName;
        copy.description = this.description;
        copy.overwriteExisting = this.overwriteExisting;
        copy.generateComments = this.generateComments;
        copy.generateToString = this.generateToString;
        copy.generateEquals = this.generateEquals;
        copy.generateHashCode = this.generateHashCode;
        copy.generateGetters = this.generateGetters;
        copy.generateSetters = this.generateSetters;
        copy.generateConstructors = this.generateConstructors;
        copy.encoding = this.encoding;
        copy.customProperties = new HashMap<>(this.customProperties);
        
        return copy;
    }
    
    @Override
    public String toString() {
        return "GeneratorConfig{" +
                "templatePath='" + templatePath + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", tableName='" + tableName + '\'' +
                ", description='" + description + '\'' +
                ", overwriteExisting=" + overwriteExisting +
                ", generateComments=" + generateComments +
                ", generateToString=" + generateToString +
                ", generateEquals=" + generateEquals +
                ", generateHashCode=" + generateHashCode +
                ", generateGetters=" + generateGetters +
                ", generateSetters=" + generateSetters +
                ", generateConstructors=" + generateConstructors +
                ", encoding='" + encoding + '\'' +
                ", customProperties=" + customProperties +
                '}';
    }
}
