package cn.qaiu.db.dsl.test;

import cn.qaiu.db.dsl.core.EnhancedTypeMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 增强类型映射器测试
 * 
 * 验证类型转换和日期处理功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class EnhancedTypeMapperTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedTypeMapperTest.class);
    
    @Test
    void testStringToLong() {
        Long result = EnhancedTypeMapper.convertToType("123", Long.class);
        assertEquals(Long.valueOf(123), result);
        
        Long result2 = EnhancedTypeMapper.convertToType(123, Long.class);
        assertEquals(Long.valueOf(123), result2);
    }
    
    @Test
    void testStringToInteger() {
        Integer result = EnhancedTypeMapper.convertToType("456", Integer.class);
        assertEquals(Integer.valueOf(456), result);
        
        Integer result2 = EnhancedTypeMapper.convertToType(456, Integer.class);
        assertEquals(Integer.valueOf(456), result2);
    }
    
    @Test
    void testStringToBigDecimal() {
        BigDecimal result = EnhancedTypeMapper.convertToType("123.45", BigDecimal.class);
        assertEquals(new BigDecimal("123.45"), result);
        
        BigDecimal result2 = EnhancedTypeMapper.convertToType(123.45, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(123.45), result2);
    }
    
    @Test
    void testStringToBoolean() {
        Boolean result1 = EnhancedTypeMapper.convertToType("true", Boolean.class);
        assertTrue(result1);
        
        Boolean result2 = EnhancedTypeMapper.convertToType("false", Boolean.class);
        assertFalse(result2);
        
        Boolean result3 = EnhancedTypeMapper.convertToType("1", Boolean.class);
        assertTrue(result3);
        
        Boolean result4 = EnhancedTypeMapper.convertToType("0", Boolean.class);
        assertFalse(result4);
    }
    
    @Test
    void testDateTimeParsing() {
        // 测试ISO格式
        LocalDateTime result1 = EnhancedTypeMapper.convertToType("2025-10-08T15:30:00", LocalDateTime.class);
        assertNotNull(result1);
        assertEquals(2025, result1.getYear());
        assertEquals(10, result1.getMonthValue());
        assertEquals(8, result1.getDayOfMonth());
        assertEquals(15, result1.getHour());
        assertEquals(30, result1.getMinute());
        assertEquals(0, result1.getSecond());
        
        // 测试SQL格式
        LocalDateTime result2 = EnhancedTypeMapper.convertToType("2025-10-08 15:30:00", LocalDateTime.class);
        assertNotNull(result2);
        assertEquals(2025, result2.getYear());
        assertEquals(10, result2.getMonthValue());
        assertEquals(8, result2.getDayOfMonth());
        
        // 测试带微秒的格式
        LocalDateTime result3 = EnhancedTypeMapper.convertToType("2025-10-08T15:30:00.123456", LocalDateTime.class);
        assertNotNull(result3);
        assertEquals(123456000, result3.getNano());
        
        // 测试空格格式带微秒
        LocalDateTime result4 = EnhancedTypeMapper.convertToType("2025-10-08 15:30:00.123456", LocalDateTime.class);
        assertNotNull(result4);
        assertEquals(123456000, result4.getNano());
    }
    
    @Test
    void testDatabaseValueConversion() {
        // 测试LocalDateTime转换为Timestamp
        LocalDateTime now = LocalDateTime.now();
        Object result = EnhancedTypeMapper.convertToDatabaseValue(now);
        assertTrue(result instanceof java.sql.Timestamp);
        
        // 测试字符串日期时间转换
        Object result2 = EnhancedTypeMapper.convertToDatabaseValue("2025-10-08T15:30:00");
        assertTrue(result2 instanceof java.sql.Timestamp);
        
        // 测试数字字符串转换
        Object result3 = EnhancedTypeMapper.convertToDatabaseValue("123.45");
        assertTrue(result3 instanceof BigDecimal);
        assertEquals(new BigDecimal("123.45"), result3);
        
        // 测试普通字符串
        Object result4 = EnhancedTypeMapper.convertToDatabaseValue("hello");
        assertEquals("hello", result4);
    }
    
    @Test
    void testNullHandling() {
        assertNull(EnhancedTypeMapper.convertToType(null, String.class));
        assertNull(EnhancedTypeMapper.convertToType(null, Long.class));
        assertNull(EnhancedTypeMapper.convertToType(null, Integer.class));
        assertNull(EnhancedTypeMapper.convertToType(null, BigDecimal.class));
        assertNull(EnhancedTypeMapper.convertToType(null, Boolean.class));
        assertNull(EnhancedTypeMapper.convertToType(null, LocalDateTime.class));
        assertNull(EnhancedTypeMapper.convertToDatabaseValue(null));
    }
    
    @Test
    void testInvalidConversion() {
        // 测试无效的数字字符串
        Long result1 = EnhancedTypeMapper.convertToType("abc", Long.class);
        assertNull(result1);
        
        // 测试无效的日期字符串
        LocalDateTime result2 = EnhancedTypeMapper.convertToType("invalid-date", LocalDateTime.class);
        assertNull(result2);
        
        // 测试无效的布尔字符串
        Boolean result3 = EnhancedTypeMapper.convertToType("maybe", Boolean.class);
        assertNull(result3);
    }
    
    @Test
    void testCustomTypeConverter() {
        // 添加自定义转换器
        EnhancedTypeMapper.addTypeConverter(String.class, obj -> {
            if (obj instanceof Integer) {
                return "Number: " + obj;
            }
            return obj.toString();
        });
        
        String result = EnhancedTypeMapper.convertToType(123, String.class);
        assertEquals("Number: 123", result);
        
        // 移除自定义转换器
        EnhancedTypeMapper.removeTypeConverter(String.class);
        
        String result2 = EnhancedTypeMapper.convertToType(123, String.class);
        assertEquals("123", result2);
    }
    
    @Test
    void testPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 执行1000次转换
        for (int i = 0; i < 1000; i++) {
            EnhancedTypeMapper.convertToType("123.45", BigDecimal.class);
            EnhancedTypeMapper.convertToType("2025-10-08T15:30:00", LocalDateTime.class);
            EnhancedTypeMapper.convertToType("true", Boolean.class);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("1000次类型转换耗时: {}ms", duration);
        assertTrue(duration < 1000, "类型转换应该很快");
    }
}
