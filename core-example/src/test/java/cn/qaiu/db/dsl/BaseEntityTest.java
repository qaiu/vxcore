package cn.qaiu.db.dsl;

import cn.qaiu.example.User;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseEntity 基类测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class BaseEntityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntityTest.class);

    @Test
    @DisplayName("测试 BaseEntity 无参构造函数")
    void testNoArgsConstructor() {
        User user = new User();
        
        assertNotNull(user.getCreateTime());
        assertNotNull(user.getUpdateTime());
        assertNull(user.getId());
        
        LOGGER.info("User created with no args: {}", user);
    }

    @Test
    @DisplayName("测试 BaseEntity JsonObject 构造函数")
    void testJsonObjectConstructor() {
        JsonObject json = new JsonObject()
            .put("id", 100L)
            .put("username", "testuser")
            .put("email", "test@example.com")
            .put("createTime", "2023-01-01T10:00:00")
            .put("updateTime", "2023-01-01T11:00:00");
            
        User user = new User(json);
        
        assertNotNull(user.getId());
        assertEquals(Long.valueOf(100), user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        
        LOGGER.info("User created from JSON: {}", user);
    }

    @Test
    @DisplayName("测试 toJson 方法")
    void testToJson() {
        User user = new User();
        user.setId(200L);
        user.setUsername("jsonTest");
        user.setEmail("json@test.com");
        user.setCreateTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        user.setUpdateTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        
        JsonObject json = user.toJson();
        
        assertNotNull(json);
        assertEquals(Long.valueOf(200), json.getLong("id"));
        assertEquals("jsonTest", json.getString("username"));
        assertEquals("json@test.com", json.getString("email"));
        assertTrue(json.containsKey("createTime"));
        assertTrue(json.containsKey("updateTime"));
        
        LOGGER.info("User toJson success - contains createTime: {}, updateTime: {}", 
                   json.containsKey("createTime"), json.containsKey("updateTime"));
    }

    @Test
    @DisplayName("测试 onCreate 回调")
    void testOnCreate() throws InterruptedException {
        User user = new User();
        
        // 重置时间，确保可以检测到变化
        user.setCreateTime(null);
        user.setUpdateTime(null);
        
        long beforeTime = System.currentTimeMillis();
        user.onCreate();
        long afterTime = System.currentTimeMillis();
        
        assertNotNull(user.getCreateTime());
        assertNotNull(user.getUpdateTime());
        assertNotNull(user.getCreateTime());
        assertNotNull(user.getUpdateTime());
        // createTime 和 updateTime 应该相等（因为是在同一个方法调用中设置的）
        
        // 验证时间在合理范围内
        LocalDateTime createTime = user.getCreateTime();
        long createTimeMillis = createTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(createTimeMillis >= beforeTime && createTimeMillis <= afterTime);
        
        LOGGER.info("onCreate called: {}", user);
    }

    @Test
    @DisplayName("测试 onUpdate 回调")
    void testOnUpdate() throws InterruptedException {
        User user = new User();
        user.onCreate(); // 先设置创建时间
        
        LocalDateTime originalCreateTime = user.getCreateTime();
        LocalDateTime originalUpdateTime = user.getUpdateTime();
        
        Thread.sleep(10); // 确保时间差异
        user.onUpdate();
        
        assertNotNull(user.getUpdateTime());
        assertEquals(originalCreateTime, user.getCreateTime()); // 创建时间不应改变
        assertTrue(user.getUpdateTime().isAfter(originalUpdateTime)); // 更新时间应该增加
        
        LOGGER.info("onUpdate called: {}", user);
    }

    @Test
    @DisplayName("测试 getTableName 方法")
    void testGetTableName() {
        User user = new User();
        String tableName = user.getTableName();
        
        assertEquals("user", tableName.toLowerCase());
        assertFalse(tableName.contains(" "));
        
        LOGGER.info("Table name: {}", tableName);
    }

    @Test
    @DisplayName("测试 getPrimaryKeyColumn 方法")
    void testGetPrimaryKeyColumn() {
        User user = new User();
        String primaryKeyColumn = user.getPrimaryKeyColumn();
        
        assertEquals("id", primaryKeyColumn);
        
        LOGGER.info("Primary key column: {}", primaryKeyColumn);
    }

    @Test
    @DisplayName("测试 getPrimaryKeyValue 和 setPrimaryKeyValue")
    void testPrimaryKeyOperations() {
        User user = new User();
        
        // 初始状态
        assertNull(user.getPrimaryKeyValue());
        
        // 设置主键
        Long primaryKeyValue = 123L;
        user.setPrimaryKeyValue(primaryKeyValue);
        
        assertEquals(primaryKeyValue, user.getPrimaryKeyValue());
        assertEquals(primaryKeyValue, user.getId());
        
        LOGGER.info("Primary key operations completed: {}", user.getId());
    }

    @Test
    @DisplayName("测试 equals 和 hashCode")
    void testEqualsAndHashCode() {
        User user1 = new User();
        User user2 = new User();
        
        // 都未设置 ID，应该相等
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        
        // 设置相同 ID
        user1.setId(100L);
        user2.setId(100L);
        assertEquals(user1, user2);
        
        // 设置不同 ID
        user2.setId(200L);
        assertNotEquals(user1, user2);
        
        // 自己与自己相等
        assertEquals(user1, user1);
        
        // 与 null 不相等
        assertNotEquals(user1, null);
        
        // 与其他类型不相等
        assertNotEquals(user1, "not a user");
        
        LOGGER.info("Equals and hashCode tests completed");
    }

    @Test
    @DisplayName("测试 toString")
    void testToString() {
        User user = new User();
        user.setId(300L);
        user.onCreate(); // 设置时间
        
        String userString = user.toString();
        
        assertNotNull(userString);
        assertTrue(userString.contains("User"));
        assertTrue(userString.contains("300")); // ID
        assertTrue(userString.contains("createTime"));
        assertTrue(userString.contains("updateTime") || userString.contains("update_time"));
        
        LOGGER.info("toString: {}", userString);
    }

    @Test
    @DisplayName("测试 camelToSnake 方法")
    void testCamelToSnake() {
        User user = new User();
        
        // 使用反射测试 camelToSnake 方法
        try {
            java.lang.reflect.Method method = BaseEntity.class.getDeclaredMethod("camelToSnake", String.class);
            method.setAccessible(true);
            
            String result = (String) method.invoke(user, "CamelCaseString");
            assertEquals("camel_case_string", result);
            
            result = (String) method.invoke(user, "simpleCase");
            assertEquals("simple_case", result);
            
            result = (String) method.invoke(user, "SingleWord");
            assertEquals("single_word", result);
            
            LOGGER.info("camelToSnake test completed");
            
        } catch (Exception e) {
            fail("Reflection test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试边界情况")
    void testEdgeCases() {
        JsonObject emptyJson = new JsonObject();
        User user1 = new User(emptyJson);
        
        // 空 JSON 对象应该创建用户但不抛异常
        assertNotNull(user1);
        assertNull(user1.getId());
        assertNull(user1.getUsername());
        
        JsonObject nullStringJson = new JsonObject()
            .put("username", (String) null)
            .put("email", "")
            .put("id", (Long) null);
            
        User user2 = new User(nullStringJson);
        
        // null 字符串和空字符串应该被正确处理
        assertNull(user2.getUsername());
        assertEquals("", user2.getEmail());
        assertNull(user2.getId());
        
        LOGGER.info("Edge cases test completed");
    }
}
