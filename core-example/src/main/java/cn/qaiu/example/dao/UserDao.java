package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问对象 (DAO) - 演示 MyBatis-Plus 风格的 Lambda 查询
 * 
 * 继承 LambdaDao，父类已提供基础 CRUD 功能：
 * - insert(T entity)
 * - update(T entity) 
 * - delete(ID id)
 * - findById(ID id)
 * - findAll()
 * - findByCondition(Condition condition)
 * - count()
 * - lambdaQuery()
 * 
 * 本类只提供个性化的业务查询方法。
 * 
 * @author QAIU
 */
public class UserDao extends LambdaDao<User, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    /**
     * 默认构造函数
     */
    public UserDao() {
        super(null, User.class);
    }

    /**
     * 带参数的构造函数
     */
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }

    // =================== 个性化业务查询方法 ===================
    
    /**
     * 创建用户 - 简化版本（用于测试）
     */
    public Future<User> createUser(String name, String email, String password) {
        LOGGER.debug("Creating user: name={}, email={}", name, email);
        
        User user = new User();
        user.setUsername(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setAge(25); // 默认年龄
        user.setBio(""); // 默认简介
        user.setStatus(User.UserStatus.ACTIVE);
        user.setBalance(new BigDecimal("100.00")); // 默认余额
        user.setEmailVerified(false);
        
        return insert(user).map(optionalUser -> {
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            } else {
                throw new RuntimeException("Failed to create user");
            }
        });
    }
    
    /**
     * 创建用户 - 完整版本
     */
    public Future<User> createUser(String name, String email, String password, Integer age, String bio) {
        LOGGER.debug("Creating user: name={}, email={}", name, email);
        
        User user = new User();
        user.setUsername(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setAge(age);
        user.setBio(bio);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setBalance(BigDecimal.ZERO);
        user.setEmailVerified(false);
        
        return insert(user).map(optionalUser -> {
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            } else {
                throw new RuntimeException("Failed to create user");
            }
        });
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<List<User>> findByName(String name) {
        LOGGER.debug("Finding user by name: {}", name);
        
        return lambdaList(lambdaQuery()
                .eq(User::getUsername, name));
    }
    
    /**
     * 根据邮箱查找用户 - 演示 Lambda 查询
     */
    public Future<List<User>> findByEmail(String email) {
        LOGGER.debug("Finding user by email: {}", email);
        
        return lambdaList(lambdaQuery()
                .eq(User::getEmail, email));
    }
    
    /**
     * 根据邮箱查找单个用户
     */
    public Future<Optional<User>> findOneByEmail(String email) {
        LOGGER.debug("Finding one user by email: {}", email);
        
        return lambdaOne(lambdaQuery()
                .eq(User::getEmail, email));
    }
    
    /**
     * 根据状态查找用户
     */
    public Future<List<User>> findByStatus(User.UserStatus status) {
        LOGGER.debug("Finding users by status: {}", status);
        
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, status));
    }
    
    /**
     * 查找活跃用户
     */
    public Future<List<User>> findActiveUsers() {
        return findByStatus(User.UserStatus.ACTIVE);
    }
    
    /**
     * 更新用户密码
     */
    public Future<Boolean> updatePassword(Long userId, String newPassword) {
        LOGGER.debug("Updating password for user: {}", userId);
        
        return findById(userId)
                .compose(userOpt -> {
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.setPassword(newPassword);
                        return update(user).map(opt -> opt.isPresent());
                    }
                    return Future.succeededFuture(false);
                });
    }
    
    /**
     * 验证用户邮箱
     */
    public Future<Boolean> verifyUserEmail(Long userId) {
        LOGGER.debug("Verifying email for user: {}", userId);
        
        return findById(userId)
                .compose(userOpt -> {
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.setEmailVerified(true);
                        return update(user).map(opt -> opt.isPresent());
                    }
                    return Future.succeededFuture(false);
                });
    }
    
    /**
     * 更新用户状态
     */
    public Future<Boolean> updateUserStatus(Long userId, User.UserStatus status) {
        LOGGER.debug("Updating status for user: {}", userId);
        
        return findById(userId)
                .compose(userOpt -> {
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.setStatus(status);
                        return update(user).map(opt -> opt.isPresent());
                    }
                    return Future.succeededFuture(false);
                });
    }
    
    /**
     * 根据年龄范围查找用户
     */
    public Future<List<User>> findByAgeRange(Integer minAge, Integer maxAge) {
        LOGGER.debug("Finding users by age range: {}-{}", minAge, maxAge);
        
        return lambdaList(lambdaQuery()
                .ge(User::getAge, minAge)
                .le(User::getAge, maxAge));
    }
    
    /**
     * 根据余额范围查找用户
     */
    public Future<List<User>> findByBalanceRange(BigDecimal minBalance, BigDecimal maxBalance) {
        LOGGER.debug("Finding users by balance range: {}-{}", minBalance, maxBalance);
        
        return lambdaList(lambdaQuery()
                .between(User::getBalance, minBalance, maxBalance));
    }
    
    /**
     * 根据最小余额查找用户
     */
    public Future<List<User>> findByMinBalance(BigDecimal minBalance) {
        LOGGER.debug("Finding users with minimum balance: {}", minBalance);
        
        return lambdaList(lambdaQuery()
                .ge(User::getBalance, minBalance));
    }
    
    /**
     * 搜索用户
     */
    public Future<List<User>> searchUsers(String keyword) {
        LOGGER.debug("Searching users with keyword: {}", keyword);
        
        return lambdaList(lambdaQuery()
                .like(User::getUsername, "%" + keyword + "%"));
    }
    
    /**
     * 获取用户状态统计
     */
    public Future<JsonObject> getUserStatusStatistics() {
        LOGGER.debug("Getting user status statistics");
        
        return findAll().map(allUsers -> {
            JsonObject stats = new JsonObject();
            for (User.UserStatus status : User.UserStatus.values()) {
                long count = allUsers.stream()
                        .filter(user -> user.getStatus() == status)
                        .count();
                stats.put(status.name(), count);
            }
            return stats;
        });
    }
    
    /**
     * 更新用户余额
     */
    public Future<Boolean> updateBalance(Long userId, BigDecimal balance) {
        LOGGER.debug("Updating user balance: userId={}, balance={}", userId, balance);
        
        return findById(userId).compose(optionalUser -> {
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setBalance(balance);
                return update(user).map(updatedUser -> updatedUser.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 验证邮箱
     */
    public Future<Boolean> verifyEmail(Long userId) {
        LOGGER.debug("Verifying email for user: {}", userId);
        
        return findById(userId).compose(optionalUser -> {
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setEmailVerified(true);
                return update(user).map(updatedUser -> updatedUser.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 更新用户状态
     */
    public Future<Boolean> updateStatus(Long userId, User.UserStatus status) {
        LOGGER.debug("Updating user {} status to {}", userId, status);
        
        return findById(userId).compose(optionalUser -> {
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setStatus(status);
                return update(user).map(updatedUser -> updatedUser.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 激活用户
     */
    public Future<Boolean> activateUser(Long userId) {
        return updateStatus(userId, User.UserStatus.ACTIVE);
    }
    
    /**
     * 停用用户
     */
    public Future<Boolean> deactivateUser(Long userId) {
        return updateStatus(userId, User.UserStatus.INACTIVE);
    }
    
    /**
     * 统计用户数量
     */
    public Future<Long> countByStatus(User.UserStatus status) {
        LOGGER.debug("Counting users by status: {}", status);
        
        return lambdaCount(lambdaQuery()
                .eq(User::getStatus, status));
    }
    
    /**
     * 查找余额大于指定金额的用户
     */
    public Future<List<User>> findByBalanceGreaterThan(BigDecimal minBalance) {
        LOGGER.debug("Finding users with balance greater than: {}", minBalance);
        
        return lambdaList(lambdaQuery()
                .gt(User::getBalance, minBalance));
    }
    
    /**
     * 查找已验证邮箱的用户
     */
    public Future<List<User>> findVerifiedUsers() {
        LOGGER.debug("Finding verified users");
        
        return lambdaList(lambdaQuery()
                .eq(User::getEmailVerified, true));
    }
    
    /**
     * 查找未验证邮箱的用户
     */
    public Future<List<User>> findUnverifiedUsers() {
        LOGGER.debug("Finding unverified users");
        
        return lambdaList(lambdaQuery()
                .eq(User::getEmailVerified, false));
    }
    
    /**
     * 批量插入用户
     */
    public Future<Integer> insertBatch(List<User> users) {
        LOGGER.debug("Batch inserting {} users", users.size());
        // 简化实现：逐个插入
        Future<Void> result = Future.succeededFuture();
        for (User user : users) {
            result = result.compose(v -> insert(user).map(opt -> null));
        }
        return result.map(v -> users.size());
    }
    
    /**
     * 批量保存用户
     */
    public Future<List<User>> saveAll(List<User> users) {
        LOGGER.debug("Batch saving {} users", users.size());
        List<Future<User>> futures = users.stream()
                .map(this::save)
                .collect(java.util.stream.Collectors.toList());
        
        return Future.all(futures)
                .map(compositeFuture -> {
                    List<User> results = new ArrayList<>();
                    for (int i = 0; i < futures.size(); i++) {
                        results.add(compositeFuture.resultAt(i));
                    }
                    return results;
                });
    }
    
    /**
     * 批量更新用户
     */
    public Future<Integer> updateAll(List<User> users) {
        LOGGER.debug("Batch updating {} users", users.size());
        Future<Void> result = Future.succeededFuture();
        for (User user : users) {
            result = result.compose(v -> update(user).map(opt -> null));
        }
        return result.map(v -> users.size());
    }
    
    /**
     * 批量删除用户
     */
    public Future<Integer> deleteAllById(List<Long> ids) {
        LOGGER.debug("Batch deleting {} users", ids.size());
        Future<Void> result = Future.succeededFuture();
        for (Long id : ids) {
            result = result.compose(v -> deleteById(id).map(deleted -> null));
        }
        return result.map(v -> ids.size());
    }
    
    /**
     * Lambda插入（兼容性方法）
     */
    public Future<Optional<User>> lambdaInsert(User user) {
        LOGGER.debug("Lambda inserting user: {}", user.getEmail());
        return insert(user);
    }
    
    /**
     * 保存用户
     */
    public Future<User> save(User user) {
        LOGGER.debug("Saving user: {}", user.getEmail());
        return insert(user).map(optional -> optional.orElse(user));
    }
    
    /**
     * 根据ID获取用户
     */
    public Future<User> getById(Long id) {
        LOGGER.debug("Getting user by ID: {}", id);
        return findById(id).map(optional -> optional.orElse(null));
    }
    
    /**
     * 根据时间范围查找用户
     */
    public Future<List<User>> findByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LOGGER.debug("Finding users by time range: {} - {}", startTime, endTime);
        // 简化实现：使用 findAll 然后过滤
        return findAll().map(allUsers -> 
            allUsers.stream()
                .filter(user -> user.getCreatedAt().isAfter(startTime) && user.getCreatedAt().isBefore(endTime))
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * 根据ID删除用户
     */
    public Future<Boolean> deleteById(Long id) {
        LOGGER.debug("Deleting user by ID: {}", id);
        return delete(id);
    }

    /**
     * 获取用户统计信息 - 演示聚合查询
     */
    public Future<JsonObject> getUserStatistics() {
        LOGGER.debug("Getting user statistics");
        
        Field<BigDecimal> balanceField = DSL.field("balance", BigDecimal.class);
        Field<Integer> ageField = DSL.field("age", Integer.class);
        
        Query query = DSL.select(
                DSL.count().as("total_users"),
                DSL.count(DSL.field("status").eq("ACTIVE")).as("active_users"),
                DSL.avg(ageField).as("avg_age"),
                DSL.sum(balanceField).as("total_balance"),
                DSL.avg(balanceField).as("avg_balance"),
                DSL.max(balanceField).as("max_balance"),
                DSL.min(balanceField).as("min_balance")
        )
        .from(DSL.table(getTableName()));
        
        return executor.executeQuery(query)
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject()
                                .put("totalUsers", 0)
                                .put("activeUsers", 0)
                                .put("averageAge", 0.0)
                                .put("totalBalance", 0)
                                .put("avgBalance", 0)
                                .put("maxBalance", 0)
                                .put("minBalance", 0);
                    }
                    
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("totalUsers", row.getInteger("total_users"))
                            .put("activeUsers", row.getInteger("active_users"))
                            .put("averageAge", row.getBigDecimal("avg_age") != null ? row.getBigDecimal("avg_age").doubleValue() : 0.0)
                            .put("totalBalance", row.getBigDecimal("total_balance"))
                            .put("avgBalance", row.getBigDecimal("avg_balance"))
                            .put("maxBalance", row.getBigDecimal("max_balance"))
                            .put("minBalance", row.getBigDecimal("min_balance"));
                });
    }

    /**
     * 获取用户及其订单信息 - 演示Join查询
     */
    public Future<List<JsonObject>> getUsersWithOrders() {
        LOGGER.debug("Getting users with orders using join");
        
        return lambdaList(lambdaQuery()
                .leftJoin(cn.qaiu.example.entity.Order.class, (user, order) -> 
                    DSL.field("user_id", Long.class).eq(user.getId())))
                .map(users -> users.stream()
                        .map(User::toJson)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 根据产品ID获取购买该产品的用户 - 演示多表Join查询
     */
    public Future<List<JsonObject>> getUsersByProductId(Long productId) {
        LOGGER.debug("Getting users by product ID: {} using join", productId);
        
        // 使用原生SQL进行Join查询
        Field<Long> userIdField = DSL.field("u.id", Long.class);
        Field<String> userNameField = DSL.field("u.username", String.class);
        Field<String> userEmailField = DSL.field("u.email", String.class);
        
        Query query = DSL.select(
                userIdField,
                userNameField,
                userEmailField
        )
        .from(DSL.table("user").as("u"))
        .innerJoin(DSL.table("order").as("o"))
        .on(DSL.field("u.id", Long.class).eq(DSL.field("o.user_id", Long.class)))
        .where(DSL.field("o.product_id", Long.class).eq(productId));
        
        return executor.executeQuery(query)
                .map(rows -> {
                    List<JsonObject> result = new ArrayList<>();
                    for (var row : rows) {
                        result.add(new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("username", row.getString("username"))
                                .put("email", row.getString("email")));
                    }
                    return result;
                });
    }

    /**
     * 获取用户购买统计信息 - 演示复杂Join查询
     */
    public Future<JsonObject> getUserPurchaseStats(Long userId) {
        LOGGER.debug("Getting user purchase stats for user ID: {}", userId);
        
        // 使用原生SQL进行复杂查询
        Field<Long> userIdField = DSL.field("u.id", Long.class);
        Field<String> userNameField = DSL.field("u.name", String.class);
        Field<String> userEmailField = DSL.field("u.email", String.class);
        Field<Integer> orderCountField = DSL.count(DSL.field("o.id")).as("order_count");
        Field<Integer> productCountField = DSL.countDistinct(DSL.field("o.product_id")).as("product_count");
        Field<BigDecimal> totalAmountField = DSL.sum(DSL.field("o.total_amount", BigDecimal.class)).as("total_amount");
        Field<BigDecimal> avgAmountField = DSL.avg(DSL.field("o.total_amount", BigDecimal.class)).as("avg_amount");
        
        Query query = DSL.select(
                userIdField,
                userNameField,
                userEmailField,
                orderCountField,
                productCountField,
                totalAmountField,
                avgAmountField
        )
        .from(DSL.table("user").as("u"))
        .leftJoin(DSL.table("order").as("o"))
        .on(DSL.field("u.id", Long.class).eq(DSL.field("o.user_id", Long.class)))
        .where(userIdField.eq(userId))
        .groupBy(userIdField, userNameField, userEmailField);
        
        return executor.executeQuery(query)
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject()
                                .put("userId", userId)
                                .put("userName", "")
                                .put("userEmail", "")
                                .put("orderCount", 0)
                                .put("productCount", 0)
                                .put("totalAmount", 0)
                                .put("avgAmount", 0);
                    }
                    
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("userId", row.getLong("id"))
                            .put("userName", row.getString("name"))
                            .put("userEmail", row.getString("email"))
                            .put("orderCount", row.getLong("order_count"))
                            .put("productCount", row.getLong("product_count"))
                            .put("totalAmount", row.getBigDecimal("total_amount"))
                            .put("avgAmount", row.getBigDecimal("avg_amount"));
                });
    }

    /**
     * 获取用户消费排行榜 - 演示Join和排序
     */
    public Future<List<JsonObject>> getTopSpendingUsers(Integer limit) {
        LOGGER.debug("Getting top spending users with limit: {}", limit);
        
        Field<Long> userIdField = DSL.field("u.id", Long.class);
        Field<String> userNameField = DSL.field("u.name", String.class);
        Field<String> userEmailField = DSL.field("u.email", String.class);
        Field<Integer> orderCountField = DSL.count(DSL.field("o.id")).as("order_count");
        Field<BigDecimal> totalAmountField = DSL.sum(DSL.field("o.total_amount", BigDecimal.class)).as("total_amount");
        Field<BigDecimal> avgAmountField = DSL.avg(DSL.field("o.total_amount", BigDecimal.class)).as("avg_amount");
        
        Query query = DSL.select(
                userIdField,
                userNameField,
                userEmailField,
                orderCountField,
                totalAmountField,
                avgAmountField
        )
        .from(DSL.table("user").as("u"))
        .innerJoin(DSL.table("order").as("o"))
        .on(DSL.field("u.id", Long.class).eq(DSL.field("o.user_id", Long.class)))
        .groupBy(userIdField, userNameField, userEmailField)
        .orderBy(totalAmountField.desc(), orderCountField.desc())
        .limit(limit != null ? limit : 10);
        
        return executor.executeQuery(query)
                .map(rows -> {
                    List<JsonObject> result = new ArrayList<>();
                    for (var row : rows) {
                        result.add(new JsonObject()
                                .put("userId", row.getLong("id"))
                                .put("userName", row.getString("name"))
                                .put("userEmail", row.getString("email"))
                                .put("orderCount", row.getLong("order_count"))
                                .put("totalAmount", row.getBigDecimal("total_amount"))
                                .put("avgAmount", row.getBigDecimal("avg_amount")));
                    }
                    return result;
                });
    }

}