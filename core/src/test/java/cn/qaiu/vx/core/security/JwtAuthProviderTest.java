package cn.qaiu.vx.core.security;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtAuthProvider 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("JWT认证提供者测试")
public class JwtAuthProviderTest {

    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
    }

    @Test
    @DisplayName("测试HS256算法初始化")
    void testInitializeWithHS256(VertxTestContext testContext) {
        JsonObject configJson = new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-key-must-be-at-least-32-characters-long");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .onComplete(testContext.succeeding(v -> {
                assertNotNull(provider.getJwtAuth(), "JWTAuth不应为空");
                assertNotNull(provider.getConfig(), "Config不应为空");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试RS256算法初始化（自动生成密钥）")
    void testInitializeWithRS256AutoKeys(VertxTestContext testContext) {
        JsonObject configJson = new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "RS256");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .onComplete(testContext.succeeding(v -> {
                assertNotNull(provider.getJwtAuth(), "JWTAuth不应为空");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试重复初始化")
    void testDoubleInitialization(VertxTestContext testContext) {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-double-init-32-chars");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> provider.initialize())  // 第二次初始化
            .onComplete(testContext.succeeding(v -> {
                // 第二次初始化应该直接成功，不重新初始化
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试Token生成和验证")
    void testGenerateAndVerifyToken(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-token-generation-32")
            .put("jwt-expire-seconds", 3600)
            .put("jwt-issuer", "test-issuer");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                // 生成Token
                JsonObject claims = new JsonObject()
                    .put("sub", "user123")
                    .put("username", "testuser")
                    .put("roles", new io.vertx.core.json.JsonArray().add("user"));
                
                String token = provider.generateToken(claims);
                assertNotNull(token, "生成的Token不应为空");
                assertFalse(token.isEmpty(), "Token不应为空字符串");
                
                // 验证Token
                return provider.authenticate(token);
            })
            .onComplete(testContext.succeeding(user -> {
                assertNotNull(user, "User不应为空");
                assertEquals("user123", user.principal().getString("sub"));
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试刷新Token生成")
    void testGenerateRefreshToken(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-refresh-token-32ch")
            .put("jwt-expire-seconds", 3600)
            .put("refresh-token-expire-seconds", 86400);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "user456")
                    .put("username", "refreshuser");
                
                String refreshToken = provider.generateRefreshToken(claims);
                assertNotNull(refreshToken, "刷新Token不应为空");
                
                // 验证刷新Token
                return provider.authenticate(refreshToken);
            })
            .onComplete(testContext.succeeding(user -> {
                assertNotNull(user);
                assertTrue(provider.isRefreshToken(user), "应该识别为刷新Token");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试Token撤销")
    void testRevokeToken(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-revoke-test-32ch")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "userToRevoke");
                
                String token = provider.generateToken(claims);
                
                // 先验证Token有效
                return provider.authenticate(token)
                    .compose(user -> {
                        // 撤销Token
                        provider.revokeToken(token, user);
                        
                        // 再次验证应该失败
                        return provider.authenticate(token);
                    });
            })
            .onComplete(testContext.failing(e -> {
                assertTrue(e.getMessage().contains("revoked") || e.getMessage().contains("blacklist"),
                    "撤销后的Token验证应该失败");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试Token撤销（仅Token）")
    void testRevokeTokenOnly(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-revoke-only-32ch")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "userRevokeOnly");
                
                String token = provider.generateToken(claims);
                
                // 撤销Token（不传User对象）
                provider.revokeToken(token);
                
                // 验证应该失败
                return provider.authenticate(token);
            })
            .onComplete(testContext.failing(e -> {
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试authenticateAndGetContext")
    void testAuthenticateAndGetContext(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-context-test-32ch")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "contextUser")
                    .put("roles", new io.vertx.core.json.JsonArray().add("admin"));
                
                String token = provider.generateToken(claims);
                
                return provider.authenticateAndGetContext(token);
            })
            .onComplete(testContext.succeeding(context -> {
                assertNotNull(context, "SecurityContext不应为空");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试未初始化时调用方法")
    void testUninitializedAccess() {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-uninit-test-32ch");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        // 未初始化时调用generateToken应抛出异常
        assertThrows(IllegalStateException.class, () -> {
            provider.generateToken(new JsonObject());
        }, "未初始化时应抛出IllegalStateException");
        
        // 未初始化时调用getJwtAuth应抛出异常
        assertThrows(IllegalStateException.class, () -> {
            provider.getJwtAuth();
        }, "未初始化时应抛出IllegalStateException");
    }

    @Test
    @DisplayName("测试getConfig方法")
    void testGetConfig() {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-config-access-32");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        SecurityConfig retrievedConfig = provider.getConfig();
        
        assertNotNull(retrievedConfig, "getConfig不应返回null");
        assertEquals("HS256", retrievedConfig.getJwtAlgorithm());
    }

    @Test
    @DisplayName("测试generateRandomSecret方法")
    void testGenerateRandomSecret() throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        Method method = JwtAuthProvider.class.getDeclaredMethod("generateRandomSecret");
        method.setAccessible(true);
        
        String secret1 = (String) method.invoke(provider);
        String secret2 = (String) method.invoke(provider);
        
        assertNotNull(secret1);
        assertNotNull(secret2);
        assertNotEquals(secret1, secret2, "每次生成的密钥应该不同");
    }

    @Test
    @DisplayName("测试generateRSAKeyPair方法")
    void testGenerateRSAKeyPair() throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "RS256");
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        Method method = JwtAuthProvider.class.getDeclaredMethod("generateRSAKeyPair");
        method.setAccessible(true);
        
        KeyPair keyPair = (KeyPair) method.invoke(provider);
        
        assertNotNull(keyPair, "KeyPair不应为空");
        assertNotNull(keyPair.getPublic(), "公钥不应为空");
        assertNotNull(keyPair.getPrivate(), "私钥不应为空");
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
    }

    @Test
    @DisplayName("测试isRefreshToken - 普通Token")
    void testIsRefreshTokenNormal(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-refresh-check-32")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "normalUser");
                
                String token = provider.generateToken(claims);
                return provider.authenticate(token);
            })
            .onComplete(testContext.succeeding(user -> {
                assertFalse(provider.isRefreshToken(user), "普通Token不应是刷新Token");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试无效Token验证")
    void testInvalidTokenAuthentication(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-invalid-test-32")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> provider.authenticate("invalid.token.here"))
            .onComplete(testContext.failing(e -> {
                assertNotNull(e, "无效Token应该导致错误");
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试空Token验证")
    void testEmptyTokenAuthentication(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-empty-test-32ch")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> provider.authenticate(""))
            .onComplete(testContext.failing(e -> {
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试带角色的Token")
    void testTokenWithRoles(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-roles-test-32ch")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "adminUser")
                    .put("roles", new io.vertx.core.json.JsonArray()
                        .add("admin")
                        .add("user")
                        .add("moderator"));
                
                String token = provider.generateToken(claims);
                return provider.authenticate(token);
            })
            .onComplete(testContext.succeeding(user -> {
                JsonObject principal = user.principal();
                assertNotNull(principal.getJsonArray("roles"));
                assertEquals(3, principal.getJsonArray("roles").size());
                testContext.completeNow();
            }));
    }

    @Test
    @DisplayName("测试带自定义Claims的Token")
    void testTokenWithCustomClaims(VertxTestContext testContext) throws Exception {
        JsonObject configJson = new JsonObject()
            .put("jwt-algorithm", "HS256")
            .put("jwt-secret", "test-secret-for-custom-claims-32")
            .put("jwt-expire-seconds", 3600);
        
        SecurityConfig config = new SecurityConfig(configJson);
        JwtAuthProvider provider = new JwtAuthProvider(vertx, config);
        
        provider.initialize()
            .compose(v -> {
                JsonObject claims = new JsonObject()
                    .put("sub", "customUser")
                    .put("customField1", "value1")
                    .put("customField2", 12345)
                    .put("nested", new JsonObject().put("key", "nestedValue"));
                
                String token = provider.generateToken(claims);
                return provider.authenticate(token);
            })
            .onComplete(testContext.succeeding(user -> {
                JsonObject principal = user.principal();
                assertEquals("value1", principal.getString("customField1"));
                assertEquals(12345, principal.getInteger("customField2"));
                testContext.completeNow();
            }));
    }
}
