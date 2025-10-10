package cn.qaiu.vx.core.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 包信息
 * 封装包结构的元数据信息
 * 
 * @author QAIU
 */
public class PackageInfo {
    
    private String basePackage;
    private String entityPackage;
    private String daoPackage;
    private String servicePackage;
    private String controllerPackage;
    private String configPackage;
    private String utilPackage;
    private String exceptionPackage;
    private List<String> imports = new ArrayList<>();
    
    public PackageInfo() {
    }
    
    public PackageInfo(String basePackage) {
        this.basePackage = basePackage;
        initDefaultPackages();
    }
    
    /**
     * 初始化默认包结构
     */
    private void initDefaultPackages() {
        if (basePackage != null && !basePackage.trim().isEmpty()) {
            this.entityPackage = basePackage + ".entity";
            this.daoPackage = basePackage + ".dao";
            this.servicePackage = basePackage + ".service";
            this.controllerPackage = basePackage + ".controller";
            this.configPackage = basePackage + ".config";
            this.utilPackage = basePackage + ".util";
            this.exceptionPackage = basePackage + ".exception";
        }
    }
    
    public String getBasePackage() {
        return basePackage;
    }
    
    /**
     * 获取包名（与basePackage相同）
     * 
     * @return 包名
     */
    public String getPackageName() {
        return basePackage;
    }
    
    public PackageInfo setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    
    public String getEntityPackage() {
        return entityPackage;
    }
    
    public PackageInfo setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
        return this;
    }
    
    public String getDaoPackage() {
        return daoPackage;
    }
    
    public PackageInfo setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
        return this;
    }
    
    public String getServicePackage() {
        return servicePackage;
    }
    
    public PackageInfo setServicePackage(String servicePackage) {
        this.servicePackage = servicePackage;
        return this;
    }
    
    public String getControllerPackage() {
        return controllerPackage;
    }
    
    public PackageInfo setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
        return this;
    }
    
    public String getConfigPackage() {
        return configPackage;
    }
    
    public PackageInfo setConfigPackage(String configPackage) {
        this.configPackage = configPackage;
        return this;
    }
    
    public String getUtilPackage() {
        return utilPackage;
    }
    
    public PackageInfo setUtilPackage(String utilPackage) {
        this.utilPackage = utilPackage;
        return this;
    }
    
    public String getExceptionPackage() {
        return exceptionPackage;
    }
    
    public PackageInfo setExceptionPackage(String exceptionPackage) {
        this.exceptionPackage = exceptionPackage;
        return this;
    }
    
    public List<String> getImports() {
        return imports;
    }
    
    public PackageInfo setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }
    
    public PackageInfo addImport(String importClass) {
        if (!this.imports.contains(importClass)) {
            this.imports.add(importClass);
        }
        return this;
    }
    
    /**
     * 获取实体包路径
     * 
     * @return 实体包路径
     */
    public String getEntityPackagePath() {
        return entityPackage != null ? entityPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取DAO包路径
     * 
     * @return DAO包路径
     */
    public String getDaoPackagePath() {
        return daoPackage != null ? daoPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取服务包路径
     * 
     * @return 服务包路径
     */
    public String getServicePackagePath() {
        return servicePackage != null ? servicePackage.replace('.', '/') : null;
    }
    
    /**
     * 获取控制器包路径
     * 
     * @return 控制器包路径
     */
    public String getControllerPackagePath() {
        return controllerPackage != null ? controllerPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取配置包路径
     * 
     * @return 配置包路径
     */
    public String getConfigPackagePath() {
        return configPackage != null ? configPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取工具包路径
     * 
     * @return 工具包路径
     */
    public String getUtilPackagePath() {
        return utilPackage != null ? utilPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取异常包路径
     * 
     * @return 异常包路径
     */
    public String getExceptionPackagePath() {
        return exceptionPackage != null ? exceptionPackage.replace('.', '/') : null;
    }
    
    /**
     * 获取基础包路径
     * 
     * @return 基础包路径
     */
    public String getBasePackagePath() {
        return basePackage != null ? basePackage.replace('.', '/') : null;
    }
    
    /**
     * 获取所有包路径
     * 
     * @return 包路径列表
     */
    public List<String> getAllPackagePaths() {
        List<String> paths = new ArrayList<>();
        
        if (basePackage != null) {
            paths.add(basePackage);
        }
        if (entityPackage != null) {
            paths.add(entityPackage);
        }
        if (daoPackage != null) {
            paths.add(daoPackage);
        }
        if (servicePackage != null) {
            paths.add(servicePackage);
        }
        if (controllerPackage != null) {
            paths.add(controllerPackage);
        }
        if (configPackage != null) {
            paths.add(configPackage);
        }
        if (utilPackage != null) {
            paths.add(utilPackage);
        }
        if (exceptionPackage != null) {
            paths.add(exceptionPackage);
        }
        
        return paths;
    }
    
    /**
     * 检查包结构是否完整
     * 
     * @return 是否完整
     */
    public boolean isComplete() {
        return basePackage != null && !basePackage.trim().isEmpty() &&
               entityPackage != null && !entityPackage.trim().isEmpty() &&
               daoPackage != null && !daoPackage.trim().isEmpty() &&
               servicePackage != null && !servicePackage.trim().isEmpty() &&
               controllerPackage != null && !controllerPackage.trim().isEmpty();
    }
    
    /**
     * 获取包数量
     * 
     * @return 包数量
     */
    public int getPackageCount() {
        int count = 0;
        if (basePackage != null) count++;
        if (entityPackage != null) count++;
        if (daoPackage != null) count++;
        if (servicePackage != null) count++;
        if (controllerPackage != null) count++;
        if (configPackage != null) count++;
        if (utilPackage != null) count++;
        if (exceptionPackage != null) count++;
        return count;
    }
    
    @Override
    public String toString() {
        return "PackageInfo{" +
                "basePackage='" + basePackage + '\'' +
                ", entityPackage='" + entityPackage + '\'' +
                ", daoPackage='" + daoPackage + '\'' +
                ", servicePackage='" + servicePackage + '\'' +
                ", controllerPackage='" + controllerPackage + '\'' +
                ", configPackage='" + configPackage + '\'' +
                ", utilPackage='" + utilPackage + '\'' +
                ", exceptionPackage='" + exceptionPackage + '\'' +
                ", imports=" + imports.size() +
                '}';
    }
}
