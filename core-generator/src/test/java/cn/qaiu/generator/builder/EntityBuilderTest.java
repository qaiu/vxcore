package cn.qaiu.generator.builder;

import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.FieldInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体构建器测试
 * 
 * @author QAIU
 */
public class EntityBuilderTest {
    
    private EntityBuilder entityBuilder;
    private FeatureConfig featureConfig;
    private PackageConfig packageConfig;
    
    @BeforeEach
    void setUp() {
        featureConfig = new FeatureConfig();
        packageConfig = new PackageConfig("com.example");
        entityBuilder = new EntityBuilder(featureConfig, packageConfig);
    }
    
    @Test
    void testBuildEntity() {
        // 创建测试表信息
        TableInfo tableInfo = createTestTableInfo();
        
        // 构建实体
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 验证结果
        assertNotNull(entityInfo);
        assertEquals("TestUser", entityInfo.getClassName());
        assertEquals("test_user", entityInfo.getTableName());
        assertEquals("com.example.entity", entityInfo.getPackageName());
        assertEquals("测试用户表", entityInfo.getDescription());
        
        // 验证字段
        List<FieldInfo> fields = entityInfo.getFields();
        assertEquals(4, fields.size());
        
        // 验证主键字段
        FieldInfo idField = fields.stream()
                .filter(FieldInfo::isPrimaryKey)
                .findFirst()
                .orElse(null);
        assertNotNull(idField);
        assertEquals("id", idField.getFieldName());
        assertEquals("Long", idField.getFieldType());
        assertEquals("getId", idField.getGetterName());
        assertEquals("setId", idField.getSetterName());
        
        // 验证普通字段
        FieldInfo nameField = fields.stream()
                .filter(field -> "name".equals(field.getFieldName()))
                .findFirst()
                .orElse(null);
        assertNotNull(nameField);
        assertEquals("name", nameField.getFieldName());
        assertEquals("String", nameField.getFieldType());
        assertEquals("getName", nameField.getGetterName());
        assertEquals("setName", nameField.getSetterName());
        assertFalse(nameField.isNullable());
        
        // 验证导入语句
        List<String> imports = entityInfo.getImports();
        assertTrue(imports.contains("java.time.LocalDateTime"));
        assertTrue(imports.contains("java.util.Objects"));
    }
    
    @Test
    void testBuildEntityWithLombok() {
        // 启用 Lombok
        featureConfig.setUseLombok(true);
        
        TableInfo tableInfo = createTestTableInfo();
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 验证 Lombok 导入
        List<String> imports = entityInfo.getImports();
        assertTrue(imports.contains("lombok.Data"));
        assertTrue(imports.contains("lombok.NoArgsConstructor"));
        assertTrue(imports.contains("lombok.AllArgsConstructor"));
    }
    
    @Test
    void testBuildEntityWithJpaAnnotations() {
        // 启用 JPA 注解
        featureConfig.setUseJpaAnnotations(true);
        
        TableInfo tableInfo = createTestTableInfo();
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 验证 JPA 导入
        List<String> imports = entityInfo.getImports();
        assertTrue(imports.contains("jakarta.persistence.Entity"));
        assertTrue(imports.contains("jakarta.persistence.Table"));
        assertTrue(imports.contains("jakarta.persistence.Id"));
        assertTrue(imports.contains("jakarta.persistence.Column"));
    }
    
    @Test
    void testBuildEntityWithVertxAnnotations() {
        // 启用 Vert.x 注解
        featureConfig.setUseVertxAnnotations(true);
        
        TableInfo tableInfo = createTestTableInfo();
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 验证 Vert.x 导入
        List<String> imports = entityInfo.getImports();
        assertTrue(imports.contains("io.vertx.sqlclient.templates.annotations.RowMapped"));
        assertTrue(imports.contains("io.vertx.sqlclient.templates.annotations.Column"));
    }
    
    @Test
    void testBuildEntityWithValidation() {
        // 启用参数校验
        featureConfig.setGenerateValidation(true);
        
        TableInfo tableInfo = createTestTableInfo();
        EntityInfo entityInfo = entityBuilder.buildEntity(tableInfo);
        
        // 验证校验注解导入
        List<String> imports = entityInfo.getImports();
        assertTrue(imports.contains("jakarta.validation.constraints.NotNull"));
        assertTrue(imports.contains("jakarta.validation.constraints.NotBlank"));
        assertTrue(imports.contains("jakarta.validation.constraints.Size"));
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
        
        ColumnInfo createdAtColumn = new ColumnInfo();
        createdAtColumn.setColumnName("created_at");
        createdAtColumn.setColumnType("TIMESTAMP");
        createdAtColumn.setJavaType("LocalDateTime");
        createdAtColumn.setNullable(false);
        createdAtColumn.setComment("创建时间");
        columns.add(createdAtColumn);
        
        tableInfo.setColumns(columns);
        return tableInfo;
    }
}
