package cn.qaiu.vx.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CastUtil 工具类单元测试
 *
 * @author test
 */
@DisplayName("CastUtil 工具类测试")
class CastUtilTest {

  @Nested
  @DisplayName("基本类型转换测试")
  class BasicTypeCastTest {

    @Test
    @DisplayName("Object 转 String")
    void testCastToString() {
      Object obj = "Hello";
      String result = CastUtil.cast(obj);
      assertEquals("Hello", result);
    }

    @Test
    @DisplayName("Object 转 Integer")
    void testCastToInteger() {
      Object obj = 42;
      Integer result = CastUtil.cast(obj);
      assertEquals(42, result);
    }

    @Test
    @DisplayName("Object 转 Double")
    void testCastToDouble() {
      Object obj = 3.14;
      Double result = CastUtil.cast(obj);
      assertEquals(3.14, result);
    }

    @Test
    @DisplayName("Object 转 Boolean")
    void testCastToBoolean() {
      Object obj = true;
      Boolean result = CastUtil.cast(obj);
      assertTrue(result);
    }

    @Test
    @DisplayName("null 值转换")
    void testCastNull() {
      Object obj = null;
      String result = CastUtil.cast(obj);
      assertNull(result);
    }
  }

  @Nested
  @DisplayName("集合类型转换测试")
  class CollectionTypeCastTest {

    @Test
    @DisplayName("Object 转 List<String>")
    void testCastToList() {
      List<String> original = new ArrayList<>();
      original.add("a");
      original.add("b");

      Object obj = original;
      List<String> result = CastUtil.cast(obj);

      assertEquals(2, result.size());
      assertEquals("a", result.get(0));
      assertEquals("b", result.get(1));
    }

    @Test
    @DisplayName("Object 转 Map<String, Integer>")
    void testCastToMap() {
      Map<String, Integer> original = new HashMap<>();
      original.put("one", 1);
      original.put("two", 2);

      Object obj = original;
      Map<String, Integer> result = CastUtil.cast(obj);

      assertEquals(2, result.size());
      assertEquals(1, result.get("one"));
      assertEquals(2, result.get("two"));
    }
  }

  @Nested
  @DisplayName("自定义类型转换测试")
  class CustomTypeCastTest {

    @Test
    @DisplayName("Object 转自定义类")
    void testCastToCustomClass() {
      Person person = new Person("John", 30);
      Object obj = person;

      Person result = CastUtil.cast(obj);

      assertSame(person, result);
      assertEquals("John", result.name);
      assertEquals(30, result.age);
    }

    @Test
    @DisplayName("子类转父类")
    void testCastChildToParent() {
      Student student = new Student("Alice", 20, "CS");
      Object obj = student;

      Person result = CastUtil.cast(obj);

      assertSame(student, result);
      assertEquals("Alice", result.name);
    }

    @Test
    @DisplayName("父类转子类 - 类型不匹配时抛出 ClassCastException")
    void testCastParentToChild() {
      Person person = new Person("Bob", 25);
      Object obj = person;

      // cast 操作本身会抛出 ClassCastException
      assertThrows(
          ClassCastException.class,
          () -> {
            Student result = CastUtil.cast(obj);
            // 触发类型检查
            String name = result.name;
          });
    }
  }

  @Nested
  @DisplayName("泛型擦除场景测试")
  class GenericErasureTest {

    @Test
    @DisplayName("泛型 List 的类型安全转换")
    void testGenericListCast() {
      List<Object> objects = new ArrayList<>();
      objects.add("string");
      objects.add(123);

      // 使用 CastUtil 转换 - 运行时不会检查泛型类型
      List<String> strings = CastUtil.cast(objects);

      // 第一个元素是 String，可以正常访问
      assertEquals("string", strings.get(0));

      // 第二个元素实际是 Integer，访问时会有类型问题
      assertThrows(
          ClassCastException.class,
          () -> {
            String s = strings.get(1); // 运行时抛出 ClassCastException
          });
    }

    @Test
    @DisplayName("正确使用泛型 List 转换")
    void testCorrectGenericListCast() {
      List<String> original = new ArrayList<>();
      original.add("a");
      original.add("b");

      Object obj = original;
      List<String> result = CastUtil.cast(obj);

      // 类型一致，不会有问题
      assertEquals("a", result.get(0));
      assertEquals("b", result.get(1));
    }
  }

  // 测试用类
  private static class Person {
    String name;
    int age;

    Person(String name, int age) {
      this.name = name;
      this.age = age;
    }
  }

  private static class Student extends Person {
    String major;

    Student(String name, int age, String major) {
      super(name, age);
      this.major = major;
    }
  }
}
