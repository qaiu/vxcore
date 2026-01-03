package cn.qaiu.generator.core;

import cn.qaiu.generator.config.*;
import cn.qaiu.generator.model.DaoStyle;
import cn.qaiu.generator.model.GeneratorContext;
import cn.qaiu.generator.reader.ConfigMetadataReader;
import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 代码生成器门面测试
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
public class CodeGeneratorFacadeTest {
    
    private Vertx vertx;
    private GeneratorContext context;
    private String testOutputDir;
    
    @BeforeEach
    void setUp(VertxTestContext testContext) throws IOException {
        vertx = Vertx.vertx();
        
        // 创建测试输出目录
        testOutputDir = Files.createTempDirectory("vxcore-generator-test").toString();
        
        // 创建配置
        context = createTestContext();
        
        testContext.completeNow();
    }
    
    @Test
    void testGenerateEntity(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        TableInfo tableInfo = createTestTableInfo();
        
        generator.generateEntity(tableInfo)
                .onSuccess(filePath -> {
                    assertNotNull(filePath);
                    assertTrue(Files.exists(Paths.get(filePath)));
                    
                    // 验证文件内容
                    try {
                        String content = Files.readString(Paths.get(filePath));
                        assertTrue(content.contains("class TestUser"));
                        assertTrue(content.contains("package com.example.entity"));
                    } catch (IOException e) {
                        testContext.failNow(e);
                    }
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testGenerateDao(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        TableInfo tableInfo = createTestTableInfo();
        
        generator.generateDao(tableInfo)
                .onSuccess(filePath -> {
                    assertNotNull(filePath);
                    assertTrue(Files.exists(Paths.get(filePath)));
                    
                    // 验证文件内容
                    try {
                        String content = Files.readString(Paths.get(filePath));
                        assertTrue(content.contains("class TestUserDao"));
                        assertTrue(content.contains("package com.example.dao"));
                    } catch (IOException e) {
                        testContext.failNow(e);
                    }
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testGenerateService(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        TableInfo tableInfo = createTestTableInfo();
        
        generator.generateService(tableInfo)
                .onSuccess(filePath -> {
                    assertNotNull(filePath);
                    assertTrue(Files.exists(Paths.get(filePath)));
                    
                    // 验证文件内容
                    try {
                        String content = Files.readString(Paths.get(filePath));
                        assertTrue(content.contains("interface TestUserService"));
                        assertTrue(content.contains("package com.example.service"));
                    } catch (IOException e) {
                        testContext.failNow(e);
                    }
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testGenerateController(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        TableInfo tableInfo = createTestTableInfo();
        
        generator.generateController(tableInfo)
                .onSuccess(filePath -> {
                    assertNotNull(filePath);
                    assertTrue(Files.exists(Paths.get(filePath)));
                    
                    // 验证文件内容
                    try {
                        String content = Files.readString(Paths.get(filePath));
                        assertTrue(content.contains("class TestUserController"));
                        assertTrue(content.contains("package com.example.controller"));
                        assertTrue(content.contains("@RouteHandler"));
                    } catch (IOException e) {
                        testContext.failNow(e);
                    }
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testGenerateDto(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        TableInfo tableInfo = createTestTableInfo();
        
        generator.generateDto(tableInfo)
                .onSuccess(filePath -> {
                    assertNotNull(filePath);
                    assertTrue(Files.exists(Paths.get(filePath)));
                    
                    // 验证文件内容
                    try {
                        String content = Files.readString(Paths.get(filePath));
                        assertTrue(content.contains("class TestUserCreateRequest"));
                        assertTrue(content.contains("package com.example.dto"));
                    } catch (IOException e) {
                        testContext.failNow(e);
                    }
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testGenerateTable(VertxTestContext testContext) {
        CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
        
        // 先创建测试表
        createTestTable().compose(v -> {
            // 然后生成代码
            return generator.generateTable("test_user");
        }).onSuccess(files -> {
            assertNotNull(files);
            assertFalse(files.isEmpty());
            
            // 验证生成的文件
            for (String filePath : files) {
                assertTrue(Files.exists(Paths.get(filePath)));
            }
            
            testContext.completeNow();
        }).onFailure(testContext::failNow);
    }
    
    /**
     * 创建测试表
     */
    private Future<Void> createTestTable() {
        return Future.future(promise -> {
            try {
                java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE", 
                    "sa", "");
                java.sql.Statement stmt = conn.createStatement();
                
                // 创建测试表
                stmt.execute("DROP TABLE IF EXISTS test_user");
                stmt.execute("""
                    CREATE TABLE test_user (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(255),
                        age INTEGER,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                
                stmt.close();
                conn.close();
                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }
    
    /**
     * 创建测试上下文
     */
    private GeneratorContext createTestContext() {
        // 包配置
        PackageConfig packageConfig = new PackageConfig("com.example");
        
        // 模板配置
        TemplateConfig templateConfig = new TemplateConfig();
        
        // 输出配置
        OutputConfig outputConfig = new OutputConfig(testOutputDir);
        outputConfig.setOverwriteExisting(true);
        
        // 功能配置
        FeatureConfig featureConfig = new FeatureConfig();
        featureConfig.setGenerateEntity(true);
        featureConfig.setGenerateDao(true);
        featureConfig.setGenerateService(true);
        featureConfig.setGenerateController(true);
        featureConfig.setGenerateDto(true);
        featureConfig.setDaoStyle(DaoStyle.LAMBDA);
        
        // 数据库配置（用于testGenerateTable测试）
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        databaseConfig.setUsername("sa");
        databaseConfig.setPassword("");
        
        return GeneratorContext.builder()
                .packageConfig(packageConfig)
                .templateConfig(templateConfig)
                .outputConfig(outputConfig)
                .featureConfig(featureConfig)
                .databaseConfig(databaseConfig)
                .build();
    }
    
    /**
     * 创建测试表信息
     */
    private TableInfo createTestTableInfo() {
        TableInfo tableInfo = new TableInfo("test_user");
        tableInfo.setComment("测试用户表");
        
        List<ColumnInfo> columns = new ArrayList<>();
        
        // 主键列
        ColumnInfo idColumn = new ColumnInfo();
        idColumn.setColumnName("id");
        idColumn.setColumnType("BIGINT");
        idColumn.setJavaType("Long");
        idColumn.setPrimaryKey(true);
        idColumn.setNullable(false);
        columns.add(idColumn);
        
        // 普通列
        ColumnInfo nameColumn = new ColumnInfo();
        nameColumn.setColumnName("name");
        nameColumn.setColumnType("VARCHAR");
        nameColumn.setJavaType("String");
        nameColumn.setLength(100);
        nameColumn.setNullable(false);
        nameColumn.setComment("用户名");
        columns.add(nameColumn);
        
        ColumnInfo emailColumn = new ColumnInfo();
        emailColumn.setColumnName("email");
        emailColumn.setColumnType("VARCHAR");
        emailColumn.setJavaType("String");
        emailColumn.setLength(255);
        emailColumn.setNullable(true);
        emailColumn.setComment("邮箱");
        columns.add(emailColumn);
        
        tableInfo.setColumns(columns);
        return tableInfo;
    }
}
