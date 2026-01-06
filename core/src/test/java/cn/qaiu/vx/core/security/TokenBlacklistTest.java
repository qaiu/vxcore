package cn.qaiu.vx.core.security;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenBlacklist 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("Token黑名单测试")
class TokenBlacklistTest {

    private TokenBlacklist blacklist;
    private Vertx vertx;

    @BeforeEach
    void setUp() {
        blacklist = TokenBlacklist.getInstance();
        blacklist.clear();
        vertx = Vertx.vertx();
        blacklist.initialize(vertx);
    }

    @AfterEach
    void tearDown() {
        blacklist.clear();
        if (vertx != null) {
            vertx.close();
        }
    }

    @Test
    @DisplayName("测试添加Token到黑名单")
    void testAddToBlacklist() {
        String tokenId = "test-token-123";
        long expireAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        
        blacklist.addToBlacklist(tokenId, expireAt);
        
        assertTrue(blacklist.isBlacklisted(tokenId));
        assertEquals(1, blacklist.size());
    }

    @Test
    @DisplayName("测试空Token不加入黑名单")
    void testAddNullOrEmptyToken() {
        blacklist.addToBlacklist(null);
        blacklist.addToBlacklist("");
        
        assertEquals(0, blacklist.size());
        assertFalse(blacklist.isBlacklisted(null));
        assertFalse(blacklist.isBlacklisted(""));
    }

    @Test
    @DisplayName("测试已过期Token不加入黑名单")
    void testAddExpiredToken() {
        String tokenId = "expired-token";
        long expiredTime = System.currentTimeMillis() - 1000; // 1秒前过期
        
        blacklist.addToBlacklist(tokenId, expiredTime);
        
        assertFalse(blacklist.isBlacklisted(tokenId));
        assertEquals(0, blacklist.size());
    }

    @Test
    @DisplayName("测试Token黑名单查询")
    void testIsBlacklisted() {
        String tokenId1 = "token-1";
        String tokenId2 = "token-2";
        long expireAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        
        blacklist.addToBlacklist(tokenId1, expireAt);
        
        assertTrue(blacklist.isBlacklisted(tokenId1));
        assertFalse(blacklist.isBlacklisted(tokenId2));
    }

    @Test
    @DisplayName("测试黑名单条目自动过期")
    void testBlacklistEntryExpiration() throws InterruptedException {
        String tokenId = "short-lived-token";
        long expireAt = System.currentTimeMillis() + 100; // 100ms后过期
        
        blacklist.addToBlacklist(tokenId, expireAt);
        assertTrue(blacklist.isBlacklisted(tokenId));
        
        // 等待过期
        Thread.sleep(150);
        
        // 过期后查询应返回false
        assertFalse(blacklist.isBlacklisted(tokenId));
    }

    @Test
    @DisplayName("测试从黑名单移除Token")
    void testRemoveFromBlacklist() {
        String tokenId = "removable-token";
        long expireAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        
        blacklist.addToBlacklist(tokenId, expireAt);
        assertTrue(blacklist.isBlacklisted(tokenId));
        
        blacklist.removeFromBlacklist(tokenId);
        assertFalse(blacklist.isBlacklisted(tokenId));
    }

    @Test
    @DisplayName("测试清空黑名单")
    void testClearBlacklist() {
        long expireAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        
        blacklist.addToBlacklist("token-1", expireAt);
        blacklist.addToBlacklist("token-2", expireAt);
        blacklist.addToBlacklist("token-3", expireAt);
        
        assertEquals(3, blacklist.size());
        
        blacklist.clear();
        
        assertEquals(0, blacklist.size());
    }

    @Test
    @DisplayName("测试使用TTL添加Token")
    void testAddToBlacklistWithTtl() {
        String tokenId = "ttl-token";
        
        blacklist.addToBlacklistWithTtl(tokenId, 3600); // 1小时
        
        assertTrue(blacklist.isBlacklisted(tokenId));
    }

    @Test
    @DisplayName("测试生成Token标识符")
    void testGenerateTokenIdentifier() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        
        String identifier = TokenBlacklist.generateTokenIdentifier(token);
        
        assertNotNull(identifier);
        assertFalse(identifier.isEmpty());
        
        // 相同token应生成相同标识符
        String identifier2 = TokenBlacklist.generateTokenIdentifier(token);
        assertEquals(identifier, identifier2);
    }

    @Test
    @DisplayName("测试从claims提取Token标识符")
    void testExtractTokenIdentifierWithJti() {
        io.vertx.core.json.JsonObject claims = new io.vertx.core.json.JsonObject()
                .put("jti", "unique-jwt-id-123")
                .put("sub", "user-1");
        String token = "some-token";
        
        String identifier = TokenBlacklist.extractTokenIdentifier(claims, token);
        
        assertEquals("unique-jwt-id-123", identifier);
    }

    @Test
    @DisplayName("测试从claims提取Token标识符（无JTI）")
    void testExtractTokenIdentifierWithoutJti() {
        io.vertx.core.json.JsonObject claims = new io.vertx.core.json.JsonObject()
                .put("sub", "user-1");
        String token = "some-token";
        
        String identifier = TokenBlacklist.extractTokenIdentifier(claims, token);
        
        assertNotNull(identifier);
        // 应该返回token的hash
        assertEquals(TokenBlacklist.generateTokenIdentifier(token), identifier);
    }

    @Test
    @DisplayName("测试黑名单清理功能")
    void testCleanup() throws InterruptedException {
        // 添加一个已过期的条目
        String expiredToken = "expired";
        blacklist.addToBlacklist(expiredToken, System.currentTimeMillis() + 50);
        
        // 添加一个未过期的条目
        String validToken = "valid";
        blacklist.addToBlacklist(validToken, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        
        assertEquals(2, blacklist.size());
        
        // 等待第一个过期
        Thread.sleep(100);
        
        // 手动清理
        blacklist.cleanup();
        
        // 应该只剩下未过期的
        assertEquals(1, blacklist.size());
        assertFalse(blacklist.isBlacklisted(expiredToken));
        assertTrue(blacklist.isBlacklisted(validToken));
    }

    @Test
    @DisplayName("测试单例模式")
    void testSingleton() {
        TokenBlacklist instance1 = TokenBlacklist.getInstance();
        TokenBlacklist instance2 = TokenBlacklist.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("测试使用默认TTL添加Token")
    void testAddToBlacklistWithDefaultTtl() {
        String tokenId = "default-ttl-token";
        
        blacklist.addToBlacklist(tokenId);
        
        assertTrue(blacklist.isBlacklisted(tokenId));
        assertEquals(1, blacklist.size());
    }
}
