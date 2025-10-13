package cn.qaiu.example.service;

import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.model.User;
import cn.qaiu.example.model.UserRegistrationRequest;
import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 用户服务
 * 演示三层架构中的Service层
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Service
public class UserService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Password validation: at least 8 characters, 1 letter and 1 number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"
    );
    
    private final UserDao userDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserService() {
        this.userDao = new UserDao();
    }
    
    /**
     * 查找所有用户
     */
    public Future<List<User>> findAllUsers() {
        LOGGER.info("Finding all users");
        return userDao.findAll()
            .onSuccess(users -> LOGGER.info("Found {} users", users.size()))
            .onFailure(error -> LOGGER.error("Failed to find all users", error));
    }
    
    /**
     * 根据ID查找用户
     */
    public Future<User> findUserById(Long id) {
        LOGGER.info("Finding user by id: {}", id);
        return userDao.findById(id)
            .onSuccess(user -> {
                if (user != null) {
                    LOGGER.info("Found user: {}", user);
                } else {
                    LOGGER.info("User not found with id: {}", id);
                }
            })
            .onFailure(error -> LOGGER.error("Failed to find user by id: {}", id, error));
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<List<User>> findUsersByName(String name) {
        LOGGER.info("Finding users by name: {}", name);
        return userDao.findByName(name)
            .onSuccess(users -> LOGGER.info("Found {} users with name: {}", users.size(), name))
            .onFailure(error -> LOGGER.error("Failed to find users by name: {}", name, error));
    }
    
    /**
     * 创建用户
     */
    public Future<User> createUser(User user) {
        LOGGER.info("Creating user: {}", user);
        
        // 业务逻辑验证
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
        }
        
        // 设置ID
        user.setId(idGenerator.getAndIncrement());
        
        return userDao.save(user)
            .onSuccess(savedUser -> LOGGER.info("Created user: {}", savedUser))
            .onFailure(error -> LOGGER.error("Failed to create user: {}", user, error));
    }
    
    /**
     * 更新用户
     */
    public Future<User> updateUser(User user) {
        LOGGER.info("Updating user: {}", user);
        
        if (user.getId() == null) {
            return Future.failedFuture(new IllegalArgumentException("用户ID不能为空"));
        }
        
        return userDao.update(user)
            .onSuccess(updatedUser -> LOGGER.info("Updated user: {}", updatedUser))
            .onFailure(error -> LOGGER.error("Failed to update user: {}", user, error));
    }
    
    /**
     * 删除用户
     */
    public Future<Boolean> deleteUser(Long id) {
        LOGGER.info("Deleting user with id: {}", id);
        
        if (id == null) {
            return Future.failedFuture(new IllegalArgumentException("用户ID不能为空"));
        }
        
        return userDao.deleteById(id)
            .onSuccess(deleted -> {
                if (deleted) {
                    LOGGER.info("Deleted user with id: {}", id);
                } else {
                    LOGGER.info("User not found with id: {}", id);
                }
            })
            .onFailure(error -> LOGGER.error("Failed to delete user with id: {}", id, error));
    }
    
    /**
     * 批量创建用户
     */
    public Future<List<User>> batchCreateUsers(List<User> users) {
        LOGGER.info("Batch creating {} users", users.size());
        
        if (users == null || users.isEmpty()) {
            return Future.succeededFuture(List.of());
        }
        
        // 业务逻辑验证
        for (User user : users) {
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
            }
        }
        
        // 设置ID
        for (User user : users) {
            user.setId(idGenerator.getAndIncrement());
        }
        
        return userDao.batchSave(users)
            .onSuccess(savedUsers -> LOGGER.info("Batch created {} users", savedUsers.size()))
            .onFailure(error -> LOGGER.error("Failed to batch create users", error));
    }
    
    /**
     * 获取用户统计信息
     */
    public Future<JsonObject> getUserStatistics() {
        LOGGER.info("Getting user statistics");
        
        return userDao.count()
            .compose(count -> {
                JsonObject stats = new JsonObject()
                    .put("totalUsers", count)
                    .put("timestamp", System.currentTimeMillis());
                
                LOGGER.info("User statistics: {}", stats.encodePrettily());
                return Future.succeededFuture(stats);
            })
            .onFailure(error -> LOGGER.error("Failed to get user statistics", error));
    }
    
    /**
     * 用户注册
     * 包含完整的验证逻辑：邮箱格式、密码强度、用户名/邮箱唯一性等
     * 
     * @param request 注册请求
     * @return 注册成功的用户
     */
    public Future<User> registerUser(UserRegistrationRequest request) {
        LOGGER.info("Registering user: {}", request);
        
        // 1. 验证必填字段
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("密码不能为空"));
        }
        
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("确认密码不能为空"));
        }
        
        // 2. 验证用户名长度
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            return Future.failedFuture(new IllegalArgumentException("用户名长度必须在3-50个字符之间"));
        }
        
        // 3. 验证邮箱格式
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱格式不正确"));
        }
        
        // 4. 验证密码强度
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return Future.failedFuture(new IllegalArgumentException(
                "密码必须至少8个字符,包含至少1个字母和1个数字"
            ));
        }
        
        // 5. 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Future.failedFuture(new IllegalArgumentException("两次输入的密码不一致"));
        }
        
        // 6. 检查用户名是否已存在
        return userDao.findByName(request.getUsername())
            .compose(existingUsers -> {
                if (!existingUsers.isEmpty()) {
                    return Future.failedFuture(new IllegalArgumentException("用户名已被使用"));
                }
                
                // 7. 检查邮箱是否已存在
                // Note: This is a simplified check. In production, you'd have a findByEmail method
                return userDao.findAll()
                    .compose(allUsers -> {
                        boolean emailExists = allUsers.stream()
                            .anyMatch(u -> request.getEmail().equalsIgnoreCase(u.getEmail()));
                        
                        if (emailExists) {
                            return Future.failedFuture(new IllegalArgumentException("邮箱已被使用"));
                        }
                        
                        // 8. 创建新用户
                        User newUser = new User();
                        newUser.setId(idGenerator.getAndIncrement());
                        newUser.setName(request.getUsername());
                        newUser.setEmail(request.getEmail());
                        // In production, you should hash the password before storing
                        newUser.setPassword(request.getPassword());
                        newUser.setAge(request.getAge());
                        
                        // 9. 保存用户
                        return userDao.save(newUser)
                            .onSuccess(savedUser -> LOGGER.info("User registered successfully: {}", savedUser))
                            .onFailure(error -> LOGGER.error("Failed to register user: {}", request, error));
                    });
            });
    }
}