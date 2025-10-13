package cn.qaiu.example.controller;

import cn.qaiu.example.model.User;
import cn.qaiu.example.service.UserService;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 用户控制器
 * 演示三层架构中的Controller层
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@RouteHandler("/api/users")
public class UserController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController() {
        this.userService = new UserService();
    }
    
    /**
     * 获取所有用户
     */
    @RouteMapping(value = "", method = HttpMethod.GET)
    public Future<JsonResult<?>> getAllUsers() {
        LOGGER.info("Getting all users");
        return userService.findAllUsers()
            .map(users -> (JsonResult<?>) JsonResult.data(users))
            .recover(error -> {
                LOGGER.error("Failed to get all users", error);
                return Future.succeededFuture(JsonResult.error("获取用户列表失败: " + error.getMessage()));
            });
    }
    
    /**
     * 根据ID获取用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.GET)
    public Future<JsonResult<?>> getUserById(Long id) {
        LOGGER.info("Getting user by id: {}", id);
        return userService.findUserById(id)
            .map(user -> {
                if (user != null) {
                    return (JsonResult<?>) JsonResult.data(user);
                } else {
                    return (JsonResult<?>) JsonResult.error("用户不存在", 404);
                }
            })
            .recover(error -> {
                LOGGER.error("Failed to get user by id: {}", id, error);
                return Future.succeededFuture(JsonResult.error("获取用户失败: " + error.getMessage()));
            });
    }
    
    /**
     * 创建用户
     */
    @RouteMapping(value = "", method = HttpMethod.POST)
    public Future<JsonResult<?>> createUser(User user) {
        LOGGER.info("Creating user: {}", user);
        return userService.createUser(user)
            .map(createdUser -> (JsonResult<?>) JsonResult.data("用户创建成功", createdUser))
            .recover(error -> {
                LOGGER.error("Failed to create user: {}", user, error);
                return Future.succeededFuture(JsonResult.error("创建用户失败: " + error.getMessage()));
            });
    }
    
    /**
     * 更新用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.PUT)
    public Future<JsonResult<?>> updateUser(Long id, User user) {
        LOGGER.info("Updating user id: {}, data: {}", id, user);
        user.setId(id);
        return userService.updateUser(user)
            .map(updatedUser -> (JsonResult<?>) JsonResult.data("用户更新成功", updatedUser))
            .recover(error -> {
                LOGGER.error("Failed to update user id: {}", id, error);
                return Future.succeededFuture(JsonResult.error("更新用户失败: " + error.getMessage()));
            });
    }
    
    /**
     * 删除用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.DELETE)
    public Future<JsonResult<?>> deleteUser(Long id) {
        LOGGER.info("Deleting user id: {}", id);
        return userService.deleteUser(id)
            .map(deleted -> {
                if (deleted) {
                    return (JsonResult<?>) JsonResult.success("用户删除成功");
                } else {
                    return (JsonResult<?>) JsonResult.error("用户不存在", 404);
                }
            })
            .recover(error -> {
                LOGGER.error("Failed to delete user id: {}", id, error);
                return Future.succeededFuture(JsonResult.error("删除用户失败: " + error.getMessage()));
            });
    }
    
    /**
     * 根据用户名搜索用户
     */
    @RouteMapping(value = "/search", method = HttpMethod.GET)
    public Future<JsonResult<?>> searchUsers(String name) {
        LOGGER.info("Searching users by name: {}", name);
        return userService.findUsersByName(name)
            .map(users -> (JsonResult<?>) JsonResult.data(users))
            .recover(error -> {
                LOGGER.error("Failed to search users by name: {}", name, error);
                return Future.succeededFuture(JsonResult.error("搜索用户失败: " + error.getMessage()));
            });
    }
    
    /**
     * 批量创建用户
     */
    @RouteMapping(value = "/batch", method = HttpMethod.POST)
    public Future<JsonResult<?>> batchCreateUsers(List<User> users) {
        LOGGER.info("Batch creating {} users", users.size());
        return userService.batchCreateUsers(users)
            .map(createdUsers -> (JsonResult<?>) JsonResult.data("批量创建用户成功", createdUsers))
            .recover(error -> {
                LOGGER.error("Failed to batch create users", error);
                return Future.succeededFuture(JsonResult.error("批量创建用户失败: " + error.getMessage()));
            });
    }
}