package cn.qaiu.vx.core.security;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SecurityInterceptor 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("安全拦截器测试")
public class SecurityInterceptorTest {

    private SecurityConfig config;
    private JwtAuthProvider jwtAuthProvider;
    private SecurityInterceptor interceptor;

    @BeforeEach
    void setUp(Vertx vertx) {
        // 创建基本配置
        JsonObject configJson = new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-key-for-unit-tests-minimum-32-chars")
            .put("jwt-expire-seconds", 3600)
            .put("jwt-auth-reg", new io.vertx.core.json.JsonArray()
                .add("/api/secure/.*")
                .add("/api/admin/.*"))
            .put("jwt-ignores-reg", new io.vertx.core.json.JsonArray()
                .add("/api/public/.*")
                .add("/health")
                .add("/api/auth/login"));
        
        config = new SecurityConfig(configJson);
        jwtAuthProvider = new JwtAuthProvider(vertx, config);
        interceptor = new SecurityInterceptor(jwtAuthProvider, config);
    }

    @Test
    @DisplayName("测试构造函数 - 使用JwtAuthProvider和SecurityConfig")
    void testConstructorWithProviderAndConfig() {
        assertNotNull(interceptor, "拦截器不应为空");
    }

    @Test
    @DisplayName("测试构造函数 - 使用JsonObject配置")
    void testConstructorWithJsonConfig(Vertx vertx) {
        JsonObject configJson = new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "another-test-secret-key-32-chars-long");
        
        SecurityInterceptor interceptorFromJson = new SecurityInterceptor(configJson);
        
