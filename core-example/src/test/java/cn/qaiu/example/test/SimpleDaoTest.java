package cn.qaiu.example.test;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 最简单的无参构造函数DAO测试
 * 验证DAO可以使用无参构造函数自动获取泛型类型
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SimpleDaoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDaoTest.class);

    /**
     * 测试最简单的DAO创建
     */
    @Test
    public void testSimpleDaoCreation() {
        LOGGER.info("开始测试最简单的DAO创建");
        
        try {
            // 创建最简单的DAO实例 - 连构造函数都没有
            UserDao userDao = new UserDao();
            LOGGER.info("UserDao创建成功！");
            
            LOGGER.info("最简单的DAO创建测试完成");
            
        } catch (Exception e) {
            LOGGER.error("最简单的DAO创建测试失败", e);
            throw e;
        }
    }

    /**
     * 最简单的用户DAO实现
     * 连构造函数都没有，编译器自动生成无参构造函数
     */
    public static class UserDao extends AbstractDao<User, Long> {
        // 完全空的类，编译器自动生成无参构造函数并调用父类无参构造函数
    }
}
