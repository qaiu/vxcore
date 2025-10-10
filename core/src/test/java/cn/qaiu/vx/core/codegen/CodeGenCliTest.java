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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("CodeGenCli CLI工具测试")
class CodeGenCliTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenCliTest.class);
    
    private Vertx vertx;
    private CodeGenCli codeGenCli;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.codeGenCli = new CodeGenCli(vertx);
        
        testContext.completeNow();
    }
    
    @Nested
    @DisplayName("实体生成测试")
    class EntityGenerationTest {
        
        @Test
        @DisplayName("测试生成实体类")
        void testGenerateEntity(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "user";
                String className = "User";
                String packageName = "com.example";
                String outputPath = "target/generated";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateEntity(tableName, className, packageName, outputPath);
                
                assertTrue(result.isSuccess(), "生成实体应该成功: " + result.getMessage());
                
                // 检查生成的文件
                String expectedPath = outputPath + "/com/example/entity/User.java";
                assertTrue(Files.exists(Paths.get(expectedPath)), "生成的实体文件应该存在: " + expectedPath);
                
                // 读取文件内容
                String content = Files.readString(Paths.get(expectedPath));
                assertTrue(content.contains("package com.example.entity"), "应该包含包声明");
                assertTrue(content.contains("public class User"), "应该包含类声明");
                assertTrue(content.contains("private Long id"), "应该包含ID字段");
                assertTrue(content.contains("private LocalDateTime createTime"), "应该包含创建时间字段");
                assertTrue(content.contains("private LocalDateTime updateTime"), "应该包含更新时间字段");
                
                LOGGER.info("✅ 生成实体类测试通过: {}", expectedPath);
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试生成实体类失败处理")
        void testGenerateEntityFailure(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "user";
                String className = "User";
                String packageName = "com.example";
                String outputPath = "/invalid/path/that/does/not/exist";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateEntity(tableName, className, packageName, outputPath);
                
                assertFalse(result.isSuccess(), "生成实体应该失败");
                assertNotNull(result.getMessage(), "错误消息不应为空");
                
                LOGGER.info("✅ 生成实体类失败处理测试通过: {}", result.getMessage());
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("DAO生成测试")
    class DaoGenerationTest {
        
        @Test
        @DisplayName("测试生成DAO类")
        void testGenerateDao(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "product";
                String className = "Product";
                String packageName = "com.example";
                String outputPath = "target/generated";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateDao(tableName, className, packageName, outputPath);
                
                assertTrue(result.isSuccess(), "生成DAO应该成功: " + result.getMessage());
                
                // 检查生成的文件
                String expectedPath = outputPath + "/com/example/dao/ProductDao.java";
                assertTrue(Files.exists(Paths.get(expectedPath)), "生成的DAO文件应该存在: " + expectedPath);
                
                // 读取文件内容
                String content = Files.readString(Paths.get(expectedPath));
                assertTrue(content.contains("package com.example.dao"), "应该包含包声明");
                assertTrue(content.contains("public class ProductDao"), "应该包含类声明");
                assertTrue(content.contains("extends LambdaDao<Product>"), "应该继承LambdaDao");
                assertTrue(content.contains("public Future<Optional<Product>> findById"), "应该包含findById方法");
                assertTrue(content.contains("public Future<List<Product>> findAll"), "应该包含findAll方法");
                assertTrue(content.contains("public Future<Product> save"), "应该包含save方法");
                assertTrue(content.contains("public Future<Product> update"), "应该包含update方法");
                assertTrue(content.contains("public Future<Integer> deleteById"), "应该包含deleteById方法");
                
                LOGGER.info("✅ 生成DAO类测试通过: {}", expectedPath);
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试生成DAO类失败处理")
        void testGenerateDaoFailure(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "product";
                String className = "Product";
                String packageName = "com.example";
                String outputPath = "/invalid/path/that/does/not/exist";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateDao(tableName, className, packageName, outputPath);
                
                assertFalse(result.isSuccess(), "生成DAO应该失败");
                assertNotNull(result.getMessage(), "错误消息不应为空");
                
                LOGGER.info("✅ 生成DAO类失败处理测试通过: {}", result.getMessage());
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("控制器生成测试")
    class ControllerGenerationTest {
        
        @Test
        @DisplayName("测试生成控制器类")
        void testGenerateController(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "order";
                String className = "Order";
                String packageName = "com.example";
                String outputPath = "target/generated";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateController(tableName, className, packageName, outputPath);
                
                assertTrue(result.isSuccess(), "生成控制器应该成功: " + result.getMessage());
                
                // 检查生成的文件
                String expectedPath = outputPath + "/com/example/controller/OrderController.java";
                assertTrue(Files.exists(Paths.get(expectedPath)), "生成的控制器文件应该存在: " + expectedPath);
                
                // 读取文件内容
                String content = Files.readString(Paths.get(expectedPath));
                assertTrue(content.contains("package com.example.controller"), "应该包含包声明");
                assertTrue(content.contains("public class OrderController"), "应该包含类声明");
                assertTrue(content.contains("@RouteHandler(\"/api/order\")"), "应该包含路由处理器注解");
                assertTrue(content.contains("public Future<JsonResult> getById"), "应该包含getById方法");
                assertTrue(content.contains("public Future<JsonResult> getAll"), "应该包含getAll方法");
                assertTrue(content.contains("public Future<JsonResult> create"), "应该包含create方法");
                assertTrue(content.contains("public Future<JsonResult> update"), "应该包含update方法");
                assertTrue(content.contains("public Future<JsonResult> delete"), "应该包含delete方法");
                
                LOGGER.info("✅ 生成控制器类测试通过: {}", expectedPath);
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试生成控制器类失败处理")
        void testGenerateControllerFailure(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "order";
                String className = "Order";
                String packageName = "com.example";
                String outputPath = "/invalid/path/that/does/not/exist";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateController(tableName, className, packageName, outputPath);
                
                assertFalse(result.isSuccess(), "生成控制器应该失败");
                assertNotNull(result.getMessage(), "错误消息不应为空");
                
                LOGGER.info("✅ 生成控制器类失败处理测试通过: {}", result.getMessage());
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("批量生成测试")
    class BatchGenerationTest {
        
        @Test
        @DisplayName("测试批量生成所有代码")
        void testGenerateAll(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "customer";
                String className = "Customer";
                String packageName = "com.example";
                String outputPath = "target/generated";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateAll(tableName, className, packageName, outputPath);
                
                assertTrue(result.isSuccess(), "批量生成应该成功: " + result.getMessage());
                
                // 检查生成的所有文件
                String[] expectedPaths = {
                        outputPath + "/com/example/entity/Customer.java",
                        outputPath + "/com/example/dao/CustomerDao.java",
                        outputPath + "/com/example/controller/CustomerController.java"
                };
                
                for (String expectedPath : expectedPaths) {
                    assertTrue(Files.exists(Paths.get(expectedPath)), "生成的文件应该存在: " + expectedPath);
                }
                
                // 验证实体文件内容
                String entityContent = Files.readString(Paths.get(expectedPaths[0]));
                assertTrue(entityContent.contains("public class Customer"), "实体类应该正确");
                
                // 验证DAO文件内容
                String daoContent = Files.readString(Paths.get(expectedPaths[1]));
                assertTrue(daoContent.contains("public class CustomerDao"), "DAO类应该正确");
                
                // 验证控制器文件内容
                String controllerContent = Files.readString(Paths.get(expectedPaths[2]));
                assertTrue(controllerContent.contains("public class CustomerController"), "控制器类应该正确");
                
                LOGGER.info("✅ 批量生成所有代码测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试批量生成失败处理")
        void testGenerateAllFailure(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "customer";
                String className = "Customer";
                String packageName = "com.example";
                String outputPath = "/invalid/path/that/does/not/exist";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateAll(tableName, className, packageName, outputPath);
                
                assertFalse(result.isSuccess(), "批量生成应该失败");
                assertNotNull(result.getMessage(), "错误消息不应为空");
                
                LOGGER.info("✅ 批量生成失败处理测试通过: {}", result.getMessage());
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("结果对象测试")
    class ResultObjectTest {
        
        @Test
        @DisplayName("测试CodeGenResult成功结果")
        void testCodeGenResultSuccess(VertxTestContext testContext) {
            testContext.verify(() -> {
                String message = "Test success message";
                CodeGenCli.CodeGenResult result = CodeGenCli.CodeGenResult.success(message);
                
                assertTrue(result.isSuccess(), "应该标记为成功");
                assertEquals(message, result.getMessage(), "消息应该正确");
                assertTrue(result.toString().contains("success=true"), "toString应该包含成功状态");
                
                LOGGER.info("✅ CodeGenResult成功结果测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试CodeGenResult失败结果")
        void testCodeGenResultFailure(VertxTestContext testContext) {
            testContext.verify(() -> {
                String message = "Test failure message";
                CodeGenCli.CodeGenResult result = CodeGenCli.CodeGenResult.failure(message);
                
                assertFalse(result.isSuccess(), "应该标记为失败");
                assertEquals(message, result.getMessage(), "消息应该正确");
                assertTrue(result.toString().contains("success=false"), "toString应该包含失败状态");
                
                LOGGER.info("✅ CodeGenResult失败结果测试通过");
            });
            testContext.completeNow();
        }
    }
    
    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {
        
        @Test
        @DisplayName("测试空参数处理")
        void testEmptyParameters(VertxTestContext testContext) {
            testContext.verify(() -> {
                // 测试空表名
                CodeGenCli.CodeGenResult result1 = codeGenCli.generateEntity("", "User", "com.example", "target/generated");
                assertFalse(result1.isSuccess(), "空表名应该失败");
                
                // 测试空类名
                CodeGenCli.CodeGenResult result2 = codeGenCli.generateEntity("user", "", "com.example", "target/generated");
                assertFalse(result2.isSuccess(), "空类名应该失败");
                
                // 测试空包名
                CodeGenCli.CodeGenResult result3 = codeGenCli.generateEntity("user", "User", "", "target/generated");
                assertFalse(result3.isSuccess(), "空包名应该失败");
                
                LOGGER.info("✅ 空参数处理测试通过");
            });
            testContext.completeNow();
        }
        
        @Test
        @DisplayName("测试特殊字符处理")
        void testSpecialCharacters(VertxTestContext testContext) {
            testContext.verify(() -> {
                String tableName = "user_profile";
                String className = "UserProfile";
                String packageName = "com.example.test";
                String outputPath = "target/generated";
                
                CodeGenCli.CodeGenResult result = codeGenCli.generateEntity(tableName, className, packageName, outputPath);
                
                assertTrue(result.isSuccess(), "特殊字符应该处理成功: " + result.getMessage());
                
                // 检查生成的文件
                String expectedPath = outputPath + "/com/example/test/entity/UserProfile.java";
                assertTrue(Files.exists(Paths.get(expectedPath)), "生成的文件应该存在: " + expectedPath);
                
                LOGGER.info("✅ 特殊字符处理测试通过: {}", expectedPath);
            });
            testContext.completeNow();
        }
    }
}
