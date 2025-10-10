package cn.qaiu.example.service;

import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 * 演示 VXCore 框架的业务逻辑处理
 * 
 * @author QAIU
 */
public class UserService {
    
    private final UserDao userDao;
    
    public UserService() {
        this.userDao = new UserDao();
    }
    
    /**
     * 创建用户
     */
    public Future<User> createUser(User user) {
        return userDao.save(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public Future<User> getUserById(Long id) {
        return userDao.getById(id);
    }
    
    /**
     * 根据邮箱获取用户
     */
    public Future<User> getUserByEmail(String email) {
        return userDao.findOneByEmail(email)
                .map(optional -> optional.orElse(null));
    }
    
    /**
     * 获取所有活跃用户
     */
    public Future<List<User>> getActiveUsers() {
        return userDao.findActiveUsers();
    }
    
    /**
     * 根据年龄范围获取用户
     */
    public Future<List<User>> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        return userDao.findByAgeRange(minAge, maxAge);
    }
    
    /**
     * 更新用户信息
     */
    public Future<Optional<User>> updateUser(User user) {
        return userDao.update(user);
    }
    
    /**
     * 删除用户
     */
    public Future<Boolean> deleteUser(Long id) {
        return userDao.deleteById(id);
    }
    
    /**
     * 更新用户余额
     */
    public Future<Boolean> updateUserBalance(Long userId, BigDecimal balance) {
        return userDao.updateBalance(userId, balance);
    }
    
    /**
     * 验证用户邮箱
     */
    public Future<Boolean> verifyUserEmail(Long userId) {
        return userDao.verifyEmail(userId);
    }
    
    /**
     * 更新用户状态
     */
    public Future<Boolean> updateUserStatus(Long userId, User.UserStatus status) {
        return userDao.updateStatus(userId, status);
    }
    
    /**
     * 获取用户统计信息
     */
    public Future<JsonObject> getUserStatistics() {
        return Future.all(
                userDao.countByStatus(User.UserStatus.ACTIVE),
                userDao.countByStatus(User.UserStatus.INACTIVE),
                userDao.countByStatus(User.UserStatus.SUSPENDED)
        ).map(result -> {
            JsonObject stats = new JsonObject();
            stats.put("active", result.resultAt(0));
            stats.put("inactive", result.resultAt(1));
            stats.put("suspended", result.resultAt(2));
            return stats;
        });
    }
    
    /**
     * 获取高余额用户
     */
    public Future<List<User>> getHighBalanceUsers(BigDecimal minBalance) {
        return userDao.findByBalanceGreaterThan(minBalance);
    }
}
