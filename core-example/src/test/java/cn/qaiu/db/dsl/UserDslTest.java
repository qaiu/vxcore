package cn.qaiu.db.dsl;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.dao.UserDao;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User DSL 框架测试类
 * 
 * 测试 JOQ + Vert.x DSL 框架的基本功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("User DSL Framework Test")
public class UserDslTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDslTest.class);

    private JooqExecutor executor;
    private UserDao userDao;

    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(VertxTestContext testContext) {
        try {
            Vertx vertx = Vertx.vertx();

            // 创建 H2 测试数据库连接 - 使用随机数据库名避免冲突
            String dbName = "testdb_" + System.currentTimeMillis();
            PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
            JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                    .setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL")
                    .setUser("sa")
                    .setPassword("");

            JDBCPool pool = JDBCPool.pool(vertx, connectOptions, poolOptions);

            // 初始化表和 DAO
            initTestDatabase(pool)
                    .onSuccess(v -> {
                        this.executor = new JooqExecutor(pool);
                        this.userDao = new UserDao(executor);
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);

        } catch (Exception e) {
            LOGGER.error("Test setup failed", e);
            testContext.failNow(e);
        }
    }

    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown(VertxTestContext testContext) {
        if (executor != null) {
            // 清理测试数据
            executor.executeQuery(DSL.query("DELETE FROM users"))
                    .onSuccess(v -> {
                        LOGGER.debug("Test data cleaned up");
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }

    /**
     * 初始化测试数据库表
     */
    private Future<Void> initTestDatabase(JDBCPool pool) {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    age INT DEFAULT 0,
                    status VARCHAR(20) DEFAULT 'ACTIVE',
                    balance DECIMAL(10,2) DEFAULT 0.00,
                    email_verified BOOLEAN DEFAULT FALSE,
                    bio TEXT,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        return pool.query(createTableSQL).execute()
                .onSuccess(result -> LOGGER.info("Test database table created"))
                .onFailure(error -> LOGGER.error("Failed to create test table", error))
                .map(v -> null);
    }

    @Test
    @DisplayName("测试创建用户")
    void testCreateUser(VertxTestContext testContext) {
        userDao.createUser("testuser", "test@example.com", "password123")
                .onSuccess(user -> {
                    testContext.verify(() -> {
                        assertNotNull(user);
                        assertEquals("testuser", user.getUsername());
                        assertEquals("test@example.com", user.getEmail());
                        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
                        assertNotNull(user.getId());
                        assertTrue(user.getId() > 0);
                    });
                    LOGGER.info("User created: {} ", user.getId());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试根据用户名查询")
    void testFindByUsername(VertxTestContext testContext) {
        // 首先创建一个用户
        userDao.createUser("alice", "alice@example.com", "password123")
                .compose(user -> {
                    // 然后根据用户名查询
                    return userDao.findByName("alice");
                })
                .onSuccess(users -> {
                    testContext.verify(() -> {
                        assertTrue(!users.isEmpty());
                        User user = users.get(0);
                        assertEquals("alice", user.getUsername());
                        assertEquals("alice@example.com", user.getEmail());
                    });
                    User foundUser = users.get(0);
                    LOGGER.info("User found by username: {}, username field: {}", foundUser.getUsername(), foundUser.getUsername());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试根据邮箱查询")
    void testFindByEmail(VertxTestContext testContext) {
        userDao.createUser("bob", "bob@example.com", "password123")
                .compose(user -> userDao.findOneByEmail("bob@example.com"))
                .onSuccess(userOpt -> {
                    testContext.verify(() -> {
                        assertTrue(userOpt.isPresent());
                        User user = userOpt.get();
                        assertEquals("bob", user.getUsername());
                        assertEquals("bob@example.com", user.getEmail());
                    });
                    LOGGER.info("User found by email: {}", userOpt.get().getName());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试更新用户密码")
    void testUpdatePassword(VertxTestContext testContext) {
        userDao.createUser("passwordTest", "passwordtest@example.com", "oldpassword")
                .compose(user -> {
                    // 更新密码
                    return userDao.updatePassword(user.getId(), "newpassword123");
                })
                .compose(updated -> {
                    testContext.verify(() -> {
                        assertTrue(updated);
                    });
                    LOGGER.info("Password updated successfully");
                    
                    // 验证更新是否生效
                    return userDao.findByName("passwordTest");
                })
                .onSuccess(users -> {
                    testContext.verify(() -> {
                        assertTrue(!users.isEmpty());
                        User user = users.get(0);
                        assertEquals("newpassword123", user.getPassword());
                    });
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试查询激活用户")
    void testFindActiveUsers(VertxTestContext testContext) {
        // 创建多个用户
        userDao.createUser("user1", "user1@example.com", "pass1")
                .compose(u1 -> userDao.createUser("user2", "user2@example.com", "pass2"))
                .compose(u2 -> userDao.createUser("user3", "user3@example.com", "pass3"))
                .compose(u3 -> {
                    // 查询激活用户
                    return userDao.findActiveUsers();
                })
                .onSuccess(activeUsers -> {
                    testContext.verify(() -> {
                        assertNotNull(activeUsers);
                        assertTrue(activeUsers.size() >= 3);
                        activeUsers.forEach(user -> {
                            assertEquals(User.UserStatus.ACTIVE, user.getStatus());
                        });
                    });
                    LOGGER.info("Found {} active users", activeUsers.size());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试邮箱验证")
    void testVerifyEmail(VertxTestContext testContext) {
        userDao.createUser("emailTest", "emailtest@example.com", "password")
                .compose(user -> {
                    // 验证邮箱
                    return userDao.verifyUserEmail(user.getId());
                })
                .compose(verified -> {
                    testContext.verify(() -> {
                        assertTrue(verified);
                    });
                    
                    // 验证邮箱验证状态
                    return userDao.findByName("emailTest");
                })
                .onSuccess(users -> {
                    testContext.verify(() -> {
                        assertTrue(!users.isEmpty());
                        assertTrue(users.get(0).getEmailVerified());
                    });
                    LOGGER.info("Email verification completed");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试用户统计")
    void testUserStatistics(VertxTestContext testContext) {
        // 创建测试数据
        userDao.createUser("stats1", "stats1@example.com", "pass1")
                .compose(u1 -> userDao.createUser("stats2", "stats2@example.com", "pass2"))
                .compose(u2 -> {
                    UserDao dao = userDao;
                    return dao.createUser("stats3", "stats3@example.com", "pass3")
                            .compose(u3 -> dao.verifyUserEmail(u2.getId()));
                })
                .compose(verified -> userDao.getUserStatistics())
                .onSuccess(stats -> {
                    testContext.verify(() -> {
                        assertNotNull(stats);
                        assertTrue(stats.getInteger("totalUsers") >= 3);
                        assertTrue(stats.getInteger("activeUsers") >= 0);
                    });
                    LOGGER.info("User statistics: {}", stats.encodePrettily());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试年龄范围查询")
    void testFindByAgeRange(VertxTestContext testContext) {
        // 创建不同年龄的用户
        createUserWithAge("young", "young@test.com", "pass", 25)
                .compose(u1 -> createUserWithAge("middle", "middle@test.com", "pass", 35))
                .compose(u2 -> createUserWithAge("old", "old@test.com", "pass", 45))
                .compose(u3 -> {
                    // 查询 30-40 岁用户
                    return userDao.findByAgeRange(30, 40);
                })
                .onSuccess(users -> {
                    testContext.verify(() -> {
                        assertNotNull(users);
                        // 在 H2 中，只有 35 岁的用户应该被找到
                        assertEquals(1, users.size());
                        assertEquals("middle", users.get(0).getName());
                    });
                    LOGGER.info("Found {} users in age range 30-40", users.size());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    /**
     * 创建指定年龄的用户的辅助方法
     */
    private Future<User> createUserWithAge(String username, String email, String password, Integer age) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setAge(age);
        user.onCreate();
        
        return userDao.insert(user)
                .map(optionalUser -> optionalUser.orElseThrow(() -> new RuntimeException("Failed to create user")));
    }

    @Test
    @DisplayName("测试 BaseEntity 功能")
    void testBaseEntity(VertxTestContext testContext) {
        User user = new User();
        user.setUsername("entityTest");
        user.setEmail("entity@test.com");
        user.setBio("Test bio");
        
        // 测试创建时间回调
        user.onCreate();
        
        testContext.verify(() -> {
            assertNotNull(user.getCreateTime());
            assertNotNull(user.getUpdateTime());
            // 使用时间差比较而不是严格相等，因为LocalDateTime.now()可能有微秒级差异
            long timeDiff = Math.abs(java.time.Duration.between(user.getCreateTime(), user.getUpdateTime()).toMillis());
            assertTrue(timeDiff < 100, 
                      "Create time and update time should be very close (within 100ms), but diff is: " + timeDiff + "ms");
        });
        
        // 测试更新时间回调
        try {
            Thread.sleep(10); // 确保时间有差异
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        user.onUpdate();
        
        testContext.verify(() -> {
            assertTrue(user.getUpdateTime().isAfter(user.getCreateTime()));
        });
        
        // 测试 JSON 转换
        JsonObject json = user.toJson();
        testContext.verify(() -> {
            assertEquals("entityTest", json.getString("username"));
            assertEquals("entity@test.com", json.getString("email"));
            assertEquals("Test bio", json.getString("bio"));
            assertTrue(json.containsKey("id"));
        });
        
        // 测试从 JSON 构造
        User fromJson = new User(json);
        testContext.verify(() -> {
            assertEquals(user.getUsername(), fromJson.getUsername());
            assertEquals(user.getEmail(), fromJson.getEmail());
            assertEquals(user.getBio(), fromJson.getBio());
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试用户状态枚举")
    void testUserStatus(VertxTestContext testContext) {
        testContext.verify(() -> {
            // 测试枚举值
            assertEquals("ACTIVE", User.UserStatus.ACTIVE.name());
            assertEquals("INACTIVE", User.UserStatus.INACTIVE.name());
            assertEquals("SUSPENDED", User.UserStatus.SUSPENDED.name());
            
            // 测试描述
            assertEquals("激活", User.UserStatus.ACTIVE.getDescription());
            assertEquals("未激活", User.UserStatus.INACTIVE.getDescription());
            assertEquals("暂停", User.UserStatus.SUSPENDED.getDescription());
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试用户业务方法")
    void testUserBusinessMethods(VertxTestContext testContext) {
        User user = new User();
        user.setUsername("businessTest");
        user.setEmail("business@test.com");
        user.setPassword("password123");
        user.setStatus(User.UserStatus.ACTIVE);
        
        testContext.verify(() -> {
            // 测试密码验证
            assertTrue(user.verifyPassword("password123"));
            assertFalse(user.verifyPassword("wrongpassword"));
            
            // 测试激活状态
            assertTrue(user.isActive());
            
            // 测试状态变更
            user.suspend();
            assertFalse(user.isActive());
            assertEquals(User.UserStatus.SUSPENDED, user.getStatus());
            
            user.activate();
            assertTrue(user.isActive());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        });
        
        testContext.completeNow();
    }
}