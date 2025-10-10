package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("用户DAO测试")
class UserDaoTest {

    private UserDao userDao;
    private JooqExecutor executor;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 创建 H2 内存数据库配置
        JsonObject config = new JsonObject()
                .put("url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .put("driver_class", "org.h2.Driver")
                .put("user", "sa")
                .put("password", "")
                .put("max_pool_size", 10);

        // 创建连接池
        io.vertx.jdbcclient.JDBCConnectOptions connectOptions = new io.vertx.jdbcclient.JDBCConnectOptions()
                .setJdbcUrl(config.getString("url"))
                .setUser(config.getString("user"))
                .setPassword(config.getString("password"));
        io.vertx.sqlclient.PoolOptions poolOptions = new io.vertx.sqlclient.PoolOptions()
                .setMaxSize(config.getInteger("max_pool_size", 10));
        io.vertx.sqlclient.Pool pool = io.vertx.jdbcclient.JDBCPool.pool(vertx, connectOptions, poolOptions);
        
        // 创建 JooqExecutor
        executor = new JooqExecutor(pool);
        userDao = new UserDao(executor);

        // 初始化数据库
        executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "age INT DEFAULT 0, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                "balance DECIMAL(10,2) DEFAULT 0.00, " +
                "email_verified BOOLEAN DEFAULT FALSE, " +
                "bio TEXT, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM users")))
                .onComplete(testContext.succeedingThenComplete());
    }

    @Nested
    @DisplayName("基本CRUD操作测试")
    class BasicCrudTest {

        @Test
        @DisplayName("创建用户测试")
        void testCreateUser(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .onComplete(testContext.succeeding(user -> {
                        assertNotNull(user);
                        assertNotNull(user.getId());
                        assertEquals("testuser", user.getUsername());
                        assertEquals("test@example.com", user.getEmail());
                        assertEquals("password123", user.getPassword());
                        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
                        assertEquals(new BigDecimal("100.00"), user.getBalance());
                        assertFalse(user.getEmailVerified());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据ID查找用户测试")
        void testFindById(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(user -> userDao.findById(user.getId()))
                    .onComplete(testContext.succeeding(optional -> {
                        assertTrue(optional.isPresent());
                        User user = optional.get();
                        assertEquals("testuser", user.getUsername());
                        assertEquals("test@example.com", user.getEmail());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("更新用户密码测试")
        void testUpdatePassword(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(user -> userDao.updatePassword(user.getId(), "newpassword456"))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("验证用户邮箱测试")
        void testVerifyUserEmail(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(user -> userDao.verifyUserEmail(user.getId()))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("更新用户状态测试")
        void testUpdateUserStatus(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(user -> userDao.updateUserStatus(user.getId(), User.UserStatus.SUSPENDED))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTest {

        @Test
        @DisplayName("根据用户名查找用户测试")
        void testFindByName(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(v -> userDao.findByName("testuser"))
                    .onComplete(testContext.succeeding(users -> {
                        assertTrue(!users.isEmpty());
                        User user = users.get(0);
                        assertEquals("testuser", user.getUsername());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据邮箱查找用户测试")
        void testFindByEmail(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(v -> userDao.findOneByEmail("test@example.com"))
                    .onComplete(testContext.succeeding(optional -> {
                        assertTrue(optional.isPresent());
                        User user = optional.get();
                        assertEquals("test@example.com", user.getEmail());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("查找所有活跃用户测试")
        void testFindActiveUsers(VertxTestContext testContext) {
            userDao.createUser("user1", "user1@example.com", "password123")
                    .compose(v -> userDao.createUser("user2", "user2@example.com", "password123"))
                    .compose(v -> userDao.createUser("user3", "user3@example.com", "password123"))
                    .compose(v -> userDao.findActiveUsers())
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(3, users.size());
                        users.forEach(user -> assertEquals(User.UserStatus.ACTIVE, user.getStatus()));
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据年龄范围查找用户测试")
        void testFindByAgeRange(VertxTestContext testContext) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPassword("password123");
            user1.setAge(25);

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setPassword("password123");
            user2.setAge(35);

            userDao.insert(user1)
                    .compose(v -> userDao.insert(user2))
                    .compose(v -> userDao.findByAgeRange(20, 30))
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(1, users.size());
                        assertEquals("user1", users.get(0).getName());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据最小余额查找用户测试")
        void testFindByMinBalance(VertxTestContext testContext) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPassword("password123");
            user1.setBalance(new BigDecimal("50.00"));

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setPassword("password123");
            user2.setBalance(new BigDecimal("200.00"));

            userDao.insert(user1)
                    .compose(v -> userDao.insert(user2))
                    .compose(v -> userDao.findByMinBalance(new BigDecimal("100.00")))
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(1, users.size());
                        assertEquals("user2", users.get(0).getName());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsTest {

        @Test
        @DisplayName("获取用户统计信息测试")
        void testGetUserStatistics(VertxTestContext testContext) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPassword("password123");
            user1.setAge(25);

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setPassword("password123");
            user2.setAge(35);

            userDao.insert(user1)
                    .compose(v -> userDao.insert(user2))
                    .compose(v -> userDao.getUserStatistics())
                    .onComplete(testContext.succeeding(statistics -> {
                        assertEquals(2, statistics.getInteger("totalUsers"));
                        assertEquals(2, statistics.getInteger("activeUsers"));
                        assertEquals(30.0, statistics.getDouble("averageAge"), 0.1);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("分页操作测试")
    class PaginationTest {

        @Test
        @DisplayName("分页查询用户测试")
        void testFindPage(VertxTestContext testContext) {
            // 创建多个用户
            Future<Void> createUsers = Future.succeededFuture();
            for (int i = 1; i <= 5; i++) {
                final int index = i;
                createUsers = createUsers.compose(v -> {
                    User user = new User();
                    user.setUsername("user" + index);
                    user.setEmail("user" + index + "@example.com");
                    user.setPassword("password123");
                    return userDao.insert(user).map(u -> null);
                });
            }

            createUsers.compose(v -> userDao.findAll())
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(3, users.size());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("Lambda查询测试")
    class LambdaQueryTest {

        @Test
        @DisplayName("Lambda查询基本功能测试")
        void testLambdaQuery(VertxTestContext testContext) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("password123");
            user.setAge(25);

            userDao.insert(user)
                    .compose(v -> userDao.lambdaQuery()
                            .eq(User::getUsername, "testuser")
                            .eq(User::getAge, 25)
                            .list())
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(1, users.size());
                        User foundUser = users.get(0);
                        assertEquals("testuser", foundUser.getUsername());
                        assertEquals(25, foundUser.getAge());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询条件组合测试")
        void testLambdaQueryConditions(VertxTestContext testContext) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPassword("password123");
            user1.setAge(25);
            user1.setStatus(User.UserStatus.ACTIVE);

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setPassword("password123");
            user2.setAge(35);
            user2.setStatus(User.UserStatus.ACTIVE);

            userDao.insert(user1)
                    .compose(v -> userDao.insert(user2))
                    .compose(v -> userDao.lambdaQuery()
                            .eq(User::getStatus, User.UserStatus.ACTIVE)
                            .ge(User::getAge, 30)
                            .list())
                    .onComplete(testContext.succeeding(users -> {
                        assertEquals(1, users.size());
                        assertEquals("user2", users.get(0).getName());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationTest {

        @Test
        @DisplayName("批量插入用户测试")
        void testInsertBatch(VertxTestContext testContext) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPassword("password123");

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setPassword("password123");

            userDao.insertBatch(List.of(user1, user2))
                    .onComplete(testContext.succeeding(count -> {
                        assertEquals(2, count);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class DeleteTest {

        @Test
        @DisplayName("根据ID删除用户测试")
        void testDeleteById(VertxTestContext testContext) {
            userDao.createUser("testuser", "test@example.com", "password123")
                    .compose(user -> userDao.deleteById(user.getId()))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }
    }
}
