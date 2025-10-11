package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Component;

/**
 * 测试用的组件实现
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Component(name = "testComponent", priority = 1, lazy = false)
public class TestComponent {
    
    public String process(String input) {
        return "processed-" + input;
    }
}
