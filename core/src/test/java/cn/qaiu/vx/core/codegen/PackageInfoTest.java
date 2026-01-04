package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PackageInfo 单元测试
 *
 * @author test
 */
@DisplayName("PackageInfo 测试")
class PackageInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      PackageInfo info = new PackageInfo();
      assertNull(info.getBasePackage());
      assertNull(info.getEntityPackage());
      assertNotNull(info.getImports());
      assertTrue(info.getImports().isEmpty());
    }

    @Test
    @DisplayName("带基础包名的构造函数 - 自动初始化子包")
    void testConstructorWithBasePackage() {
      PackageInfo info = new PackageInfo("com.example");
      
      assertEquals("com.example", info.getBasePackage());
      assertEquals("com.example.entity", info.getEntityPackage());
      assertEquals("com.example.dao", info.getDaoPackage());
      assertEquals("com.example.service", info.getServicePackage());
      assertEquals("com.example.controller", info.getControllerPackage());
      assertEquals("com.example.config", info.getConfigPackage());
      assertEquals("com.example.util", info.getUtilPackage());
      assertEquals("com.example.exception", info.getExceptionPackage());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有包名")
    void testFluentSetters() {
      PackageInfo info = new PackageInfo()
          .setBasePackage("cn.qaiu")
          .setEntityPackage("cn.qaiu.model")
          .setDaoPackage("cn.qaiu.repository")
          .setServicePackage("cn.qaiu.svc")
          .setControllerPackage("cn.qaiu.api")
          .setConfigPackage("cn.qaiu.conf")
          .setUtilPackage("cn.qaiu.helper")
          .setExceptionPackage("cn.qaiu.error");

      assertEquals("cn.qaiu", info.getBasePackage());
      assertEquals("cn.qaiu.model", info.getEntityPackage());
      assertEquals("cn.qaiu.repository", info.getDaoPackage());
      assertEquals("cn.qaiu.svc", info.getServicePackage());
      assertEquals("cn.qaiu.api", info.getControllerPackage());
      assertEquals("cn.qaiu.conf", info.getConfigPackage());
      assertEquals("cn.qaiu.helper", info.getUtilPackage());
      assertEquals("cn.qaiu.error", info.getExceptionPackage());
    }
  }

  @Nested
  @DisplayName("getPackageName 测试")
  class PackageNameTest {

    @Test
    @DisplayName("返回与 basePackage 相同的值")
    void testPackageNameEqualsBasePackage() {
      PackageInfo info = new PackageInfo("com.test");
      assertEquals(info.getBasePackage(), info.getPackageName());
    }
  }

  @Nested
  @DisplayName("导入管理测试")
  class ImportManagementTest {

    @Test
    @DisplayName("添加导入")
    void testAddImport() {
      PackageInfo info = new PackageInfo();
      info.addImport("java.util.List");
      info.addImport("java.util.Map");
      
      assertEquals(2, info.getImports().size());
      assertTrue(info.getImports().contains("java.util.List"));
      assertTrue(info.getImports().contains("java.util.Map"));
    }

    @Test
    @DisplayName("重复导入不会添加")
    void testNoDuplicateImport() {
      PackageInfo info = new PackageInfo();
      info.addImport("java.util.List");
      info.addImport("java.util.List");
      
      assertEquals(1, info.getImports().size());
    }

    @Test
    @DisplayName("设置导入列表")
    void testSetImports() {
      PackageInfo info = new PackageInfo();
      List<String> imports = Arrays.asList("java.util.List", "java.util.Map");
      info.setImports(imports);
      
      assertEquals(2, info.getImports().size());
    }
  }

  @Nested
  @DisplayName("包路径转换测试")
  class PackagePathTest {

    @Test
    @DisplayName("将点分隔转换为路径分隔")
    void testPackagePaths() {
      PackageInfo info = new PackageInfo("com.example.app");
      
      assertEquals("com/example/app", info.getBasePackagePath());
      assertEquals("com/example/app/entity", info.getEntityPackagePath());
      assertEquals("com/example/app/dao", info.getDaoPackagePath());
      assertEquals("com/example/app/service", info.getServicePackagePath());
      assertEquals("com/example/app/controller", info.getControllerPackagePath());
      assertEquals("com/example/app/config", info.getConfigPackagePath());
      assertEquals("com/example/app/util", info.getUtilPackagePath());
      assertEquals("com/example/app/exception", info.getExceptionPackagePath());
    }

    @Test
    @DisplayName("null 包名返回 null 路径")
    void testNullPackagePath() {
      PackageInfo info = new PackageInfo();
      
      assertNull(info.getBasePackagePath());
      assertNull(info.getEntityPackagePath());
    }
  }

  @Nested
  @DisplayName("getAllPackagePaths 测试")
  class AllPackagePathsTest {

    @Test
    @DisplayName("返回所有非空包")
    void testGetAllPackagePaths() {
      PackageInfo info = new PackageInfo("com.example");
      List<String> paths = info.getAllPackagePaths();
      
      assertEquals(8, paths.size());
      assertTrue(paths.contains("com.example"));
      assertTrue(paths.contains("com.example.entity"));
      assertTrue(paths.contains("com.example.service"));
    }

    @Test
    @DisplayName("空包信息返回空列表")
    void testEmptyPackageInfo() {
      PackageInfo info = new PackageInfo();
      List<String> paths = info.getAllPackagePaths();
      
      assertTrue(paths.isEmpty());
    }
  }

  @Nested
  @DisplayName("isComplete 测试")
  class IsCompleteTest {

    @Test
    @DisplayName("完整包结构返回 true")
    void testCompletePackageInfo() {
      PackageInfo info = new PackageInfo("com.example");
      assertTrue(info.isComplete());
    }

    @Test
    @DisplayName("缺少必要包返回 false")
    void testIncompletePackageInfo() {
      PackageInfo info = new PackageInfo()
          .setBasePackage("com.example")
          .setEntityPackage("com.example.entity");
      
      assertFalse(info.isComplete());
    }

    @Test
    @DisplayName("空 basePackage 返回 false")
    void testEmptyBasePackage() {
      PackageInfo info = new PackageInfo("");
      assertFalse(info.isComplete());
    }
  }

  @Nested
  @DisplayName("getPackageCount 测试")
  class PackageCountTest {

    @Test
    @DisplayName("完整包结构返回 8")
    void testFullPackageCount() {
      PackageInfo info = new PackageInfo("com.example");
      assertEquals(8, info.getPackageCount());
    }

    @Test
    @DisplayName("空包信息返回 0")
    void testEmptyPackageCount() {
      PackageInfo info = new PackageInfo();
      assertEquals(0, info.getPackageCount());
    }

    @Test
    @DisplayName("部分设置返回正确数量")
    void testPartialPackageCount() {
      PackageInfo info = new PackageInfo()
          .setBasePackage("com.example")
          .setEntityPackage("com.example.entity")
          .setServicePackage("com.example.service");
      
      assertEquals(3, info.getPackageCount());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含主要信息")
    void testToString() {
      PackageInfo info = new PackageInfo("com.example");
      info.addImport("java.util.List");
      
      String str = info.toString();
      
      assertTrue(str.contains("basePackage='com.example'"));
      assertTrue(str.contains("entityPackage='com.example.entity'"));
      assertTrue(str.contains("imports=1"));
    }
  }
}
