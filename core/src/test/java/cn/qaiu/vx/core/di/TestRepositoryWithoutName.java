package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Repository;

/**
 * 测试用的仓储实现（没有指定name）
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Repository  // 没有指定name，应该使用类名首字母小写
public class TestRepositoryWithoutName {
    
    public String save(String data) {
        return "saved-without-name-" + data;
    }
}
