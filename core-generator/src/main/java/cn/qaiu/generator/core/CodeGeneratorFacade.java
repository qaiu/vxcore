package cn.qaiu.generator.core;

import cn.qaiu.generator.builder.*;
import cn.qaiu.generator.config.*;
import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.generator.model.GeneratorContext;
import cn.qaiu.generator.reader.DatabaseMetadataReader;
import cn.qaiu.generator.reader.JdbcMetadataReader;
import cn.qaiu.generator.reader.ConfigMetadataReader;
import cn.qaiu.generator.template.TemplateManager;
import cn.qaiu.vx.core.codegen.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码生成器门面类
 * 提供统一的代码生成入口
 * 
 * @author QAIU
 */
public class CodeGeneratorFacade {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGeneratorFacade.class);
    
    private final Vertx vertx;
    private final GeneratorContext context;
    private final TemplateManager templateManager;
    
    private final EntityBuilder entityBuilder;
    private final DaoBuilder daoBuilder;
    private final ServiceBuilder serviceBuilder;
    private final JServiceBuilder jServiceBuilder;
    private final ControllerBuilder controllerBuilder;
    private final DtoBuilder dtoBuilder;
    
    public CodeGeneratorFacade(Vertx vertx, GeneratorContext context) {
        this.vertx = vertx;
        this.context = context;
        this.templateManager = new TemplateManager(vertx, context.getTemplateConfig());
        
        // 初始化构建器
        this.entityBuilder = new EntityBuilder(context.getFeatureConfig(), context.getPackageConfig());
        this.daoBuilder = new DaoBuilder(context.getFeatureConfig(), context.getPackageConfig());
        this.serviceBuilder = new ServiceBuilder(context.getFeatureConfig(), context.getPackageConfig());
        this.jServiceBuilder = new JServiceBuilder(context.getFeatureConfig(), context.getPackageConfig());
        this.controllerBuilder = new ControllerBuilder(context.getFeatureConfig(), context.getPackageConfig());
        this.dtoBuilder = new DtoBuilder(context.getFeatureConfig(), context.getPackageConfig());
    }
    
    /**
     * 生成所有代码
     * 
     * @param tableNames 表名列表
     * @return 生成结果
     */
    public Future<List<String>> generateAll(List<String> tableNames) {
        return getMetadataReader()
                .compose(reader -> generateAll(reader, tableNames));
    }
    
    /**
     * 生成所有代码
     * 
     * @param reader 元数据读取器
     * @param tableNames 表名列表
     * @return 生成结果
     */
    public Future<List<String>> generateAll(DatabaseMetadataReader reader, List<String> tableNames) {
        return Future.future(promise -> {
            List<String> generatedFiles = new ArrayList<>();
            List<Future<String>> futures = new ArrayList<>();
            
            for (String tableName : tableNames) {
                Future<String> future = generateTable(reader, tableName)
                        .map(result -> {
                            generatedFiles.addAll(result);
                            return tableName;
                        });
                futures.add(future);
            }
            
            Future.all(futures)
                    .onSuccess(v -> {
                        LOGGER.info("Generated {} files for {} tables", generatedFiles.size(), tableNames.size());
                        promise.complete(generatedFiles);
                    })
                    .onFailure(promise::fail);
        });
    }
    
    /**
     * 生成单个表的代码
     * 
     * @param tableName 表名
     * @return 生成的文件列表
     */
    public Future<List<String>> generateTable(String tableName) {
        return getMetadataReader()
                .compose(reader -> generateTable(reader, tableName));
    }
    
    /**
     * 生成单个表的代码
     * 
     * @param reader 元数据读取器
     * @param tableName 表名
     * @return 生成的文件列表
     */
    public Future<List<String>> generateTable(DatabaseMetadataReader reader, String tableName) {
        return reader.readTable(tableName)
                .compose(tableInfo -> {
                    List<String> generatedFiles = new ArrayList<>();
                    List<Future<String>> futures = new ArrayList<>();
                    
                    // 生成实体类
                    if (context.getFeatureConfig().isGenerateEntity()) {
                        Future<String> entityFuture = generateEntity(tableInfo)
                                .map(file -> {
                                    generatedFiles.add(file);
                                    return file;
                                });
                        futures.add(entityFuture);
                    }
                    
                    // 生成 DAO
                    if (context.getFeatureConfig().isGenerateDao()) {
                        Future<String> daoFuture = generateDao(tableInfo)
                                .map(file -> {
                                    generatedFiles.add(file);
                                    return file;
                                });
                        futures.add(daoFuture);
                    }
                    
                    // 生成 Service
                    if (context.getFeatureConfig().isGenerateService()) {
                        Future<String> serviceFuture = generateService(tableInfo)
                                .map(file -> {
                                    generatedFiles.add(file);
                                    return file;
                                });
                        futures.add(serviceFuture);
                    }
                    
                    // 生成 Controller
                    if (context.getFeatureConfig().isGenerateController()) {
                        Future<String> controllerFuture = generateController(tableInfo)
                                .map(file -> {
                                    generatedFiles.add(file);
                                    return file;
                                });
                        futures.add(controllerFuture);
                    }
                    
                    // 生成 DTO
                    if (context.getFeatureConfig().isGenerateDto()) {
                        Future<String> dtoFuture = generateDto(tableInfo)
                                .map(file -> {
                                    generatedFiles.add(file);
                                    return file;
                                });
                        futures.add(dtoFuture);
                    }
                    
                    return Future.all(futures)
                            .map(v -> generatedFiles);
                });
    }
    
    /**
     * 生成实体类
     */
    public Future<String> generateEntity(TableInfo tableInfo) {
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        return generateCode("entity", entityInfo, getEntityOutputPath(entityInfo));
    }
    
    /**
     * 生成 DAO
     */
    public Future<String> generateDao(TableInfo tableInfo) {
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        EntityInfo daoInfo = daoBuilder.buildDao(tableInfo, entityInfo);
        
        DaoStyle daoStyle = context.getFeatureConfig().getDaoStyle();
        String templateName = getDaoTemplateName(daoStyle);
        
        return generateCode(templateName, daoInfo, getDaoOutputPath(daoInfo));
    }
    
    /**
     * 生成 Service
     */
    public Future<String> generateService(TableInfo tableInfo) {
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 根据配置选择使用 JService 还是传统 Service
        if (jServiceBuilder.shouldGenerateJService()) {
            return generateJService(tableInfo, entityInfo);
        } else {
            return generateTraditionalService(tableInfo, entityInfo);
        }
    }
    
    /**
     * 生成 JService
     */
    private Future<String> generateJService(TableInfo tableInfo, EntityInfo entityInfo) {
        // 生成 JService 接口
        EntityInfo serviceInfo = jServiceBuilder.buildJServiceInterface(tableInfo, entityInfo);
        Future<String> interfaceFuture = generateCode(jServiceBuilder.getJServiceInterfaceTemplateName(), serviceInfo, getServiceOutputPath(serviceInfo));
        
        // 生成 JService 实现类
        EntityInfo serviceImplInfo = jServiceBuilder.buildJServiceImplementation(tableInfo, entityInfo);
        Future<String> implFuture = generateCode(jServiceBuilder.getJServiceImplementationTemplateName(), serviceImplInfo, getServiceOutputPath(serviceImplInfo));
        
        return Future.all(interfaceFuture, implFuture)
                .map(v -> interfaceFuture.result());
    }
    
    /**
     * 生成传统 Service
     */
    private Future<String> generateTraditionalService(TableInfo tableInfo, EntityInfo entityInfo) {
        // 生成 Service 接口
        EntityInfo serviceInfo = serviceBuilder.buildServiceInterface(tableInfo, entityInfo);
        Future<String> interfaceFuture = generateCode("service", serviceInfo, getServiceOutputPath(serviceInfo));
        
        // 生成 Service 实现类
        EntityInfo serviceImplInfo = serviceBuilder.buildServiceImplementation(tableInfo, entityInfo);
        Future<String> implFuture = generateCode("service-impl", serviceImplInfo, getServiceOutputPath(serviceImplInfo));
        
        return Future.all(interfaceFuture, implFuture)
                .map(v -> interfaceFuture.result());
    }
    
    /**
     * 生成 Controller
     */
    public Future<String> generateController(TableInfo tableInfo) {
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        EntityInfo controllerInfo = controllerBuilder.buildController(tableInfo, entityInfo);
        return generateCode("controller", controllerInfo, getControllerOutputPath(controllerInfo));
    }
    
    /**
     * 生成 DTO
     */
    public Future<String> generateDto(TableInfo tableInfo) {
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 生成创建请求 DTO
        EntityInfo createDtoInfo = dtoBuilder.buildCreateRequestDto(tableInfo, entityInfo);
        Future<String> createFuture = generateCode("dto-create", createDtoInfo, getDtoOutputPath(createDtoInfo));
        
        // 生成更新请求 DTO
        EntityInfo updateDtoInfo = dtoBuilder.buildUpdateRequestDto(tableInfo, entityInfo);
        Future<String> updateFuture = generateCode("dto-update", updateDtoInfo, getDtoOutputPath(updateDtoInfo));
        
        // 生成响应 DTO
        EntityInfo responseDtoInfo = dtoBuilder.buildResponseDto(tableInfo, entityInfo);
        Future<String> responseFuture = generateCode("dto-response", responseDtoInfo, getDtoOutputPath(responseDtoInfo));
        
        // 生成 DTO 转换器
        EntityInfo converterInfo = dtoBuilder.buildDtoConverter(tableInfo, entityInfo);
        Future<String> converterFuture = generateCode("dto-converter", converterInfo, getDtoOutputPath(converterInfo));
        
        return Future.all(createFuture, updateFuture, responseFuture, converterFuture)
                .map(v -> createFuture.result());
    }
    
    /**
     * 生成代码
     */
    private Future<String> generateCode(String templateName, EntityInfo entityInfo, String outputPath) {
        return templateManager.generate(templateName, entityInfo, outputPath);
    }
    
    /**
     * 获取元数据读取器
     */
    private Future<DatabaseMetadataReader> getMetadataReader() {
        return Future.future(promise -> {
            try {
                DatabaseMetadataReader reader;
                
                if (context.getDatabaseConfig() != null) {
                    // 使用数据库连接
                    DatabaseConfig dbConfig = context.getDatabaseConfig();
                    reader = new JdbcMetadataReader(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
                } else {
                    // 使用配置文件
                    String configPath = context.getCustomPropertyAsString("configPath");
                    if (configPath == null) {
                        promise.fail(new IllegalArgumentException("No database config or config file specified"));
                        return;
                    }
                    reader = new ConfigMetadataReader(configPath);
                }
                
                promise.complete(reader);
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }
    
    /**
     * 获取 DAO 模板名称
     */
    private String getDaoTemplateName(DaoStyle daoStyle) {
        switch (daoStyle) {
            case VERTX_SQL:
                return "dao-vertx";
            case JOOQ:
                return "dao-jooq";
            case LAMBDA:
            default:
                return "dao";
        }
    }
    
    /**
     * 获取实体类输出路径
     */
    private String getEntityOutputPath(EntityInfo entityInfo) {
        String packagePath = entityInfo.getPackageName().replace('.', '/');
        return Paths.get(context.getOutputConfig().getOutputPath(), packagePath, entityInfo.getClassName() + ".java").toString();
    }
    
    /**
     * 获取 DAO 输出路径
     */
    private String getDaoOutputPath(EntityInfo daoInfo) {
        String packagePath = daoInfo.getPackageName().replace('.', '/');
        return Paths.get(context.getOutputConfig().getOutputPath(), packagePath, daoInfo.getClassName() + ".java").toString();
    }
    
    /**
     * 获取 Service 输出路径
     */
    private String getServiceOutputPath(EntityInfo serviceInfo) {
        String packagePath = serviceInfo.getPackageName().replace('.', '/');
        return Paths.get(context.getOutputConfig().getOutputPath(), packagePath, serviceInfo.getClassName() + ".java").toString();
    }
    
    /**
     * 获取 Controller 输出路径
     */
    private String getControllerOutputPath(EntityInfo controllerInfo) {
        String packagePath = controllerInfo.getPackageName().replace('.', '/');
        return Paths.get(context.getOutputConfig().getOutputPath(), packagePath, controllerInfo.getClassName() + ".java").toString();
    }
    
    /**
     * 获取 DTO 输出路径
     */
    private String getDtoOutputPath(EntityInfo dtoInfo) {
        String packagePath = dtoInfo.getPackageName().replace('.', '/');
        return Paths.get(context.getOutputConfig().getOutputPath(), packagePath, dtoInfo.getClassName() + ".java").toString();
    }
    
    /**
     * 关闭资源
     */
    public Future<Void> close() {
        return Future.succeededFuture();
    }
}
