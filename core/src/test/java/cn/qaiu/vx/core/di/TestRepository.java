package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Repository;

/**
 * 测试用的仓储实现
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Repository(name = "testRepository", datasource = "testDB", transactional = true)
public class TestRepository {
    
    public String save(String data) {
        return "saved-" + data;
    }
}
