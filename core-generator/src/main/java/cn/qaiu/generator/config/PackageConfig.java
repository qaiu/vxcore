package cn.qaiu.generator.config;

/**
 * 包配置
 * 
 * @author QAIU
 */
public class PackageConfig {
    
    private String basePackage;
    private String entityPackage;
    private String daoPackage;
    private String servicePackage;
    private String controllerPackage;
    private String dtoPackage;
    
    public PackageConfig() {
    }
    
    public PackageConfig(String basePackage) {
        this.basePackage = basePackage;
        this.entityPackage = basePackage + ".entity";
        this.daoPackage = basePackage + ".dao";
        this.servicePackage = basePackage + ".service";
        this.controllerPackage = basePackage + ".controller";
        this.dtoPackage = basePackage + ".dto";
    }
    
    public String getBasePackage() {
        return basePackage;
    }
    
    public PackageConfig setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    
    public String getEntityPackage() {
        return entityPackage;
    }
    
    public PackageConfig setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
        return this;
    }
    
    public String getDaoPackage() {
        return daoPackage;
    }
    
    public PackageConfig setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
        return this;
    }
    
    public String getServicePackage() {
        return servicePackage;
    }
    
    public PackageConfig setServicePackage(String servicePackage) {
        this.servicePackage = servicePackage;
        return this;
    }
    
    public String getControllerPackage() {
        return controllerPackage;
    }
    
    public PackageConfig setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
        return this;
    }
    
    public String getDtoPackage() {
        return dtoPackage;
    }
    
    public PackageConfig setDtoPackage(String dtoPackage) {
        this.dtoPackage = dtoPackage;
        return this;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return basePackage != null && !basePackage.trim().isEmpty();
    }
}