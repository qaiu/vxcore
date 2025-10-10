package cn.qaiu.generator.template;

import cn.qaiu.generator.config.TemplateConfig;
import cn.qaiu.vx.core.codegen.CodeGenerator;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.GeneratorConfig;
import cn.qaiu.vx.core.codegen.TemplateContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板管理器
 * 负责模板的加载、缓存和代码生成
 * 
 * @author QAIU
 */
public class TemplateManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManager.class);
    
    private final Vertx vertx;
    private final TemplateConfig templateConfig;
    private final CodeGenerator codeGenerator;
    
    public TemplateManager(Vertx vertx, TemplateConfig templateConfig) {
        this.vertx = vertx;
        this.templateConfig = templateConfig;
        
        // 创建代码生成器配置
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setTemplatePath(templateConfig.getTemplatePath());
        generatorConfig.setGenerateComments(true);
        generatorConfig.setGenerateGetters(true);
        generatorConfig.setGenerateSetters(true);
        generatorConfig.setGenerateConstructors(true);
        generatorConfig.setGenerateEquals(true);
        generatorConfig.setGenerateHashCode(true);
        generatorConfig.setGenerateToString(true);
        
        this.codeGenerator = new CodeGenerator(vertx, generatorConfig);
    }
    
    /**
     * 生成代码
     * 
     * @param templateName 模板名称
     * @param entityInfo 实体信息
     * @param outputPath 输出路径
     * @return 生成的文件路径
     */
    public Future<String> generate(String templateName, EntityInfo entityInfo, String outputPath) {
        return Future.future(promise -> {
            try {
                // 创建输出目录
                Path outputDir = Paths.get(outputPath).getParent();
                if (outputDir != null && !Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);
                }
                
                // 构建模板上下文
                TemplateContext context = buildTemplateContext(entityInfo);
                
                // 生成代码
                codeGenerator.generate(templateName, context, outputPath)
                        .onSuccess(result -> {
                            LOGGER.info("Code generated successfully: {} -> {}", templateName, outputPath);
                            promise.complete(result);
                        })
                        .onFailure(error -> {
                            LOGGER.error("Failed to generate code: template={}, output={}", templateName, outputPath, error);
                            promise.fail(error);
                        });
                
            } catch (Exception e) {
                LOGGER.error("Failed to generate code: template={}, output={}", templateName, outputPath, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 生成代码到字符串
     * 
     * @param templateName 模板名称
     * @param entityInfo 实体信息
     * @return 生成的代码字符串
     */
    public Future<String> generateToString(String templateName, EntityInfo entityInfo) {
        return Future.future(promise -> {
            try {
                // 构建模板上下文
                TemplateContext context = buildTemplateContext(entityInfo);
                
                // 生成代码
                codeGenerator.generateToString(templateName, context)
                        .onSuccess(result -> {
                            LOGGER.info("Code generated to string successfully: {}", templateName);
                            promise.complete(result);
                        })
                        .onFailure(error -> {
                            LOGGER.error("Failed to generate code to string: template={}", templateName, error);
                            promise.fail(error);
                        });
                
            } catch (Exception e) {
                LOGGER.error("Failed to generate code to string: template={}", templateName, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 构建模板上下文
     */
    private TemplateContext buildTemplateContext(EntityInfo entityInfo) {
        Map<String, Object> data = new HashMap<>();
        
        // 添加实体信息
        data.put("entity", entityInfo);
        
        // 添加包信息
        Map<String, String> packageInfo = new HashMap<>();
        packageInfo.put("basePackage", entityInfo.getPackageName().substring(0, entityInfo.getPackageName().lastIndexOf('.')));
        packageInfo.put("entityPackage", entityInfo.getPackageName());
        packageInfo.put("daoPackage", entityInfo.getPackageName().replace("entity", "dao"));
        packageInfo.put("servicePackage", entityInfo.getPackageName().replace("entity", "service"));
        packageInfo.put("controllerPackage", entityInfo.getPackageName().replace("entity", "controller"));
        packageInfo.put("dtoPackage", entityInfo.getPackageName().replace("entity", "dto"));
        data.put("package", packageInfo);
        
        // 添加配置信息
        Map<String, Object> configInfo = new HashMap<>();
        configInfo.put("generateComments", true);
        configInfo.put("generateGetters", true);
        configInfo.put("generateSetters", true);
        configInfo.put("generateConstructors", true);
        configInfo.put("generateEquals", true);
        configInfo.put("generateHashCode", true);
        configInfo.put("generateToString", true);
        configInfo.put("useLombok", false);
        configInfo.put("generateValidation", true);
        data.put("config", configInfo);
        
        // 添加生成信息
        data.put("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("author", "QAIU");
        data.put("version", "1.0.0");
        
        return new TemplateContext(data);
    }
    
    /**
     * 检查模板是否存在
     * 
     * @param templateName 模板名称
     * @return 是否存在
     */
    public boolean hasTemplate(String templateName) {
        return codeGenerator.hasTemplate(templateName);
    }
    
    /**
     * 获取所有可用的模板
     * 
     * @return 模板名称列表
     */
    public String[] getAvailableTemplates() {
        return codeGenerator.getAvailableTemplates();
    }
    
    /**
     * 验证模板配置
     * 
     * @return 验证结果
     */
    public boolean validateConfig() {
        return codeGenerator.validateConfig(codeGenerator.getGeneratorConfig());
    }
}
