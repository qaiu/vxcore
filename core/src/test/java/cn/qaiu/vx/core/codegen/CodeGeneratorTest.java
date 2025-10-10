package cn.qaiu.vx.core.codegen;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("CodeGenerator代码生成器测试")
class CodeGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGeneratorTest.class);
    
    private Vertx vertx;
    private CodeGenerator codeGenerator;
    private GeneratorConfig config;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        
        // 创建生成器配置
        this.config = new GeneratorConfig()
                .setOutputPath("target/generated")
                .setPackageName("com.example")
                .setClassName("User")
                .setTableName("user")
                .setDescription("用户实体")
                .setGenerateComments(true)
                .setGenerateGetters(true)
                .setGenerateSetters(true)
                .setGenerateConstructors(true)
                .setGenerateToString(true)
                .setGenerateEquals(true)
                .setGenerateHashCode(true);
        
        // 创建代码生成器
        this.codeGenerator = new CodeGenerator(vertx, config);
        
        testContext.completeNow();
    }
    
    @Nested
    @DisplayName("基础功能测试")
    class BasicFunctionTest {
        
        @Test
        @DisplayName("测试代码生成器初始化")
        void testCodeGeneratorInitialization(VertxTestContext testContext) {
            testContext.verify(() -> {
                assertNotNull(codeGenerator, "代码生成器不应为空");
                assertNotNull(codeGenerator.getVertx(), "Vert.x实例不应为空");
                assertNotNull(codeGenerator.getTemplateEngine(), "模板引擎不应为空");
                assertNotNull(codeGenerator.getGeneratorConfig(), "生成器配置不应为空");
                LOGGER.info("✅ 代码生成器初始化测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试模板存在性检查")
        void testTemplateExistence(VertxTestContext testContext) {
            testContext.verify(() -> {
                assertTrue(codeGenerator.hasTemplate("entity.ftl"), "entity.ftl模板应该存在");
                assertTrue(codeGenerator.hasTemplate("dao.ftl"), "dao.ftl模板应该存在");
                assertTrue(codeGenerator.hasTemplate("controller.ftl"), "controller.ftl模板应该存在");
                assertFalse(codeGenerator.hasTemplate("nonexistent.ftl"), "不存在的模板应该返回false");
                LOGGER.info("✅ 模板存在性检查测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试获取可用模板列表")
        void testGetAvailableTemplates(VertxTestContext testContext) {
            testContext.verify(() -> {
                String[] templates = codeGenerator.getAvailableTemplates();
                assertNotNull(templates, "模板列表不应为空");
                assertTrue(templates.length > 0, "应该有可用的模板");
                
                // 检查是否包含内置模板
                boolean hasEntity = false;
                boolean hasDao = false;
                boolean hasController = false;
                
                for (String template : templates) {
                    if ("entity.ftl".equals(template)) hasEntity = true;
                    if ("dao.ftl".equals(template)) hasDao = true;
                    if ("controller.ftl".equals(template)) hasController = true;
                }
                
                assertTrue(hasEntity, "应该包含entity.ftl模板");
                assertTrue(hasDao, "应该包含dao.ftl模板");
                assertTrue(hasController, "应该包含controller.ftl模板");
                
                LOGGER.info("✅ 获取可用模板列表测试通过，模板数量: {}", templates.length);
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("模板上下文测试")
    class TemplateContextTest {
        
        @Test
        @DisplayName("测试模板上下文创建")
        void testTemplateContextCreation(VertxTestContext testContext) {
            testContext.verify(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("testKey", "testValue");
                
                TemplateContext context = codeGenerator.createContext(data);
                assertNotNull(context, "模板上下文不应为空");
                assertEquals("testValue", context.getString("testKey"), "应该能获取设置的值");
                assertTrue(context.containsKey("generatedTime"), "应该包含生成时间");
                assertTrue(context.containsKey("author"), "应该包含作者信息");
                
                LOGGER.info("✅ 模板上下文创建测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试实体信息设置")
        void testEntityInfoSetting(VertxTestContext testContext) {
            testContext.verify(() -> {
                EntityInfo entityInfo = new EntityInfo("User", "user");
                entityInfo.setDescription("用户实体");
                entityInfo.setPackageName("com.example.entity");
                
                TemplateContext context = new TemplateContext();
                context.setEntityInfo(entityInfo);
                
                EntityInfo retrievedEntity = context.getEntityInfo();
                assertNotNull(retrievedEntity, "实体信息不应为空");
                assertEquals("User", retrievedEntity.getClassName(), "类名应该正确");
                assertEquals("user", retrievedEntity.getTableName(), "表名应该正确");
                assertEquals("用户实体", retrievedEntity.getDescription(), "描述应该正确");
                
                LOGGER.info("✅ 实体信息设置测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试包信息设置")
        void testPackageInfoSetting(VertxTestContext testContext) {
            testContext.verify(() -> {
                PackageInfo packageInfo = new PackageInfo("com.example");
                
                TemplateContext context = new TemplateContext();
                context.setPackageInfo(packageInfo);
                
                PackageInfo retrievedPackage = context.getPackageInfo();
                assertNotNull(retrievedPackage, "包信息不应为空");
                assertEquals("com.example", retrievedPackage.getBasePackage(), "基础包名应该正确");
                assertEquals("com.example.entity", retrievedPackage.getEntityPackage(), "实体包名应该正确");
                assertEquals("com.example.dao", retrievedPackage.getDaoPackage(), "DAO包名应该正确");
                
                LOGGER.info("✅ 包信息设置测试通过");
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("代码生成测试")
    class CodeGenerationTest {
        
        @Test
        @DisplayName("测试生成代码到字符串")
        void testGenerateToString(VertxTestContext testContext) {
            // 创建实体信息
            EntityInfo entityInfo = new EntityInfo("User", "user");
            entityInfo.setDescription("用户实体");
            entityInfo.setPackageName("com.example.entity");
            
            // 添加字段
            FieldInfo idField = new FieldInfo("id", "Long");
            idField.setPrimaryKey(true);
            idField.setDescription("主键ID");
            entityInfo.addField(idField);
            
            FieldInfo nameField = new FieldInfo("name", "String");
            nameField.setDescription("用户名");
            entityInfo.addField(nameField);
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo("com.example");
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(config);
            
            // 手动设置包名到上下文中
            context.put("package", packageInfo);
            
            codeGenerator.generateToString("entity.ftl", context)
                    .onSuccess(result -> {
                        testContext.verify(() -> {
                            assertNotNull(result, "生成的代码不应为空");
                            assertTrue(result.contains("package com.example.entity"), "应该包含包声明");
                            assertTrue(result.contains("public class User"), "应该包含类声明");
                            assertTrue(result.contains("private Long id"), "应该包含字段声明");
                            assertTrue(result.contains("private String name"), "应该包含字段声明");
                            assertTrue(result.contains("public Long getId()"), "应该包含getter方法");
                            assertTrue(result.contains("public void setId(Long id)"), "应该包含setter方法");
                            
                            LOGGER.info("✅ 生成代码到字符串测试通过");
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试生成代码到文件")
        void testGenerateToFile(VertxTestContext testContext) {
            // 创建实体信息
            EntityInfo entityInfo = new EntityInfo("Product", "product");
            entityInfo.setDescription("产品实体");
            entityInfo.setPackageName("com.example.entity");
            
            // 添加字段
            FieldInfo idField = new FieldInfo("id", "Long");
            idField.setPrimaryKey(true);
            idField.setDescription("主键ID");
            entityInfo.addField(idField);
            
            FieldInfo nameField = new FieldInfo("name", "String");
            nameField.setDescription("产品名称");
            entityInfo.addField(nameField);
            
            FieldInfo priceField = new FieldInfo("price", "BigDecimal");
            priceField.setDescription("产品价格");
            entityInfo.addField(priceField);
            
            // 添加导入
            entityInfo.addImport("java.math.BigDecimal");
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo("com.example");
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(config);
            
            // 手动设置包名到上下文中
            context.put("package", packageInfo);
            
            String outputPath = "target/generated/com/example/entity/Product.java";
            
            codeGenerator.generate("entity.ftl", context, outputPath)
                    .onSuccess(result -> {
                        testContext.verify(() -> {
                            assertEquals(outputPath, result, "输出路径应该正确");
                            assertTrue(Files.exists(Paths.get(outputPath)), "生成的文件应该存在");
                            
                            // 读取生成的文件内容
                            String content = Files.readString(Paths.get(outputPath));
                            assertTrue(content.contains("package com.example.entity"), "应该包含包声明");
                            assertTrue(content.contains("public class Product"), "应该包含类声明");
                            assertTrue(content.contains("import java.math.BigDecimal"), "应该包含导入语句");
                            assertTrue(content.contains("private Long id"), "应该包含字段声明");
                            assertTrue(content.contains("private String name"), "应该包含字段声明");
                            assertTrue(content.contains("private BigDecimal price"), "应该包含字段声明");
                            
                            LOGGER.info("✅ 生成代码到文件测试通过: {}", outputPath);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试批量生成代码")
        void testBatchGeneration(VertxTestContext testContext) {
            // 创建实体信息
            EntityInfo entityInfo = new EntityInfo("Order", "order");
            entityInfo.setDescription("订单实体");
            entityInfo.setPackageName("com.example.entity");
            
            // 添加字段
            FieldInfo idField = new FieldInfo("id", "Long");
            idField.setPrimaryKey(true);
            idField.setDescription("主键ID");
            entityInfo.addField(idField);
            
            FieldInfo orderNoField = new FieldInfo("orderNo", "String");
            orderNoField.setDescription("订单号");
            entityInfo.addField(orderNoField);
            
            // 创建包信息
            PackageInfo packageInfo = new PackageInfo("com.example");
            
            // 创建模板上下文
            TemplateContext context = new TemplateContext()
                    .setEntityInfo(entityInfo)
                    .setPackageInfo(packageInfo)
                    .setGeneratorConfig(config);
            
            // 手动设置包名到上下文中
            context.put("package", packageInfo);
            
            // 创建模板配置
            TemplateConfig[] templates = {
                    new TemplateConfig("entity.ftl", "target/generated/com/example/entity/Order.java"),
                    new TemplateConfig("dao.ftl", "target/generated/com/example/dao/OrderDao.java"),
                    new TemplateConfig("controller.ftl", "target/generated/com/example/controller/OrderController.java")
            };
            
            codeGenerator.generateBatch(templates, context)
                    .onSuccess(results -> {
                        testContext.verify(() -> {
                            assertNotNull(results, "生成结果不应为空");
                            assertEquals(3, results.length, "应该生成3个文件");
                            
                            // 检查生成的文件
                            for (String result : results) {
                                assertTrue(Files.exists(Paths.get(result)), "生成的文件应该存在: " + result);
                            }
                            
                            LOGGER.info("✅ 批量生成代码测试通过，生成文件数: {}", results.length);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("配置验证测试")
    class ConfigValidationTest {
        
        @Test
        @DisplayName("测试配置验证")
        void testConfigValidation(VertxTestContext testContext) {
            testContext.verify(() -> {
                // 测试有效配置
                assertTrue(codeGenerator.validateConfig(config), "有效配置应该通过验证");
                
                // 测试无效配置
                GeneratorConfig invalidConfig1 = new GeneratorConfig();
                assertFalse(codeGenerator.validateConfig(invalidConfig1), "空配置应该验证失败");
                
                GeneratorConfig invalidConfig2 = new GeneratorConfig()
                        .setOutputPath("")
                        .setPackageName("com.example");
                assertFalse(codeGenerator.validateConfig(invalidConfig2), "空输出路径应该验证失败");
                
                GeneratorConfig invalidConfig3 = new GeneratorConfig()
                        .setOutputPath("target/generated")
                        .setPackageName("");
                assertFalse(codeGenerator.validateConfig(invalidConfig3), "空包名应该验证失败");
                
                LOGGER.info("✅ 配置验证测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试配置属性")
        void testConfigProperties(VertxTestContext testContext) {
            testContext.verify(() -> {
                // 测试自定义属性
                config.addCustomProperty("author", "TestAuthor");
                config.addCustomProperty("version", "2.0.0");
                
                assertEquals("TestAuthor", config.getCustomPropertyAsString("author"), "自定义属性应该正确");
                assertEquals("2.0.0", config.getCustomPropertyAsString("version"), "自定义属性应该正确");
                assertEquals("default", config.getCustomPropertyAsString("nonexistent", "default"), "默认值应该正确");
                
                // 测试配置副本
                GeneratorConfig copy = config.copy();
                assertNotNull(copy, "配置副本不应为空");
                assertEquals(config.getPackageName(), copy.getPackageName(), "包名应该相同");
                assertEquals(config.getClassName(), copy.getClassName(), "类名应该相同");
                
                LOGGER.info("✅ 配置属性测试通过");
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTest {
        
        @Test
        @DisplayName("测试无效模板处理")
        void testInvalidTemplateHandling(VertxTestContext testContext) {
            TemplateContext context = new TemplateContext();
            
            codeGenerator.generateToString("nonexistent.ftl", context)
                    .onSuccess(result -> {
                        testContext.failNow(new AssertionError("应该失败但成功了"));
                    })
                    .onFailure(error -> {
                        testContext.verify(() -> {
                            assertNotNull(error, "错误不应为空");
                            LOGGER.info("✅ 无效模板处理测试通过: {}", error.getMessage());
                        });
                        testContext.completeNow();
                    });
        }
        
        @Test
        @DisplayName("测试无效输出路径处理")
        void testInvalidOutputPathHandling(VertxTestContext testContext) {
            TemplateContext context = new TemplateContext();
            String invalidPath = "/invalid/path/that/does/not/exist/file.java";
            
            codeGenerator.generate("entity.ftl", context, invalidPath)
                    .onSuccess(result -> {
                        testContext.failNow(new AssertionError("应该失败但成功了"));
                    })
                    .onFailure(error -> {
                        testContext.verify(() -> {
                            assertNotNull(error, "错误不应为空");
                            LOGGER.info("✅ 无效输出路径处理测试通过: {}", error.getMessage());
                        });
                        testContext.completeNow();
                    });
        }
    }
}
