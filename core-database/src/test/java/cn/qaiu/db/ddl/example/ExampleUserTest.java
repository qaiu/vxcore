package cn.qaiu.db.ddl.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExampleUser测试用例
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("ExampleUser测试")
public class ExampleUserTest {

    /**
     * 测试ExampleUser的注解配置
     */
    @Test
    @DisplayName("测试ExampleUser的注解配置")
    public void testExampleUserAnnotations() {
        // 验证类级别的注解
        assertTrue(ExampleUser.class.isAnnotationPresent(DdlTable.class));
        assertTrue(ExampleUser.class.isAnnotationPresent(RowMapped.class));

        DdlTable ddlTable = ExampleUser.class.getAnnotation(DdlTable.class);
        assertNotNull(ddlTable);
        assertEquals("example_user", ddlTable.value());
        assertEquals("id", ddlTable.keyFields());
        assertEquals(1, ddlTable.version());
        assertTrue(ddlTable.autoSync());
        assertEquals("示例用户表", ddlTable.comment());
        assertEquals("utf8mb4", ddlTable.charset());
        assertEquals("utf8mb4_unicode_ci", ddlTable.collate());
        assertEquals("InnoDB", ddlTable.engine());
    }

    /**
     * 测试ExampleUser的字段注解
     */
    @Test
    @DisplayName("测试ExampleUser的字段注解")
    public void testExampleUserFieldAnnotations() {
        try {
            // 测试id字段
            Field idField = ExampleUser.class.getDeclaredField("id");
            assertTrue(idField.isAnnotationPresent(DdlColumn.class));
            
            DdlColumn idColumn = idField.getAnnotation(DdlColumn.class);
            assertEquals("BIGINT", idColumn.type());
            assertTrue(idColumn.autoIncrement());
            assertFalse(idColumn.nullable());
            assertEquals("用户ID", idColumn.comment());

            // 测试username字段
            Field usernameField = ExampleUser.class.getDeclaredField("username");
            assertTrue(usernameField.isAnnotationPresent(DdlColumn.class));
            
            DdlColumn usernameColumn = usernameField.getAnnotation(DdlColumn.class);
            assertEquals("VARCHAR", usernameColumn.type());
            assertEquals(50, usernameColumn.length());
            assertFalse(usernameColumn.nullable());
            assertEquals("username", usernameColumn.uniqueKey());
            assertEquals("用户名", usernameColumn.comment());

            // 测试email字段
            Field emailField = ExampleUser.class.getDeclaredField("email");
            assertTrue(emailField.isAnnotationPresent(DdlColumn.class));
            
            DdlColumn emailColumn = emailField.getAnnotation(DdlColumn.class);
            assertEquals("VARCHAR", emailColumn.type());
            assertEquals(100, emailColumn.length());
            assertFalse(emailColumn.nullable());
            assertEquals("email", emailColumn.uniqueKey());
            assertEquals("idx_email", emailColumn.indexName());
            assertEquals("邮箱地址", emailColumn.comment());

        } catch (NoSuchFieldException e) {
            fail("字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试ExampleUser的构造函数
     */
    @Test
    @DisplayName("测试ExampleUser的构造函数")
    public void testExampleUserConstructors() {
        // 测试无参构造函数
        ExampleUser user1 = new ExampleUser();
        assertNotNull(user1);
        assertNull(user1.getId());
        assertNull(user1.getUsername());

        // 测试JsonObject构造函数
        JsonObject json = new JsonObject()
            .put("id", 1L)
            .put("username", "testuser")
            .put("email", "test@example.com")
            .put("password", "password123")
            .put("age", 25)
            .put("balance", "1000.50")
            .put("active", true)
            .put("createTime", "2023-01-01T10:00:00")
            .put("updateTime", "2023-01-02T10:00:00")
            .put("remark", "测试用户");

        ExampleUser user2 = new ExampleUser(json);
        assertNotNull(user2);
        assertEquals(Long.valueOf(1L), user2.getId());
        assertEquals("testuser", user2.getUsername());
        assertEquals("test@example.com", user2.getEmail());
        assertEquals("password123", user2.getPassword());
        assertEquals(Integer.valueOf(25), user2.getAge());
        assertNotNull(user2.getBalance());
        assertEquals(0, user2.getBalance().compareTo(new java.math.BigDecimal("1000.50")));
        assertTrue(user2.getActive());
        assertNotNull(user2.getCreateTime());
        assertNotNull(user2.getUpdateTime());
        assertEquals("测试用户", user2.getRemark());
    }

    /**
     * 测试ExampleUser的getter和setter方法
     */
    @Test
    @DisplayName("测试ExampleUser的getter和setter方法")
    public void testExampleUserGettersAndSetters() {
        ExampleUser user = new ExampleUser();

        // 测试id
        user.setId(1L);
        assertEquals(Long.valueOf(1L), user.getId());

        // 测试username
        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());

        // 测试email
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());

        // 测试password
        user.setPassword("password123");
        assertEquals("password123", user.getPassword());

        // 测试age
        user.setAge(25);
        assertEquals(Integer.valueOf(25), user.getAge());

        // 测试balance
        java.math.BigDecimal balance = new java.math.BigDecimal("1000.50");
        user.setBalance(balance);
        assertEquals(balance, user.getBalance());

        // 测试active
        user.setActive(true);
        assertTrue(user.getActive());

        // 测试createTime
        LocalDateTime createTime = LocalDateTime.now();
        user.setCreateTime(createTime);
        assertEquals(createTime, user.getCreateTime());

        // 测试updateTime
        LocalDateTime updateTime = LocalDateTime.now();
        user.setUpdateTime(updateTime);
        assertEquals(updateTime, user.getUpdateTime());

        // 测试remark
        user.setRemark("测试用户");
        assertEquals("测试用户", user.getRemark());
    }

    /**
     * 测试ExampleUser的字段类型
     */
    @Test
    @DisplayName("测试ExampleUser的字段类型")
    public void testExampleUserFieldTypes() {
        try {
            // 验证字段类型
            Field idField = ExampleUser.class.getDeclaredField("id");
            assertEquals(Long.class, idField.getType());

            Field usernameField = ExampleUser.class.getDeclaredField("username");
            assertEquals(String.class, usernameField.getType());

            Field emailField = ExampleUser.class.getDeclaredField("email");
            assertEquals(String.class, emailField.getType());

            Field passwordField = ExampleUser.class.getDeclaredField("password");
            assertEquals(String.class, passwordField.getType());

            Field ageField = ExampleUser.class.getDeclaredField("age");
            assertEquals(Integer.class, ageField.getType());

            Field balanceField = ExampleUser.class.getDeclaredField("balance");
            assertEquals(java.math.BigDecimal.class, balanceField.getType());

            Field activeField = ExampleUser.class.getDeclaredField("active");
            assertEquals(Boolean.class, activeField.getType());

            Field createTimeField = ExampleUser.class.getDeclaredField("createTime");
            assertEquals(LocalDateTime.class, createTimeField.getType());

            Field updateTimeField = ExampleUser.class.getDeclaredField("updateTime");
            assertEquals(LocalDateTime.class, updateTimeField.getType());

            Field remarkField = ExampleUser.class.getDeclaredField("remark");
            assertEquals(String.class, remarkField.getType());

        } catch (NoSuchFieldException e) {
            fail("字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试ExampleUser的JsonObject构造函数处理null值
     */
    @Test
    @DisplayName("测试ExampleUser的JsonObject构造函数处理null值")
    public void testExampleUserJsonConstructorWithNulls() {
        JsonObject json = new JsonObject()
            .put("id", (Long) null)
            .put("username", (String) null)
            .put("email", (String) null)
            .put("password", (String) null)
            .put("age", (Integer) null)
            .put("balance", (String) null)
            .put("active", (Boolean) null)
            .put("createTime", (String) null)
            .put("updateTime", (String) null)
            .put("remark", (String) null);

        ExampleUser user = new ExampleUser(json);
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getAge());
        assertNull(user.getBalance());
        assertNull(user.getActive());
        assertNull(user.getCreateTime());
        assertNull(user.getUpdateTime());
        assertNull(user.getRemark());
    }

    /**
     * 测试ExampleUser的JsonObject构造函数处理无效日期
     */
    @Test
    @DisplayName("测试ExampleUser的JsonObject构造函数处理无效日期")
    public void testExampleUserJsonConstructorWithInvalidDates() {
        JsonObject json = new JsonObject()
            .put("createTime", "invalid-date")
            .put("updateTime", "invalid-date");

        // 应该抛出异常或处理无效日期
        assertThrows(Exception.class, () -> {
            new ExampleUser(json);
        });
    }
}
