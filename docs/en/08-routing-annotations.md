# Routing Annotations

[中文](../08-routing-annotations.md) | English

VXCore provides Spring MVC–style annotation-based routing for Web and JSON API development.

## Core annotations

### @RouteHandler

Marks a controller class and sets the base path.

```java
@RouteHandler("/api")
public class UserController {
    // handler methods
}
```

- `value`: Base path (default `""`)
- `description`: Optional description

### @RouteMapping

Maps a method to a route (path + HTTP method).

```java
@RouteMapping(value = "/users", method = HttpMethod.GET)
public Future<JsonResult> getUsers() {
    return userService.findAll().map(JsonResult::success);
}
```

- `value`: Path (supports path variables, e.g. `/users/{id}`)
- `method`: GET, POST, PUT, DELETE, etc. (default GET)
- `description`, `produces`, `consumes`: Optional

## Parameter annotations

### @RequestParam

Binds query or form parameters.

```java
@RouteMapping("/users")
public Future<JsonResult> getUsers(
    @RequestParam("page") int page,
    @RequestParam(value = "size", defaultValue = "10") int size,
    @RequestParam(value = "keyword", required = false) String keyword
) { ... }
```

- `value`: Parameter name
- `required`: Default true
- `defaultValue`: Default if absent

### @PathVariable

Binds path variables.

```java
@RouteMapping("/users/{id}")
public Future<JsonResult> getUser(@PathVariable("id") Long id) {
    return userService.findById(id).map(JsonResult::success);
}
```

### @RequestBody

Binds request body (e.g. JSON) to a DTO.

```java
@RouteMapping(value = "/users", method = HttpMethod.POST)
public Future<JsonResult> createUser(@RequestBody User user) {
    return userService.create(user).map(JsonResult::success);
}
```

## Method overloading

VXCore supports overloaded handler methods; the best match is chosen by parameter names and types.

## Return types

- `Future<T>`: Async result; framework waits and serializes (e.g. to JSON)
- `JsonResult`: Wrapper for success/error and data

## Related

- [Exception handling](09-exception-handling.md)
- [Configuration](10-configuration.md)
- [WebSocket](WEBSOCKET_GUIDE.md)
- [Overview](01-overview.md)
