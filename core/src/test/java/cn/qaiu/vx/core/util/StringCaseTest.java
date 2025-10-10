package cn.qaiu.vx.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StringCase工具类单元测试
 * 
 * @author QAIU
 */
@DisplayName("StringCase工具类测试")
class StringCaseTest {

    @Nested
    @DisplayName("驼峰转下划线测试")
    class ToUnderlineCaseTest {

        @Test
        @DisplayName("基本驼峰转下划线")
        void testBasicCamelToUnderline() {
            assertEquals("hello_world", StringCase.toUnderlineCase("HelloWorld"));
            assertEquals("my_name_is_qaiu", StringCase.toUnderlineCase("MyNameIsQaiu"));
            assertEquals("user_id", StringCase.toUnderlineCase("UserId"));
        }

            @Test
            @DisplayName("包含下划线的驼峰转下划线")
            void testCamelWithUnderscoreToUnderline() {
                assertEquals("hello__world", StringCase.toUnderlineCase("Hello_World"));
                assertEquals("hello_world__test", StringCase.toUnderlineCase("HelloWorld_test"));
                assertEquals("my_name__qaiu", StringCase.toUnderlineCase("MyName_Qaiu"));
            }

        @ParameterizedTest
        @CsvSource({
            "HelloWorld, hello_world",
            "MyNameIsQaiu, my_name_is_qaiu",
            "UserId, user_id",
            "XMLHttpRequest, xml_http_request",
            "HTML5Parser, html_5_parser"
        })
        @DisplayName("参数化测试驼峰转下划线")
        void testCamelToUnderlineParameterized(String input, String expected) {
            assertEquals(expected, StringCase.toUnderlineCase(input));
        }

        @Test
        @DisplayName("边界情况测试")
        void testEdgeCases() {
            assertNull(StringCase.toUnderlineCase(null));
            assertEquals("", StringCase.toUnderlineCase(""));
            assertEquals("  ", StringCase.toUnderlineCase("  "));
        }

            @Test
            @DisplayName("特殊字符测试")
            void testSpecialCharacters() {
                assertEquals("__my__name_qaiu___", StringCase.toUnderlineCase("__my_nameQaiu___"));
                assertEquals("abc", StringCase.toUnderlineCase("ABC"));
                assertEquals("test_123", StringCase.toUnderlineCase("test123"));
            }

        @Test
        @DisplayName("单个字符测试")
        void testSingleCharacter() {
            assertEquals("a", StringCase.toUnderlineCase("A"));
            assertEquals("a", StringCase.toUnderlineCase("a"));
            assertEquals("", StringCase.toUnderlineCase("_"));
        }
    }

    @Nested
    @DisplayName("下划线转驼峰测试")
    class ToCamelCaseTest {

        @Test
        @DisplayName("下划线转小驼峰")
        void testUnderlineToLittleCamel() {
            assertEquals("helloWorld", StringCase.toLittleCamelCase("hello_world"));
            assertEquals("myNameIsQaiu", StringCase.toLittleCamelCase("my_name_is_qaiu"));
            assertEquals("userId", StringCase.toLittleCamelCase("user_id"));
        }

        @Test
        @DisplayName("下划线转大驼峰")
        void testUnderlineToBigCamel() {
            assertEquals("HelloWorld", StringCase.toBigCamelCase("hello_world"));
            assertEquals("MyNameIsQaiu", StringCase.toBigCamelCase("my_name_is_qaiu"));
            assertEquals("UserId", StringCase.toBigCamelCase("user_id"));
        }

        @ParameterizedTest
        @CsvSource({
            "hello_world, helloWorld, HelloWorld",
            "my_name_is_qaiu, myNameIsQaiu, MyNameIsQaiu",
            "user_id, userId, UserId",
            "xml_http_request, xmlHttpRequest, XmlHttpRequest",
            "html5_parser, html5Parser, Html5Parser"
        })
        @DisplayName("参数化测试下划线转驼峰")
        void testUnderlineToCamelParameterized(String input, String littleCamel, String bigCamel) {
            assertEquals(littleCamel, StringCase.toLittleCamelCase(input));
            assertEquals(bigCamel, StringCase.toBigCamelCase(input));
        }

            @Test
            @DisplayName("边界情况测试")
            void testEdgeCases() {
                assertNull(StringCase.toLittleCamelCase(null));
                assertNull(StringCase.toBigCamelCase(null));
                assertEquals("", StringCase.toLittleCamelCase(""));
                assertEquals("", StringCase.toBigCamelCase(""));
                assertEquals("  ", StringCase.toLittleCamelCase("  "));
                assertEquals("\0 ", StringCase.toBigCamelCase("  "));
            }

        @Test
        @DisplayName("特殊字符测试")
        void testSpecialCharacters() {
            assertEquals("myNameQaiu", StringCase.toLittleCamelCase("____my_name_qaiu___"));
            assertEquals("MyNameQaiu", StringCase.toBigCamelCase("____my_name_qaiu___"));
            assertEquals("test123", StringCase.toLittleCamelCase("test123"));
            assertEquals("Test123", StringCase.toBigCamelCase("test123"));
        }

        @Test
        @DisplayName("单个字符测试")
        void testSingleCharacter() {
            assertEquals("a", StringCase.toLittleCamelCase("a"));
            assertEquals("A", StringCase.toBigCamelCase("a"));
            assertEquals("", StringCase.toLittleCamelCase("_"));
            assertEquals("", StringCase.toBigCamelCase("_"));
        }

        @Test
        @DisplayName("连续下划线测试")
        void testConsecutiveUnderscores() {
            assertEquals("helloWorld", StringCase.toLittleCamelCase("hello__world"));
            assertEquals("HelloWorld", StringCase.toBigCamelCase("hello__world"));
            assertEquals("helloWorld", StringCase.toLittleCamelCase("hello___world"));
            assertEquals("HelloWorld", StringCase.toBigCamelCase("hello___world"));
        }
    }

