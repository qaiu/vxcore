package cn.qaiu.example.controller;

import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.service.UserService;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 * 演示 VXCore 框架的三层架构 Controller 层，使用 JService
 * 
 * @author QAIU
 */
@RouteHandler("/api/users")
public class UserController implements BaseHttpApi {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController() {
        // 这里应该通过依赖注入获取 UserService 实例
        // 为了演示，暂时使用 null，实际使用时需要注入 JooqExecutor
        this.userService = null; // new UserServiceImpl(jooqExecutor);
    }
    
    /**
     * 获取用户列表
     */
    @RouteMapping(value = "/", method = RouteMethod.GET)
    public Future<List<User>> getUsers(
            @RequestParam(value = "status", required = false) String status) {
        
        LOGGER.info("Get users requested, status: {}", status);
        
        if (userService == null) {
            return Future.failedFuture("UserService not initialized");
        }
        
        if (status != null) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                return userService.findActiveUsers();
            } catch (IllegalArgumentException e) {
                return Future.failedFuture("Invalid status: " + status);
            }
        }
        
        return userService.list();
    }
    
    /**
     * 根据ID获取用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<User> getUserById(@PathVariable("id") Long id) {
        LOGGER.info("Get user by id: {}", id);
        
        if (userService == null) {
            return Future.failedFuture("UserService not initialized");
        }
        
        return userService.getById(id)
                .compose(optional -> {
                    if (optional.isPresent()) {
                        return Future.succeededFuture(optional.get());
                    } else {
                        return Future.failedFuture("User not found with id: " + id);
                    }
                });
    }
    
    /**
     * 根据邮箱获取用户
     */
    @RouteMapping(value = "/email/{email}", method = RouteMethod.GET)
    public Future<User> getUserByEmail(@PathVariable("email") String email) {
        LOGGER.info("Get user by email: {}", email);
        
        return userService.findByEmail(email)
                .map(user -> {
                    if (user == null) {
                        throw new RuntimeException("User not found");
                    }
                    return user;
                });
    }
    
    /**
     * 创建用户
     */
    @RouteMapping(value = "/", method = RouteMethod.POST)
    public Future<User> createUser(@RequestBody User user) {
        LOGGER.info("Create user: {}", user.getUsername());
        
        return userService.save(user)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("Failed to create user");
                    }
                });
    }
    
    /**
     * 更新用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
    public Future<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        LOGGER.info("Update user: {}", id);
        
        user.setId(id);
        return userService.updateById(user)
                .map(result -> {
                    if (!result.isPresent()) {
                        throw new RuntimeException("User not found");
                    }
                    return result.get();
                });
    }
    
    /**
     * 删除用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
    public Future<Boolean> deleteUser(@PathVariable("id") Long id) {
        LOGGER.info("Delete user: {}", id);
        
        return userService.removeById(id);
    }
    
    /**
     * 更新用户余额
     */
    @RouteMapping(value = "/{id}/balance", method = RouteMethod.PUT)
    public Future<Boolean> updateUserBalance(
            @PathVariable("id") Long id,
            @RequestParam("balance") BigDecimal balance) {
        
        LOGGER.info("Update user balance: {} -> {}", id, balance);
        
        return userService.updateUserBalance(id, balance);
    }
    
    /**
     * 验证用户邮箱
     */
    @RouteMapping(value = "/{id}/verify-email", method = RouteMethod.POST)
    public Future<Boolean> verifyUserEmail(@PathVariable("id") Long id) {
        LOGGER.info("Verify user email: {}", id);
        
        return userService.verifyUserEmail(id);
    }
    
    /**
     * 获取用户统计信息
     */
    @RouteMapping(value = "/statistics", method = RouteMethod.GET)
    public Future<JsonObject> getUserStatistics() {
        LOGGER.info("Get user statistics");
        
        return userService.getUserStatistics();
    }
    
    /**
     * 根据年龄范围获取用户
     */
    @RouteMapping(value = "/age-range", method = RouteMethod.GET)
    public Future<List<User>> getUsersByAgeRange(
            @RequestParam("minAge") Integer minAge,
            @RequestParam("maxAge") Integer maxAge) {
        
        LOGGER.info("Get users by age range: {} - {}", minAge, maxAge);
        
        return userService.getUsersByAgeRange(minAge, maxAge);
    }
}

