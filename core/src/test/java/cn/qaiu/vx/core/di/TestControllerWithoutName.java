package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Controller;

/**
 * 测试用的控制器实现（没有指定name）
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Controller  // 没有指定name，应该使用类名首字母小写
public class TestControllerWithoutName {
    
    public String handleRequest(String request) {
        return "response-without-name-" + request;
    }
}
