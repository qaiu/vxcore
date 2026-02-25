package cn.qaiu.demo.database.controller;

import cn.qaiu.demo.database.entity.User;
import cn.qaiu.demo.database.service.UserService;
import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.annotations.param.RequestBody;
import cn.qaiu.vx.core.annotations.param.RequestParam;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RouteHandler("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Inject
    UserService userService;

    @RouteMapping(value = "/create", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> createUser(@RequestBody JsonObject body) {
        User user = new User(body);
        return userService.createUser(user)
            .map(opt -> {
                log.info("Created user: {}", opt.orElse(null));
                return JsonResult.data(opt.map(User::toJson).orElse(new JsonObject()));
            });
    }

    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getUser(Long id) {
        return userService.findById(id)
            .map(opt -> opt.isPresent()
                ? JsonResult.data(opt.get().toJson())
                : JsonResult.error("User not found", 404));
    }

    @RouteMapping(value = "/list", method = RouteMethod.GET)
    public Future<JsonResult<JsonArray>> listUsers() {
        return userService.findAll()
            .map(users -> JsonResult.data(toJsonArray(users)));
    }

    @RouteMapping(value = "/search", method = RouteMethod.GET)
    public Future<JsonResult<JsonArray>> searchUsers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status) {
        Future<List<User>> future;
        if (name != null && !name.isEmpty()) {
            future = userService.searchByName(name);
        } else if (status != null && !status.isEmpty()) {
            future = userService.findByStatus(status);
        } else {
            future = userService.findAll();
        }
        return future.map(users -> JsonResult.data(toJsonArray(users)));
    }

    @RouteMapping(value = "/update/{id}", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> updateUser(Long id, @RequestBody JsonObject body) {
        body.put("id", id);
        User user = new User(body);
        user.setId(id);
        return userService.updateUser(user)
            .map(opt -> JsonResult.data(opt.map(User::toJson).orElse(new JsonObject())));
    }

    @RouteMapping(value = "/delete/{id}", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> deleteUser(Long id) {
        return userService.deleteUser(id)
            .map(deleted -> JsonResult.data(new JsonObject().put("deleted", deleted)));
    }

    private static JsonArray toJsonArray(List<User> users) {
        JsonArray arr = new JsonArray();
        users.forEach(u -> arr.add(u.toJson()));
        return arr;
    }
}
