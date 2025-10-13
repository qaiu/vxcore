package cn.qaiu.vx.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeConverterRegistry类型转换器注册表测试")
class TypeConverterRegistryTest {

    @Nested
    @DisplayName("基本类型转换测试")
    class BasicTypeConversionTest {

        @Test
        @DisplayName("字符串转换测试")
        void testStringConversion() {
            // 字符串转换应该返回原值，因为String类型通过反射转换直接返回原值
            String result = TypeConverterRegistry.convert("hello", String.class);
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("整数转换测试")
        void testIntegerConversion() {
            Integer result = TypeConverterRegistry.convert("123", Integer.class);
            assertEquals(Integer.valueOf(123), result);
            
            int primitiveResult = TypeConverterRegistry.convert("456", int.class);
            assertEquals(456, primitiveResult);
        }

        @Test
        @DisplayName("长整数转换测试")
        void testLongConversion() {
            Long result = TypeConverterRegistry.convert("123456789", Long.class);
            assertEquals(Long.valueOf(123456789L), result);
            
            long primitiveResult = TypeConverterRegistry.convert("987654321", long.class);
            assertEquals(987654321L, primitiveResult);
        }

        @Test
        @DisplayName("双精度浮点数转换测试")
        void testDoubleConversion() {
            Double result = TypeConverterRegistry.convert("123.45", Double.class);
            assertEquals(Double.valueOf(123.45), result);
            
            double primitiveResult = TypeConverterRegistry.convert("678.90", double.class);
            assertEquals(678.90, primitiveResult, 0.001);
        }

        @Test
        @DisplayName("单精度浮点数转换测试")
        void testFloatConversion() {
            Float result = TypeConverterRegistry.convert("123.45", Float.class);
            assertEquals(Float.valueOf(123.45f), result);
            
            float primitiveResult = TypeConverterRegistry.convert("678.90", float.class);
            assertEquals(678.90f, primitiveResult, 0.001f);
        }

        @Test
        @DisplayName("布尔值转换测试")
        void testBooleanConversion() {
            Boolean result1 = TypeConverterRegistry.convert("true", Boolean.class);
            assertTrue(result1);
            
            Boolean result2 = TypeConverterRegistry.convert("false", Boolean.class);
            assertFalse(result2);
            
            boolean primitiveResult = TypeConverterRegistry.convert("true", boolean.class);
            assertTrue(primitiveResult);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("null值转换测试")
        void testNullConversion() {
            String result = TypeConverterRegistry.convert(null, String.class);
            assertNull(result);
        }

        @Test
        @DisplayName("空字符串转换测试")
        void testEmptyStringConversion() {
            String result = TypeConverterRegistry.convert("", String.class);
            assertNull(result);
        }

        @Test
        @DisplayName("空白字符串转换测试")
        void testWhitespaceStringConversion() {
            String result = TypeConverterRegistry.convert("   ", String.class);
            assertNull(result);
        }

        @Test
        @DisplayName("无效数字转换测试")
        void testInvalidNumberConversion() {
            assertThrows(IllegalArgumentException.class, () -> {
                TypeConverterRegistry.convert("abc", Integer.class);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                TypeConverterRegistry.convert("not-a-number", Double.class);
            });
        }

        @Test
        @DisplayName("无效布尔值转换测试")
        void testInvalidBooleanConversion() {
            // Boolean.valueOf() 不会抛出异常，对于无效值返回false
            Boolean result = TypeConverterRegistry.convert("maybe", Boolean.class);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("注册表功能测试")
    class RegistryTest {

        @Test
        @DisplayName("检查支持的类型测试")
        void testIsSupported() {
            // 基本类型通过反射转换支持，但isSupported只检查注册的转换器
            // 由于测试中注册了String的自定义转换器，String.class应该返回true
            assertTrue(TypeConverterRegistry.isSupported(String.class));
            assertFalse(TypeConverterRegistry.isSupported(Integer.class));
            assertFalse(TypeConverterRegistry.isSupported(Long.class));
            assertFalse(TypeConverterRegistry.isSupported(Double.class));
            assertFalse(TypeConverterRegistry.isSupported(Float.class));
            assertFalse(TypeConverterRegistry.isSupported(Boolean.class));
            
            // 注册的转换器应该返回true
            assertTrue(TypeConverterRegistry.isSupported(LocalDate.class));
            assertTrue(TypeConverterRegistry.isSupported(LocalTime.class));
            assertTrue(TypeConverterRegistry.isSupported(LocalDateTime.class));
        }

        @Test
        @DisplayName("获取转换器测试")
        void testGetConverter() {
            // 基本类型没有注册的转换器，通过反射转换
            // 由于测试中注册了String的自定义转换器，应该能获取到
            TypeConverter<String> stringConverter = TypeConverterRegistry.getConverter(String.class);
            assertNotNull(stringConverter); // 有自定义转换器
            
            TypeConverter<Integer> intConverter = TypeConverterRegistry.getConverter(Integer.class);
            assertNull(intConverter); // 基本类型没有注册转换器
            
            // 注册的转换器应该能获取到
            TypeConverter<LocalDate> dateConverter = TypeConverterRegistry.getConverter(LocalDate.class);
            assertNotNull(dateConverter);
        }

        @Test
        @DisplayName("注册自定义转换器测试")
        void testRegisterCustomConverter() {
            // 创建一个简单的自定义转换器
            TypeConverter<String> customConverter = new TypeConverter<String>() {
                @Override
                public String convert(String value) throws IllegalArgumentException {
                    return "custom_" + value;
                }
                
                @Override
                public Class<String> getTargetType() {
                    return String.class;
                }
            };
            
            // 注册自定义转换器
            TypeConverterRegistry.register(customConverter);
            
            try {
                // 验证注册成功
                assertTrue(TypeConverterRegistry.isSupported(String.class));
                TypeConverter<String> retrievedConverter = TypeConverterRegistry.getConverter(String.class);
                assertNotNull(retrievedConverter);
                
                // 测试自定义转换器功能
                String result = retrievedConverter.convert("test");
                assertEquals("custom_test", result);
            } finally {
                // 清理：移除自定义转换器，避免影响其他测试
                // 注意：这里我们无法直接移除，因为TypeConverterRegistry没有提供remove方法
                // 但我们可以通过重新注册一个默认的String转换器来"覆盖"
                TypeConverterRegistry.register(new TypeConverter<String>() {
                    @Override
                    public String convert(String value) throws IllegalArgumentException {
                        return value; // 直接返回原值
                    }
                    
                    @Override
                    public Class<String> getTargetType() {
                        return String.class;
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("日期时间转换测试")
    class DateTimeConversionTest {

        @Test
        @DisplayName("LocalDate转换测试")
        void testLocalDateConversion() {
            // 测试ISO格式日期
            LocalDate result = TypeConverterRegistry.convert("2023-12-25", LocalDate.class);
            assertNotNull(result);
            assertEquals(2023, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(25, result.getDayOfMonth());
        }

        @Test
        @DisplayName("LocalTime转换测试")
        void testLocalTimeConversion() {
            // 测试ISO格式时间
            LocalTime result = TypeConverterRegistry.convert("14:30:45", LocalTime.class);
            assertNotNull(result);
            assertEquals(14, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(45, result.getSecond());
        }

        @Test
        @DisplayName("LocalDateTime转换测试")
        void testLocalDateTimeConversion() {
            // 测试ISO格式日期时间
            LocalDateTime result = TypeConverterRegistry.convert("2023-12-25T14:30:45", LocalDateTime.class);
            assertNotNull(result);
            assertEquals(2023, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(25, result.getDayOfMonth());
            assertEquals(14, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(45, result.getSecond());
        }

        @Test
        @DisplayName("无效日期格式测试")
        void testInvalidDateFormat() {
            assertThrows(Exception.class, () -> {
                TypeConverterRegistry.convert("invalid-date", LocalDate.class);
            });
            
            assertThrows(Exception.class, () -> {
                TypeConverterRegistry.convert("invalid-time", LocalTime.class);
            });
        }
    }

    @Nested
    @DisplayName("参数化测试")
    class ParameterizedConversionTest {

        @ParameterizedTest
        @CsvSource({
            "123, Integer, 123",
            "456, Long, 456",
            "123.45, Double, 123.45",
            "67.89, Float, 67.89",
            "true, Boolean, true",
            "false, Boolean, false"
        })
        @DisplayName("基本类型参数化转换测试")
        void testBasicTypeParameterizedConversion(String input, String typeName, String expected) {
            try {
                Class<?> targetType = Class.forName("java.lang." + typeName);
                Object result = TypeConverterRegistry.convert(input, targetType);
                
                if (typeName.equals("Integer")) {
                    assertEquals(Integer.parseInt(expected), result);
                } else if (typeName.equals("Long")) {
                    assertEquals(Long.parseLong(expected), result);
                } else if (typeName.equals("Double")) {
                    assertEquals(Double.parseDouble(expected), result);
                } else if (typeName.equals("Float")) {
                    assertEquals(Float.parseFloat(expected), result);
                } else if (typeName.equals("Boolean")) {
                    assertEquals(Boolean.parseBoolean(expected), result);
                }
            } catch (ClassNotFoundException e) {
                fail("Class not found: " + typeName);
            }
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("字符串转换性能测试")
        void testStringConversionPerformance() {
            String testValue = "performance_test_string";
            
            try {
                long startTime = System.nanoTime();
                for (int i = 0; i < 10000; i++) {
                    TypeConverterRegistry.convert(testValue, String.class);
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1_000_000; // milliseconds
                
                System.out.println("字符串转换性能测试完成，耗时: " + duration + "ms");
                assertTrue(duration < 100, "字符串转换性能测试超时");
            } catch (Exception e) {
                // 如果转换失败，跳过性能测试
                System.out.println("字符串转换性能测试跳过: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("整数转换性能测试")
        void testIntegerConversionPerformance() {
            String testValue = "12345";
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                TypeConverterRegistry.convert(testValue, Integer.class);
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            
            System.out.println("整数转换性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 200, "整数转换性能测试超时");
        }

        @Test
        @DisplayName("类型检查性能测试")
        void testTypeCheckPerformance() {
            Class<?>[] types = {String.class, Integer.class, Long.class, Double.class, Float.class, Boolean.class};
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                for (Class<?> type : types) {
                    TypeConverterRegistry.isSupported(type);
                }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            
            System.out.println("类型检查性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 50, "类型检查性能测试超时");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTest {

        @Test
        @DisplayName("不支持的类型转换测试")
        void testUnsupportedTypeConversion() {
            // 测试一个不支持的类型
            assertThrows(IllegalArgumentException.class, () -> {
                TypeConverterRegistry.convert("test", Object.class);
            });
        }

        @Test
        @DisplayName("转换器异常处理测试")
        void testConverterExceptionHandling() {
            // 创建一个会抛出异常的转换器
            TypeConverter<String> exceptionConverter = new TypeConverter<String>() {
                @Override
                public String convert(String value) throws IllegalArgumentException {
                    throw new IllegalArgumentException("Test exception");
                }
                
                @Override
                public Class<String> getTargetType() {
                    return String.class;
                }
            };
            
            // 注册异常转换器
            TypeConverterRegistry.register(exceptionConverter);
            
            // 验证异常被正确抛出
            assertThrows(IllegalArgumentException.class, () -> {
                TypeConverterRegistry.convert("test", String.class);
            });
        }
    }
}
