package cn.qaiu.db.dsl.example;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问对象 (DAO) - 基于 jOOQ DSL
 * 
 * 继承 AbstractDao，提供 User 实体的特定 CRUD 和查询操作。
 * 使用 jOOQ DSL 构建类型安全的 SQL 查询。
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class UserDao extends AbstractDao<User, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    /**
     * 构造函数
     * 
     * @param executor jOOQ DSL 执行器
     */
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
        LOGGER.debug("Initialized UserDao for table: {}", getTableName());
    }

    /**
     * 创建新用户
     * 
     * @param username 用户名
     * @param email    邮箱
     * @param password 密码
     * @return 包含新创建用户的 Future
     */
    public Future<User> createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setAge(25); // 默认年龄
        user.setStatus(User.UserStatus.ACTIVE);
        user.setBalance(new BigDecimal("100.00"));
        user.setEmailVerified(false);
        
        return insert(user)
                .map(optionalUser -> {
                    if (optionalUser.isPresent()) {
                        LOGGER.info("User created successfully with ID: {}", optionalUser.get().getId());
                        return optionalUser.get();
                    } else {
                        throw new RuntimeException("Failed to create user");
                    }
                });
    }

    /**
     * 更新用户密码
     * 
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 包含更新是否成功的 Future
     */
    public Future<Boolean> updatePassword(Long userId, String newPassword) {
        LOGGER.debug("Updating password for user ID: {}", userId);
        
        return findById(userId)
                .compose(userOptional -> {
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setPassword(newPassword);
                        return update(user)
                                .map(updatedUser -> updatedUser.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    /**
     * 验证用户邮箱
     * 
     * @param userId 用户ID
     * @return 包含验证是否成功的 Future
     */
    public Future<Boolean> verifyUserEmail(Long userId) {
        LOGGER.debug("Verifying email for user ID: {}", userId);
        
        return findById(userId)
                .compose(userOptional -> {
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setEmailVerified(true);
                        return update(user)
                                .map(updatedUser -> updatedUser.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态
     * @return 包含更新是否成功的 Future
     */
    public Future<Boolean> updateUserStatus(Long userId, User.UserStatus status) {
        LOGGER.debug("Updating status for user ID: {} to {}", userId, status);
        
        return findById(userId)
                .compose(userOptional -> {
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setStatus(status);
                        return update(user)
                                .map(updatedUser -> updatedUser.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 包含 Optional<User> 的 Future
     */
    public Future<Optional<User>> findByUsername(String username) {
        LOGGER.debug("Finding user by username: {}", username);
        
        Field<String> usernameField = DSL.field("username", String.class);
        Condition condition = usernameField.eq(username);
        
        return findByCondition(condition)
                .map(users -> users.isEmpty() ? Optional.empty() : Optional.of(users.get(0)));
    }

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 包含 Optional<User> 的 Future
     */
    public Future<Optional<User>> findByEmail(String email) {
        LOGGER.debug("Finding user by email: {}", email);
        
        Field<String> emailField = DSL.field("email", String.class);
        Condition condition = emailField.eq(email);
        
        return findByCondition(condition)
                .map(users -> users.isEmpty() ? Optional.empty() : Optional.of(users.get(0)));
    }

    /**
     * 查找所有活跃用户
     * 
     * @return 包含活跃用户列表的 Future
     */
    public Future<List<User>> findActiveUsers() {
        LOGGER.debug("Finding all active users");
        
        Field<String> statusField = DSL.field("status", String.class);
        Condition condition = statusField.eq(User.UserStatus.ACTIVE.name());
        
        return findByCondition(condition);
    }

    /**
     * 获取用户统计信息
     * 
     * @return 包含统计信息的 JsonObject Future
     */
    public Future<JsonObject> getUserStatistics() {
        LOGGER.debug("Getting user statistics");

        return findAll().map(allUsers -> {
            long activeCount = allUsers.stream()
                    .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
                    .count();
            
            double avgAge = allUsers.stream()
                    .filter(u -> u.getAge() != null)
                    .mapToInt(User::getAge)
                    .average()
                    .orElse(0.0);

            return new JsonObject()
                    .put("totalUsers", allUsers.size())
                    .put("activeUsers", activeCount)
                    .put("averageAge", avgAge);
        });
    }

    /**
     * 查找指定年龄范围内的用户
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 包含用户列表的 Future
     */
    public Future<List<User>> findByAgeRange(int minAge, int maxAge) {
        LOGGER.debug("Finding users in age range {}-{}", minAge, maxAge);
        
        Field<Integer> ageField = DSL.field("age", Integer.class);
        Condition condition = ageField.between(minAge, maxAge);
        
        return findByCondition(condition);
    }

    /**
     * 查找余额大于等于指定金额的用户
     * 
     * @param minBalance 最小余额
     * @return 包含用户列表的 Future
     */
    public Future<List<User>> findByMinBalance(BigDecimal minBalance) {
        LOGGER.debug("Finding users with balance >= {}", minBalance);
        
        Field<BigDecimal> balanceField = DSL.field("balance", BigDecimal.class);
        Condition condition = balanceField.ge(minBalance);
        
        return findByCondition(condition);
    }

    /**
     * 分页查询用户
     * 
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 包含用户列表的 Future
     */
    public Future<List<User>> findPage(int offset, int limit) {
        LOGGER.debug("Finding users with offset {} and limit {}", offset, limit);
        
        // 使用jOOQ DSL构建分页查询
        Field<Long> idField = DSL.field("id", Long.class);
        Query selectQuery = executor.dsl()
                .selectFrom(DSL.name(getTableName()))
                .orderBy(idField.asc())
                .offset(offset)
                .limit(limit);
        
        return executor.executeQuery(selectQuery)
                .map(rowSet -> {
                    // 使用DefaultMapper转换结果
                    cn.qaiu.db.dsl.mapper.DefaultMapper<User> mapper = 
                            new cn.qaiu.db.dsl.mapper.DefaultMapper<>(User.class, getTableName());
                    return mapper.fromMultiple(rowSet);
                });
    }
}