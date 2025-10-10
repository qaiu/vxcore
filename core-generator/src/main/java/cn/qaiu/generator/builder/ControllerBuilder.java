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
 * Controller 层构建器
 * 生成 RESTful API 控制器
 * 
 * @author QAIU
 */
public class ControllerBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public ControllerBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建 Controller 信息
     * 
     * @param tableInfo 表信息
     * @param entityInfo 实体信息
     * @return Controller 信息
     */
    public EntityInfo buildController(TableInfo tableInfo, EntityInfo entityInfo) {
        LOGGER.info("Building Controller for table: {}", tableInfo.getTableName());
        
        EntityInfo controllerInfo = new EntityInfo();
        
        // 设置基本信息
        String className = entityInfo.getClassName() + "Controller";
        controllerInfo.setClassName(className);
        controllerInfo.setTableName(tableInfo.getTableName());
        controllerInfo.setDescription(entityInfo.getDescription() + "控制器");
        controllerInfo.setPackageName(packageConfig.getControllerPackage());
        
        // 构建导入语句
        Set<String> imports = buildImports(entityInfo);
        controllerInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("Controller built successfully: {}", className);
        return controllerInfo;
    }
    
    /**
     * 构建导入语句
     */
    private Set<String> buildImports(EntityInfo entityInfo) {
        Set<String> imports = new HashSet<>();
        
        // 实体类导入
        imports.add(entityInfo.getFullClassName());
        
        // Service 类导入
        String serviceClassName = entityInfo.getClassName() + "Service";
        imports.add(packageConfig.getServicePackage() + "." + serviceClassName);
        
        // Core 模块注解导入
        imports.add("cn.qaiu.vx.core.annotaions.RouteHandler");
        imports.add("cn.qaiu.vx.core.annotaions.RouteMapping");
        imports.add("cn.qaiu.vx.core.annotaions.RouteMethod");
        imports.add("cn.qaiu.vx.core.annotaions.param.RequestParam");
        imports.add("cn.qaiu.vx.core.annotaions.param.PathVariable");
        imports.add("cn.qaiu.vx.core.annotaions.param.RequestBody");
        imports.add("cn.qaiu.vx.core.util.JsonResult");
        
        // 基础导入
        imports.add("io.vertx.core.Future");
        imports.add("io.vertx.core.json.JsonObject");
        imports.add("java.util.List");
        imports.add("java.util.Optional");
        
        // 如果启用 DTO，添加 DTO 导入
        if (featureConfig.isGenerateDto()) {
            String dtoClassName = entityInfo.getClassName() + "Dto";
            imports.add(packageConfig.getDtoPackage() + "." + dtoClassName);
        }
        
        // 如果启用参数校验，添加校验注解导入
        if (featureConfig.isGenerateValidation()) {
            imports.add("jakarta.validation.Valid");
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
        }
        
        return imports;
    }
    
    /**
     * 获取 Controller 类名
     */
    public String getControllerClassName(String entityClassName) {
        return entityClassName + "Controller";
    }
    
    /**
     * 获取 Controller 包名
     */
    public String getControllerPackageName() {
        return packageConfig.getControllerPackage();
    }
    
    /**
     * 获取 Controller 完整类名
     */
    public String getControllerFullClassName(String entityClassName) {
        return packageConfig.getControllerPackage() + "." + getControllerClassName(entityClassName);
    }
    
    /**
     * 检查是否需要生成 Controller
     */
    public boolean shouldGenerateController() {
        return featureConfig.isGenerateController();
    }
}
