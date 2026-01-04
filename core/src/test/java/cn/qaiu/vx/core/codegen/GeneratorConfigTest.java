package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * GeneratorConfig 单元测试
 *
 * @author test
 */
@DisplayName("GeneratorConfig 测试")
class GeneratorConfigTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数 - 默认值")
    void testDefaultConstructor() {
      GeneratorConfig config = new GeneratorConfig();

      assertNull(config.getTemplatePath());
      assertNull(config.getOutputPath());
      assertNull(config.getPackageName());
      assertFalse(config.isOverwriteExisting());
      assertTrue(config.isGenerateComments());
      assertTrue(config.isGenerateToString());
      assertTrue(config.isGenerateEquals());
      assertTrue(config.isGenerateHashCode());
      assertTrue(config.isGenerateGetters());
      assertTrue(config.isGenerateSetters());
      assertTrue(config.isGenerateConstructors());
      assertEquals("UTF-8", config.getEncoding());
      assertNotNull(config.getCustomProperties());
      assertTrue(config.getCustomProperties().isEmpty());
    }

    @Test
    @DisplayName("双参数构造函数")
    void testTwoArgConstructor() {
      GeneratorConfig config = new GeneratorConfig("/output", "com.example");

      assertEquals("/output", config.getOutputPath());
      assertEquals("com.example", config.getPackageName());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      GeneratorConfig config =
          new GeneratorConfig()
              .setTemplatePath("/templates")
              .setOutputPath("/output/User.java")
              .setPackageName("com.example.entity")
              .setClassName("User")
              .setTableName("t_user")
              .setDescription("用户实体")
              .setOverwriteExisting(true)
              .setGenerateComments(false)
              .setGenerateToString(false)
              .setGenerateEquals(false)
              .setGenerateHashCode(false)
              .setGenerateGetters(false)
              .setGenerateSetters(false)
              .setGenerateConstructors(false)
              .setEncoding("GBK");

      assertEquals("/templates", config.getTemplatePath());
      assertEquals("/output/User.java", config.getOutputPath());
      assertEquals("com.example.entity", config.getPackageName());
      assertEquals("User", config.getClassName());
      assertEquals("t_user", config.getTableName());
      assertEquals("用户实体", config.getDescription());
      assertTrue(config.isOverwriteExisting());
      assertFalse(config.isGenerateComments());
      assertFalse(config.isGenerateToString());
      assertFalse(config.isGenerateEquals());
      assertFalse(config.isGenerateHashCode());
      assertFalse(config.isGenerateGetters());
      assertFalse(config.isGenerateSetters());
      assertFalse(config.isGenerateConstructors());
      assertEquals("GBK", config.getEncoding());
    }
  }

  @Nested
  @DisplayName("自定义属性测试")
  class CustomPropertiesTest {

    @Test
    @DisplayName("添加和获取自定义属性")
    void testAddAndGetCustomProperty() {
      GeneratorConfig config =
          new GeneratorConfig().addCustomProperty("author", "QAIU").addCustomProperty("version", 1);

      assertEquals("QAIU", config.getCustomProperty("author"));
      assertEquals(1, config.getCustomProperty("version"));
    }

    @Test
    @DisplayName("getCustomPropertyAsString")
    void testGetCustomPropertyAsString() {
      GeneratorConfig config =
          new GeneratorConfig().addCustomProperty("name", "test").addCustomProperty("count", 42);

      assertEquals("test", config.getCustomPropertyAsString("name"));
      assertEquals("42", config.getCustomPropertyAsString("count"));
      assertNull(config.getCustomPropertyAsString("notExist"));
    }

    @Test
    @DisplayName("getCustomPropertyAsString 带默认值")
    void testGetCustomPropertyAsStringWithDefault() {
      GeneratorConfig config = new GeneratorConfig().addCustomProperty("name", "test");

      assertEquals("test", config.getCustomPropertyAsString("name", "default"));
      assertEquals("default", config.getCustomPropertyAsString("notExist", "default"));
    }

    @Test
    @DisplayName("设置自定义属性 Map")
    void testSetCustomProperties() {
      Map<String, Object> props = new HashMap<>();
      props.put("key1", "value1");
      props.put("key2", 123);

      GeneratorConfig config = new GeneratorConfig().setCustomProperties(props);

      assertEquals("value1", config.getCustomProperty("key1"));
      assertEquals(123, config.getCustomProperty("key2"));
    }
  }

  @Nested
  @DisplayName("路径处理测试")
  class PathHandlingTest {

    @Test
    @DisplayName("getOutputDirectory - Unix 路径")
    void testGetOutputDirectoryUnix() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("/home/user/output/User.java");

      assertEquals("/home/user/output", config.getOutputDirectory());
    }

    @Test
    @DisplayName("getOutputDirectory - Windows 路径")
    void testGetOutputDirectoryWindows() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("C:\\Users\\output\\User.java");

      assertEquals("C:\\Users\\output", config.getOutputDirectory());
    }

    @Test
    @DisplayName("getOutputDirectory - 无路径分隔符")
    void testGetOutputDirectoryNoSeparator() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("User.java");

      assertEquals(".", config.getOutputDirectory());
    }

    @Test
    @DisplayName("getOutputDirectory - null 路径")
    void testGetOutputDirectoryNull() {
      GeneratorConfig config = new GeneratorConfig();
      assertNull(config.getOutputDirectory());
    }

    @Test
    @DisplayName("getOutputFileName - Unix 路径")
    void testGetOutputFileNameUnix() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("/home/user/output/User.java");

      assertEquals("User.java", config.getOutputFileName());
    }

    @Test
    @DisplayName("getOutputFileName - 无路径分隔符")
    void testGetOutputFileNameNoSeparator() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("User.java");

      assertEquals("User.java", config.getOutputFileName());
    }

    @Test
    @DisplayName("getOutputFileName - null 路径")
    void testGetOutputFileNameNull() {
      GeneratorConfig config = new GeneratorConfig();
      assertNull(config.getOutputFileName());
    }

    @Test
    @DisplayName("getPackagePath")
    void testGetPackagePath() {
      GeneratorConfig config = new GeneratorConfig().setPackageName("com.example.entity");

      assertEquals("com/example/entity", config.getPackagePath());
    }

    @Test
    @DisplayName("getPackagePath - null 包名")
    void testGetPackagePathNull() {
      GeneratorConfig config = new GeneratorConfig();
      assertNull(config.getPackagePath());
    }
  }

  @Nested
  @DisplayName("验证测试")
  class ValidationTest {

    @Test
    @DisplayName("有效配置返回 true")
    void testValidConfig() {
      GeneratorConfig config = new GeneratorConfig("/output/User.java", "com.example");
      assertTrue(config.isValid());
    }

    @Test
    @DisplayName("缺少 outputPath 返回 false")
    void testMissingOutputPath() {
      GeneratorConfig config = new GeneratorConfig().setPackageName("com.example");
      assertFalse(config.isValid());
    }

    @Test
    @DisplayName("缺少 packageName 返回 false")
    void testMissingPackageName() {
      GeneratorConfig config = new GeneratorConfig().setOutputPath("/output/User.java");
      assertFalse(config.isValid());
    }

    @Test
    @DisplayName("空白 outputPath 返回 false")
    void testBlankOutputPath() {
      GeneratorConfig config = new GeneratorConfig("  ", "com.example");
      assertFalse(config.isValid());
    }

    @Test
    @DisplayName("空白 packageName 返回 false")
    void testBlankPackageName() {
      GeneratorConfig config = new GeneratorConfig("/output", "  ");
      assertFalse(config.isValid());
    }
  }

  @Nested
  @DisplayName("copy 测试")
  class CopyTest {

    @Test
    @DisplayName("复制所有属性")
    void testCopyAllProperties() {
      GeneratorConfig original =
          new GeneratorConfig()
              .setTemplatePath("/templates")
              .setOutputPath("/output/User.java")
              .setPackageName("com.example")
              .setClassName("User")
              .setTableName("t_user")
              .setDescription("用户")
              .setOverwriteExisting(true)
              .setGenerateComments(false)
              .setEncoding("GBK")
              .addCustomProperty("author", "test");

      GeneratorConfig copy = original.copy();

      assertEquals(original.getTemplatePath(), copy.getTemplatePath());
      assertEquals(original.getOutputPath(), copy.getOutputPath());
      assertEquals(original.getPackageName(), copy.getPackageName());
      assertEquals(original.getClassName(), copy.getClassName());
      assertEquals(original.getTableName(), copy.getTableName());
      assertEquals(original.getDescription(), copy.getDescription());
      assertEquals(original.isOverwriteExisting(), copy.isOverwriteExisting());
      assertEquals(original.isGenerateComments(), copy.isGenerateComments());
      assertEquals(original.getEncoding(), copy.getEncoding());
      assertEquals("test", copy.getCustomProperty("author"));
    }

    @Test
    @DisplayName("修改副本不影响原始对象")
    void testCopyIsIndependent() {
      GeneratorConfig original =
          new GeneratorConfig().setOutputPath("/original").addCustomProperty("key", "original");

      GeneratorConfig copy = original.copy();
      copy.setOutputPath("/copy");
      copy.addCustomProperty("key", "copy");

      assertEquals("/original", original.getOutputPath());
      assertEquals("original", original.getCustomProperty("key"));
      assertEquals("/copy", copy.getOutputPath());
      assertEquals("copy", copy.getCustomProperty("key"));
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含主要信息")
    void testToString() {
      GeneratorConfig config =
          new GeneratorConfig("/output", "com.example").setClassName("User").setTableName("t_user");

      String str = config.toString();

      assertTrue(str.contains("outputPath='/output'"));
      assertTrue(str.contains("packageName='com.example'"));
      assertTrue(str.contains("className='User'"));
      assertTrue(str.contains("tableName='t_user'"));
    }
  }
}
