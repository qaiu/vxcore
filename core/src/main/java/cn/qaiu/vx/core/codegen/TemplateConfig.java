package cn.qaiu.vx.core.codegen;

/**
 * 模板配置
 * 定义单个模板的配置信息
 * 
 * @author QAIU
 */
public class TemplateConfig {
    
    private String templateName;
    private String outputPath;
    private String outputFileName;
    private boolean enabled = true;
    private int priority = 0;
    
    public TemplateConfig() {
    }
    
    public TemplateConfig(String templateName, String outputPath) {
        this.templateName = templateName;
        this.outputPath = outputPath;
    }
    
    public TemplateConfig(String templateName, String outputPath, String outputFileName) {
        this.templateName = templateName;
        this.outputPath = outputPath;
        this.outputFileName = outputFileName;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public TemplateConfig setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public TemplateConfig setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }
    
    public String getOutputFileName() {
        return outputFileName;
    }
    
    public TemplateConfig setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
        return this;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public TemplateConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public TemplateConfig setPriority(int priority) {
        this.priority = priority;
        return this;
    }
    
    /**
     * 获取完整的输出路径
     * 
     * @return 完整输出路径
     */
    public String getFullOutputPath() {
        if (outputFileName != null && !outputFileName.trim().isEmpty()) {
            return outputPath + "/" + outputFileName;
        }
        return outputPath;
    }
    
    /**
     * 验证配置
     * 
     * @return 验证结果
     */
    public boolean isValid() {
        return templateName != null && !templateName.trim().isEmpty() &&
               outputPath != null && !outputPath.trim().isEmpty();
    }
    
    /**
     * 创建副本
     * 
     * @return 配置副本
     */
    public TemplateConfig copy() {
        TemplateConfig copy = new TemplateConfig();
        copy.templateName = this.templateName;
        copy.outputPath = this.outputPath;
        copy.outputFileName = this.outputFileName;
        copy.enabled = this.enabled;
        copy.priority = this.priority;
        
        return copy;
    }
    
    @Override
    public String toString() {
        return "TemplateConfig{" +
                "templateName='" + templateName + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", outputFileName='" + outputFileName + '\'' +
                ", enabled=" + enabled +
                ", priority=" + priority +
                '}';
    }
}
