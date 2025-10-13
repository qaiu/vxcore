package cn.qaiu.example.integration;

import cn.qaiu.example.controller.UserController;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.service.UserServiceImpl;
import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 三层架构集成测试
 * 测试Controller -> Service -> DAO 完整流程
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("三层架构集成测试")
public class ThreeLayerIntegrationTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeLayerIntegrationTest.class);
    
    private VXCoreApplication application;
    private Vertx vertx;
    private HttpClient httpClient;
    private UserController userController;
    private UserServiceImpl userService;
    private UserDao userDao;
    
    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.application = new VXCoreApplication();
        this.httpClient = vertx.createHttpClient();
        
        // 初始化三层组件
        this.userDao = new UserDao();
        this.userService = new UserServiceImpl();
        this.userController = new UserController();
        
        // 启动应用
        application.start(new String[]{"test"}, config -> {
            LOGGER.info("Test application started");
        }).onSuccess(v -> {
            testContext.completeNow();
        }).onFailure(testContext::failNow);
    }
    
    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown(VertxTestContext testContext) {
        if (httpClient != null) {
            httpClient.close();
        }
        
        if (application != null) {
            application.stop()
                .onSuccess(v -> {
                    LOGGER.info("Test application stopped");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }
    
    /**
     * 创建User对象的辅助方法
     */
    private User createUser(String username, String email, Integer age) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
    
    @Test
    @DisplayName("测试DAO层 - 基础CRUD操作")
    void testDaoLayerBasicCrud(VertxTestContext testContext) {
        // 测试查找所有用户
        userDao.findAll()
            .compose(users -> {
                testContext.verify(() -> {
                    assertNotNull(users, "用户列表不应为空");
                    assertTrue(users.size() >= 3, "应该有至少3个测试用户");
                    LOGGER.info("Found {} users", users.size());
                });
                
                // 测试根据ID查找用户
                return userDao.findById(1L);
            })
            .compose(userOpt -> {
                testContext.verify(() -> {
                    assertTrue(userOpt.isPresent(), "用户应该存在");
                    User user = userOpt.get();
                    assertEquals(1L, user.getId(), "用户ID应该是1");
                    assertEquals("张三", user.getUsername(), "用户名应该是张三");
                    LOGGER.info("Found user: {}", user);
                });
                
                // 测试创建新用户
                User newUser = new User();
                newUser.setUsername("测试用户");
                newUser.setEmail("test@example.com");
                newUser.setAge(25);
                return userDao.save(newUser);
            })
            .compose(savedUser -> {
                testContext.verify(() -> {
                    assertNotNull(savedUser.getId(), "保存的用户应该有ID");
                    assertEquals("测试用户", savedUser.getUsername(), "用户名应该正确");
                    LOGGER.info("Created user: {}", savedUser);
                });
                
                // 测试更新用户
                savedUser.setAge(26);
                return userDao.update(savedUser);
            })
            .compose(updatedUserOpt -> {
                testContext.verify(() -> {
                    assertTrue(updatedUserOpt.isPresent(), "更新后的用户应该存在");
                    User updatedUser = updatedUserOpt.get();
                    assertEquals(26, updatedUser.getAge(), "年龄应该已更新");
                    LOGGER.info("Updated user: {}", updatedUser);
                });
                
                // 测试删除用户
                User updatedUser = updatedUserOpt.get();
                return userDao.deleteById(updatedUser.getId());
            })
            .onSuccess(deleted -> {
                testContext.verify(() -> {
                    assertTrue(deleted, "删除应该成功");
                    LOGGER.info("Deleted user successfully");
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试Service层 - 业务逻辑")
    void testServiceLayerBusinessLogic(VertxTestContext testContext) {
        // 测试查找所有用户
        userService.findAllUsers()
            .compose(users -> {
                testContext.verify(() -> {
                    assertNotNull(users, "用户列表不应为空");
                    assertTrue(users.size() >= 3, "应该有至少3个用户");
                    LOGGER.info("Service found {} users", users.size());
                });
                
                // 测试创建用户
                User newUser = new User();
                newUser.setUsername("服务测试用户");
                newUser.setEmail("service@example.com");
                newUser.setAge(30);
                return userService.createUser(newUser);
            })
            .compose(createdUser -> {
                testContext.verify(() -> {
                    assertNotNull(createdUser.getId(), "创建的用户应该有ID");
                    assertEquals("服务测试用户", createdUser.getUsername(), "用户名应该正确");
                    LOGGER.info("Service created user: {}", createdUser);
                });
                
                // 测试根据用户名搜索
                return userService.findUsersByName("服务");
            })
            .compose(searchedUsers -> {
                testContext.verify(() -> {
                    assertNotNull(searchedUsers, "搜索结果不应为空");
                    assertTrue(searchedUsers.size() >= 1, "应该找到至少1个用户");
                    LOGGER.info("Service found {} users by name", searchedUsers.size());
                });
                
                // 测试批量创建用户
                List<User> batchUsers = List.of(
                    createUser("批量用户1", "batch1@example.com", 25),
                    createUser("批量用户2", "batch2@example.com", 26),
                    createUser("批量用户3", "batch3@example.com", 27)
                );
                return userService.batchCreateUsers(batchUsers);
            })
            .onSuccess(batchCreatedUsers -> {
                testContext.verify(() -> {
                    assertNotNull(batchCreatedUsers, "批量创建结果不应为空");
                    assertEquals(3, batchCreatedUsers.size(), "应该创建3个用户");
                    LOGGER.info("Service batch created {} users", batchCreatedUsers.size());
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试Service层 - 业务验证")
    void testServiceLayerValidation(VertxTestContext testContext) {
        // 测试创建用户时缺少用户名
        User invalidUser1 = new User();
        invalidUser1.setEmail("test@example.com");
        
        userService.createUser(invalidUser1)
            .onSuccess(user -> {
                testContext.failNow("应该失败");
            })
            .onFailure(error -> {
                testContext.verify(() -> {
                    assertTrue(error.getMessage().contains("用户名不能为空"), 
                              "应该返回用户名不能为空的错误");
                    LOGGER.info("Validation error as expected: {}", error.getMessage());
                });
                
                // 测试创建用户时缺少邮箱
                User invalidUser2 = new User();
                invalidUser2.setUsername("测试用户");
                
                userService.createUser(invalidUser2)
                    .onSuccess(user -> {
                        testContext.failNow("应该失败");
                    })
                    .onFailure(error2 -> {
                        testContext.verify(() -> {
                            assertTrue(error2.getMessage().contains("邮箱不能为空"), 
                                      "应该返回邮箱不能为空的错误");
                            LOGGER.info("Validation error as expected: {}", error2.getMessage());
                            testContext.completeNow();
                        });
                    });
            });
    }
    
    @Test
    @DisplayName("测试Controller层 - HTTP接口")
    void testControllerLayerHttpInterface(VertxTestContext testContext) {
        // 测试获取所有用户
        userController.getAllUsers()
            .compose(result -> {
                testContext.verify(() -> {
                    assertNotNull(result, "结果不应为空");
                    assertTrue(result.getSuccess(), "应该成功");
                    assertNotNull(result.getData(), "数据不应为空");
                    LOGGER.info("Controller got all users: {}", result);
                });
                
                // 测试根据ID获取用户
                return userController.getUserById(1L);
            })
            .compose(result -> {
                testContext.verify(() -> {
                    assertNotNull(result, "结果不应为空");
                    assertTrue(result.getSuccess(), "应该成功");
                    assertNotNull(result.getData(), "数据不应为空");
                    LOGGER.info("Controller got user by id: {}", result);
                });
                
                // 测试创建用户
                User newUser = createUser("控制器测试用户", "controller@example.com", 28);
                return userController.createUser(newUser);
            })
            .compose(result -> {
                testContext.verify(() -> {
                    assertNotNull(result, "结果不应为空");
                    assertTrue(result.getSuccess(), "应该成功");
                    assertNotNull(result.getData(), "数据不应为空");
                    LOGGER.info("Controller created user: {}", result);
                });
                
                // 测试搜索用户
                return userController.searchUsers("控制器");
            })
            .onSuccess(result -> {
                testContext.verify(() -> {
                    assertNotNull(result, "结果不应为空");
                    assertTrue(result.getSuccess(), "应该成功");
                    assertNotNull(result.getData(), "数据不应为空");
                    LOGGER.info("Controller searched users: {}", result);
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试完整业务流程")
    void testCompleteBusinessFlow(VertxTestContext testContext) {
        // 1. 创建用户
        User newUser = createUser("完整流程测试", "complete@example.com", 30);
        
        userController.createUser(newUser)
            .compose(createResult -> {
                testContext.verify(() -> {
                    assertTrue(createResult.getSuccess(), "创建用户应该成功");
                    LOGGER.info("Step 1 - Created user: {}", createResult);
                });
                
                // 2. 获取用户详情
                User createdUser = createResult.getData();
                return userController.getUserById(createdUser.getId());
            })
            .compose(getResult -> {
                testContext.verify(() -> {
                    assertTrue(getResult.getSuccess(), "获取用户应该成功");
                    LOGGER.info("Step 2 - Got user: {}", getResult);
                });
                
                // 3. 更新用户
                User userToUpdate = getResult.getData();
                userToUpdate.setAge(31);
                return userController.updateUser(userToUpdate.getId(), userToUpdate);
            })
            .compose(updateResult -> {
                testContext.verify(() -> {
                    assertTrue(updateResult.getSuccess(), "更新用户应该成功");
                    LOGGER.info("Step 3 - Updated user: {}", updateResult);
                });
                
                // 4. 搜索用户
                return userController.searchUsers("完整流程");
            })
            .compose(searchResult -> {
                testContext.verify(() -> {
                    assertTrue(searchResult.getSuccess(), "搜索用户应该成功");
                    assertNotNull(searchResult.getData(), "搜索结果不应为空");
                    LOGGER.info("Step 4 - Searched users: {}", searchResult);
                });
                
                // 5. 删除用户
                List<User> searchResults = searchResult.getData();
                User userToDelete = searchResults.get(0);
                return userController.deleteUser(userToDelete.getId());
            })
            .onSuccess(deleteResult -> {
                testContext.verify(() -> {
                    assertTrue(deleteResult.getSuccess(), "删除用户应该成功");
                    LOGGER.info("Step 5 - Deleted user: {}", deleteResult);
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling(VertxTestContext testContext) {
        // 测试获取不存在的用户
        userController.getUserById(999L)
            .onSuccess(result -> {
                testContext.verify(() -> {
                    assertFalse(result.getSuccess(), "应该失败");
                    assertEquals(404, result.getCode(), "应该返回404错误");
                    LOGGER.info("Error handling test - User not found: {}", result);
                });
                
                // 测试创建无效用户
                User invalidUser = new User();
                userController.createUser(invalidUser)
                    .onSuccess(createResult -> {
                        testContext.failNow("应该失败");
                    })
                    .onFailure(error -> {
                        testContext.verify(() -> {
                            LOGGER.info("Error handling test - Invalid user creation failed as expected: {}", error.getMessage());
                            testContext.completeNow();
                        });
                    });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试并发操作")
    void testConcurrentOperations(VertxTestContext testContext) {
        // 并发创建多个用户
        List<Future<JsonResult<User>>> futures = List.of(
            userController.createUser(createUser("并发用户1", "concurrent1@example.com", 25)),
            userController.createUser(createUser("并发用户2", "concurrent2@example.com", 26)),
            userController.createUser(createUser("并发用户3", "concurrent3@example.com", 27)),
            userController.createUser(createUser("并发用户4", "concurrent4@example.com", 28)),
            userController.createUser(createUser("并发用户5", "concurrent5@example.com", 29))
        );
        
        Future.all(futures)
            .onSuccess(results -> {
                testContext.verify(() -> {
                    assertEquals(5, results.size(), "应该创建5个用户");
                    
                    for (int i = 0; i < results.size(); i++) {
                        JsonResult<User> result = results.resultAt(i);
                        assertTrue(result.getSuccess(), "第" + (i+1) + "个用户创建应该成功");
                        LOGGER.info("Concurrent user {} created: {}", i+1, result);
                    }
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
}