        assertNotNull(interceptorFromJson, "从JsonObject创建的拦截器不应为空");
    }

    @Test
    @DisplayName("测试compilePatterns - 空数组")
    void testCompilePatternsEmpty() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("compilePatterns", String[].class);
        method.setAccessible(true);
        
        Pattern[] result = (Pattern[]) method.invoke(interceptor, (Object) new String[0]);
        
        assertNotNull(result, "结果不应为空");
        assertEquals(0, result.length, "空数组应返回空Pattern数组");
    }

    @Test
    @DisplayName("测试compilePatterns - null数组")
    void testCompilePatternsNull() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("compilePatterns", String[].class);
        method.setAccessible(true);
        
        Pattern[] result = (Pattern[]) method.invoke(interceptor, (Object) null);
        
        assertNotNull(result, "结果不应为空");
        assertEquals(0, result.length, "null应返回空Pattern数组");
    }

    @Test
    @DisplayName("测试compilePatterns - 有效正则表达式")
    void testCompilePatternsValid() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("compilePatterns", String[].class);
        method.setAccessible(true);
        
        String[] patterns = {"/api/.*", "/user/\\d+", "^/admin"};
        Pattern[] result = (Pattern[]) method.invoke(interceptor, (Object) patterns);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        
        // 验证编译后的Pattern可以正常工作
        assertTrue(result[0].matcher("/api/test").matches());
        assertTrue(result[1].matcher("/user/123").matches());
        assertTrue(result[2].matcher("/admin").find());
    }

    @Test
    @DisplayName("测试isPathIgnored - 路径在忽略列表中")
    void testIsPathIgnoredTrue() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("isPathIgnored", String.class);
        method.setAccessible(true);
        
        Boolean result = (Boolean) method.invoke(interceptor, "/health");
        
        assertTrue(result, "/health应该被忽略");
    }

    @Test
    @DisplayName("测试isPathIgnored - 路径不在忽略列表中")
    void testIsPathIgnoredFalse() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("isPathIgnored", String.class);
        method.setAccessible(true);
        
        Boolean result = (Boolean) method.invoke(interceptor, "/api/secure/data");
        
        assertFalse(result, "/api/secure/data不应该被忽略");
    }

    @Test
    @DisplayName("测试isPathRequiresAuth - 需要认证的路径")
    void testIsPathRequiresAuthTrue() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("isPathRequiresAuth", String.class);
        method.setAccessible(true);
        
        Boolean result = (Boolean) method.invoke(interceptor, "/api/secure/data");
        
        assertTrue(result, "/api/secure/data应该需要认证");
    }

    @Test
    @DisplayName("测试isPathRequiresAuth - 不需要认证的路径")
    void testIsPathRequiresAuthFalse() throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("isPathRequiresAuth", String.class);
        method.setAccessible(true);
        
        Boolean result = (Boolean) method.invoke(interceptor, "/api/other/data");
        
        assertFalse(result, "/api/other/data不应该需要认证");
    }

    @Test
    @DisplayName("测试extractToken - 从Authorization Header提取")
    void testExtractTokenFromHeader(Vertx vertx) throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("extractToken", HttpServerRequest.class);
        method.setAccessible(true);
        
        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token-123");
        
        String token = (String) method.invoke(interceptor, request);
        
        assertEquals("test-token-123", token, "应该正确提取Bearer后的token");
    }

    @Test
    @DisplayName("测试extractToken - Authorization Header为空")
    void testExtractTokenEmptyHeader(Vertx vertx) throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("extractToken", HttpServerRequest.class);
        method.setAccessible(true);
        
        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParam("token")).thenReturn(null);
        when(request.getCookie(anyString())).thenReturn(null);
        
        String token = (String) method.invoke(interceptor, request);
        
        assertNull(token, "没有token时应该返回null");
    }

    @Test
    @DisplayName("测试extractToken - 从查询参数提取")
    void testExtractTokenFromQueryParam(Vertx vertx) throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("extractToken", HttpServerRequest.class);
        method.setAccessible(true);
        
        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParam("access_token")).thenReturn("query-token-456");
        
        String token = (String) method.invoke(interceptor, request);
        
        assertEquals("query-token-456", token, "应该从查询参数提取token");
    }

    @Test
    @DisplayName("测试Pattern匹配 - 认证路径模式")
    void testAuthPathPatternMatching() throws Exception {
        Field field = SecurityInterceptor.class.getDeclaredField("authPathPatterns");
        field.setAccessible(true);
        
        Pattern[] patterns = (Pattern[]) field.get(interceptor);
        
        assertNotNull(patterns);
        assertTrue(patterns.length >= 2, "应该有至少2个认证路径模式");
        
        // 验证模式匹配
        boolean matches = false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher("/api/secure/users").matches()) {
                matches = true;
                break;
            }
        }
        assertTrue(matches, "/api/secure/users应该匹配认证路径模式");
    }

    @Test
    @DisplayName("测试Pattern匹配 - 忽略路径模式")
    void testIgnorePathPatternMatching() throws Exception {
        Field field = SecurityInterceptor.class.getDeclaredField("ignorePathPatterns");
        field.setAccessible(true);
        
        Pattern[] patterns = (Pattern[]) field.get(interceptor);
        
        assertNotNull(patterns);
        
        // 验证忽略路径
        boolean healthMatches = false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher("/health").matches()) {
                healthMatches = true;
                break;
            }
        }
        assertTrue(healthMatches, "/health应该匹配忽略路径模式");
    }

    @Test
    @DisplayName("测试SecurityRequirement内部类存在")
    void testSecurityRequirementInnerClassExists() throws Exception {
        // SecurityRequirement是内部私有类，通过反射验证其存在
        Class<?>[] declaredClasses = SecurityInterceptor.class.getDeclaredClasses();
        
        boolean found = false;
        for (Class<?> innerClass : declaredClasses) {
            if (innerClass.getSimpleName().equals("SecurityRequirement")) {
                found = true;
                // 验证可以创建实例
                var constructor = innerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object instance = constructor.newInstance();
                assertNotNull(instance);
                break;
            }
        }
        
        assertTrue(found, "SecurityRequirement内部类应存在");
    }

    @Test
    @DisplayName("测试拦截器实现Handler接口")
    void testInterceptorImplementsHandler() {
        assertTrue(interceptor instanceof Handler, "SecurityInterceptor应该实现Handler接口");
    }

    @Test
    @DisplayName("测试空认证路径配置")
    void testEmptyAuthPaths(Vertx vertx) {
        JsonObject emptyConfig = new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-key-for-empty-auth-paths");
        
        SecurityConfig emptySecConfig = new SecurityConfig(emptyConfig);
        JwtAuthProvider emptyProvider = new JwtAuthProvider(vertx, emptySecConfig);
        SecurityInterceptor emptyInterceptor = new SecurityInterceptor(emptyProvider, emptySecConfig);
        
        assertNotNull(emptyInterceptor, "即使没有配置认证路径也应该能创建拦截器");
    }

    @Test
    @DisplayName("测试多种路径模式")
    void testVariousPathPatterns() throws Exception {
        Method isIgnored = SecurityInterceptor.class.getDeclaredMethod("isPathIgnored", String.class);
        isIgnored.setAccessible(true);
        
        Method requiresAuth = SecurityInterceptor.class.getDeclaredMethod("isPathRequiresAuth", String.class);
        requiresAuth.setAccessible(true);
        
        // 测试各种路径
        assertTrue((Boolean) isIgnored.invoke(interceptor, "/api/public/docs"));
        assertTrue((Boolean) isIgnored.invoke(interceptor, "/api/auth/login"));
        
        assertTrue((Boolean) requiresAuth.invoke(interceptor, "/api/admin/users"));
        assertTrue((Boolean) requiresAuth.invoke(interceptor, "/api/secure/profile"));
        
        assertFalse((Boolean) requiresAuth.invoke(interceptor, "/static/css/style.css"));
    }

    @Test
    @DisplayName("测试Bearer前缀处理")
    void testBearerPrefixHandling(Vertx vertx) throws Exception {
        Method method = SecurityInterceptor.class.getDeclaredMethod("extractToken", HttpServerRequest.class);
        method.setAccessible(true);
        
        HttpServerRequest request = mock(HttpServerRequest.class);
        
        // 测试不同的Bearer格式
        when(request.getHeader("Authorization")).thenReturn("Bearer   spaced-token");
        String token1 = (String) method.invoke(interceptor, request);
        // 应该处理额外空格
        assertNotNull(token1);
        
        // 测试没有Bearer前缀
        when(request.getHeader("Authorization")).thenReturn("direct-token");
        String token2 = (String) method.invoke(interceptor, request);
        // 没有Bearer前缀可能返回null或原始值，取决于实现
    }

    @Test
    @DisplayName("测试配置获取")
    void testConfigAccess() throws Exception {
        Field configField = SecurityInterceptor.class.getDeclaredField("config");
        configField.setAccessible(true);
        
        SecurityConfig storedConfig = (SecurityConfig) configField.get(interceptor);
        
        assertNotNull(storedConfig, "配置不应为空");
        assertTrue(storedConfig.isJwtEnabled(), "JWT应该启用");
    }

    @Test
    @DisplayName("测试JwtAuthProvider获取")
    void testJwtAuthProviderAccess() throws Exception {
        Field providerField = SecurityInterceptor.class.getDeclaredField("jwtAuthProvider");
        providerField.setAccessible(true);
        
        JwtAuthProvider storedProvider = (JwtAuthProvider) providerField.get(interceptor);
        
        assertNotNull(storedProvider, "JwtAuthProvider不应为空");
    }
}
