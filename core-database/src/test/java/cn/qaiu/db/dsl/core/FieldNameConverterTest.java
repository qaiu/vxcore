package cn.qaiu.db.dsl.core;

import cn.qaiu.db.ddl.DdlColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FieldNameConverter字段名转换工具类测试")
class FieldNameConverterTest {

    @Nested
    @DisplayName("Java字段名转数据库字段名测试")
    class ToDatabaseFieldNameTest {

        @Test
        @DisplayName("标准驼峰字段名转换")
        void testStandardCamelFieldName() {
            assertEquals("user_name", FieldNameConverter.toDatabaseFieldName("userName"));
            assertEquals("user_id", FieldNameConverter.toDatabaseFieldName("userId"));
            assertEquals("created_at", FieldNameConverter.toDatabaseFieldName("createdAt"));
            assertEquals("is_active", FieldNameConverter.toDatabaseFieldName("isActive"));
        }

        @Test
        @DisplayName("复杂驼峰字段名转换")
        void testComplexCamelFieldName() {
            assertEquals("xml_http_request", FieldNameConverter.toDatabaseFieldName("xmlHttpRequest"));
            assertEquals("html_5_parser", FieldNameConverter.toDatabaseFieldName("html5Parser"));
            assertEquals("api_key_value", FieldNameConverter.toDatabaseFieldName("apiKeyValue"));
        }

        @Test
        @DisplayName("边界情况测试")
        void testEdgeCases() {
            assertEquals("", FieldNameConverter.toDatabaseFieldName(""));
            assertEquals("a", FieldNameConverter.toDatabaseFieldName("A"));
            assertEquals("abc", FieldNameConverter.toDatabaseFieldName("ABC"));
        }

        @ParameterizedTest
        @CsvSource({
            "userName, user_name",
            "userId, user_id", 
            "createdAt, created_at",
            "isActive, is_active",
            "xmlHttpRequest, xml_http_request",
            "html5Parser, html_5_parser",
            "apiKeyValue, api_key_value"
        })
        @DisplayName("参数化字段名转换测试")
        void testParameterizedFieldNameConversion(String javaFieldName, String expectedDatabaseFieldName) {
            assertEquals(expectedDatabaseFieldName, FieldNameConverter.toDatabaseFieldName(javaFieldName));
        }
    }

    @Nested
    @DisplayName("数据库字段名转Java字段名测试")
    class ToJavaFieldNameTest {

        @Test
        @DisplayName("标准下划线字段名转换")
        void testStandardUnderlineFieldName() {
            assertEquals("userName", FieldNameConverter.toJavaFieldName("user_name"));
            assertEquals("userId", FieldNameConverter.toJavaFieldName("user_id"));
            assertEquals("createdAt", FieldNameConverter.toJavaFieldName("created_at"));
            assertEquals("isActive", FieldNameConverter.toJavaFieldName("is_active"));
        }

        @Test
        @DisplayName("复杂下划线字段名转换")
        void testComplexUnderlineFieldName() {
            assertEquals("xmlHttpRequest", FieldNameConverter.toJavaFieldName("xml_http_request"));
            assertEquals("html5Parser", FieldNameConverter.toJavaFieldName("html_5_parser"));
            assertEquals("apiKeyValue", FieldNameConverter.toJavaFieldName("api_key_value"));
        }

        @Test
        @DisplayName("边界情况测试")
        void testEdgeCases() {
            assertEquals("", FieldNameConverter.toJavaFieldName(""));
            assertEquals("a", FieldNameConverter.toJavaFieldName("a"));
            assertEquals("abc", FieldNameConverter.toJavaFieldName("abc"));
        }

        @ParameterizedTest
        @CsvSource({
            "user_name, userName",
            "user_id, userId",
            "created_at, createdAt", 
            "is_active, isActive",
            "xml_http_request, xmlHttpRequest",
            "html_5_parser, html5Parser",
            "api_key_value, apiKeyValue"
        })
        @DisplayName("参数化字段名转换测试")
        void testParameterizedFieldNameConversion(String databaseFieldName, String expectedJavaFieldName) {
            assertEquals(expectedJavaFieldName, FieldNameConverter.toJavaFieldName(databaseFieldName));
        }
    }

