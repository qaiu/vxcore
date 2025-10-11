package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Controller;

/**
 * 测试用的控制器实现
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Controller(name = "testController", basePath = "/api/test", cors = true, authenticated = false)
public class TestController {
    
    public String handleRequest(String request) {
        return "response-" + request;
    }
}