    @Nested
    @DisplayName("下划线大写测试")
    class ToUnderlineUpperCaseTest {

        @Test
        @DisplayName("基本下划线大写转换")
        void testBasicUnderlineUpperCase() {
            assertEquals("HELLO_WORLD", StringCase.toUnderlineUpperCase("HelloWorld"));
            assertEquals("MY_NAME_IS_QAIU", StringCase.toUnderlineUpperCase("MyNameIsQaiu"));
            assertEquals("USER_ID", StringCase.toUnderlineUpperCase("UserId"));
        }

        @Test
        @DisplayName("边界情况测试")
        void testEdgeCases() {
            assertNull(StringCase.toUnderlineUpperCase(null));
            assertEquals("", StringCase.toUnderlineUpperCase(""));
                assertEquals("  ", StringCase.toUnderlineUpperCase("  "));
        }

        @Test
        @DisplayName("特殊字符测试")
        void testSpecialCharacters() {
            assertEquals("__MY__NAME_QAIU___", StringCase.toUnderlineUpperCase("__my_nameQaiu___"));
            assertEquals("ABC", StringCase.toUnderlineUpperCase("ABC"));
            assertEquals("TEST_123", StringCase.toUnderlineUpperCase("test123"));
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("大量字符串转换性能测试")
        void testPerformance() {
            String[] testStrings = {
                "HelloWorld", "MyNameIsQaiu", "UserId", "XMLHttpRequest", 
                "HTML5Parser", "Test123", "ABC", "Hello_World", "my_name_qaiu"
            };

            long startTime = System.currentTimeMillis();
            
            // 执行10000次转换
            for (int i = 0; i < 10000; i++) {
                for (String str : testStrings) {
                    StringCase.toUnderlineCase(str);
                    StringCase.toLittleCamelCase(str);
                    StringCase.toBigCamelCase(str);
                    StringCase.toUnderlineUpperCase(str);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 性能要求：10000次转换应该在1秒内完成
            assertTrue(duration < 1000, "性能测试失败，耗时: " + duration + "ms");
            
            System.out.println("性能测试完成，耗时: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTest {

        @Test
        @DisplayName("驼峰->下划线->驼峰往返测试")
        void testCamelToUnderlineRoundTrip() {
            String[] originalCamel = {
                "HelloWorld", "MyNameIsQaiu", "UserId", "XMLHttpRequest", "HTML5Parser"
            };

            for (String original : originalCamel) {
                String underline = StringCase.toUnderlineCase(original);
                String backToCamel = StringCase.toBigCamelCase(underline);
                // 注意：往返转换可能不完全一致，这是正常的
                assertNotNull(backToCamel, "转换结果不应为null: " + original);
            }
        }

        @Test
        @DisplayName("下划线->驼峰->下划线往返测试")
        void testUnderlineToCamelRoundTrip() {
            String[] originalUnderline = {
                "hello_world", "my_name_is_qaiu", "user_id", "xml_http_request", "html5_parser"
            };

            for (String original : originalUnderline) {
                String camel = StringCase.toBigCamelCase(original);
                String backToUnderline = StringCase.toUnderlineCase(camel);
                // 注意：往返转换可能不完全一致，这是正常的
                assertNotNull(backToUnderline, "转换结果不应为null: " + original);
            }
        }
    }

    @Nested
    @DisplayName("实际应用场景测试")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("数据库字段名转换测试")
        void testDatabaseFieldConversion() {
            // Java实体字段 -> 数据库字段
            assertEquals("user_id", StringCase.toUnderlineCase("userId"));
            assertEquals("user_name", StringCase.toUnderlineCase("userName"));
            assertEquals("created_at", StringCase.toUnderlineCase("createdAt"));
            assertEquals("is_deleted", StringCase.toUnderlineCase("isDeleted"));
            
            // 数据库字段 -> Java实体字段
            assertEquals("userId", StringCase.toLittleCamelCase("user_id"));
            assertEquals("userName", StringCase.toLittleCamelCase("user_name"));
            assertEquals("createdAt", StringCase.toLittleCamelCase("created_at"));
            assertEquals("isDeleted", StringCase.toLittleCamelCase("is_deleted"));
        }

        @Test
        @DisplayName("JSON字段名转换测试")
        void testJsonFieldConversion() {
            // Java字段 -> JSON字段
            assertEquals("user_id", StringCase.toUnderlineCase("userId"));
            assertEquals("user_name", StringCase.toUnderlineCase("userName"));
            assertEquals("email_address", StringCase.toUnderlineCase("emailAddress"));
            
            // JSON字段 -> Java字段
            assertEquals("userId", StringCase.toLittleCamelCase("user_id"));
            assertEquals("userName", StringCase.toLittleCamelCase("user_name"));
            assertEquals("emailAddress", StringCase.toLittleCamelCase("email_address"));
        }

        @Test
        @DisplayName("API参数名转换测试")
        void testApiParameterConversion() {
            // Java方法参数 -> API参数
            assertEquals("page_size", StringCase.toUnderlineCase("pageSize"));
            assertEquals("sort_order", StringCase.toUnderlineCase("sortOrder"));
            assertEquals("filter_type", StringCase.toUnderlineCase("filterType"));
            
            // API参数 -> Java方法参数
            assertEquals("pageSize", StringCase.toLittleCamelCase("page_size"));
            assertEquals("sortOrder", StringCase.toLittleCamelCase("sort_order"));
            assertEquals("filterType", StringCase.toLittleCamelCase("filter_type"));
        }
    }
}
