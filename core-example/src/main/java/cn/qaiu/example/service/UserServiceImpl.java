package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.model.UserRegistrationRequest;
import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

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
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Password validation: at least 8 characters, 1 letter and 1 number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"
    );
    
    private final AtomicLong idGenerator = new AtomicLong(1);
    
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
    
    // =================== 实现UserService接口的其他方法 ===================
    
    @Override
    public Future<List<User>> findAllUsers() {
        LOGGER.info("查找所有用户");
        return list();
    }
    
    @Override
    public Future<User> findUserById(Long id) {
        LOGGER.info("根据ID查找用户: {}", id);
        return getById(id)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("用户不存在: " + id);
                    }
                });
    }
    
    @Override
    public Future<List<User>> findUsersByName(String name) {
        LOGGER.info("根据用户名查找用户: {}", name);
        return lambdaList(lambdaQuery()
                .eq(User::getUsername, name)
                .orderByDesc(User::getCreateTime));
    }
    
    @Override
    public Future<User> createUser(User user) {
        LOGGER.info("创建用户: {}", user);
        
        // 业务逻辑验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
        }
        
        // 设置ID
        user.setId(idGenerator.getAndIncrement());
        
        return save(user)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("创建用户失败");
                    }
                });
    }
    
    @Override
    public Future<User> updateUser(User user) {
        LOGGER.info("更新用户: {}", user);
        
        if (user.getId() == null) {
            return Future.failedFuture(new IllegalArgumentException("用户ID不能为空"));
        }
        
        return updateById(user)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("更新用户失败");
                    }
                });
    }
    
    @Override
    public Future<Boolean> deleteUser(Long id) {
        LOGGER.info("删除用户: {}", id);
        
        if (id == null) {
            return Future.failedFuture(new IllegalArgumentException("用户ID不能为空"));
        }
        
        return removeById(id);
    }
    
    @Override
    public Future<List<User>> batchCreateUsers(List<User> users) {
        LOGGER.info("批量创建用户: {}", users.size());
        
        if (users == null || users.isEmpty()) {
            return Future.succeededFuture(List.of());
        }
        
        // 业务逻辑验证
        for (User user : users) {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
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
        
        return saveBatch(users);
    }
    
    @Override
    public Future<User> registerUser(UserRegistrationRequest request) {
        LOGGER.info("用户注册: {}", request);
        
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
        return findUsersByName(request.getUsername())
            .compose(existingUsers -> {
                if (!existingUsers.isEmpty()) {
                    return Future.failedFuture(new IllegalArgumentException("用户名已被使用"));
                }
                
                // 7. 检查邮箱是否已存在
                return existsByEmail(request.getEmail())
                    .compose(emailExists -> {
                        if (emailExists) {
                            return Future.failedFuture(new IllegalArgumentException("邮箱已被使用"));
                        }
                        
                        // 8. 创建新用户
                        User newUser = new User();
                        newUser.setId(idGenerator.getAndIncrement());
                        newUser.setUsername(request.getUsername());
                        newUser.setEmail(request.getEmail());
                        // In production, you should hash the password before storing
                        newUser.setPassword(request.getPassword());
                        newUser.setAge(request.getAge());
                        newUser.setStatus(User.UserStatus.ACTIVE);
                        newUser.setEmailVerified(false);
                        
                        // 9. 保存用户
                        return createUser(newUser);
                    });
            });
    }
}