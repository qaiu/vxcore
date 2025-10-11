package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.example.User;
import cn.qaiu.db.dsl.lambda.example.UserDao;
import cn.qaiu.db.pool.JDBCPoolInit;
import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lambda查询功能测试
 * 
 * @author qaiu
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LambdaQueryTest {
    
    private static final Logger logger = LoggerFactory.getLogger(LambdaQueryTest.class);
    
    private static Vertx vertx;
    private static JDBCPool pool;
    private static JooqExecutor executor;
    private static UserDao userDao;
    
    @BeforeAll
    static void setUp() {
        vertx = Vertx.vertx();
        
        // 创建H2内存数据库连接池
        io.vertx.jdbcclient.JDBCConnectOptions connectOptions = new io.vertx.jdbcclient.JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .setUser("sa")
                .setPassword("");
        
        pool = JDBCPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(10));
        executor = new JooqExecutor(pool);
        userDao = new UserDao(executor);
        
        // 创建测试表
        createTestTable();
    }
    
    @AfterAll
    static void tearDown() {
        if (pool != null) {
            pool.close();
        }
        if (vertx != null) {
            vertx.close();
        }
    }
    
    @BeforeEach
    void setUpEach() {
        // 清空测试数据
        clearTestData();
        // 插入测试数据
        insertTestData();
    }
    
    /**
     * 创建测试表
     */
    private static void createTestTable() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                age INTEGER,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                balance DECIMAL(10,2) DEFAULT 0.00,
                email_verified BOOLEAN DEFAULT FALSE,
                bio TEXT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try {
            cn.qaiu.vx.core.util.FutureUtils.getResult(pool.query(createTableSql).execute());
            logger.info("Test table created successfully");
        } catch (Exception e) {
            logger.error("Failed to create test table", e);
            throw new RuntimeException("Failed to create test table", e);
        }
    }
    
    /**
     * 清空测试数据
     */
    private void clearTestData() {
        try {
            cn.qaiu.vx.core.util.FutureUtils.getResult(pool.query("DELETE FROM users").execute());
            logger.debug("Test data cleared");
        } catch (Exception e) {
            logger.error("Failed to clear test data", e);
            throw new RuntimeException("Failed to clear test data", e);
        }
    }
    
    /**
     * 插入测试数据
     */
    private void insertTestData() {
        String insertSql = """
            INSERT INTO users (username, email, password, age, status, balance, email_verified, bio, create_time, update_time) VALUES
            ('john_doe', 'john@example.com', 'password123', 25, 'ACTIVE', 1000.50, true, 'Software Developer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('jane_smith', 'jane@example.com', 'password456', 30, 'ACTIVE', 2500.75, true, 'Data Scientist', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('bob_wilson', 'bob@example.com', 'password789', 35, 'INACTIVE', 500.25, false, 'Marketing Manager', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('alice_brown', 'alice@example.com', 'password101', 28, 'ACTIVE', 1500.00, true, 'UX Designer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('charlie_davis', 'charlie@example.com', 'password202', 32, 'PENDING', 750.80, false, 'Product Manager', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        try {
            var result = cn.qaiu.vx.core.util.FutureUtils.getResult(pool.query(insertSql).execute());
            logger.debug("Test data inserted: {} rows", result.rowCount());
        } catch (Exception e) {
            logger.error("Failed to insert test data", e);
            throw new RuntimeException("Failed to insert test data", e);
        }
    }
    
    @Test
    @Order(1)
    void testBasicLambdaQuery() {
        logger.info("=== 测试基础Lambda查询 ===");
        
        // 测试等值查询
        userDao.lambdaOne(User::getUsername, "john_doe")
                .onSuccess(user -> {
                    assertTrue(user.isPresent());
                    assertEquals("john_doe", user.get().getUsername());
                    assertEquals("john@example.com", user.get().getEmail());
                    logger.info("✓ 等值查询测试通过: {}", user.get().getUsername());
                })
                .onFailure(error -> fail("等值查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(2)
    void testLikeQuery() {
        logger.info("=== 测试LIKE查询 ===");
        
        // 测试模糊查询
        userDao.lambdaList(userDao.lambdaQuery().like(User::getUsername, "john"))
                .onSuccess(users -> {
                    assertEquals(1, users.size());
                    assertEquals("john_doe", users.get(0).getUsername());
                    logger.info("✓ LIKE查询测试通过: 找到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("LIKE查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(3)
    void testRangeQuery() {
        logger.info("=== 测试范围查询 ===");
        
        // 测试年龄范围查询
        userDao.findByAgeRange(25, 30)
                .onSuccess(users -> {
                    assertTrue(users.size() >= 2);
                    users.forEach(user -> {
                        assertTrue(user.getAge() >= 25 && user.getAge() <= 30);
                    });
                    logger.info("✓ 范围查询测试通过: 找到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("范围查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(4)
    void testComplexQuery() {
        logger.info("=== 测试复杂查询 ===");
        
        // 测试多条件组合查询
        userDao.findActiveUsersWithHighBalance(1000.0)
                .onSuccess(users -> {
                    assertTrue(users.size() >= 2);
                    users.forEach(user -> {
                        assertEquals("ACTIVE", user.getStatus());
                        assertTrue(user.getBalance() >= 1000.0);
                        assertTrue(user.getEmailVerified());
                    });
                    logger.info("✓ 复杂查询测试通过: 找到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("复杂查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(5)
    void testNestedConditionQuery() {
        logger.info("=== 测试嵌套条件查询 ===");
        
        // 测试嵌套条件查询
        userDao.findUsersWithComplexCondition("ACTIVE", 25, 1000.0)
                .onSuccess(users -> {
                    assertTrue(users.size() >= 1);
                    users.forEach(user -> {
                        assertEquals("ACTIVE", user.getStatus());
                        assertTrue(user.getAge() >= 25 || 
                                 (user.getBalance() >= 1000.0 && user.getEmailVerified()));
                    });
                    logger.info("✓ 嵌套条件查询测试通过: 找到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("嵌套条件查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(6)
    void testPageQuery() {
        logger.info("=== 测试分页查询 ===");
        
        // 测试分页查询
        userDao.findUsersByPage(1, 2, "ACTIVE")
                .onSuccess(pageResult -> {
                    assertNotNull(pageResult);
                    assertTrue(pageResult.getTotal() >= 3);
                    assertEquals(2, pageResult.getSize());
                    assertEquals(1, pageResult.getCurrent());
                    assertTrue(pageResult.getRecords().size() <= 2);
                    
                    pageResult.getRecords().forEach(user -> {
                        assertEquals("ACTIVE", user.getStatus());
                    });
                    
                    logger.info("✓ 分页查询测试通过: 总数={}, 当前页={}, 页大小={}", 
                            pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
                })
                .onFailure(error -> fail("分页查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(7)
    void testCountQuery() {
        logger.info("=== 测试统计查询 ===");
        
        // 测试统计查询
        userDao.countActiveUsers()
                .onSuccess(count -> {
                    assertTrue(count >= 3);
                    logger.info("✓ 统计查询测试通过: 活跃用户数量 = {}", count);
                })
                .onFailure(error -> fail("统计查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(8)
    void testExistsQuery() {
        logger.info("=== 测试存在性查询 ===");
        
        // 测试存在性查询
        userDao.existsByEmail("john@example.com")
                .onSuccess(exists -> {
                    assertTrue(exists);
                    logger.info("✓ 存在性查询测试通过: 邮箱存在 = {}", exists);
                })
                .onFailure(error -> fail("存在性查询失败: " + error.getMessage()));
        
        // 测试不存在的邮箱
        userDao.existsByEmail("nonexistent@example.com")
                .onSuccess(exists -> {
                    assertFalse(exists);
                    logger.info("✓ 不存在性查询测试通过: 邮箱不存在 = {}", exists);
                })
                .onFailure(error -> fail("不存在性查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(9)
    void testFieldSelectionQuery() {
        logger.info("=== 测试字段选择查询 ===");
        
        // 测试字段选择查询
        userDao.findUserBasicInfo()
                .onSuccess(users -> {
                    assertTrue(users.size() >= 1);
                    users.forEach(user -> {
                        assertNotNull(user.getId());
                        assertNotNull(user.getUsername());
                        assertNotNull(user.getEmail());
                        assertNotNull(user.getStatus());
                        // 其他字段应该为null（未选择）
                        assertNull(user.getPassword());
                        assertNull(user.getAge());
                    });
                    logger.info("✓ 字段选择查询测试通过: 查询到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("字段选择查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(10)
    void testBatchUpdate() {
        logger.info("=== 测试批量更新 ===");
        
        // 测试批量更新
        List<Long> userIds = Arrays.asList(1L, 2L);
        userDao.updateUserStatus(userIds, "UPDATED")
                .onSuccess(updatedCount -> {
                    assertEquals(2, updatedCount);
                    
                    // 验证更新结果
                    userDao.lambdaList(User::getStatus, "UPDATED")
                            .onSuccess(updatedUsers -> {
                                assertEquals(2, updatedUsers.size());
                                logger.info("✓ 批量更新测试通过: 更新了 {} 个用户", updatedCount);
                            })
                            .onFailure(error -> fail("验证批量更新结果失败: " + error.getMessage()));
                })
                .onFailure(error -> fail("批量更新失败: " + error.getMessage()));
    }
    
    @Test
    @Order(11)
    void testLambdaQueryWrapper() {
        logger.info("=== 测试LambdaQueryWrapper ===");
        
        // 测试LambdaQueryWrapper的各种方法
        LambdaQueryWrapper<User> wrapper = userDao.lambdaQuery()
                .eq(User::getStatus, "ACTIVE")
                .ge(User::getAge, 25)
                .le(User::getAge, 35)
                .orderByDesc(User::getBalance)
                .orderByAsc(User::getCreateTime)
                .limit(3);
        
        userDao.lambdaList(wrapper)
                .onSuccess(users -> {
                    assertTrue(users.size() <= 3);
                    users.forEach(user -> {
                        assertEquals("ACTIVE", user.getStatus());
                        assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
                    });
                    logger.info("✓ LambdaQueryWrapper测试通过: 查询到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("LambdaQueryWrapper测试失败: " + error.getMessage()));
    }
    
    @Test
    @Order(12)
    void testInQuery() {
        logger.info("=== 测试IN查询 ===");
        
        // 测试IN查询
        List<String> statuses = Arrays.asList("ACTIVE", "PENDING");
        userDao.lambdaList(userDao.lambdaQuery().in(User::getStatus, statuses))
                .onSuccess(users -> {
                    assertTrue(users.size() >= 3);
                    users.forEach(user -> {
                        assertTrue(statuses.contains(user.getStatus()));
                    });
                    logger.info("✓ IN查询测试通过: 查询到 {} 个用户", users.size());
                })
                .onFailure(error -> fail("IN查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(13)
    void testIsNullQuery() {
        logger.info("=== 测试NULL查询 ===");
        
        // 先插入一个bio为null的用户
        User newUser = new User();
        newUser.setUsername("null_bio_user");
        newUser.setEmail("nullbio@example.com");
        newUser.setPassword("password");
        newUser.setStatus("ACTIVE");
        newUser.setBio(null); // 显式设置为null
        
        userDao.insert(newUser)
                .onSuccess(inserted -> {
                    // 测试IS NULL查询
                    userDao.lambdaList(userDao.lambdaQuery().isNull(User::getBio))
                            .onSuccess(users -> {
                                assertTrue(users.size() >= 1);
                                users.forEach(user -> {
                                    assertNull(user.getBio());
                                });
                                logger.info("✓ IS NULL查询测试通过: 查询到 {} 个用户", users.size());
                            })
                            .onFailure(error -> fail("IS NULL查询失败: " + error.getMessage()));
                })
                .onFailure(error -> fail("插入测试用户失败: " + error.getMessage()));
    }
}
