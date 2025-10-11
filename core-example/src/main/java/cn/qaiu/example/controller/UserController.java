package cn.qaiu.example.controller;

import cn.qaiu.example.entity.User;
import cn.qaiu.example.service.UserService;
import cn.qaiu.example.service.UserServiceImpl;
import cn.qaiu.vx.core.annotaions.Controller;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.util.AsyncServiceUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户控制器
 * 演示 VXCore 框架的三层架构 Controller 层，使用 JService
 * 
 * @author QAIU
 */
@Controller
@Singleton
@RouteHandler("/api/users")
public class UserController implements BaseHttpApi {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    private UserService userService;
    
    /**
     * 无参构造函数 - VXCore框架要求
     */
    public UserController() {
        // VXCore框架会调用无参构造函数
        // UserService将在需要时通过其他方式获取
    }
    
    /**
     * 获取UserService实例
     * 直接创建UserServiceImpl实例
     */
    private UserService getUserService() {
        if (userService == null) {
            try {
                userService = new UserServiceImpl();
                LOGGER.info("UserService created directly");
            } catch (Exception e) {
                LOGGER.error("Failed to create UserService", e);
                return null;
            }
        }
        return userService;
    }
    
    /**
     * 获取用户列表
     */
    @RouteMapping(value = "/", method = RouteMethod.GET)
    public Future<List<User>> getUsers(
            @RequestParam(value = "status", required = false) String status) {
        
        LOGGER.info("Get users requested, status: {}", status);
        
        UserService service = getUserService();
        if (service == null) {
            return Future.failedFuture("UserService not available");
        }
        
        if (status != null) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                return service.findActiveUsers();
            } catch (IllegalArgumentException e) {
                return Future.failedFuture("Invalid status: " + status);
            }
        }
        
        return service.list();
    }
    
    /**
     * 根据ID获取用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<User> getUserById(@PathVariable("id") Long id) {
        LOGGER.info("Get user by id: {}", id);
        
        UserService service = getUserService();
        if (service == null) {
            return Future.failedFuture("UserService not available");
        }
        
        return service.getById(id)
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
     * 测试API - 验证Controller是否被正确注册
     */
    @RouteMapping(value = "/test", method = RouteMethod.GET, order = 100)
    public Future<String> test() {
        LOGGER.info("UserController test API called");
        return Future.succeededFuture("UserController is working!");
    }
    
    /**
     * 获取用户统计信息
     */
    @RouteMapping(value = "/statistics", method = RouteMethod.GET, order = 100)
    public Future<JsonObject> getUserStatistics() {
        LOGGER.info("Get user statistics");
        
        UserService service = getUserService();
        if (service == null) {
            return Future.failedFuture("UserService not available");
        }
        
        return service.getUserStatistics();
    }
    
    /**
     * 根据年龄范围获取用户
     */
    @RouteMapping(value = "/age-range", method = RouteMethod.GET, order = 100)
    public Future<List<User>> getUsersByAgeRange(
            @RequestParam("minAge") Integer minAge,
            @RequestParam("maxAge") Integer maxAge) {
        
        LOGGER.info("Get users by age range: {} - {}", minAge, maxAge);
        
        return userService.getUsersByAgeRange(minAge, maxAge);
    }
}

