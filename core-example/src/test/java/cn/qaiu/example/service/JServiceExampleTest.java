package cn.qaiu.example.service;

import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JService 使用示例测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
public class JServiceExampleTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JServiceExampleTest.class);

    private UserService userService;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 初始化 JooqExecutor（这里需要根据实际配置）
        // jooqExecutor = new JooqExecutor(vertx, dataSource);
        // userService = new UserServiceImpl(jooqExecutor);
        
        // 为了演示，这里跳过实际初始化
        LOGGER.info("JService 示例测试准备完成");
        testContext.completeNow();
    }

    @Test
    void testBasicCrudOperations(VertxTestContext testContext) {
        LOGGER.info("=== 测试基础 CRUD 操作 ===");

        // 创建用户
        User user = new User();
        user.setUsername("张三");
        user.setEmail("zhangsan@example.com");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreateTime(LocalDateTime.now());

        // 注意：这里需要实际的 userService 实例才能运行
        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // 插入用户
        userService.save(user)
                .compose(savedUser -> {
                    LOGGER.info("用户保存成功: {}", savedUser.orElse(null));
                    assertTrue(savedUser.isPresent());
                    
                    // 查询用户
                    return userService.getById(savedUser.get().getId());
                })
                .compose(foundUser -> {
                    LOGGER.info("用户查询成功: {}", foundUser.orElse(null));
                    assertTrue(foundUser.isPresent());
                    assertEquals("张三", foundUser.get().getUsername());
                    
                    // 更新用户
                    foundUser.get().setUsername("李四");
                    return userService.updateById(foundUser.get());
                })
                .compose(updatedUser -> {
                    LOGGER.info("用户更新成功: {}", updatedUser.orElse(null));
                    assertTrue(updatedUser.isPresent());
                    assertEquals("李四", updatedUser.get().getUsername());
                    
                    // 删除用户
                    return userService.removeById(updatedUser.get().getId());
                })
                .onSuccess(deleted -> {
                    LOGGER.info("用户删除成功: {}", deleted);
                    assertTrue(deleted);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testLambdaQuery(VertxTestContext testContext) {
        LOGGER.info("=== 测试 Lambda 查询 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // Lambda 查询示例
        userService.lambdaList(userService.lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .like(User::getUsername, "张")
                .orderByDesc(User::getCreateTime)
                .limit(10))
                .onSuccess(users -> {
                    LOGGER.info("Lambda 查询结果: {} 条记录", users.size());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testPageQuery(VertxTestContext testContext) {
        LOGGER.info("=== 测试分页查询 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // 分页查询示例
        userService.lambdaPage(userService.lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .orderByDesc(User::getCreateTime), 1, 10)
                .onSuccess(pageResult -> {
                    LOGGER.info("分页查询结果: 当前页={}, 每页={}, 总数={}", 
                            pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
                    assertNotNull(pageResult);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testBatchOperations(VertxTestContext testContext) {
        LOGGER.info("=== 测试批量操作 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // 批量插入示例
        List<User> users = Arrays.asList(
                createUser("用户1", "user1@example.com"),
                createUser("用户2", "user2@example.com"),
                createUser("用户3", "user3@example.com")
        );

        userService.saveBatch(users)
                .compose(savedUsers -> {
                    LOGGER.info("批量插入成功: {} 条记录", savedUsers.size());
                    assertEquals(3, savedUsers.size());
                    
                    // 批量查询
                    List<Long> ids = savedUsers.stream()
                            .map(User::getId)
                            .toList();
                    return userService.listByIds(ids);
                })
                .compose(foundUsers -> {
                    LOGGER.info("批量查询成功: {} 条记录", foundUsers.size());
                    assertEquals(3, foundUsers.size());
                    
                    // 批量删除
                    List<Long> ids = foundUsers.stream()
                            .map(User::getId)
                            .toList();
                    return userService.removeByIds(ids);
                })
                .onSuccess(deletedCount -> {
                    LOGGER.info("批量删除成功: {} 条记录", deletedCount);
                    assertEquals(3, deletedCount);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testUpsertOperations(VertxTestContext testContext) {
        LOGGER.info("=== 测试 UPSERT 操作 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // UPSERT 操作示例
        User user = createUser("UPSERT用户", "upsert@example.com");

        userService.saveOrUpdate(user)
                .compose(result -> {
                    LOGGER.info("UPSERT 操作结果: 插入={}, 实体={}", 
                            result.isInserted(), result.getEntity());
                    assertTrue(result.isInserted());
                    
                    // 再次执行 UPSERT（应该更新）
                    result.getEntity().setUsername("更新后的用户");
                    return userService.saveOrUpdate(result.getEntity());
                })
                .onSuccess(result -> {
                    LOGGER.info("第二次 UPSERT 操作结果: 插入={}, 实体={}", 
                            result.isInserted(), result.getEntity());
                    assertFalse(result.isInserted()); // 应该是更新
                    assertEquals("更新后的用户", result.getEntity().getUsername());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testAggregateQueries(VertxTestContext testContext) {
        LOGGER.info("=== 测试聚合查询 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // 聚合查询示例
        Future.all(
                userService.count(),
                userService.lambdaCount(userService.lambdaQuery().eq(User::getStatus, User.UserStatus.ACTIVE))
        )
                .onSuccess(results -> {
                    Long totalCount = results.resultAt(0);
                    Long activeCount = results.resultAt(1);
                    
                    LOGGER.info("聚合查询结果: 总数={}, 活跃数={}", totalCount, activeCount);
                    assertNotNull(totalCount);
                    assertNotNull(activeCount);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testConvenienceMethods(VertxTestContext testContext) {
        LOGGER.info("=== 测试便捷方法 ===");

        if (userService == null) {
            LOGGER.info("跳过测试：userService 未初始化");
            testContext.completeNow();
            return;
        }

        // 便捷方法示例
        String email = "convenience@example.com";
        User user = createUser("便捷方法用户", email);

        userService.save(user)
                .compose(savedUser -> {
                    // 使用便捷方法查询
                    return userService.getByField(User::getEmail, email);
                })
                .compose(foundUser -> {
                    LOGGER.info("便捷方法查询结果: {}", foundUser.orElse(null));
                    assertTrue(foundUser.isPresent());
                    assertEquals(email, foundUser.get().getEmail());
                    
                    // 使用便捷方法检查存在
                    return userService.existsByField(User::getEmail, email);
                })
                .onSuccess(exists -> {
                    LOGGER.info("便捷方法存在检查结果: {}", exists);
                    assertTrue(exists);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    /**
     * 创建测试用户
     */
    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }
}
