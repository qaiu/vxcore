package cn.qaiu.demo.quickstart.controller;

import cn.qaiu.demo.quickstart.entity.User;
import cn.qaiu.demo.quickstart.service.UserService;
import cn.qaiu.demo.quickstart.service.UserServiceImpl;
import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 用户控制器
 * 
 * 验证 02-quick-start.md 第五步中的控制器示例
 * 
 * 文档不一致问题:
 * - ISSUE-001: 文档使用 HttpMethod.GET, 实际为 RouteMethod.GET
 * - ISSUE-002: 文档使用 @RequestParam("page"), 实际框架支持自动参数绑定
 * - ISSUE-003: 文档 Controller 构造函数手动 new Service, 没有使用 DI
 */
@RouteHandler("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 按文档示例: 构造函数手动创建 Service
    private final UserService userService;

    public UserController() {
        this.userService = new UserServiceImpl();
    }

    /**
     * 获取所有用户
     * 文档: GET /api/users
     */
    @RouteMapping(value = "", method = RouteMethod.GET)
    public Future<JsonResult<List<User>>> getAllUsers() {
        log.info("GET /api/users");
        return userService.findAllUsers()
                .map(users -> JsonResult.data("查询成功", users));
    }

    /**
     * 根据ID获取用户
     * 文档: GET /api/users/{id}
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<JsonResult<User>> getUserById(Long id) {
        log.info("GET /api/users/{}", id);
        return userService.findUserById(id)
                .map(opt -> {
                    if (opt.isPresent()) {
                        return JsonResult.data("查询成功", opt.get());
                    } else {
                        return JsonResult.<User>error("用户不存在", 404);
                    }
                });
    }

    /**
     * 创建用户
     * 文档: POST /api/users
     */
    @RouteMapping(value = "", method = RouteMethod.POST)
    public Future<JsonResult<User>> createUser(User user) {
        log.info("POST /api/users, body={}", user);
        return userService.createUser(user)
                .map(created -> JsonResult.data("创建成功", created));
    }

    /**
     * 更新用户
     * 文档: PUT /api/users/{id}
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
    public Future<JsonResult<User>> updateUser(Long id, User user) {
        log.info("PUT /api/users/{}", id);
        user.setId(id);
        return userService.updateUser(user)
                .map(updated -> JsonResult.data("更新成功", updated));
    }

    /**
     * 删除用户
     * 文档: DELETE /api/users/{id}
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
    public Future<JsonResult<String>> deleteUser(Long id) {
        log.info("DELETE /api/users/{}", id);
        return userService.deleteUser(id)
                .map(ok -> ok ? JsonResult.data("删除成功")
                              : JsonResult.<String>error("用户不存在", 404));
    }

    /**
     * 搜索用户
     * 文档: GET /api/users/search?keyword=xxx
     */
    @RouteMapping(value = "/search", method = RouteMethod.GET)
    public Future<JsonResult<List<User>>> searchUsers(String keyword) {
        log.info("GET /api/users/search?keyword={}", keyword);
        return userService.searchUsers(keyword)
                .map(users -> JsonResult.data("搜索成功", users));
    }
}
