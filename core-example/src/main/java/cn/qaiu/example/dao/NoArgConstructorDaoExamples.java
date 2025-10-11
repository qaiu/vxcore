package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.EnhancedDao;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.entity.Order;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 无参构造函数DAO示例
 * 展示如何使用无参构造函数创建DAO，框架自动处理所有初始化
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class NoArgConstructorDaoExamples {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoArgConstructorDaoExamples.class);

    /**
     * 最简单的用户DAO
     * 连构造函数都没有，编译器自动生成无参构造函数
     */
    public static class SimpleUserDao extends AbstractDao<User, Long> {
        // 完全空的类，框架自动处理所有初始化
        // 1. 自动通过泛型获取User类型
        // 2. 自动初始化SQL执行器
        // 3. 自动获取表名和主键信息
    }

    /**
     * 多数据源用户DAO
     * 通过@DataSource注解指定数据源
     */
    @cn.qaiu.db.datasource.DataSource("user")
    public static class MultiDataSourceUserDao extends EnhancedDao<User, Long> {
        // 完全空的类，框架自动处理所有初始化
        // 1. 自动通过泛型获取User类型
        // 2. 自动使用"user"数据源
        // 3. 自动初始化SQL执行器
    }

    /**
     * 订单DAO
     * 展示业务方法的使用
     */
    public static class OrderDao extends EnhancedDao<Order, Long> {
        // 完全空的类，框架自动处理所有初始化
        // 可以添加业务方法，但为了演示简洁性，这里保持空类
    }

    /**
     * 演示无参构造函数的使用
     * 展示如何创建和使用无参构造函数的DAO实例
     */
    public static void demonstrateUsage() {
        LOGGER.info("=== 无参构造函数DAO使用演示 ===");
        
        // 1. 最简单的使用方式 - 连构造函数都没有
        SimpleUserDao simpleUserDao = new SimpleUserDao();
        LOGGER.info("SimpleUserDao创建成功，实体类型: {}", simpleUserDao.getEntityClass().getSimpleName());
        
        // 2. 多数据源使用方式
        MultiDataSourceUserDao multiDataSourceUserDao = new MultiDataSourceUserDao();
        LOGGER.info("MultiDataSourceUserDao创建成功，实体类型: {}", multiDataSourceUserDao.getEntityClass().getSimpleName());
        
        // 3. 带业务方法的DAO
        OrderDao orderDao = new OrderDao();
        LOGGER.info("OrderDao创建成功，实体类型: {}", orderDao.getEntityClass().getSimpleName());
        
        LOGGER.info("=== 所有DAO都成功创建，无需手动传递任何参数 ===");
    }

    /**
     * 对比传统方式和无参构造函数方式
     * 展示两种使用方式的差异和优势
     */
    public static void compareUsage() {
        LOGGER.info("=== 使用方式对比 ===");
        
        LOGGER.info("传统方式（需要手动传递参数）:");
        LOGGER.info("  new UserDao(executor, User.class);");
        LOGGER.info("  new OrderDao(executor, Order.class);");
        
        LOGGER.info("");
        LOGGER.info("无参构造函数方式（框架自动处理）:");
        LOGGER.info("  new SimpleUserDao();  // 自动获取User类型");
        LOGGER.info("  new OrderDao();       // 自动获取Order类型");
        LOGGER.info("  new MultiDataSourceUserDao(); // 自动使用指定数据源");
        
        LOGGER.info("");
        LOGGER.info("优势:");
        LOGGER.info("  ✓ 代码更简洁");
        LOGGER.info("  ✓ 无需手动管理JooqExecutor");
        LOGGER.info("  ✓ 自动通过泛型获取实体类型");
        LOGGER.info("  ✓ 自动处理多数据源");
        LOGGER.info("  ✓ 减少出错可能性");
    }
}
