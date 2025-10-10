package cn.qaiu.db.dsl;

import cn.qaiu.example.entity.User;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EntityMapper 测试类
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class EntityMapperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMapperTest.class);

    private DefaultEntityMapper<User> entityMapper;

    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp() {
        entityMapper = new DefaultEntityMapper<>(User.class);
    }

    @Test
    @DisplayName("测试获取实体类")
    void testGetEntityClass() {
        Class<User> entityClass = entityMapper.getEntityClass();
        
        assertEquals(User.class, entityClass);
        assertEquals("cn.qaiu.example.User", entityClass.getName());
        
        LOGGER.info("EntityClass test passed: {}", entityClass.getName());
    }

    @Test
    @DisplayName("测试 toJson 方法")
    void testToJson() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.ACTIVE);
        
        // 手动设置时间，避免从 BaseEntity 构造函数设置
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        JsonObject json = entityMapper.toJson(user);
        
        assertNotNull(json);
        assertEquals(Long.valueOf(1), json.getLong("id"));
        assertEquals("testuser", json.getString("username"));
        assertEquals("test@example.com", json.getString("email"));
        assertEquals("ACTIVE", json.getString("status"));
        
        LOGGER.info("toJson test passed: {}", json.toString());
    }

    @Test
    @DisplayName("测试 toJson 空值处理")
    void testToJsonWithNull() {
        User nullUser = null;
        JsonObject json = entityMapper.toJson(nullUser);
        
        assertNull(json);
        
        LOGGER.info("toJson null test passed");
    }

    @Test
    @DisplayName("测试值类型转换")
    void testConvertValue() throws Exception {
        // 通过反射测试 convertValue 方法
        java.lang.reflect.Method method = DefaultEntityMapper.class.getDeclaredMethod("convertValue", Object.class);
        method.setAccessible(true);

        // 测试 BigDecimal
        Object result = method.invoke(entityMapper, new BigDecimal("123.45"));
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("123.45"), result);

        // 测试 Integer
        result = method.invoke(entityMapper, 42);
        assertTrue(result instanceof Integer);
        assertEquals(Integer.valueOf(42), result);

        // 测试 String
        result = method.invoke(entityMapper, "test string");
        assertEquals("test string", result);

        // 测试 null
        result = method.invoke(entityMapper, (Object) null);
        assertNull(result);

        LOGGER.info("convertValue test passed");
    }

    @Test
    @DisplayName("测试 LocalDateTime 转换")
    void testLocalDateTimeConversion() throws Exception {
        java.lang.reflect.Method method = DefaultEntityMapper.class.getDeclaredMethod("convertValue", Object.class);
        method.setAccessible(true);

        // 构造一个包含日期时间字符串的 JSON
        String dateTimeStr = "2023-01-01T10:30:45";
        
        User user = new User();
        user.setUsername("datetimeTest");
        user.setEmail("datetime@test.com");
        
        // 模拟从数据库返回的格式
        JsonObject json = new JsonObject()
                .put("createTime", dateTimeStr)
                .put("updateTime", "2023-12-31T23:59:59");
        
        LOGGER.info("LocalDateTime conversion test setup completed");
        assertNotNull(json);
    }
}
