package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Component;

/**
 * 测试用的组件实现（没有指定name）
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Component  // 没有指定name，应该使用类名首字母小写
public class TestComponentWithoutName {
    
    public String process(String input) {
        return "processed-without-name-" + input;
    }
}
