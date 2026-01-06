package cn.qaiu.vx.core.security;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单管理器
 * 用于管理已失效/登出的Token，防止Token在过期前被继续使用
 *
 * <p>特性：
 * <ul>
 *   <li>基于内存的快速查询</li>
 *   <li>自动清理过期条目</li>
 *   <li>支持JTI（JWT ID）和完整Token两种方式</li>
 *   <li>线程安全</li>
 * </ul>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TokenBlacklist {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBlacklist.class);

    /**
     * 黑名单存储：key为token标识（JTI或token hash），value为过期时间戳
     */
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 清理任务间隔（毫秒）
     */
    private static final long CLEANUP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);

    /**
     * 默认Token过期时间（毫秒）- 24小时
     */
    private static final long DEFAULT_TOKEN_TTL_MS = TimeUnit.HOURS.toMillis(24);

    private volatile boolean cleanupScheduled = false;
    private Vertx vertx;

    /**
     * 单例持有者（懒加载）
     */
    private static class Holder {
        static final TokenBlacklist INSTANCE = new TokenBlacklist();
    }

    /**
     * 获取单例实例
     *
     * @return TokenBlacklist实例
     */
    public static TokenBlacklist getInstance() {
        return Holder.INSTANCE;
    }

    private TokenBlacklist() {
    }

    /**
     * 初始化黑名单管理器
     * 启动定时清理任务
     *
     * @param vertx Vertx实例
     */
    public void initialize(Vertx vertx) {
        this.vertx = vertx;
        scheduleCleanup();
        LOGGER.info("TokenBlacklist initialized with cleanup interval: {}ms", CLEANUP_INTERVAL_MS);
    }

    /**
     * 将Token加入黑名单
     *
     * @param tokenIdentifier Token标识（JTI或token本身的hash）
     * @param expireAtMs Token的原始过期时间戳（毫秒），黑名单条目将保留到该时间
     */
    public void addToBlacklist(String tokenIdentifier, long expireAtMs) {
        if (tokenIdentifier == null || tokenIdentifier.isEmpty()) {
            LOGGER.warn("Attempted to blacklist null or empty token identifier");
            return;
        }

        // 如果Token已经过期，无需加入黑名单
        if (expireAtMs <= System.currentTimeMillis()) {
            LOGGER.debug("Token already expired, skipping blacklist: {}", tokenIdentifier);
            return;
        }

        blacklist.put(tokenIdentifier, expireAtMs);
        LOGGER.debug("Token added to blacklist: {}, expires at: {}", tokenIdentifier, expireAtMs);
    }

    /**
     * 将Token加入黑名单（使用默认过期时间）
     *
     * @param tokenIdentifier Token标识
     */
    public void addToBlacklist(String tokenIdentifier) {
        addToBlacklist(tokenIdentifier, System.currentTimeMillis() + DEFAULT_TOKEN_TTL_MS);
    }

    /**
     * 使用Token的JTI和过期秒数加入黑名单
     *
     * @param jti JWT ID
     * @param expiresInSeconds Token剩余有效秒数
     */
    public void addToBlacklistWithTtl(String jti, int expiresInSeconds) {
        long expireAtMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSeconds);
        addToBlacklist(jti, expireAtMs);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param tokenIdentifier Token标识
     * @return 是否被列入黑名单
     */
    public boolean isBlacklisted(String tokenIdentifier) {
        if (tokenIdentifier == null || tokenIdentifier.isEmpty()) {
            return false;
        }

        Long expireAt = blacklist.get(tokenIdentifier);
        if (expireAt == null) {
            return false;
        }

        // 检查黑名单条目是否已过期
        if (expireAt <= System.currentTimeMillis()) {
            // 条目已过期，移除并返回false
            blacklist.remove(tokenIdentifier);
            return false;
        }

        return true;
    }

    /**
     * 从黑名单中移除Token（一般不需要手动调用）
     *
     * @param tokenIdentifier Token标识
     */
    public void removeFromBlacklist(String tokenIdentifier) {
        if (tokenIdentifier != null) {
            blacklist.remove(tokenIdentifier);
            LOGGER.debug("Token removed from blacklist: {}", tokenIdentifier);
        }
    }

    /**
     * 清空黑名单
     */
    public void clear() {
        int size = blacklist.size();
        blacklist.clear();
        LOGGER.info("TokenBlacklist cleared, {} entries removed", size);
    }

    /**
     * 获取当前黑名单大小
     *
     * @return 黑名单条目数
     */
    public int size() {
        return blacklist.size();
    }

    /**
     * 清理过期的黑名单条目
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        int beforeSize = blacklist.size();

        blacklist.entrySet().removeIf(entry -> entry.getValue() <= now);

        int removed = beforeSize - blacklist.size();
        if (removed > 0) {
            LOGGER.debug("TokenBlacklist cleanup completed, removed {} expired entries, remaining: {}",
                    removed, blacklist.size());
        }
    }

    /**
     * 调度定期清理任务
     */
    private void scheduleCleanup() {
        if (cleanupScheduled || vertx == null) {
            return;
        }

        cleanupScheduled = true;
        vertx.setPeriodic(CLEANUP_INTERVAL_MS, timerId -> {
            try {
                cleanup();
            } catch (Exception e) {
                LOGGER.error("Error during TokenBlacklist cleanup", e);
            }
        });

        LOGGER.debug("TokenBlacklist cleanup task scheduled");
    }

    /**
     * 生成Token标识（用于没有JTI的情况）
     * 使用Token内容的hash作为标识
     *
     * @param token 完整的JWT Token
     * @return Token标识
     */
    public static String generateTokenIdentifier(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 使用简单的hash，避免存储完整token
        return String.valueOf(token.hashCode());
    }

    /**
     * 从JWT claims中提取JTI，如果没有则生成hash标识
     *
     * @param claims JWT claims
     * @param token 完整token（作为fallback）
     * @return Token标识
     */
    public static String extractTokenIdentifier(io.vertx.core.json.JsonObject claims, String token) {
        String jti = claims.getString("jti");
        if (jti != null && !jti.isEmpty()) {
            return jti;
        }
        return generateTokenIdentifier(token);
    }
}
