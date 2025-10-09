package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import cn.qaiu.db.dsl.lambda.SFunction;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * 用户DAO - Lambda查询示例
 * 
 * @author qaiu
 */
public class UserDao extends LambdaDao<User, Long> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    /**
     * 根据用户名查询用户
     */
    public Future<Optional<User>> findByUsername(String username) {
        return lambdaOne(User::getUsername, username);
    }
    
    /**
     * 根据邮箱查询用户
     */
    public Future<Optional<User>> findByEmail(String email) {
        return lambdaOne(User::getEmail, email);
    }
    
    /**
     * 根据状态查询用户列表
     */
    public Future<List<User>> findByStatus(String status) {
        return lambdaList(User::getStatus, status);
    }
    
    /**
     * 根据年龄范围查询用户
     */
    public Future<List<User>> findByAgeRange(Integer minAge, Integer maxAge) {
        return lambdaList(lambdaQuery()
                .ge(User::getAge, minAge)
                .le(User::getAge, maxAge));
    }
    
    /**
     * 根据用户名模糊查询
     */
    public Future<List<User>> findByUsernameLike(String username) {
        return lambdaList(lambdaQuery()
                .like(User::getUsername, username));
    }
    
    /**
     * 查询已认证邮箱的用户
     */
    public Future<List<User>> findEmailVerifiedUsers() {
        return lambdaList(lambdaQuery()
                .eq(User::getEmailVerified, true));
    }
    
    /**
     * 查询余额大于指定金额的用户
     */
    public Future<List<User>> findUsersWithBalanceGreaterThan(Double balance) {
        return lambdaList(lambdaQuery()
                .gt(User::getBalance, balance));
    }
    
    /**
     * 复杂查询示例 - 多条件组合
     */
    public Future<List<User>> findActiveUsersWithHighBalance(Double minBalance) {
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, "ACTIVE")
                .ge(User::getBalance, minBalance)
                .eq(User::getEmailVerified, true)
                .orderByDesc(User::getBalance)
                .orderByAsc(User::getCreateTime));
    }
    
    /**
     * 嵌套条件查询示例
     */
    public Future<List<User>> findUsersWithComplexCondition(String status, Integer minAge, Double minBalance) {
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, status)
                .and(wrapper -> wrapper
                        .ge(User::getAge, minAge)
                        .or(subWrapper -> subWrapper
                                .ge(User::getBalance, minBalance)
                                .eq(User::getEmailVerified, true)))
                .orderByDesc(User::getCreateTime));
    }
    
    /**
     * 分页查询示例
     */
    public Future<LambdaPageResult<User>> findUsersByPage(long current, long size, String status) {
        LambdaQueryWrapper<User> wrapper = lambdaQuery()
                .eq(User::getStatus, status)
                .orderByDesc(User::getCreateTime);
        
        return lambdaPage(wrapper, current, size);
    }
    
    /**
     * 统计查询示例
     */
    public Future<Long> countActiveUsers() {
        return lambdaCount(lambdaQuery()
                .eq(User::getStatus, "ACTIVE"));
    }
    
    /**
     * 存在性检查示例
     */
    public Future<Boolean> existsByEmail(String email) {
        return lambdaExists(User::getEmail, email);
    }
    
    /**
     * 字段选择查询示例
     */
    public Future<List<User>> findUserBasicInfo() {
        return lambdaList(lambdaQuery()
                .select(User::getId, User::getUsername, User::getEmail, User::getStatus)
                .orderByAsc(User::getUsername));
    }
    
    /**
     * 批量更新示例 - 更新用户状态
     */
    public Future<Integer> updateUserStatus(List<Long> userIds, String newStatus) {
        return lambdaUpdate(lambdaQuery()
                .in(User::getId, userIds), 
                createUserWithStatus(newStatus));
    }
    
    /**
     * 批量删除示例 - 删除指定状态的用户
     */
    public Future<Integer> deleteUsersByStatus(String status) {
        return lambdaDelete(lambdaQuery()
                .eq(User::getStatus, status));
    }
    
    /**
     * 创建只有状态的用户对象（用于更新）
     */
    private User createUserWithStatus(String status) {
        User user = new User();
        user.setStatus(status);
        return user;
    }
}
