package cn.qaiu.example.controller;

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
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户控制器
 * 演示 VXCore 框架的三层架构 Controller 层
 * 
 * @author QAIU
 */
@RouteHandler("/api/users")
public class UserController implements BaseHttpApi {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController() {
        this.userService = new UserService();
    }
    
    /**
     * 获取用户列表
     */
    @RouteMapping(value = "/", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUsers(
            @RequestParam(value = "status", required = false) String status) {
        
        LOGGER.info("Get users requested, status: {}", status);
        
        if (status != null) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                return userService.getActiveUsers()
                        .map(JsonResult::data);
            } catch (IllegalArgumentException e) {
                return Future.succeededFuture(JsonResult.error("Invalid status: " + status));
            }
        }
        
        return userService.getActiveUsers()
                .map(JsonResult::data);
    }
    
    /**
     * 根据ID获取用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUserById(@PathVariable("id") Long id) {
        LOGGER.info("Get user by id: {}", id);
        
        return userService.getUserById(id)
                .map(user -> {
                    if (user != null) {
                        return JsonResult.data(user);
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 根据邮箱获取用户
     */
    @RouteMapping(value = "/email/{email}", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUserByEmail(@PathVariable("email") String email) {
        LOGGER.info("Get user by email: {}", email);
        
        return userService.getUserByEmail(email)
                .map(user -> {
                    if (user != null) {
                        return JsonResult.data(user);
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 创建用户
     */
    @RouteMapping(value = "/", method = RouteMethod.POST)
    public Future<JsonResult<?>> createUser(@RequestBody User user) {
        LOGGER.info("Create user: {}", user.getUsername());
        
        return userService.createUser(user)
                .map(JsonResult::data);
    }
    
    /**
     * 更新用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
    public Future<JsonResult<?>> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        LOGGER.info("Update user: {}", id);
        
        user.setId(id);
        return userService.updateUser(user)
                .map(result -> {
                    if (result.isPresent()) {
                        return JsonResult.data("User updated successfully", result.get().toJson());
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 删除用户
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
    public Future<JsonResult<?>> deleteUser(@PathVariable("id") Long id) {
        LOGGER.info("Delete user: {}", id);
        
        return userService.deleteUser(id)
                .map(result -> {
                    if (result) {
                        return JsonResult.success("User deleted successfully");
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 更新用户余额
     */
    @RouteMapping(value = "/{id}/balance", method = RouteMethod.PUT)
    public Future<JsonResult<?>> updateUserBalance(
            @PathVariable("id") Long id,
            @RequestParam("balance") BigDecimal balance) {
        
        LOGGER.info("Update user balance: {} -> {}", id, balance);
        
        return userService.updateUserBalance(id, balance)
                .map(result -> {
                    if (result) {
                        return JsonResult.success("Balance updated successfully");
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 验证用户邮箱
     */
    @RouteMapping(value = "/{id}/verify-email", method = RouteMethod.POST)
    public Future<JsonResult<?>> verifyUserEmail(@PathVariable("id") Long id) {
        LOGGER.info("Verify user email: {}", id);
        
        return userService.verifyUserEmail(id)
                .map(result -> {
                    if (result) {
                        return JsonResult.success("Email verified successfully");
                    } else {
                        return JsonResult.error("User not found", 404);
                    }
                });
    }
    
    /**
     * 获取用户统计信息
     */
    @RouteMapping(value = "/statistics", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUserStatistics() {
        LOGGER.info("Get user statistics");
        
        return userService.getUserStatistics()
                .map(JsonResult::data);
    }
    
    /**
     * 根据年龄范围获取用户
     */
    @RouteMapping(value = "/age-range", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUsersByAgeRange(
            @RequestParam("minAge") Integer minAge,
            @RequestParam("maxAge") Integer maxAge) {
        
        LOGGER.info("Get users by age range: {} - {}", minAge, maxAge);
        
        return userService.getUsersByAgeRange(minAge, maxAge)
                .map(JsonResult::data);
    }
}

