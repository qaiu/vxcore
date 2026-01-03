package cn.qaiu.db.dsl.common;

import static cn.qaiu.db.dsl.common.QueryCondition.ConditionType.*;
import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * QueryCondition 测试类
 *
 * @author QAIU
 */
@DisplayName("QueryCondition 测试")
class QueryConditionTest {

  @Nested
  @DisplayName("ConditionType 枚举测试")
  class ConditionTypeTest {

    @Test
    @DisplayName("测试所有条件类型的操作符")
    void testConditionTypeOperators() {
      assertEquals("=", EQUAL.getOperator());
      assertEquals("!=", NOT_EQUAL.getOperator());
      assertEquals(">", GREATER_THAN.getOperator());
      assertEquals(">=", GREATER_THAN_EQ.getOperator());
      assertEquals("<", LESS_THAN.getOperator());
      assertEquals("<=", LESS_THAN_EQ.getOperator());
      assertEquals("LIKE", LIKE.getOperator());
      assertEquals("NOT LIKE", NOT_LIKE.getOperator());
      assertEquals("IN", IN.getOperator());
      assertEquals("NOT IN", NOT_IN.getOperator());
      assertEquals("BETWEEN", BETWEEN.getOperator());
      assertEquals("NOT BETWEEN", NOT_BETWEEN.getOperator());
      assertEquals("IS NULL", IS_NULL.getOperator());
      assertEquals("IS NOT NULL", IS_NOT_NULL.getOperator());
      assertEquals("AND", AND.getOperator());
      assertEquals("OR", OR.getOperator());
    }
  }

  @Nested
  @DisplayName("工厂方法测试")
  class FactoryMethodTest {

    @Test
    @DisplayName("测试eq方法")
    void testEq() {
      QueryCondition condition = QueryCondition.eq("name", "test");

      assertEquals("name", condition.getField());
      assertEquals(EQUAL, condition.getType());
      assertEquals("test", condition.getValue());
    }

    @Test
    @DisplayName("测试ne方法")
    void testNe() {
      QueryCondition condition = QueryCondition.ne("status", "deleted");

      assertEquals("status", condition.getField());
      assertEquals(NOT_EQUAL, condition.getType());
      assertEquals("deleted", condition.getValue());
    }

    @Test
    @DisplayName("测试gt方法")
    void testGt() {
      QueryCondition condition = QueryCondition.gt("age", 18);

      assertEquals("age", condition.getField());
      assertEquals(GREATER_THAN, condition.getType());
      assertEquals(18, condition.getValue());
    }

    @Test
    @DisplayName("测试gte方法")
    void testGte() {
      QueryCondition condition = QueryCondition.gte("score", 60);

      assertEquals("score", condition.getField());
      assertEquals(GREATER_THAN_EQ, condition.getType());
      assertEquals(60, condition.getValue());
    }

    @Test
    @DisplayName("测试lt方法")
    void testLt() {
      QueryCondition condition = QueryCondition.lt("price", 100.0);

      assertEquals("price", condition.getField());
      assertEquals(LESS_THAN, condition.getType());
      assertEquals(100.0, condition.getValue());
    }

    @Test
    @DisplayName("测试lte方法")
    void testLte() {
      QueryCondition condition = QueryCondition.lte("quantity", 50);

      assertEquals("quantity", condition.getField());
      assertEquals(LESS_THAN_EQ, condition.getType());
      assertEquals(50, condition.getValue());
    }

    @Test
    @DisplayName("测试like方法")
    void testLike() {
      QueryCondition condition = QueryCondition.like("title", "%Java%");

      assertEquals("title", condition.getField());
      assertEquals(LIKE, condition.getType());
      assertEquals("%Java%", condition.getValue());
    }

    @Test
    @DisplayName("测试in方法")
    void testIn() {
      QueryCondition condition = QueryCondition.in("category", 1, 2, 3);

      assertEquals("category", condition.getField());
      assertEquals(IN, condition.getType());
      assertArrayEquals(new Object[] {1, 2, 3}, (Object[]) condition.getValue());
    }

    @Test
    @DisplayName("测试between方法")
    void testBetween() {
      QueryCondition condition = QueryCondition.between("createTime", "2024-01-01", "2024-12-31");

      assertEquals("createTime", condition.getField());
      assertEquals(BETWEEN, condition.getType());
      Object[] values = (Object[]) condition.getValue();
      assertEquals("2024-01-01", values[0]);
      assertEquals("2024-12-31", values[1]);
    }

    @Test
    @DisplayName("测试isNull方法")
    void testIsNull() {
      QueryCondition condition = QueryCondition.isNull("deletedAt");

      assertEquals("deletedAt", condition.getField());
      assertEquals(IS_NULL, condition.getType());
      assertNull(condition.getValue());
    }

    @Test
    @DisplayName("测试isNotNull方法")
    void testIsNotNull() {
      QueryCondition condition = QueryCondition.isNotNull("email");

      assertEquals("email", condition.getField());
      assertEquals(IS_NOT_NULL, condition.getType());
      assertNull(condition.getValue());
    }
  }

