package cn.qaiu.generator.config;

/**
 * 输出配置
 * 
 * @author QAIU
 */
public class OutputConfig {
    
    private String outputPath;
    private String entityOutputPath;
    private String daoOutputPath;
    private String serviceOutputPath;
    private String controllerOutputPath;
    private String dtoOutputPath;
    private boolean overwrite = false;
    private boolean createDirectories = true;
    
    public OutputConfig() {
    }
    
    public OutputConfig(String outputPath) {
        this.outputPath = outputPath;
        this.entityOutputPath = outputPath + "/entity";
        this.daoOutputPath = outputPath + "/dao";
        this.serviceOutputPath = outputPath + "/service";
        this.controllerOutputPath = outputPath + "/controller";
        this.dtoOutputPath = outputPath + "/dto";
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public OutputConfig setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }
    
    public String getEntityOutputPath() {
        return entityOutputPath;
    }
    
    public OutputConfig setEntityOutputPath(String entityOutputPath) {
        this.entityOutputPath = entityOutputPath;
        return this;
    }
    
    public String getDaoOutputPath() {
        return daoOutputPath;
    }
    
    public OutputConfig setDaoOutputPath(String daoOutputPath) {
        this.daoOutputPath = daoOutputPath;
        return this;
    }
    
    public String getServiceOutputPath() {
        return serviceOutputPath;
    }
    
    public OutputConfig setServiceOutputPath(String serviceOutputPath) {
        this.serviceOutputPath = serviceOutputPath;
        return this;
    }
    
    public String getControllerOutputPath() {
        return controllerOutputPath;
    }
    
    public OutputConfig setControllerOutputPath(String controllerOutputPath) {
        this.controllerOutputPath = controllerOutputPath;
        return this;
    }
    
    public String getDtoOutputPath() {
        return dtoOutputPath;
    }
    
    public OutputConfig setDtoOutputPath(String dtoOutputPath) {
        this.dtoOutputPath = dtoOutputPath;
        return this;
    }
    
    public boolean isOverwrite() {
        return overwrite;
    }
    
    public OutputConfig setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }
    
    public boolean isCreateDirectories() {
        return createDirectories;
    }
    
    public OutputConfig setCreateDirectories(boolean createDirectories) {
        this.createDirectories = createDirectories;
        return this;
    }
    
    // 别名方法
    public boolean isOverwriteExisting() {
        return overwrite;
    }
    
    public OutputConfig setOverwriteExisting(boolean overwriteExisting) {
        this.overwrite = overwriteExisting;
        return this;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return outputPath != null && !outputPath.trim().isEmpty();
    }
}