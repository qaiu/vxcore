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
 * JService 层构建器
 * 生成基于 JService 的业务逻辑接口和实现类
 * 
 * @author QAIU
 */
public class JServiceBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JServiceBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public JServiceBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建 JService 接口信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return JService 接口信息
     */
    public EntityInfo buildJServiceInterface(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building JService interface for table: {}", tableInfo.getTableName());
        
        EntityInfo serviceInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "Service";
        serviceInfo.setClassName(className);
        serviceInfo.setTableName(tableInfo.getTableName());
        serviceInfo.setDescription(entityInfo.getDescription() + "业务服务接口");
        serviceInfo.setPackageName(packageConfig.getServicePackage());
        
        // 构建导入语句
        Set<String> imports = buildJServiceImports(entityInfo);
        serviceInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("JService interface built successfully: {}", className);
        return serviceInfo;
    }
    
    /**
     * 构建 JService 实现类信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return JService 实现类信息
     */
    public EntityInfo buildJServiceImplementation(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building JService implementation for table: {}", tableInfo.getTableName());
        
        EntityInfo serviceImplInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "ServiceImpl";
        serviceImplInfo.setClassName(className);
        serviceImplInfo.setTableName(tableInfo.getTableName());
        serviceImplInfo.setDescription(entityInfo.getDescription() + "业务服务实现");
        serviceImplInfo.setPackageName(packageConfig.getServicePackage() + ".impl");
        
        // 构建导入语句
        Set<String> imports = buildJServiceImplementationImports(entityInfo);
        serviceImplInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("JService implementation built successfully: {}", className);
        return serviceImplInfo;
    }
    
    /**
     * 构建 JService 接口导入语句
     */
    private Set<String> buildJServiceImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // JService 导入
        imports.add("cn.qaiu.db.dsl.lambda.JService");
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("java.util.List");
        
        return imports;
    }
    
    /**
     * 构建 JService 实现类导入语句
     */
    private Set<String> buildJServiceImplementationImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // JService 相关导入
        imports.add("cn.qaiu.db.dsl.core.JooqExecutor");
        imports.add("cn.qaiu.db.dsl.lambda.JServiceImpl");
        imports.add("cn.qaiu.db.dsl.lambda.LambdaPageResult");
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // Service 接口导入
        String serviceClassName = entityInfo.getClassName() + "Service";
        imports.add(packageConfig.getServicePackage() + "." + serviceClassName);
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("java.util.List");
        imports.add("org.slf4j.Logger");
        imports.add("org.slf4j.LoggerFactory");
        
        return imports;
    }
    
    /**
     * 获取 JService 接口类名
     */
    public String getJServiceInterfaceClassName(String entityClassName) {
        return entityClassName + "Service";
    }
    
    /**
     * 获取 JService 实现类名
     */
    public String getJServiceImplementationClassName(String entityClassName) {
        return entityClassName + "ServiceImpl";
    }
    
    /**
     * 获取 JService 包名
     */
    public String getJServicePackageName() {
        return packageConfig.getServicePackage();
    }
    
    /**
     * 获取 JService 实现类包名
     */
    public String getJServiceImplementationPackageName() {
        return packageConfig.getServicePackage() + ".impl";
    }
    
    /**
     * 检查是否需要生成 JService
     */
    public boolean shouldGenerateJService() {
        return featureConfig.isGenerateService() && featureConfig.isUseJService();
    }
    
    /**
     * 获取 JService 接口模板名称
     */
    public String getJServiceInterfaceTemplateName() {
        return "service-jservice.ftl";
    }
    
    /**
     * 获取 JService 实现类模板名称
     */
    public String getJServiceImplementationTemplateName() {
        return "service-impl-jservice.ftl";
    }
}
