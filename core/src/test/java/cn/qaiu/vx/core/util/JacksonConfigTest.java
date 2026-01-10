package cn.qaiu.vx.core.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JacksonConfig 配置类测试
 */
class JacksonConfigTest {

    @Test
    void testJacksonConfigInitialization() {
        // 调用nothing()方法触发静态初始化
        JacksonConfig.nothing();

        // 验证ObjectMapper配置
        ObjectMapper mapper = DatabindCodec.mapper();

        // 验证FAIL_ON_UNKNOWN_PROPERTIES被设置为false
        assertFalse(mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // 验证可以反序列化LocalDateTime
        String dateTimeJson = "\"2023-12-25 15:30:45\"";
        assertDoesNotThrow(() -> mapper.readValue(dateTimeJson, LocalDateTime.class));

        // 验证可以反序列化LocalDate
        String dateJson = "\"2023-12-25\"";
        assertDoesNotThrow(() -> mapper.readValue(dateJson, LocalDate.class));

        // 验证可以反序列化LocalTime
        String timeJson = "\"15:30:45\"";
        assertDoesNotThrow(() -> mapper.readValue(timeJson, LocalTime.class));
    }

    @Test
    void testLocalDateTimeDeserialization() throws Exception {
        JacksonConfig.nothing();

        ObjectMapper mapper = DatabindCodec.mapper();
        String json = "\"2023-12-25 15:30:45\"";

        LocalDateTime result = mapper.readValue(json, LocalDateTime.class);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    void testLocalDateDeserialization() throws Exception {
        JacksonConfig.nothing();

        ObjectMapper mapper = DatabindCodec.mapper();
        String json = "\"2023-12-25\"";

        LocalDate result = mapper.readValue(json, LocalDate.class);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
    }

    @Test
    void testLocalTimeDeserialization() throws Exception {
        JacksonConfig.nothing();

        ObjectMapper mapper = DatabindCodec.mapper();
        String json = "\"15:30:45\"";

        LocalTime result = mapper.readValue(json, LocalTime.class);

        assertNotNull(result);
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    void testFailOnUnknownPropertiesDisabled() throws Exception {
        JacksonConfig.nothing();

        ObjectMapper mapper = DatabindCodec.mapper();
        String jsonWithExtraField = "{\"knownField\":\"value\",\"unknownField\":\"shouldNotFail\"}";

        // 这应该不会抛出异常，因为FAIL_ON_UNKNOWN_PROPERTIES被禁用
        TestObject result = mapper.readValue(jsonWithExtraField, TestObject.class);

        assertNotNull(result);
        assertEquals("value", result.knownField);
    }

    /**
     * 测试用的简单对象
     */
    public static class TestObject {
        public String knownField;
    }
}