  @Nested
  @DisplayName("ConditionGroup 测试")
  class ConditionGroupTest {

    @Test
    @DisplayName("测试andGroup创建")
    void testAndGroup() {
      QueryCondition.ConditionGroup group = QueryCondition.andGroup();

      assertEquals(AND, group.getConnective());
      assertTrue(group.getConditions().isEmpty());
    }

    @Test
    @DisplayName("测试orGroup创建")
    void testOrGroup() {
      QueryCondition.ConditionGroup group = QueryCondition.orGroup();

      assertEquals(OR, group.getConnective());
      assertTrue(group.getConditions().isEmpty());
    }

    @Test
    @DisplayName("测试addCondition方法")
    void testAddCondition() {
      QueryCondition.ConditionGroup group =
          QueryCondition.andGroup()
              .addCondition("name", EQUAL, "test")
              .addCondition("age", GREATER_THAN, 18);

      assertEquals(2, group.getConditions().size());
    }

    @Test
    @DisplayName("测试addBetweenCondition方法")
    void testAddBetweenCondition() {
      QueryCondition.ConditionGroup group =
          QueryCondition.andGroup().addBetweenCondition("age", 18, 65);

      assertEquals(1, group.getConditions().size());
      assertEquals(BETWEEN, group.getConditions().get(0).getType());
    }

    @Test
    @DisplayName("测试addInCondition方法")
    void testAddInCondition() {
      QueryCondition.ConditionGroup group =
          QueryCondition.orGroup().addInCondition("status", "active", "pending");

      assertEquals(1, group.getConditions().size());
      assertEquals(IN, group.getConditions().get(0).getType());
    }

    @Test
    @DisplayName("测试addLikeCondition方法")
    void testAddLikeCondition() {
      QueryCondition.ConditionGroup group =
          QueryCondition.andGroup().addLikeCondition("name", "%test%");

      assertEquals(1, group.getConditions().size());
      assertEquals(LIKE, group.getConditions().get(0).getType());
      assertEquals("%test%", group.getConditions().get(0).getValue());
    }

    @Test
    @DisplayName("测试链式添加QueryCondition对象")
    void testAddConditionObject() {
      QueryCondition condition = QueryCondition.eq("id", 1);
      QueryCondition.ConditionGroup group = QueryCondition.andGroup().addCondition(condition);

      assertEquals(1, group.getConditions().size());
      assertEquals(condition, group.getConditions().get(0));
    }
  }

  @Nested
  @DisplayName("构造函数和序列化测试")
  class ConstructorAndSerializationTest {

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
      QueryCondition condition = new QueryCondition();

      assertNull(condition.getField());
      assertNull(condition.getType());
      assertNull(condition.getValue());
      assertNotNull(condition.getGroups());
      assertTrue(condition.getGroups().isEmpty());
    }

    @Test
    @DisplayName("测试三参数构造函数")
    void testThreeArgConstructor() {
      QueryCondition condition = new QueryCondition("field", EQUAL, "value");

      assertEquals("field", condition.getField());
      assertEquals(EQUAL, condition.getType());
      assertEquals("value", condition.getValue());
    }

    @Test
    @DisplayName("测试toJson方法")
    void testToJson() {
      QueryCondition condition = QueryCondition.eq("name", "test");
      JsonObject json = condition.toJson();

      assertEquals("name", json.getString("field"));
      // type 被序列化为字符串
      assertEquals(EQUAL.name(), json.getString("type"));
      assertEquals("test", json.getValue("value"));
    }

    @Test
    @DisplayName("测试addGroup方法")
    void testAddGroup() {
      QueryCondition condition = QueryCondition.eq("id", 1);
      QueryCondition.ConditionGroup group =
          QueryCondition.andGroup().addCondition("status", EQUAL, "active");

      condition.addGroup(group);

      assertEquals(1, condition.getGroups().size());
      assertEquals(group, condition.getGroups().get(0));
    }

    @Test
    @DisplayName("测试链式addGroup")
    void testChainedAddGroup() {
      QueryCondition condition =
          new QueryCondition()
              .addGroup(QueryCondition.andGroup().addCondition("a", EQUAL, 1))
              .addGroup(QueryCondition.orGroup().addCondition("b", EQUAL, 2));

      assertEquals(2, condition.getGroups().size());
    }
  }

  @Nested
  @DisplayName("Getter和Setter测试")
  class GetterSetterTest {

    @Test
    @DisplayName("测试setField和getField")
    void testFieldGetterSetter() {
      QueryCondition condition = new QueryCondition();
      condition.setField("testField");

      assertEquals("testField", condition.getField());
    }

    @Test
    @DisplayName("测试setType和getType")
    void testTypeGetterSetter() {
      QueryCondition condition = new QueryCondition();
      condition.setType(LIKE);

      assertEquals(LIKE, condition.getType());
    }

    @Test
    @DisplayName("测试setValue和getValue")
    void testValueGetterSetter() {
      QueryCondition condition = new QueryCondition();
      condition.setValue("testValue");

      assertEquals("testValue", condition.getValue());
    }
  }
}
