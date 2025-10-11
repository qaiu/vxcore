package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JService;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务接口
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface UserService extends JService<User, Long> {

    /**
     * 查找活跃用户
     * 
     * @return 活跃用户列表
     */
    Future<List<User>> findActiveUsers();

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    Future<User> findByEmail(String email);

    /**
     * 根据用户名模糊查询
     * 
     * @param keyword 关键词
     * @return 用户列表
     */
    Future<List<User>> searchByName(String keyword);

    /**
     * 统计用户数量
     * 
     * @return 用户总数
     */
    Future<Long> countUsers();

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    Future<Boolean> existsByEmail(String email);

    /**
     * 更新用户余额
     * 
     * @param userId 用户ID
     * @param balance 新余额
     * @return 是否更新成功
     */
    Future<Boolean> updateUserBalance(Long userId, BigDecimal balance);

    /**
     * 验证用户邮箱
     * 
     * @param userId 用户ID
     * @return 是否验证成功
     */
    Future<Boolean> verifyUserEmail(Long userId);

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    Future<JsonObject> getUserStatistics();

    /**
     * 根据年龄范围获取用户
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    Future<List<User>> getUsersByAgeRange(Integer minAge, Integer maxAge);
}