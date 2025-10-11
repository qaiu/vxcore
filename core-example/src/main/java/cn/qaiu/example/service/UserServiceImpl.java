package cn.qaiu.example.service;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务实现类
 * 演示 JService 的使用，支持 DI 注入
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Singleton
public class UserServiceImpl extends JServiceImpl<User, Long> implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 构造函数 - 使用 DI 注入 JooqExecutor
     * 
     * @param executor JooqExecutor 实例（由 DI 容器自动注入）
     */
    @Inject
    public UserServiceImpl(JooqExecutor executor) {
        super(executor, User.class);
        LOGGER.info("UserServiceImpl initialized with DI injection");
    }

    @Override
    public Future<List<User>> findActiveUsers() {
        LOGGER.info("查找活跃用户");
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .orderByDesc(User::getCreateTime));
    }

    @Override
    public Future<User> findByEmail(String email) {
        LOGGER.info("根据邮箱查找用户: {}", email);
        return getByField(User::getEmail, email)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("用户不存在: " + email);
                    }
                });
    }

    @Override
    public Future<List<User>> searchByName(String keyword) {
        LOGGER.info("根据用户名模糊查询: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return Future.succeededFuture(List.of());
        }
        
        return lambdaList(lambdaQuery()
                .like(User::getUsername, keyword)
                .orderByDesc(User::getCreateTime)
                .limit(20));
    }

    @Override
    public Future<Long> countUsers() {
        LOGGER.info("统计用户数量");
        return count();
    }

    @Override
    public Future<Boolean> existsByEmail(String email) {
        LOGGER.info("检查邮箱是否存在: {}", email);
        return existsByField(User::getEmail, email);
    }

    @Override
    public Future<Boolean> updateUserBalance(Long userId, BigDecimal balance) {
        LOGGER.info("更新用户余额: {} -> {}", userId, balance);
        User user = new User();
        user.setBalance(balance);
        return lambdaUpdate(lambdaQuery().eq(User::getId, userId), user)
                .map(rows -> rows > 0);
    }

    @Override
    public Future<Boolean> verifyUserEmail(Long userId) {
        LOGGER.info("验证用户邮箱: {}", userId);
        User user = new User();
        user.setEmailVerified(true);
        return lambdaUpdate(lambdaQuery().eq(User::getId, userId), user)
                .map(rows -> rows > 0);
    }

    @Override
    public Future<JsonObject> getUserStatistics() {
        LOGGER.info("获取用户统计信息");
        return count()
                .compose(totalCount -> 
                    lambdaCount(lambdaQuery().eq(User::getStatus, User.UserStatus.ACTIVE))
                        .map(activeCount -> {
                            JsonObject stats = new JsonObject();
                            stats.put("totalUsers", totalCount);
                            stats.put("activeUsers", activeCount);
                            stats.put("inactiveUsers", totalCount - activeCount);
                            return stats;
                        })
                );
    }

    @Override
    public Future<List<User>> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        LOGGER.info("根据年龄范围获取用户: {} - {}", minAge, maxAge);
        return lambdaList(lambdaQuery()
                .ge(User::getAge, minAge)
                .le(User::getAge, maxAge)
                .orderByDesc(User::getCreateTime));
    }
}
