package cn.qaiu.example.dao;

import cn.qaiu.example.model.User;
import cn.qaiu.db.dsl.core.AbstractDao;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户数据访问对象
 * 演示三层架构中的DAO层
 * 使用内存存储模拟数据库操作
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class UserDao extends AbstractDao<User, Long> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    
    // 模拟数据库存储
    private static final ConcurrentHashMap<Long, User> userStorage = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserDao() {
        super();
        // 初始化一些测试数据
        initializeTestData();
    }
    
    /**
     * 初始化测试数据
     */
    private void initializeTestData() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("张三");
        user1.setEmail("zhangsan@example.com");
        user1.setAge(25);
        userStorage.put(1L, user1);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setName("李四");
        user2.setEmail("lisi@example.com");
        user2.setAge(30);
        userStorage.put(2L, user2);
        
        User user3 = new User();
        user3.setId(3L);
        user3.setName("王五");
        user3.setEmail("wangwu@example.com");
        user3.setAge(28);
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
    public Future<User> findById(Long id) {
        LOGGER.info("Finding user by id: {}", id);
        User user = userStorage.get(id);
        return Future.succeededFuture(user);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<List<User>> findByName(String name) {
        LOGGER.info("Finding users by name: {}", name);
        List<User> users = userStorage.values().stream()
            .filter(user -> user.getName() != null && user.getName().contains(name))
            .toList();
        return Future.succeededFuture(users);
    }
    
    /**
     * 保存用户
     */
    public Future<User> save(User user) {
        LOGGER.info("Saving user: {}", user);
        
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        
        userStorage.put(user.getId(), user);
        LOGGER.info("Saved user with id: {}", user.getId());
        return Future.succeededFuture(user);
    }
    
    /**
     * 更新用户
     */
    public Future<User> update(User user) {
        LOGGER.info("Updating user: {}", user);
        
        if (!userStorage.containsKey(user.getId())) {
            return Future.failedFuture(new RuntimeException("User not found with id: " + user.getId()));
        }
        
        userStorage.put(user.getId(), user);
        LOGGER.info("Updated user with id: {}", user.getId());
        return Future.succeededFuture(user);
    }
    
    /**
     * 根据ID删除用户
     */
    public Future<Boolean> deleteById(Long id) {
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
}