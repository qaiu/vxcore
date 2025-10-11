package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.example.entity.User;
import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 * 演示 VXCore 框架的服务层，支持自动SQL执行器创建
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Service
@Singleton
public class UserServiceImpl extends JServiceImpl<User, Long> implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Override
    public Future<List<User>> findActiveUsers() {
        LOGGER.info("查找活跃用户");
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .orderByDesc(User::getCreateTime));
    }

    @Override
    public Future<List<User>> list() {
        LOGGER.info("获取用户列表");
        return lambdaList(lambdaQuery().orderByDesc(User::getCreateTime));
    }

    @Override
    public Future<Optional<User>> getById(Long id) {
        LOGGER.info("根据ID获取用户: {}", id);
        return lambdaOne(lambdaQuery().eq(User::getId, id));
    }

    @Override
    public Future<User> findByEmail(String email) {
        LOGGER.info("根据邮箱获取用户: {}", email);
        return lambdaOne(lambdaQuery().eq(User::getEmail, email))
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("用户不存在: " + email);
                    }
                });
    }

    @Override
    public Future<Optional<User>> save(User user) {
        LOGGER.info("保存用户: {}", user.getUsername());
        return insert(user);
    }

    @Override
    public Future<Optional<User>> updateById(User user) {
        LOGGER.info("更新用户: {}", user.getId());
        return update(user);
    }

    @Override
    public Future<Boolean> removeById(Long id) {
        LOGGER.info("删除用户: {}", id);
        return delete(id);
    }

    @Override
    public Future<Boolean> updateUserBalance(Long id, BigDecimal balance) {
        LOGGER.info("更新用户余额: {} -> {}", id, balance);
        // 先获取用户，然后更新余额
        return getById(id)
                .compose(optional -> {
                    if (optional.isPresent()) {
                        User user = optional.get();
                        user.setBalance(balance);
                        return updateById(user)
                                .map(result -> result.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    @Override
    public Future<Boolean> verifyUserEmail(Long id) {
        LOGGER.info("验证用户邮箱: {}", id);
        // 先获取用户，然后更新邮箱验证状态
        return getById(id)
                .compose(optional -> {
                    if (optional.isPresent()) {
                        User user = optional.get();
                        user.setEmailVerified(true);
                        return updateById(user)
                                .map(result -> result.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    @Override
    public Future<List<User>> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        LOGGER.info("根据年龄范围获取用户: {} - {}", minAge, maxAge);
        return lambdaList(lambdaQuery()
                .between(User::getAge, minAge, maxAge)
                .orderByDesc(User::getCreateTime));
    }

    @Override
    public Future<JsonObject> getUserStatistics() {
        LOGGER.info("获取用户统计信息");
        return lambdaCount(lambdaQuery())
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
    public Future<Boolean> existsByEmail(String email) {
        LOGGER.info("检查邮箱是否存在: {}", email);
        return lambdaCount(lambdaQuery().eq(User::getEmail, email))
                .map(count -> count > 0);
    }

    @Override
    public Future<Long> countUsers() {
        LOGGER.info("统计用户总数");
        return lambdaCount(lambdaQuery());
    }

    @Override
    public Future<List<User>> searchByName(String keyword) {
        LOGGER.info("根据用户名模糊查询: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return Future.succeededFuture(List.of());
        }
        
        return lambdaList(lambdaQuery()
                .like(User::getUsername, "%" + keyword + "%")
                .orderByDesc(User::getCreateTime)
                .limit(20));
    }
}