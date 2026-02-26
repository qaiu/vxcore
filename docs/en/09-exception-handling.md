# Exception Handling

[中文](../09-exception-handling.md) | English

VXCore supports global and per-handler exception handling for consistent, API-friendly error responses.

## Exception types

- **BaseException** — abstract base (code + message)
- **BusinessException** — business errors (e.g. 400)
- **ValidationException** — validation errors (400)
- **SystemException** — internal errors (500)

## @ExceptionHandler

Handle exceptions in the same controller:

```java
@RouteHandler("/api")
public class UserController {

    @RouteMapping("/users/{id}")
    public Future<JsonResult> getUser(@PathVariable("id") Long id) {
        return userService.findById(id).map(JsonResult::success);
    }

    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public JsonResult handleNotFound(NotFoundException e) {
        return JsonResult.fail(404, e.getMessage());
    }
}
```

## Global handler

Register global handlers so uncaught exceptions return a unified JSON error (e.g. code, message, optional stack in dev).

## Related

- [Routing](08-routing-annotations.md)
- [Configuration](10-configuration.md)
- [Overview](01-overview.md)
