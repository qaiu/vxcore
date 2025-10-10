package cn.qaiu.generator.builder;

import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Service 层构建器
 * 生成业务逻辑接口和实现类
 * 
 * @author QAIU
 */
public class ServiceBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public ServiceBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建 Service 接口信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return Service 接口信息
     */
    public EntityInfo buildServiceInterface(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building Service interface for table: {}", tableInfo.getTableName());
        
        EntityInfo serviceInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "Service";
        serviceInfo.setClassName(className);
        serviceInfo.setTableName(tableInfo.getTableName());
        serviceInfo.setDescription(entityInfo.getDescription() + "业务服务接口");
        serviceInfo.setPackageName(packageConfig.getServicePackage());
        
        // 构建导入语句
        Set<String> imports = buildImports(entityInfo);
        serviceInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("Service interface built successfully: {}", className);
        return serviceInfo;
    }
    
    /**
     * 构建 Service 实现类信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return Service 实现类信息
     */
    public EntityInfo buildServiceImplementation(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building Service implementation for table: {}", tableInfo.getTableName());
        
        EntityInfo serviceImplInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "ServiceImpl";
        serviceImplInfo.setClassName(className);
        serviceImplInfo.setTableName(tableInfo.getTableName());
        serviceImplInfo.setDescription(entityInfo.getDescription() + "业务服务实现");
        serviceImplInfo.setPackageName(packageConfig.getServicePackage() + ".impl");
        
        // 构建导入语句
        Set<String> imports = buildImplementationImports(entityInfo);
        serviceImplInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("Service implementation built successfully: {}", className);
        return serviceImplInfo;
    }
    
    /**
     * 构建 Service 接口导入语句
     */
    private Set<String> buildImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // DAO 类导入
        String daoClassName = entityInfo.getClassName() + "Dao";
        imports.add(packageConfig.getDaoPackage() + "." + daoClassName);
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("java.util.List");
        imports.add("java.util.Optional");
        
        return imports;
    }
    
    /**
     * 构建 Service 实现类导入语句
     */
    private Set<String> buildImplementationImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // DAO 类导入
        String daoClassName = entityInfo.getClassName() + "Dao";
        imports.add(packageConfig.getDaoPackage() + "." + daoClassName);
        
        // Service 接口导入
        String serviceClassName = entityInfo.getClassName() + "Service";
        imports.add(packageConfig.getServicePackage() + "." + serviceClassName);
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("java.util.List");
        imports.add("java.util.Optional");
        
        return imports;
    }
    
    /**
     * 获取 Service 接口类名
     */
    public String getServiceInterfaceClassName(String entityClassName) {
        return entityClassName + "Service";
    }
    
    /**
     * 获取 Service 实现类名
     */
    public String getServiceImplementationClassName(String entityClassName) {
        return entityClassName + "ServiceImpl";
    }
    
    /**
     * 获取 Service 包名
     */
    public String getServicePackageName() {
        return packageConfig.getServicePackage();
    }
    
    /**
     * 获取 Service 实现类包名
     */
    public String getServiceImplementationPackageName() {
        return packageConfig.getServicePackage() + ".impl";
    }
    
    /**
     * 检查是否需要生成 Service
     */
    public boolean shouldGenerateService() {
        return featureConfig.isGenerateService();
    }
}
