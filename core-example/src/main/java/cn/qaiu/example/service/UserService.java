package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JService;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.model.UserRegistrationRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 演示三层架构中的Service层
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface UserService extends JService<User, Long> {
    
    /**
     * 查找所有用户
     */
    Future<List<User>> findAllUsers();
    
    /**
     * 根据ID查找用户
     */
    Future<User> findUserById(Long id);
    
    /**
     * 根据用户名查找用户
     */
    Future<List<User>> findUsersByName(String name);
    
    /**
     * 创建用户
     */
    Future<User> createUser(User user);
    
    /**
     * 更新用户
     */
    Future<User> updateUser(User user);
    
    /**
     * 删除用户
     */
    Future<Boolean> deleteUser(Long id);
    
    /**
     * 批量创建用户
     */
    Future<List<User>> batchCreateUsers(List<User> users);
    
    /**
     * 获取用户统计信息
     */
    Future<JsonObject> getUserStatistics();
    
    /**
     * 用户注册
     * 包含完整的验证逻辑：邮箱格式、密码强度、用户名/邮箱唯一性等
     * 
     * @param request 注册请求
     * @return 注册成功的用户
     */
    Future<User> registerUser(UserRegistrationRequest request);
    
    // =================== 扩展方法 ===================
    
    /**
     * 查找活跃用户
     */
    Future<List<User>> findActiveUsers();
    
    /**
     * 根据邮箱查找用户
     */
    Future<User> findByEmail(String email);
    
    /**
     * 更新用户余额
     */
    Future<Boolean> updateUserBalance(Long id, BigDecimal balance);
    
    /**
     * 验证用户邮箱
     */
    Future<Boolean> verifyUserEmail(Long id);
    
    /**
     * 根据年龄范围获取用户
     */
    Future<List<User>> getUsersByAgeRange(Integer minAge, Integer maxAge);
    
    /**
     * 检查邮箱是否存在
     */
    Future<Boolean> existsByEmail(String email);
    
    /**
     * 统计用户总数
     */
    Future<Long> countUsers();
    
    /**
     * 根据用户名模糊查询
     */
    Future<List<User>> searchByName(String keyword);
}