    @Nested
    @DisplayName("Java类名转数据库表名测试")
    class ToDatabaseTableNameTest {

        @Test
        @DisplayName("标准类名转换")
        void testStandardClassName() {
            assertEquals("user", FieldNameConverter.toDatabaseTableName("User"));
            assertEquals("user_info", FieldNameConverter.toDatabaseTableName("UserInfo"));
            assertEquals("order_detail", FieldNameConverter.toDatabaseTableName("OrderDetail"));
        }

        @Test
        @DisplayName("复杂类名转换")
        void testComplexClassName() {
            assertEquals("xml_http_request", FieldNameConverter.toDatabaseTableName("XMLHttpRequest"));
            assertEquals("html_5_parser", FieldNameConverter.toDatabaseTableName("HTML5Parser"));
            assertEquals("api_key_manager", FieldNameConverter.toDatabaseTableName("ApiKeyManager"));
        }

        @Test
        @DisplayName("边界情况测试")
        void testEdgeCases() {
            assertEquals("", FieldNameConverter.toDatabaseTableName(""));
            assertEquals("a", FieldNameConverter.toDatabaseTableName("A"));
            assertEquals("abc", FieldNameConverter.toDatabaseTableName("ABC"));
        }

        @ParameterizedTest
        @CsvSource({
            "User, user",
            "UserInfo, user_info",
            "OrderDetail, order_detail",
            "XMLHttpRequest, xml_http_request",
            "HTML5Parser, html_5_parser",
            "ApiKeyManager, api_key_manager"
        })
        @DisplayName("参数化类名转换测试")
        void testParameterizedClassNameConversion(String javaClassName, String expectedTableName) {
            assertEquals(expectedTableName, FieldNameConverter.toDatabaseTableName(javaClassName));
        }
    }

    @Nested
    @DisplayName("Field对象转换测试")
    class FieldObjectConversionTest {

        @Test
        @DisplayName("无注解字段转换")
        void testFieldWithoutAnnotation() throws NoSuchFieldException {
            Field field = TestEntity.class.getDeclaredField("userName");
            assertEquals("user_name", FieldNameConverter.toDatabaseFieldName(field));
        }

        @Test
        @DisplayName("有DdlColumn注解字段转换")
        void testFieldWithDdlColumnAnnotation() throws NoSuchFieldException {
            Field field = TestEntity.class.getDeclaredField("userId");
            assertEquals("user_id", FieldNameConverter.toDatabaseFieldName(field));
        }

        @Test
        @DisplayName("DdlColumn注解value优先测试")
        void testDdlColumnValuePriority() throws NoSuchFieldException {
            Field field = TestEntity.class.getDeclaredField("email");
            assertEquals("user_email", FieldNameConverter.toDatabaseFieldName(field));
        }

        @Test
        @DisplayName("DdlColumn注解name备用测试")
        void testDdlColumnNameFallback() throws NoSuchFieldException {
            Field field = TestEntity.class.getDeclaredField("phone");
            assertEquals("phone_number", FieldNameConverter.toDatabaseFieldName(field));
        }

