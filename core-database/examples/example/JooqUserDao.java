package cn.qaiu.db.dsl.example;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * UserDAO - 基于新的jOOQ DSL框架实现
 */
public class JooqUserDao extends AbstractDao<User, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqUserDao.class);
    
    public JooqUserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<Optional<User>> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Future.succeededFuture(Optional.empty());
        }
        
        try {
            Condition condition = DSL.field("username").eq(username);
            return findByCondition(condition)
                    .map(users -> users.isEmpty() ? Optional.empty() : Optional.of(users.get(0)));
        } catch (Exception e) {
            LOGGER.error("Failed to find user by username: {}", username, e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 根据邮箱查找用户
     */
    public Future<Optional<User>> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Future.succeededFuture(Optional.empty());
        }
        
        try {
            Condition condition = DSL.field("email").eq(email);
            return findByCondition(condition)
                    .map(users -> users.isEmpty() ? Optional.empty() : Optional.of(users.get(0)));
        } catch (Exception e) {
            LOGGER.error("Failed to find user by email: {}", email, e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 查找指定状态的用户
     */
    public Future<List<User>> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return findAll();
        }
        
        try {
            Condition condition = DSL.field("user_status").eq(status);
            return findByCondition(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to find users by status: {}", status, e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 验证用户登录（用户名+密码）
     */
    public Future<Optional<User>> authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return Future.succeededFuture(Optional.empty());
        }
        
        try {
            // 首先尝试按用户名查找
            return findByUsername(usernameOrEmail)
                    .compose(userOpt -> {
                        if (userOpt.isPresent()) {
                            // 验证密码 - 使用User类的方法
                            User user = userOpt.get();
                            if (user.verifyPassword(password)) {
                                return Future.succeededFuture(userOpt);
                            } else {
                                return Future.succeededFuture(Optional.empty());
                            }
                        } else {
                            // 如果用户名找不到，尝试按邮箱查找
                            return findByEmail(usernameOrEmail)
                                    .map(emailUserOpt -> {
                                        if (emailUserOpt.isPresent()) {
                                            User emailUser = emailUserOpt.get();
                                            if (emailUser.verifyPassword(password)) {
                                                return emailUserOpt;
                                            }
                                        }
                                        return Optional.empty();
                                    });
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to authenticate user: {}", usernameOrEmail, e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 查找所有活跃用户
     */
    public Future<List<User>> findActiveUsers() {
        try {
            Field<String> statusField = DSL.field("status", String.class);
            Condition condition = statusField.eq(User.UserStatus.ACTIVE.name());
            return findByCondition(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to find active users", e);
            return Future.failedFuture(e);
        }
    }
}
