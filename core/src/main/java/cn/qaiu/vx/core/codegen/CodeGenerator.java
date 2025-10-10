package cn.qaiu.vx.core.codegen;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 代码生成器核心类
 * 基于Vert.x FreeMarker模板引擎生成代码
 * 
 * @author QAIU
 */
public class CodeGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerator.class);
    
    private final FreeMarkerTemplateEngine templateEngine;
    private final GeneratorConfig generatorConfig;
    private final Vertx vertx;
    
    public CodeGenerator(Vertx vertx, GeneratorConfig config) {
        this.vertx = vertx;
        this.generatorConfig = config;
        this.templateEngine = FreeMarkerTemplateEngine.create(vertx);
    }
    
    /**
     * 获取模板路径
     */
    private String getTemplatePath(String templateName) {
        String templatePath = generatorConfig.getTemplatePath();
        if (templatePath != null && Files.exists(Paths.get(templatePath))) {
            return templatePath + "/" + templateName;
        } else {
            // 使用classpath中的模板
            return "templates/" + templateName;
        }
    }
    
    /**
     * 构建模板数据
     */
    private JsonObject buildTemplateData(TemplateContext context) {
        JsonObject templateData = new JsonObject();
        
        // 添加基础数据
        Map<String, Object> data = context.getData();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof EntityInfo) {
                EntityInfo entityInfo = (EntityInfo) value;
                JsonObject entityData = new JsonObject();
                entityData.put("className", entityInfo.getClassName());
                entityData.put("description", entityInfo.getDescription());
                entityData.put("author", entityInfo.getAuthor());
                entityData.put("version", entityInfo.getVersion());
                entityData.put("packageName", entityInfo.getPackageName());
                
                // 添加字段信息
                if (entityInfo.getFields() != null && !entityInfo.getFields().isEmpty()) {
                    JsonArray fieldsArray = new JsonArray();
                    for (FieldInfo field : entityInfo.getFields()) {
                        JsonObject fieldData = new JsonObject();
                        fieldData.put("fieldName", field.getFieldName());
                        fieldData.put("fieldType", field.getFieldType());
                        fieldData.put("description", field.getDescription());
                        fieldData.put("getterName", field.getGetterName());
                        fieldData.put("setterName", field.getSetterName());
                        fieldsArray.add(fieldData);
                    }
                    entityData.put("fields", fieldsArray);
                }
                
                // 添加导入信息
                if (entityInfo.getImports() != null && !entityInfo.getImports().isEmpty()) {
                    JsonArray importsArray = new JsonArray();
                    for (String importClass : entityInfo.getImports()) {
                        importsArray.add(importClass);
                    }
                    entityData.put("imports", importsArray);
                }
                
                templateData.put(key, entityData);
            } else if (value instanceof PackageInfo) {
                PackageInfo packageInfo = (PackageInfo) value;
                JsonObject packageData = new JsonObject();
                packageData.put("packageName", packageInfo.getPackageName());
                packageData.put("basePackage", packageInfo.getBasePackage());
                packageData.put("entityPackage", packageInfo.getEntityPackage());
                packageData.put("daoPackage", packageInfo.getDaoPackage());
                packageData.put("controllerPackage", packageInfo.getControllerPackage());
                templateData.put(key, packageData);
            } else if (value instanceof GeneratorConfig) {
                GeneratorConfig config = (GeneratorConfig) value;
                JsonObject configData = new JsonObject();
                configData.put("generateConstructors", config.isGenerateConstructors());
                configData.put("generateGetters", config.isGenerateGetters());
                configData.put("generateSetters", config.isGenerateSetters());
                configData.put("generateEquals", config.isGenerateEquals());
                configData.put("generateHashCode", config.isGenerateHashCode());
                configData.put("generateToString", config.isGenerateToString());
                configData.put("generateComments", config.isGenerateComments());
                configData.put("author", "QAIU");
                templateData.put(key, configData);
            } else {
                // 基本类型直接添加
                templateData.put(key, value);
            }
        }
        
        return templateData;
    }
    
    /**
     * 生成代码
     * 
     * @param templateName 模板名称
     * @param context 模板上下文
     * @param outputPath 输出路径
     * @return 生成的文件路径
     */
    public Future<String> generate(String templateName, TemplateContext context, String outputPath) {
        return Future.future(promise -> {
            try {
                // 创建输出目录
                Path outputDir = Paths.get(outputPath).getParent();
                if (outputDir != null && !Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);
                }
                
                // 获取模板路径
                String templatePath = getTemplatePath(templateName);
                
                // 手动构建模板数据
                JsonObject templateData = buildTemplateData(context);
                
                // 使用Vert.x模板引擎渲染
                templateEngine.render(templateData, templatePath, renderResult -> {
                    if (renderResult.succeeded()) {
                        try {
                            // 写入文件
                            Files.write(Paths.get(outputPath), renderResult.result().getBytes());
                            LOGGER.info("代码生成成功: {} -> {}", templateName, outputPath);
                            promise.complete(outputPath);
                        } catch (Exception e) {
                            LOGGER.error("写入文件失败: {}", outputPath, e);
                            promise.fail(e);
                        }
                    } else {
                        LOGGER.error("模板渲染失败: template={}", templateName, renderResult.cause());
                        promise.fail(renderResult.cause());
                    }
                });
                
            } catch (Exception e) {
                LOGGER.error("代码生成失败: template={}, output={}", templateName, outputPath, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 生成代码到字符串
     * 
     * @param templateName 模板名称
     * @param context 模板上下文
     * @return 生成的代码字符串
     */
    public Future<String> generateToString(String templateName, TemplateContext context) {
        return Future.future(promise -> {
            try {
                // 获取模板路径
                String templatePath = getTemplatePath(templateName);
                
                // 手动构建模板数据
                JsonObject templateData = buildTemplateData(context);
                
                // 使用Vert.x模板引擎渲染
                templateEngine.render(templateData, templatePath, renderResult -> {
                    if (renderResult.succeeded()) {
                        LOGGER.info("代码生成到字符串成功: {}", templateName);
                        promise.complete(renderResult.result().toString());
                    } else {
                        LOGGER.error("模板渲染失败: template={}", templateName, renderResult.cause());
                        promise.fail(renderResult.cause());
                    }
                });
                
            } catch (Exception e) {
                LOGGER.error("代码生成到字符串失败: template={}", templateName, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 批量生成代码
     * 
     * @param templates 模板配置列表
     * @param context 模板上下文
     * @return 生成的文件路径列表
     */
    public Future<String[]> generateBatch(TemplateConfig[] templates, TemplateContext context) {
        return Future.future(promise -> {
            if (templates == null || templates.length == 0) {
                promise.complete(new String[0]);
                return;
            }
            
            String[] outputPaths = new String[templates.length];
            final int[] completedCount = {0};
            
            for (int i = 0; i < templates.length; i++) {
                final int index = i;
                final TemplateConfig templateConfig = templates[i];
                
                generate(templateConfig.getTemplateName(), context, templateConfig.getOutputPath())
                    .onSuccess(outputPath -> {
                        outputPaths[index] = outputPath;
                        completedCount[0]++;
                        
                        if (completedCount[0] == templates.length) {
                            promise.complete(outputPaths);
                        }
                    })
                    .onFailure(error -> {
                        LOGGER.error("批量生成失败: template={}", templateConfig.getTemplateName(), error);
                        promise.fail(error);
                    });
            }
        });
    }
    
    /**
     * 检查模板是否存在
     * 
     * @param templateName 模板名称
     * @return 是否存在
     */
    public boolean hasTemplate(String templateName) {
        try {
            String templatePath = getTemplatePath(templateName);
            return Files.exists(Paths.get(templatePath)) || 
                   getClass().getClassLoader().getResource(templatePath) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取所有可用的模板
     * 
     * @return 模板名称列表
     */
    public String[] getAvailableTemplates() {
        try {
            String templatePath = generatorConfig.getTemplatePath();
            if (templatePath != null && Files.exists(Paths.get(templatePath))) {
                return Files.list(Paths.get(templatePath))
                    .filter(path -> path.toString().endsWith(".ftl"))
                    .map(path -> path.getFileName().toString())
                    .toArray(String[]::new);
            } else {
                // 从classpath获取模板
                return new String[]{"entity.ftl", "dao.ftl", "controller.ftl"};
            }
        } catch (Exception e) {
            LOGGER.warn("获取模板列表失败", e);
        }
        return new String[0];
    }
    
    /**
     * 验证生成配置
     * 
     * @param config 生成配置
     * @return 验证结果
     */
    public boolean validateConfig(GeneratorConfig config) {
        if (config == null) {
            LOGGER.error("生成配置不能为空");
            return false;
        }
        
        if (config.getOutputPath() == null || config.getOutputPath().trim().isEmpty()) {
            LOGGER.error("输出路径不能为空");
            return false;
        }
        
        if (config.getPackageName() == null || config.getPackageName().trim().isEmpty()) {
            LOGGER.error("包名不能为空");
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建模板上下文
     * 
     * @param data 数据
     * @return 模板上下文
     */
    public TemplateContext createContext(Map<String, Object> data) {
        return new TemplateContext(data);
    }
    
    /**
     * 获取生成器配置
     * 
     * @return 生成器配置
     */
    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }
    
    /**
     * 获取Vert.x实例
     * 
     * @return Vert.x实例
     */
    public Vertx getVertx() {
        return vertx;
    }
    
    /**
     * 获取模板引擎
     * 
     * @return 模板引擎
     */
    public FreeMarkerTemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