        @Test
        @DisplayName("DdlColumn注解空值回退测试")
        void testDdlColumnEmptyFallback() throws NoSuchFieldException {
            Field field = TestEntity.class.getDeclaredField("address");
            assertEquals("address", FieldNameConverter.toDatabaseFieldName(field));
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTest {

        @Test
        @DisplayName("Java字段名->数据库字段名->Java字段名往返测试")
        void testJavaToDatabaseRoundTrip() {
            String[] javaFieldNames = {
                "userName", "userId", "createdAt", "isActive", 
                "xmlHttpRequest", "html5Parser", "apiKeyValue"
            };

            for (String javaFieldName : javaFieldNames) {
                String databaseFieldName = FieldNameConverter.toDatabaseFieldName(javaFieldName);
                String backToJava = FieldNameConverter.toJavaFieldName(databaseFieldName);
                assertEquals(javaFieldName, backToJava, 
                    "往返转换应该保持一致: " + javaFieldName);
            }
        }

        @Test
        @DisplayName("数据库字段名->Java字段名->数据库字段名往返测试")
        void testDatabaseToJavaRoundTrip() {
            String[] databaseFieldNames = {
                "user_name", "user_id", "created_at", "is_active",
                "xml_http_request", "html_5_parser", "api_key_value"
            };

            for (String databaseFieldName : databaseFieldNames) {
                String javaFieldName = FieldNameConverter.toJavaFieldName(databaseFieldName);
                String backToDatabase = FieldNameConverter.toDatabaseFieldName(javaFieldName);
                assertEquals(databaseFieldName, backToDatabase,
                    "往返转换应该保持一致: " + databaseFieldName);
            }
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {
        private static final int ITERATIONS = 100000;

        @Test
        @DisplayName("字段名转换性能测试")
        void testFieldNameConversionPerformance() {
            String[] testNames = {
                "userName", "xmlHttpRequest", "html5Parser", "apiKeyValue"
            };

            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                for (String name : testNames) {
                    FieldNameConverter.toDatabaseFieldName(name);
                    FieldNameConverter.toJavaFieldName(name);
                }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds

            System.out.println("字段名转换性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 1000, "字段名转换性能测试超时");
        }

        @Test
        @DisplayName("类名转换性能测试")
        void testClassNameConversionPerformance() {
            String[] testNames = {
                "User", "XMLHttpRequest", "HTML5Parser", "ApiKeyManager"
            };

            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                for (String name : testNames) {
                    FieldNameConverter.toDatabaseTableName(name);
                }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds

            System.out.println("类名转换性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 500, "类名转换性能测试超时");
        }
    }

    @Nested
    @DisplayName("实际应用场景测试")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("用户实体字段转换测试")
        void testUserEntityFieldConversion() throws NoSuchFieldException {
            Field[] fields = TestEntity.class.getDeclaredFields();
            
            for (Field field : fields) {
                String databaseFieldName = FieldNameConverter.toDatabaseFieldName(field);
                String javaFieldName = FieldNameConverter.toJavaFieldName(databaseFieldName);
                
                assertNotNull(databaseFieldName, "数据库字段名不应为null: " + field.getUsername());
                assertNotNull(javaFieldName, "Java字段名不应为null: " + field.getUsername());
                assertFalse(databaseFieldName.isEmpty(), "数据库字段名不应为空: " + field.getUsername());
                assertFalse(javaFieldName.isEmpty(), "Java字段名不应为空: " + field.getUsername());
            }
        }

        @Test
        @DisplayName("数据库表名转换测试")
        void testDatabaseTableNameConversion() {
            String[] entityClassNames = {
                "User", "UserInfo", "OrderDetail", "ProductCategory",
                "XMLHttpRequest", "HTML5Parser", "ApiKeyManager"
            };

            for (String className : entityClassNames) {
                String tableName = FieldNameConverter.toDatabaseTableName(className);
                assertNotNull(tableName, "表名不应为null: " + className);
                assertFalse(tableName.isEmpty(), "表名不应为空: " + className);
                assertTrue(tableName.matches("^[a-z][a-z0-9_]*$"), 
                    "表名应符合数据库命名规范: " + tableName);
            }
        }
    }

    /**
     * 测试实体类
     */
    static class TestEntity {
        private String userName;
        
        @DdlColumn(name = "user_id")
        private Long userId;
        
        @DdlColumn(value = "user_email")
        private String email;
        
        @DdlColumn(name = "phone_number")
        private String phone;
        
        @DdlColumn(value = "", name = "")
        private String address;
        
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
    }
}
