package cn.qaiu.example.integration;

import cn.qaiu.example.SimpleExampleApplication;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库操作流程集成测试
 * 测试CRUD操作的完整流程
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("数据库操作流程集成测试")
class DatabaseOperationFlowIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseOperationFlowIntegrationTest.class);
    
    private Vertx vertx;
    private SimpleExampleApplication application;
    private UserDao userDao;
    private UserService userService;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        
        // 初始化应用
        this.application = new SimpleExampleApplication(vertx);
        
        // 启动应用
        vertx.deployVerticle(application)
                .onSuccess(deploymentId -> {
                    LOGGER.info("✅ Application started successfully, deployment ID: {}", deploymentId);
                    
                    // 获取DAO和服务实例
                    this.userDao = application.getUserDao();
                    this.userService = application.getUserService();
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Nested
    @DisplayName("基础CRUD操作测试")
    class BasicCrudTest {
        
        @Test
        @DisplayName("测试创建用户")
        void testCreateUser(VertxTestContext testContext) {
            LOGGER.info("Testing create user");
            
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user)
                    .onSuccess(savedUser -> {
                        testContext.verify(() -> {
                            assertNotNull(savedUser, "保存的用户不应为空");
                            assertNotNull(savedUser.getId(), "用户ID不应为空");
                            assertEquals("testuser", savedUser.getName(), "用户名应该正确");
                            assertEquals("testuser@example.com", savedUser.getEmail(), "邮箱应该正确");
                            assertEquals(25, savedUser.getAge(), "年龄应该正确");
                            assertEquals(User.UserStatus.ACTIVE, savedUser.getStatus(), "状态应该正确");
                            LOGGER.info("✅ Create user test passed: {}", savedUser.getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试查询用户")
        void testFindUser(VertxTestContext testContext) {
            LOGGER.info("Testing find user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("finduser");
            user.setEmail("finduser@example.com");
            user.setPassword("password123");
            user.setAge(30);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user)
                    .compose(savedUser -> {
                        // 根据ID查询用户
                        return userDao.findById(savedUser.getId());
                    })
                    .onSuccess(foundUser -> {
                        testContext.verify(() -> {
                            assertTrue(foundUser.isPresent(), "应该找到用户");
                            User user1 = foundUser.get();
                            assertEquals("finduser", user1.getName(), "用户名应该正确");
                            assertEquals("finduser@example.com", user1.getEmail(), "邮箱应该正确");
                            assertEquals(30, user1.getAge(), "年龄应该正确");
                            LOGGER.info("✅ Find user test passed: {}", user1.getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试更新用户")
        void testUpdateUser(VertxTestContext testContext) {
            LOGGER.info("Testing update user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("updateuser");
            user.setEmail("updateuser@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user)
                    .compose(savedUser -> {
                        // 更新用户信息
                        savedUser.setUsername("updateduser");
                        savedUser.setEmail("updated@example.com");
                        savedUser.setAge(35);
                        return userDao.update(savedUser);
                    })
                    .compose(updatedUser -> {
                        // 重新查询验证更新
                        return updatedUser.map(u -> userDao.findById(u.getId())).orElse(io.vertx.core.Future.succeededFuture(java.util.Optional.empty()));
                    })
                    .onSuccess(foundUser -> {
                        testContext.verify(() -> {
                            assertTrue(foundUser.isPresent(), "应该找到用户");
                            User user1 = foundUser.get();
                            assertEquals("updateduser", user1.getName(), "用户名应该已更新");
                            assertEquals("updated@example.com", user1.getEmail(), "邮箱应该已更新");
                            assertEquals(35, user1.getAge(), "年龄应该已更新");
                            LOGGER.info("✅ Update user test passed: {}", user1.getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试删除用户")
        void testDeleteUser(VertxTestContext testContext) {
            LOGGER.info("Testing delete user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("deleteuser");
            user.setEmail("deleteuser@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user)
                    .compose(savedUser -> {
                        // 删除用户
                        return userDao.deleteById(savedUser.getId());
                    })
                    .compose(deletedCount -> {
                        // 验证删除
                        return userDao.findById(user.getId());
                    })
                    .onSuccess(foundUser -> {
                        testContext.verify(() -> {
                            assertFalse(foundUser.isPresent(), "用户应该已被删除");
                            LOGGER.info("✅ Delete user test passed");
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("Lambda查询测试")
    class LambdaQueryTest {
        
        @Test
        @DisplayName("测试Lambda查询 - 根据状态查询")
        void testLambdaQueryByStatus(VertxTestContext testContext) {
            LOGGER.info("Testing Lambda query by status");
            
            // 先创建几个不同状态的用户
            User user1 = new User();
            user1.setUsername("activeuser1");
            user1.setEmail("active1@example.com");
            user1.setPassword("password123");
            user1.setAge(25);
            user1.setStatus(User.UserStatus.ACTIVE);
            
            User user2 = new User();
            user2.setUsername("inactiveuser1");
            user2.setEmail("inactive1@example.com");
            user2.setPassword("password123");
            user2.setAge(30);
            user2.setStatus(User.UserStatus.INACTIVE);
            
            userDao.save(user1)
                    .compose(v -> userDao.save(user2))
                    .compose(v -> {
                        // 使用Lambda查询查找活跃用户
                        return userDao.lambdaList(userDao.lambdaQuery()
                                .eq(User::getStatus, User.UserStatus.ACTIVE));
                    })
                    .onSuccess(activeUsers -> {
                        testContext.verify(() -> {
                            assertNotNull(activeUsers, "活跃用户列表不应为空");
                            assertTrue(activeUsers.size() >= 1, "应该至少有一个活跃用户");
                            
                            // 验证所有返回的用户都是活跃状态
                            for (User user : activeUsers) {
                                assertEquals(User.UserStatus.ACTIVE, user.getStatus(), "所有用户都应该是活跃状态");
                            }
                            
                            LOGGER.info("✅ Lambda query by status test passed: {} active users", activeUsers.size());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试Lambda查询 - 根据年龄范围查询")
        void testLambdaQueryByAgeRange(VertxTestContext testContext) {
            LOGGER.info("Testing Lambda query by age range");
            
            // 先创建几个不同年龄的用户
            User user1 = new User();
            user1.setUsername("younguser");
            user1.setEmail("young@example.com");
            user1.setPassword("password123");
            user1.setAge(20);
            user1.setStatus(User.UserStatus.ACTIVE);
            
            User user2 = new User();
            user2.setUsername("middleuser");
            user2.setEmail("middle@example.com");
            user2.setPassword("password123");
            user2.setAge(35);
            user2.setStatus(User.UserStatus.ACTIVE);
            
            User user3 = new User();
            user3.setUsername("olduser");
            user3.setEmail("old@example.com");
            user3.setPassword("password123");
            user3.setAge(50);
            user3.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user1)
                    .compose(v -> userDao.save(user2))
                    .compose(v -> userDao.save(user3))
                    .compose(v -> {
                        // 使用Lambda查询查找25-40岁的用户
                        return userDao.lambdaList(userDao.lambdaQuery()
                                .ge(User::getAge, 25)
                                .le(User::getAge, 40));
                    })
                    .onSuccess(middleAgeUsers -> {
                        testContext.verify(() -> {
                            assertNotNull(middleAgeUsers, "中年用户列表不应为空");
                            assertTrue(middleAgeUsers.size() >= 1, "应该至少有一个中年用户");
                            
                            // 验证所有返回的用户年龄都在25-40之间
                            for (User user : middleAgeUsers) {
                                assertTrue(user.getAge() >= 25 && user.getAge() <= 40, 
                                        "所有用户年龄应该在25-40之间，实际: " + user.getAge());
                            }
                            
                            LOGGER.info("✅ Lambda query by age range test passed: {} users", middleAgeUsers.size());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试Lambda查询 - 复杂条件查询")
        void testLambdaQueryComplexConditions(VertxTestContext testContext) {
            LOGGER.info("Testing Lambda query with complex conditions");
            
            // 先创建几个用户
            User user1 = new User();
            user1.setUsername("complexuser1");
            user1.setEmail("complex1@example.com");
            user1.setPassword("password123");
            user1.setAge(25);
            user1.setStatus(User.UserStatus.ACTIVE);
            
            User user2 = new User();
            user2.setUsername("complexuser2");
            user2.setEmail("complex2@example.com");
            user2.setPassword("password123");
            user2.setAge(30);
            user2.setStatus(User.UserStatus.ACTIVE);
            
            userDao.save(user1)
                    .compose(v -> userDao.save(user2))
                    .compose(v -> {
                        // 使用Lambda查询查找活跃且年龄大于20的用户
                        return userDao.lambdaList(userDao.lambdaQuery()
                                .eq(User::getStatus, User.UserStatus.ACTIVE)
                                .gt(User::getAge, 20)
                                .orderByDesc(User::getAge)
                                .limit(10));
                    })
                    .onSuccess(users -> {
                        testContext.verify(() -> {
                            assertNotNull(users, "用户列表不应为空");
                            assertTrue(users.size() >= 1, "应该至少有一个用户");
                            assertTrue(users.size() <= 10, "返回的用户数量不应超过10个");
                            
                            // 验证所有返回的用户都满足条件
                            for (User user : users) {
                                assertEquals(User.UserStatus.ACTIVE, user.getStatus(), "所有用户都应该是活跃状态");
                                assertTrue(user.getAge() > 20, "所有用户年龄应该大于20");
                            }
                            
                            // 验证排序（年龄降序）
                            for (int i = 1; i < users.size(); i++) {
                                assertTrue(users.get(i-1).getAge() >= users.get(i).getAge(), 
                                        "用户应该按年龄降序排列");
                            }
                            
                            LOGGER.info("✅ Complex Lambda query test passed: {} users", users.size());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试Lambda查询 - 分页查询")
        void testLambdaQueryPagination(VertxTestContext testContext) {
            LOGGER.info("Testing Lambda query pagination");
            
            // 先创建多个用户
            for (int i = 0; i < 5; i++) {
                User user = new User();
                user.setUsername("pageuser" + i);
                user.setEmail("page" + i + "@example.com");
                user.setPassword("password123");
                user.setAge(20 + i);
                user.setStatus(User.UserStatus.ACTIVE);
                
                userDao.save(user)
                        .onFailure(testContext::failNow);
            }
            
            // 等待所有用户创建完成
            vertx.setTimer(1000, id -> {
                // 测试分页查询
                userDao.lambdaList(userDao.lambdaQuery()
                        .eq(User::getStatus, User.UserStatus.ACTIVE)
                        .orderByAsc(User::getAge)
                        .page(1, 3)) // 第1页，每页3条
                        .onSuccess(users -> {
                            testContext.verify(() -> {
                                assertNotNull(users, "用户列表不应为空");
                                assertTrue(users.size() <= 3, "每页用户数量不应超过3个");
                                
                                LOGGER.info("✅ Pagination query test passed: {} users on page 1", users.size());
                            });
                            testContext.completeNow();
                        })
                        .onFailure(testContext::failNow);
            });
        }
    }
    
    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationTest {
        
        @Test
        @DisplayName("测试批量插入")
        void testBatchInsert(VertxTestContext testContext) {
            LOGGER.info("Testing batch insert");
            
            // 创建多个用户
            List<User> users = List.of(
                    createUser("batchuser1", "batch1@example.com", 25),
                    createUser("batchuser2", "batch2@example.com", 30),
                    createUser("batchuser3", "batch3@example.com", 35)
            );
            
            userDao.saveAll(users)
                    .onSuccess(savedUsers -> {
                        testContext.verify(() -> {
                            assertNotNull(savedUsers, "保存的用户列表不应为空");
                            assertEquals(3, savedUsers.size(), "应该保存3个用户");
                            
                            // 验证所有用户都有ID
                            for (User user : savedUsers) {
                                assertNotNull(user.getId(), "用户ID不应为空");
                            }
                            
                            LOGGER.info("✅ Batch insert test passed: {} users", savedUsers.size());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试批量更新")
        void testBatchUpdate(VertxTestContext testContext) {
            LOGGER.info("Testing batch update");
            
            // 先创建多个用户
            List<User> users = List.of(
                    createUser("batchupdate1", "batchupdate1@example.com", 25),
                    createUser("batchupdate2", "batchupdate2@example.com", 30),
                    createUser("batchupdate3", "batchupdate3@example.com", 35)
            );
            
            userDao.saveAll(users)
                    .compose(savedUsers -> {
                        // 更新所有用户的年龄
                        for (User user : savedUsers) {
                            user.setAge(user.getAge() + 10);
                        }
                        return userDao.updateAll(savedUsers);
                    })
                    .onSuccess(updateCount -> {
                        testContext.verify(() -> {
                            assertNotNull(updateCount, "更新计数不应为空");
                            assertEquals(3, updateCount.intValue(), "应该更新3个用户");
                            
                            LOGGER.info("✅ Batch update test passed: {} users", updateCount);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试批量删除")
        void testBatchDelete(VertxTestContext testContext) {
            LOGGER.info("Testing batch delete");
            
            // 先创建多个用户
            List<User> users = List.of(
                    createUser("batchdelete1", "batchdelete1@example.com", 25),
                    createUser("batchdelete2", "batchdelete2@example.com", 30),
                    createUser("batchdelete3", "batchdelete3@example.com", 35)
            );
            
            userDao.saveAll(users)
                    .compose(savedUsers -> {
                        // 提取用户ID
                        List<Long> userIds = savedUsers.stream()
                                .map(User::getId)
                                .toList();
                        return userDao.deleteAllById(userIds);
                    })
                    .onSuccess(deletedCount -> {
                        testContext.verify(() -> {
                            assertEquals(3, deletedCount, "应该删除3个用户");
                            LOGGER.info("✅ Batch delete test passed: {} users deleted", deletedCount);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("服务层集成测试")
    class ServiceLayerTest {
        
        @Test
        @DisplayName("测试服务层 - 创建用户")
        void testServiceCreateUser(VertxTestContext testContext) {
            LOGGER.info("Testing service layer create user");
            
            User user = new User();
            user.setUsername("serviceuser");
            user.setEmail("service@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userService.createUser(user.getName(), user.getEmail(), user.getPassword())
                    .onSuccess(createdUser -> {
                        testContext.verify(() -> {
                            assertNotNull(createdUser, "创建的用户不应为空");
                            assertNotNull(createdUser.getId(), "用户ID不应为空");
                            assertEquals("serviceuser", createdUser.getName(), "用户名应该正确");
                            LOGGER.info("✅ Service create user test passed: {}", createdUser.getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试服务层 - 查询用户")
        void testServiceFindUser(VertxTestContext testContext) {
            LOGGER.info("Testing service layer find user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("servicefinduser");
            user.setEmail("servicefind@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userService.createUser(user.getName(), user.getEmail(), user.getPassword())
                    .compose(createdUser -> {
                        // 通过服务层查询用户
                        return userService.findById(createdUser.getId());
                    })
                    .onSuccess(foundUser -> {
                        testContext.verify(() -> {
                            assertTrue(foundUser.isPresent(), "应该找到用户");
                            User user1 = foundUser.get();
                            assertEquals("servicefinduser", user1.getName(), "用户名应该正确");
                            LOGGER.info("✅ Service find user test passed: {}", user1.getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试服务层 - 更新用户")
        void testServiceUpdateUser(VertxTestContext testContext) {
            LOGGER.info("Testing service layer update user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("serviceupdateuser");
            user.setEmail("serviceupdate@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userService.createUser(user.getName(), user.getEmail(), user.getPassword())
                    .compose(createdUser -> {
                        // 更新用户信息
                        createdUser.setUsername("updatedserviceuser");
                        createdUser.setAge(30);
                        return userService.updateUser(createdUser);
                    })
                    .onSuccess(updatedUser -> {
                        testContext.verify(() -> {
                            assertNotNull(updatedUser, "更新的用户不应为空");
                            assertEquals("updatedserviceuser", updatedUser.get().getName(), "用户名应该已更新");
                            assertEquals(30, updatedUser.get().getAge(), "年龄应该已更新");
                            LOGGER.info("✅ Service update user test passed: {}", updatedUser.get().getId());
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试服务层 - 删除用户")
        void testServiceDeleteUser(VertxTestContext testContext) {
            LOGGER.info("Testing service layer delete user");
            
            // 先创建一个用户
            User user = new User();
            user.setUsername("servicedeleteuser");
            user.setEmail("servicedelete@example.com");
            user.setPassword("password123");
            user.setAge(25);
            user.setStatus(User.UserStatus.ACTIVE);
            
            userService.createUser(user.getName(), user.getEmail(), user.getPassword())
                    .compose(createdUser -> {
                        // 删除用户
                        return userService.deleteUser(createdUser.getId());
                    })
                    .onSuccess(deleted -> {
                        testContext.verify(() -> {
                            assertTrue(deleted, "用户应该已被删除");
                            LOGGER.info("✅ Service delete user test passed");
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {
        
        @Test
        @DisplayName("测试数据库操作性能")
        void testDatabasePerformance(VertxTestContext testContext) {
            LOGGER.info("Testing database operation performance");
            
            long startTime = System.currentTimeMillis();
            
            // 创建100个用户
            List<User> users = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                User user = new User();
                user.setUsername("perfuser" + i);
                user.setEmail("perf" + i + "@example.com");
                user.setPassword("password123");
                user.setAge(20 + (i % 50));
                user.setStatus(User.UserStatus.ACTIVE);
                users.add(user);
            }
            
            userDao.saveAll(users)
                    .onSuccess(savedUsers -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        
                        testContext.verify(() -> {
                            assertNotNull(savedUsers, "保存的用户列表不应为空");
                            assertEquals(100, savedUsers.size(), "应该保存100个用户");
                            assertTrue(duration < 5000, "批量插入100个用户应该在5秒内完成，实际: " + duration + "ms");
                            LOGGER.info("✅ Database performance test passed: {} users in {}ms", savedUsers.size(), duration);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试Lambda查询性能")
        void testLambdaQueryPerformance(VertxTestContext testContext) {
            LOGGER.info("Testing Lambda query performance");
            
            long startTime = System.currentTimeMillis();
            
            // 执行复杂的Lambda查询
            userDao.lambdaList(userDao.lambdaQuery()
                    .eq(User::getStatus, User.UserStatus.ACTIVE)
                    .gt(User::getAge, 20)
                    .orderByDesc(User::getAge)
                    .limit(50))
                    .onSuccess(users -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        
                        testContext.verify(() -> {
                            assertNotNull(users, "用户列表不应为空");
                            assertTrue(users.size() <= 50, "返回的用户数量不应超过50个");
                            assertTrue(duration < 1000, "Lambda查询应该在1秒内完成，实际: " + duration + "ms");
                            LOGGER.info("✅ Lambda query performance test passed: {} users in {}ms", users.size(), duration);
                        });
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    /**
     * 创建测试用户的辅助方法
     */
    private User createUser(String username, String email, int age) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setAge(age);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }
}
