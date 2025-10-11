package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Dao;

/**
 * 测试用的DAO实现
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Dao(name = "testDao", cacheable = true)
public class TestDao {
    
    public String findById(String id) {
        return "test-data-" + id;
    }
}
