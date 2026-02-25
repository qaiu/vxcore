package cn.qaiu.example.controller;

import cn.qaiu.example.entity.User;
import cn.qaiu.example.model.UserRegistrationRequest;
import cn.qaiu.example.service.UserService;
import cn.qaiu.example.service.UserServiceImpl;
import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户控制器 演示三层架构中的Controller层
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@RouteHandler("/api/users")
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController() {
    this.userService = new UserServiceImpl();
  }

  /** 获取所有用户 */
  @RouteMapping(value = "", method = RouteMethod.GET)
  public Future<JsonResult<List<User>>> getAllUsers() {
    LOGGER.info("Getting all users");
    return userService
        .findAllUsers()
        .map(users -> JsonResult.data(users))
        .recover(
            error -> {
              LOGGER.error("Failed to get all users", error);
              return Future.succeededFuture(JsonResult.error("获取用户列表失败: " + error.getMessage()));
            });
  }

  /** 根据ID获取用户 */
  @RouteMapping(value = "/{id}", method = RouteMethod.GET)
  public Future<JsonResult<User>> getUserById(Long id) {
    LOGGER.info("Getting user by id: {}", id);
    return userService
        .findUserById(id)
        .map(
            user -> {
              if (user != null) {
                return JsonResult.data(user);
              } else {
                return JsonResult.<User>error("用户不存在", 404);
              }
            })
        .recover(
            error -> {
              LOGGER.error("Failed to get user by id: {}", id, error);
              return Future.succeededFuture(
                  JsonResult.<User>error("获取用户失败: " + error.getMessage()));
            });
  }

  /** 创建用户 */
  @RouteMapping(value = "", method = RouteMethod.POST)
  public Future<JsonResult<User>> createUser(User user) {
    LOGGER.info("Creating user: {}", user);
    return userService
        .createUser(user)
        .map(createdUser -> JsonResult.data("用户创建成功", createdUser))
        .recover(
            error -> {
              LOGGER.error("Failed to create user: {}", user, error);
              return Future.succeededFuture(JsonResult.error("创建用户失败: " + error.getMessage()));
            });
  }

  /** 更新用户 */
  @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
  public Future<JsonResult<User>> updateUser(Long id, User user) {
    LOGGER.info("Updating user id: {}, data: {}", id, user);
    user.setId(id);
    return userService
        .updateUser(user)
        .map(updatedUser -> JsonResult.data("用户更新成功", updatedUser))
        .recover(
            error -> {
              LOGGER.error("Failed to update user id: {}", id, error);
              return Future.succeededFuture(
                  JsonResult.<User>error("更新用户失败: " + error.getMessage()));
            });
  }

  /** 删除用户 */
  @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
  public Future<JsonResult<?>> deleteUser(Long id) {
    LOGGER.info("Deleting user id: {}", id);
    return userService
        .deleteUser(id)
        .map(
            deleted -> {
              if (deleted) {
                return (JsonResult<?>) JsonResult.success("用户删除成功");
              } else {
                return (JsonResult<?>) JsonResult.error("用户不存在", 404);
              }
            })
        .recover(
            error -> {
              LOGGER.error("Failed to delete user id: {}", id, error);
              return Future.succeededFuture(JsonResult.error("删除用户失败: " + error.getMessage()));
            });
  }

  /** 根据用户名搜索用户 */
  @RouteMapping(value = "/search", method = RouteMethod.GET)
  public Future<JsonResult<List<User>>> searchUsers(String name) {
    LOGGER.info("Searching users by name: {}", name);
    return userService
        .findUsersByName(name)
        .map(users -> JsonResult.data(users))
        .recover(
            error -> {
              LOGGER.error("Failed to search users by name: {}", name, error);
              return Future.succeededFuture(JsonResult.error("搜索用户失败: " + error.getMessage()));
            });
  }

  /** 批量创建用户 */
  @RouteMapping(value = "/batch", method = RouteMethod.POST)
  public Future<JsonResult<List<User>>> batchCreateUsers(List<User> users) {
    LOGGER.info("Batch creating {} users", users.size());
    return userService
        .batchCreateUsers(users)
        .map(createdUsers -> JsonResult.data("批量创建用户成功", createdUsers))
        .recover(
            error -> {
              LOGGER.error("Failed to batch create users", error);
              return Future.succeededFuture(JsonResult.error("批量创建用户失败: " + error.getMessage()));
            });
  }

  /** 用户注册端点 提供完整的注册验证功能 */
  @RouteMapping(value = "/register", method = RouteMethod.POST)
  public Future<JsonResult<User>> registerUser(UserRegistrationRequest request) {
    LOGGER.info("User registration request: {}", request);
    return userService
        .registerUser(request)
        .map(registeredUser -> JsonResult.data("注册成功", registeredUser))
        .recover(
            error -> {
              LOGGER.error("Failed to register user: {}", request, error);
              return Future.succeededFuture(JsonResult.error("注册失败: " + error.getMessage()));
            });
  }

  // ========== 参数绑定测试用例 ==========

  /**
   * 测试1: PathVariable注解不填写字段名时自动根据参数名称映射
   * 路径: GET /api/users/test1/:userId
   * 示例: GET /api/users/test1/123
   */
  @RouteMapping(value = "test1/:userId", method = RouteMethod.GET, order = 100)
  public Future<JsonResult<String>> testPathVariableWithoutValue(Long userId) {
    LOGGER.info("Test1 - PathVariable without value annotation, userId: {}", userId);
    return Future.succeededFuture(JsonResult.data("PathVariable无注解值测试成功，userId: " + userId));
  }

  /**
   * 测试2: 使用 :语法 的路径变量自动绑定（无注解）
   * 路径: GET /api/users/test2/:id
   * 示例: GET /api/users/test2/456
   */
  @RouteMapping(value = "test2/:id", method = RouteMethod.GET, order = 100)
  public Future<JsonResult<String>> testColonSyntaxAutoBinding(Long id) {
    LOGGER.info("Test2 - Colon syntax auto binding without annotation, id: {}", id);
    return Future.succeededFuture(JsonResult.data("冒号语法自动绑定成功，id: " + id));
  }

  /**
   * 测试3: 使用 :语法 的多个路径变量自动绑定（无注解）
   * 路径: GET /api/users/test3/:id/:name
   * 示例: GET /api/users/test3/789/张三
   */
  @RouteMapping(value = "test3/:id/:name", method = RouteMethod.GET, order = 100)
  public Future<JsonResult<String>> testMultipleColonSyntax(Long id, String name) {
    LOGGER.info("Test3 - Multiple colon syntax auto binding, id: {}, name: {}", id, name);
    return Future.succeededFuture(JsonResult.data("多参数自动绑定成功，id: " + id + ", name: " + name));
  }

  /**
   * 测试4: 混合使用 {variable} 和 :variable 语法
   * 路径: GET /api/users/test4/{userId}/detail/:type
   * 示例: GET /api/users/test4/111/detail/full
   */
  @RouteMapping(value = "test4/{userId}/detail/:type", method = RouteMethod.GET, order = 100)
  public Future<JsonResult<String>> testMixedSyntax(Long userId, String type) {
    LOGGER.info("Test4 - Mixed syntax, userId: {}, type: {}", userId, type);
    return Future.succeededFuture(JsonResult.data("混合语法测试成功，userId: " + userId + ", type: " + type));
  }

  // ========== 路由优先级自动排序测试用例 ==========

  /**
   * 测试5: 路由优先级测试 - 具体路径（最高优先级）
   * 路径: GET /api/users/priority/fixed/path
   * 示例: GET /api/users/priority/fixed/path
   * 期望: 匹配此方法而不是下面的带参数路径
   */
  @RouteMapping(value = "priority/fixed/path", method = RouteMethod.GET)
  public Future<JsonResult<String>> testPriorityFixedPath() {
    LOGGER.info("Test5 - Matched fixed path (highest priority)");
    return Future.succeededFuture(JsonResult.data("匹配到固定路径：priority/fixed/path"));
  }

  /**
   * 测试6: 路由优先级测试 - 部分参数路径（中等优先级）
   * 路径: GET /api/users/priority/fixed/:param
   * 示例: GET /api/users/priority/fixed/abc
   * 期望: 当路径不匹配test5时匹配此方法
   */
  @RouteMapping(value = "priority/fixed/:param", method = RouteMethod.GET)
  public Future<JsonResult<String>> testPriorityPartialParam(String param) {
    LOGGER.info("Test6 - Matched partial param path, param: {}", param);
    return Future.succeededFuture(JsonResult.data("匹配到部分参数路径：priority/fixed/" + param));
  }

  /**
   * 测试7: 路由优先级测试 - 全参数路径（最低优先级）
   * 路径: GET /api/users/priority/:id/:name
   * 示例: GET /api/users/priority/123/test
   * 期望: 当路径不匹配test5和test6时匹配此方法
   */
  @RouteMapping(value = "priority/:id/:name", method = RouteMethod.GET)
  public Future<JsonResult<String>> testPriorityFullParam(Long id, String name) {
    LOGGER.info("Test7 - Matched full param path, id: {}, name: {}", id, name);
    return Future.succeededFuture(JsonResult.data("匹配到全参数路径：priority/" + id + "/" + name));
  }
}
