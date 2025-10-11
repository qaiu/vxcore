package cn.qaiu.example.test;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.EnhancedDao;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 无参构造函数DAO测试
 * 验证DAO可以使用无参构造函数自动获取泛型类型
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class NoArgConstructorDaoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoArgConstructorDaoTest.class);

    /**
     * 测试AbstractDao无参构造函数
     * 验证可以通过泛型自动获取实体类类型
     */
    @Test
    public void testAbstractDaoNoArgConstructor() {
        LOGGER.info("开始测试AbstractDao无参构造函数");
        
        try {
            // 创建无参构造函数DAO实例
            SimpleUserDao userDao = new SimpleUserDao();
            LOGGER.info("SimpleUserDao创建成功，实体类型: {}", userDao.getEntityClass().getSimpleName());
            
            LOGGER.info("AbstractDao无参构造函数测试完成");
            
        } catch (Exception e) {
            LOGGER.error("AbstractDao无参构造函数测试失败", e);
            throw e;
        }
    }

    /**
     * 测试EnhancedDao无参构造函数
     * 验证可以通过泛型自动获取实体类类型
     */
    @Test
    public void testEnhancedDaoNoArgConstructor() {
        LOGGER.info("开始测试EnhancedDao无参构造函数");
        
        try {
            // 创建无参构造函数DAO实例
            SimpleOrderDao orderDao = new SimpleOrderDao();
            LOGGER.info("SimpleOrderDao创建成功，实体类型: {}", orderDao.getEntityClass().getSimpleName());
            
            LOGGER.info("EnhancedDao无参构造函数测试完成");
            
        } catch (Exception e) {
            LOGGER.error("EnhancedDao无参构造函数测试失败", e);
            throw e;
        }
    }

    /**
     * 简单的用户DAO实现 - 继承AbstractDao
     * 连构造函数都可以省略，编译器会自动生成无参构造函数
     */
    private static class SimpleUserDao extends AbstractDao<User, Long> {
        // 无构造函数，编译器自动生成无参构造函数并调用父类无参构造函数
    }

    /**
     * 简单的订单DAO实现 - 继承EnhancedDao
     * 连构造函数都可以省略，编译器会自动生成无参构造函数
     */
    private static class SimpleOrderDao extends EnhancedDao<cn.qaiu.example.entity.Order, Long> {
        // 无构造函数，编译器自动生成无参构造函数并调用父类无参构造函数
    }
}