package cn.qaiu.example.dao;

import cn.qaiu.example.entity.User;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户数据访问对象
 * 演示三层架构中的DAO层
 * 使用内存存储模拟数据库操作，继承LambdaDao获得lambda查询功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class UserDao extends LambdaDao<User, Long> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    
    // 模拟数据库存储 - 使用实例变量避免测试间数据污染
    private final ConcurrentHashMap<Long, User> userStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserDao() {
        super();
        // 初始化一些测试数据
        initializeTestData();
    }
    
    /**
     * 带JooqExecutor参数的构造函数（用于测试）
     * 
     * @param executor JooqExecutor实例
     */
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
        // 初始化一些测试数据
        initializeTestData();
    }
    
    /**
     * 重写getExecutor方法，避免父类尝试初始化数据库连接
     * UserDao使用内存存储，不需要数据库连接
     */
    @Override
    protected cn.qaiu.db.dsl.core.JooqExecutor getExecutor() {
        // UserDao使用内存存储，不需要数据库连接
        throw new UnsupportedOperationException("UserDao uses mock storage, no database connection needed");
    }
    
    /**
     * 初始化测试数据
     */
    private void initializeTestData() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("张三");
        user1.setEmail("zhangsan@example.com");
        user1.setAge(25);
        user1.setStatus(User.UserStatus.ACTIVE);
        userStorage.put(1L, user1);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("李四");
        user2.setEmail("lisi@example.com");
        user2.setAge(30);
        user2.setStatus(User.UserStatus.ACTIVE);
        userStorage.put(2L, user2);
        
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("王五");
        user3.setEmail("wangwu@example.com");
        user3.setAge(28);
        user3.setStatus(User.UserStatus.ACTIVE);
        userStorage.put(3L, user3);
        
        idGenerator.set(4L);
        LOGGER.info("Initialized test data with {} users", userStorage.size());
    }
    
    /**
     * 查找所有用户
     */
    public Future<List<User>> findAll() {
        LOGGER.info("Finding all users");
        return Future.succeededFuture(List.copyOf(userStorage.values()));
    }
    
    /**
     * 根据ID查找用户
     */
    @Override
    public Future<Optional<User>> findById(Long id) {
        LOGGER.info("Finding user by id: {}", id);
        User user = userStorage.get(id);
        return Future.succeededFuture(Optional.ofNullable(user));
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<List<User>> findByName(String name) {
        LOGGER.info("Finding users by name: {}", name);
        List<User> users = userStorage.values().stream()
            .filter(user -> user.getUsername() != null && user.getUsername().contains(name))
            .toList();
        return Future.succeededFuture(users);
    }
    
    /**
     * 保存用户
     */
    @Override
    public Future<Optional<User>> insert(User user) {
        LOGGER.info("Saving user: {}", user);
        
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        
        userStorage.put(user.getId(), user);
        LOGGER.info("Saved user with id: {}", user.getId());
        return Future.succeededFuture(Optional.of(user));
    }
    
    /**
     * 保存用户（兼容方法）
     */
    public Future<User> save(User user) {
        return insert(user)
                .map(optional -> optional.orElse(null));
    }
    
    /**
     * 更新用户
     */
    @Override
    public Future<Optional<User>> update(User user) {
        LOGGER.info("Updating user: {}", user);
        
        if (!userStorage.containsKey(user.getId())) {
            return Future.succeededFuture(Optional.empty());
        }
        
        userStorage.put(user.getId(), user);
        LOGGER.info("Updated user with id: {}", user.getId());
        return Future.succeededFuture(Optional.of(user));
    }
    
    /**
     * 根据ID删除用户
     */
    @Override
    public Future<Boolean> delete(Long id) {
        LOGGER.info("Deleting user with id: {}", id);
        
        User removed = userStorage.remove(id);
        boolean deleted = removed != null;
        
        if (deleted) {
            LOGGER.info("Deleted user with id: {}", id);
        } else {
            LOGGER.info("User not found with id: {}", id);
        }
        
        return Future.succeededFuture(deleted);
    }
    
    /**
     * 根据ID删除用户（兼容方法）
     */
    public Future<Boolean> deleteById(Long id) {
        return delete(id);
    }
    
    /**
     * 批量保存用户
     */
    public Future<List<User>> batchSave(List<User> users) {
        LOGGER.info("Batch saving {} users", users.size());
        
        for (User user : users) {
            if (user.getId() == null) {
                user.setId(idGenerator.getAndIncrement());
            }
            userStorage.put(user.getId(), user);
        }
        
        LOGGER.info("Batch saved {} users", users.size());
        return Future.succeededFuture(users);
    }
    
    /**
     * 统计用户数量
     */
    public Future<Long> count() {
        LOGGER.info("Counting users");
        long count = userStorage.size();
        LOGGER.info("Total users: {}", count);
        return Future.succeededFuture(count);
    }
    
    /**
     * 清空所有用户
     */
    public Future<Void> clear() {
        LOGGER.info("Clearing all users");
        userStorage.clear();
        idGenerator.set(1L);
        LOGGER.info("Cleared all users");
        return Future.succeededFuture();
    }
    
    /**
     * 获取存储的用户数量（用于测试）
     */
    public int getStorageSize() {
        return userStorage.size();
    }
    
    // =================== 实现基础方法 ===================
    
    @Override
    public Future<Boolean> exists(Long id) {
        LOGGER.info("Checking if user exists with id: {}", id);
        return Future.succeededFuture(userStorage.containsKey(id));
    }
    
    // =================== 测试需要的方法 ===================
    
    /**
     * 创建用户（测试用方法）
     * 
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 创建的用户
     */
    public Future<User> createUser(String username, String email, String password) {
        LOGGER.info("Creating user: username={}, email={}", username, email);
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setBalance(new java.math.BigDecimal("100.00")); // 设置默认余额
        user.setAge(25); // 设置默认年龄
        
        return insert(user)
            .map(optional -> optional.orElse(null));
    }
    
    /**
     * 更新用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    public Future<Boolean> updatePassword(Long userId, String newPassword) {
        LOGGER.info("Updating password for user: {}", userId);
        
        return findById(userId)
            .map(optional -> {
                if (optional.isPresent()) {
                    User user = optional.get();
                    user.setPassword(newPassword);
                    userStorage.put(userId, user);
                    LOGGER.info("Password updated for user: {}", userId);
                    return true;
                }
                LOGGER.warn("User not found for password update: {}", userId);
                return false;
            });
    }
    
    /**
     * 验证用户邮箱
     * 
     * @param userId 用户ID
     * @return 是否验证成功
     */
    public Future<Boolean> verifyUserEmail(Long userId) {
        LOGGER.info("Verifying email for user: {}", userId);
        
        return findById(userId)
            .map(optional -> {
                if (optional.isPresent()) {
                    User user = optional.get();
                    user.setEmailVerified(true);
                    userStorage.put(userId, user);
                    LOGGER.info("Email verified for user: {}", userId);
                    return true;
                }
                LOGGER.warn("User not found for email verification: {}", userId);
                return false;
            });
    }
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public Future<Boolean> updateUserStatus(Long userId, User.UserStatus status) {
        LOGGER.info("Updating status for user: {} to {}", userId, status);
        
        return findById(userId)
            .map(optional -> {
                if (optional.isPresent()) {
                    User user = optional.get();
                    user.setStatus(status);
                    userStorage.put(userId, user);
                    LOGGER.info("Status updated for user: {} to {}", userId, status);
                    return true;
                }
                LOGGER.warn("User not found for status update: {}", userId);
                return false;
            });
    }
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户Optional
     */
    public Future<Optional<User>> findOneByEmail(String email) {
        LOGGER.info("Finding user by email: {}", email);
        
        User foundUser = userStorage.values().stream()
            .filter(user -> email.equals(user.getEmail()))
            .findFirst()
            .orElse(null);
        
        if (foundUser != null) {
            LOGGER.info("Found user by email: {} -> {}", email, foundUser.getId());
        } else {
            LOGGER.info("No user found with email: {}", email);
        }
        
        return Future.succeededFuture(Optional.ofNullable(foundUser));
    }
    
    /**
     * 查找所有活跃用户
     * 
     * @return 活跃用户列表
     */
    public Future<List<User>> findActiveUsers() {
        LOGGER.info("Finding active users");
        
        List<User> activeUsers = userStorage.values().stream()
            .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
            .toList();
        
        LOGGER.info("Found {} active users", activeUsers.size());
        return Future.succeededFuture(activeUsers);
    }
    
    /**
     * 获取用户统计信息
     * 
     * @return 用户统计信息
     */
    public Future<Map<String, Object>> getUserStatistics() {
        LOGGER.info("Getting user statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        long totalUsers = userStorage.size();
        long activeUsers = userStorage.values().stream()
            .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
            .count();
        long inactiveUsers = userStorage.values().stream()
            .filter(user -> user.getStatus() == User.UserStatus.INACTIVE)
            .count();
        long suspendedUsers = userStorage.values().stream()
            .filter(user -> user.getStatus() == User.UserStatus.SUSPENDED)
            .count();
        long verifiedUsers = userStorage.values().stream()
            .filter(user -> Boolean.TRUE.equals(user.getEmailVerified()))
            .count();
        
        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("inactiveUsers", inactiveUsers);
        statistics.put("suspendedUsers", suspendedUsers);
        statistics.put("verifiedUsers", verifiedUsers);
        statistics.put("unverifiedUsers", totalUsers - verifiedUsers);
        
        LOGGER.info("User statistics: {}", statistics);
        return Future.succeededFuture(statistics);
    }
    
    /**
     * 根据年龄范围查找用户
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    public Future<List<User>> findByAgeRange(int minAge, int maxAge) {
        LOGGER.info("Finding users by age range: {} - {}", minAge, maxAge);
        
        List<User> users = userStorage.values().stream()
            .filter(user -> user.getAge() != null && user.getAge() >= minAge && user.getAge() <= maxAge)
            .toList();
        
        LOGGER.info("Found {} users in age range {} - {}", users.size(), minAge, maxAge);
        return Future.succeededFuture(users);
    }
    
    /**
     * 根据最小余额查找用户
     * 
     * @param minBalance 最小余额
     * @return 用户列表
     */
    public Future<List<User>> findByMinBalance(java.math.BigDecimal minBalance) {
        LOGGER.info("Finding users with minimum balance: {}", minBalance);
        
        List<User> users = userStorage.values().stream()
            .filter(user -> user.getBalance() != null && user.getBalance().compareTo(minBalance) >= 0)
            .toList();
        
        LOGGER.info("Found {} users with minimum balance {}", users.size(), minBalance);
        return Future.succeededFuture(users);
    }
    
    /**
     * 批量插入用户
     * 
     * @param users 用户列表
     * @return 插入的用户数量
     */
    public Future<Integer> insertBatch(List<User> users) {
        LOGGER.info("Batch inserting {} users", users.size());
        
        int insertedCount = 0;
        for (User user : users) {
            if (user.getId() == null) {
                user.setId(idGenerator.getAndIncrement());
            }
            userStorage.put(user.getId(), user);
            insertedCount++;
        }
        
        LOGGER.info("Batch inserted {} users", insertedCount);
        return Future.succeededFuture(insertedCount);
    }
    
    /**
     * 批量保存用户（兼容方法）
     * 
     * @param users 用户列表
     * @return 保存的用户列表
     */
    public Future<List<User>> saveAll(List<User> users) {
        LOGGER.info("Batch saving {} users", users.size());
        
        List<User> savedUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getId() == null) {
                user.setId(idGenerator.getAndIncrement());
            }
            userStorage.put(user.getId(), user);
            savedUsers.add(user);
        }
        
        LOGGER.info("Batch saved {} users", savedUsers.size());
        return Future.succeededFuture(savedUsers);
    }
    
    
    /**
     * 批量更新用户
     * 
     * @param users 用户列表
     * @return 更新的用户列表
     */
    public Future<List<User>> updateAll(List<User> users) {
        LOGGER.info("Batch updating {} users", users.size());
        
        List<User> updatedUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getId() != null && userStorage.containsKey(user.getId())) {
                userStorage.put(user.getId(), user);
                updatedUsers.add(user);
            }
        }
        
        LOGGER.info("Batch updated {} users", updatedUsers.size());
        return Future.succeededFuture(updatedUsers);
    }
    
    /**
     * 批量删除用户
     * 
     * @param userIds 用户ID列表
     * @return 删除的用户数量
     */
    public Future<Integer> deleteAllById(List<Long> userIds) {
        LOGGER.info("Batch deleting {} users", userIds.size());
        
        int deletedCount = 0;
        for (Long userId : userIds) {
            if (userStorage.remove(userId) != null) {
                deletedCount++;
            }
        }
        
        LOGGER.info("Batch deleted {} users", deletedCount);
        return Future.succeededFuture(deletedCount);
    }
}