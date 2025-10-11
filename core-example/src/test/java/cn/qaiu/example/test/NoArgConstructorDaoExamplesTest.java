package cn.qaiu.example.test;

import cn.qaiu.example.dao.NoArgConstructorDaoExamples;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 无参构造函数DAO示例测试
 * 验证无参构造函数功能的完整演示
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class NoArgConstructorDaoExamplesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoArgConstructorDaoExamplesTest.class);

    /**
     * 测试无参构造函数DAO示例
     */
    @Test
    public void testNoArgConstructorDaoExamples() {
        LOGGER.info("开始测试无参构造函数DAO示例");
        
        try {
            // 测试最简单的DAO
            NoArgConstructorDaoExamples.SimpleUserDao simpleUserDao = 
                new NoArgConstructorDaoExamples.SimpleUserDao();
            LOGGER.info("SimpleUserDao创建成功，实体类型: {}", 
                simpleUserDao.getEntityClass().getSimpleName());
            
            // 测试多数据源DAO
            NoArgConstructorDaoExamples.MultiDataSourceUserDao multiDataSourceUserDao = 
                new NoArgConstructorDaoExamples.MultiDataSourceUserDao();
            LOGGER.info("MultiDataSourceUserDao创建成功，实体类型: {}", 
                multiDataSourceUserDao.getEntityClass().getSimpleName());
            
            // 测试订单DAO
            NoArgConstructorDaoExamples.OrderDao orderDao = 
                new NoArgConstructorDaoExamples.OrderDao();
            LOGGER.info("OrderDao创建成功，实体类型: {}", 
                orderDao.getEntityClass().getSimpleName());
            
            // 演示使用方式
            NoArgConstructorDaoExamples.demonstrateUsage();
            NoArgConstructorDaoExamples.compareUsage();
            
            LOGGER.info("无参构造函数DAO示例测试完成");
            
        } catch (Exception e) {
            LOGGER.error("无参构造函数DAO示例测试失败", e);
            throw e;
        }
    }
}
