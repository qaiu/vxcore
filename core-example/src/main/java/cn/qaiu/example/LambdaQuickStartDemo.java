package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Lambda查询快速入门示例
 * 
 * 本示例展示了VXCore Lambda查询功能的实际使用方法
 * 包含完整的数据库操作演示
 * 
 * 详细的Lambda查询功能文档请参考：
 * - core-database/docs/lambda/README.md - 完整功能文档
 * - core-database/docs/lambda/API_REFERENCE.md - API参考文档
 * - core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md - 使用指南
 * 
 * @author qaiu
 */
public class LambdaQuickStartDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(LambdaQuickStartDemo.class);
    
    public static void main(String[] args) {
        logger.info("=== Lambda查询快速入门示例 ===");
        
        Vertx vertx = Vertx.vertx();
        
        // 创建数据库连接
        JDBCPool pool = DatabaseUtils.createH2Pool(vertx);
        JooqExecutor executor = DatabaseUtils.createJooqExecutor(pool);
        UserLambdaDao userDao = new UserLambdaDao(executor);
        
        // 运行演示
        runDemo(pool, userDao)
                .onComplete(result -> {
                    if (result.succeeded()) {
                        logger.info("✅ Lambda查询示例完成！");
                    } else {
                        logger.error("❌ Lambda查询示例失败", result.cause());
                    }
                    
                    // 清理资源
                    pool.close().onComplete(v -> vertx.close());
                });
    }
    
    /**
     * 运行完整的Lambda查询演示
     */
    private static io.vertx.core.Future<Void> runDemo(JDBCPool pool, UserLambdaDao userDao) {
        return DatabaseUtils.createUserTable(pool)
                .compose(v -> DatabaseUtils.insertDemoData(pool))
                .compose(v -> {
                    logger.info("🚀 开始Lambda查询演示...");
                    return demonstrateBasicQueries(userDao);
                })
                .compose(v -> {
                    logger.info("📊 演示复杂查询...");
                    return demonstrateComplexQueries(userDao);
                })
                .compose(v -> {
                    logger.info("📄 演示分页查询...");
                    return demonstratePageQueries(userDao);
                })
                .compose(v -> {
                    logger.info("🔍 演示统计查询...");
                    return demonstrateCountQueries(userDao);
                })
                .compose(v -> {
                    logger.info("🎯 演示字段选择查询...");
                    return demonstrateFieldSelectionQueries(userDao);
                })
                .compose(v -> {
                    logger.info("🔄 演示更新和删除操作...");
                    return demonstrateUpdateAndDeleteQueries(userDao);
                });
    }
    
    /**
     * 演示基础查询
     */
    private static io.vertx.core.Future<Void> demonstrateBasicQueries(UserLambdaDao userDao) {
        return userDao.findByUsername("alice")
                .compose(user -> {
                    if (user.isPresent()) {
                        logger.info("📱 根据用户名查询: {} (邮箱: {}, 余额: ${})", 
                                user.get().getUsername(), user.get().getEmail(), user.get().getBalance());
                    }
                    return userDao.findByEmail("bob@example.com");
                })
                .compose(user -> {
                    if (user.isPresent()) {
                        logger.info("📧 根据邮箱查询: {} (年龄: {}, 状态: {})", 
                                user.get().getUsername(), user.get().getAge(), user.get().getStatus());
                    }
                    return userDao.findActiveUsers();
                })
                .compose(users -> {
                    logger.info("✅ 活跃用户数量: {}", users.size());
                    users.forEach(u -> logger.info("   - {} ({}): ${}", u.getUsername(), u.getStatus(), u.getBalance()));
                    return userDao.findVerifiedUsers();
                })
                .compose(users -> {
                    logger.info("🔐 邮箱已验证用户数量: {}", users.size());
                    users.forEach(u -> logger.info("   - {}: {}", u.getUsername(), u.getEmail()));
                    return io.vertx.core.Future.succeededFuture();
                });
    }
    
    /**
     * 演示复杂查询
     */
    private static io.vertx.core.Future<Void> demonstrateComplexQueries(UserLambdaDao userDao) {
        return userDao.findByAgeRange(25, 35)
                .compose(users -> {
                    logger.info("👥 年龄25-35的用户: {}", users.size());
                    users.forEach(u -> logger.info("   - {}: {}岁, ${}", u.getUsername(), u.getAge(), u.getBalance()));
                    return userDao.findByMinBalance(new BigDecimal("1000.00"));
                })
                .compose(users -> {
                    logger.info("💰 余额大于等于1000的用户: {}", users.size());
                    users.forEach(u -> logger.info("   - {}: ${}", u.getUsername(), u.getBalance()));
                    return userDao.findByUsernameLike("a");
                })
                .compose(users -> {
                    logger.info("🔍 用户名包含'a'的用户: {}", users.size());
                    users.forEach(u -> logger.info("   - {}", u.getUsername()));
                    return userDao.findActiveVerifiedRichUsers(new BigDecimal("2000.00"));
                })
                .compose(users -> {
                    logger.info("💎 活跃、已验证、余额大于2000的用户: {}", users.size());
                    users.forEach(u -> logger.info("   - {}: ${} (验证: {})", u.getUsername(), u.getBalance(), u.getEmailVerified()));
                    return userDao.findComplexConditionUsers();
                })
                .compose(users -> {
                    logger.info("🧩 复杂嵌套条件查询结果: {}", users.size());
                    users.forEach(u -> logger.info("   - {}: {}岁, ${}, 验证: {}", 
                            u.getUsername(), u.getAge(), u.getBalance(), u.getEmailVerified()));
                    return io.vertx.core.Future.succeededFuture();
                });
    }
    
    /**
     * 演示分页查询
     */
    private static io.vertx.core.Future<Void> demonstratePageQueries(UserLambdaDao userDao) {
        return userDao.findActiveUsersByPage(1, 3)
                .compose(pageResult -> {
                    logger.info("📄 分页查询结果:");
                    logger.info("   总数: {}, 当前页: {}, 页大小: {}, 总页数: {}", 
                            pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getPages());
                    pageResult.getRecords().forEach(u -> 
                            logger.info("   - {}: {} (${})", u.getUsername(), u.getStatus(), u.getBalance()));
                    return io.vertx.core.Future.succeededFuture();
                });
    }
    
    /**
     * 演示统计查询
     */
    private static io.vertx.core.Future<Void> demonstrateCountQueries(UserLambdaDao userDao) {
        return userDao.countActiveUsers()
                .compose(count -> {
                    logger.info("📊 活跃用户总数: {}", count);
                    return userDao.existsByEmail("alice@example.com");
                })
                .compose(exists -> {
                    logger.info("🔍 邮箱'alice@example.com'是否存在: {}", exists);
                    return userDao.existsByEmail("nonexistent@example.com");
                })
                .compose(exists -> {
                    logger.info("🔍 邮箱'nonexistent@example.com'是否存在: {}", exists);
                    return io.vertx.core.Future.succeededFuture();
                });
    }
    
    /**
     * 演示字段选择查询
     */
    private static io.vertx.core.Future<Void> demonstrateFieldSelectionQueries(UserLambdaDao userDao) {
        return userDao.findBasicInfo()
                .compose(users -> {
                    logger.info("🎯 用户基本信息查询结果: {}", users.size());
                    users.forEach(u -> {
                        logger.info("   - ID: {}, 用户名: {}, 邮箱: {}, 状态: {}", 
                                u.getId(), u.getUsername(), u.getEmail(), u.getStatus());
                    });
                    return io.vertx.core.Future.succeededFuture();
                });
    }
    
    /**
     * 演示更新和删除操作
     */
    private static io.vertx.core.Future<Void> demonstrateUpdateAndDeleteQueries(UserLambdaDao userDao) {
        return userDao.findByUsername("henry")
                .compose(user -> {
                    if (user.isPresent()) {
                        logger.info("🔄 更新前用户状态: {} -> {}", user.get().getUsername(), user.get().getStatus());
                        // 将henry的状态从SUSPENDED改为ACTIVE
                        return userDao.updateUsersStatus(Arrays.asList(user.get().getId()), User.UserStatus.ACTIVE);
                    }
                    return io.vertx.core.Future.succeededFuture(0);
                })
                .compose(updatedCount -> {
                    logger.info("✅ 更新了 {} 个用户的状态", updatedCount);
                    return userDao.findByUsername("henry");
                })
                .compose(user -> {
                    if (user.isPresent()) {
                        logger.info("🔄 更新后用户状态: {} -> {}", user.get().getUsername(), user.get().getStatus());
                    }
                    return userDao.countActiveUsers();
                })
                .compose(count -> {
                    logger.info("📊 更新后活跃用户总数: {}", count);
                    return io.vertx.core.Future.succeededFuture();
                });
    }
}
