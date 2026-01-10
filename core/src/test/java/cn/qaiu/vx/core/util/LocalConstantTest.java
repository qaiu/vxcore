package cn.qaiu.vx.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalConstant 工具类测试
 */
class LocalConstantTest {

    @AfterEach
    void tearDown() {
        // 清理测试数据
        LocalConstant.clear();
    }

    @Test
    void testPut_NewKey() {
        String key = "test_key";
        String value = "test_value";

        Map<String, Object> result = LocalConstant.put(key, value);

        assertNotNull(result);
        assertEquals(value, LocalConstant.get(key));
    }

    @Test
    void testPut_ExistingKey() {
        String key = "test_key";
        String value1 = "value1";
        String value2 = "value2";

        LocalConstant.put(key, value1);
        Map<String, Object> result = LocalConstant.put(key, value2);

        assertNotNull(result);
        assertEquals(value1, LocalConstant.get(key)); // 应该保持原值
    }

    @Test
    void testGet_ExistingKey() {
        String key = "test_key";
        String value = "test_value";
        LocalConstant.put(key, value);

        Object result = LocalConstant.get(key);

        assertEquals(value, result);
    }

    @Test
    void testGet_NonExistingKey() {
        String key = "non_existing";

        Object result = LocalConstant.get(key);

        assertNull(result);
    }

    @Test
    void testGetWithCast_String() {
        String key = "string_key";
        String value = "test_string";
        LocalConstant.put(key, value);

        String result = LocalConstant.getWithCast(key);

        assertEquals(value, result);
    }

    @Test
    void testGetWithCast_Integer() {
        String key = "int_key";
        Integer value = 42;
        LocalConstant.put(key, value);

        Integer result = LocalConstant.getWithCast(key);

        assertEquals(value, result);
    }

    @Test
    void testContainsKey_Existing() {
        String key = "test_key";
        LocalConstant.put(key, "value");

        boolean result = LocalConstant.containsKey(key);

        assertTrue(result);
    }

    @Test
    void testContainsKey_NonExisting() {
        String key = "non_existing";

        boolean result = LocalConstant.containsKey(key);

        assertFalse(result);
    }

    @Test
    void testGetMap() {
        String key = "map_key";
        Map<String, String> map = Map.of("k1", "v1", "k2", "v2");
        LocalConstant.put(key, map);

        Map<?, ?> result = LocalConstant.getMap(key);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("v1", result.get("k1"));
        assertEquals("v2", result.get("k2"));
    }

    @Test
    void testGetString() {
        String key = "string_key";
        Integer value = 123;
        LocalConstant.put(key, value);

        String result = LocalConstant.getString(key);

        assertEquals("123", result);
    }

    @Test
    void testGetString_NullValue() {
        String key = "null_key";
        LocalConstant.put(key, null);

        assertThrows(NullPointerException.class, () -> LocalConstant.getString(key));
    }
}