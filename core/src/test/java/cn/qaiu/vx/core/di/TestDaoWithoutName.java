package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Dao;

/**
 * 测试用的DAO实现（没有指定name）
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Dao  // 没有指定name，应该使用类名首字母小写
public class TestDaoWithoutName {
    
    public String findById(String id) {
        return "test-data-without-name-" + id;
    }
}
