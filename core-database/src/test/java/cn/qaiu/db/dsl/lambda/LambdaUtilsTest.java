package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.lambda.example.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LambdaUtils工具类测试")
class LambdaUtilsTest {

    @Test
    @DisplayName("测试Lambda表达式字段名解析")
    void testGetFieldName() {
        // 测试各种Lambda表达式 - LambdaUtils.getFieldName() 返回数据库字段名（下划线格式）
        assertEquals("id", LambdaUtils.getFieldName(User::getId));
        assertEquals("username", LambdaUtils.getFieldName(User::getUsername));
        assertEquals("email", LambdaUtils.getFieldName(User::getEmail));
        assertEquals("age", LambdaUtils.getFieldName(User::getAge));
        assertEquals("status", LambdaUtils.getFieldName(User::getStatus));
        assertEquals("balance", LambdaUtils.getFieldName(User::getBalance));
        assertEquals("email_verified", LambdaUtils.getFieldName(User::getEmailVerified));
        assertEquals("bio", LambdaUtils.getFieldName(User::getBio));
        assertEquals("create_time", LambdaUtils.getFieldName(User::getCreateTime));
        assertEquals("update_time", LambdaUtils.getFieldName(User::getUpdateTime));
    }

    @Test
    @DisplayName("测试Lambda表达式字段类型解析")
    void testGetFieldType() {
        // 测试各种字段类型
        assertEquals(Long.class, LambdaUtils.getFieldType(User::getId));
        assertEquals(String.class, LambdaUtils.getFieldType(User::getUsername));
        assertEquals(String.class, LambdaUtils.getFieldType(User::getEmail));
        assertEquals(Integer.class, LambdaUtils.getFieldType(User::getAge));
        assertEquals(String.class, LambdaUtils.getFieldType(User::getStatus));
        assertEquals(java.math.BigDecimal.class, LambdaUtils.getFieldType(User::getBalance));
        assertEquals(Boolean.class, LambdaUtils.getFieldType(User::getEmailVerified));
        assertEquals(String.class, LambdaUtils.getFieldType(User::getBio));
        assertEquals(java.time.LocalDateTime.class, LambdaUtils.getFieldType(User::getCreateTime));
        assertEquals(java.time.LocalDateTime.class, LambdaUtils.getFieldType(User::getUpdateTime));
    }

    @Test
    @DisplayName("测试缓存功能")
    void testCache() {
        // 第一次调用
        String fieldName1 = LambdaUtils.getFieldName(User::getUsername);
        assertEquals("username", fieldName1);
        
        // 第二次调用应该使用缓存
        String fieldName2 = LambdaUtils.getFieldName(User::getUsername);
        assertEquals("username", fieldName2);
        
        // 清空缓存
        LambdaUtils.clearCache();
        
        // 清空后再次调用
        String fieldName3 = LambdaUtils.getFieldName(User::getUsername);
        assertEquals("username", fieldName3);
    }
}
