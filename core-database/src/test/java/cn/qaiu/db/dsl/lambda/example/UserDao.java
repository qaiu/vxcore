package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * Lambda测试用的UserDao
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class UserDao extends LambdaDao<User, Long> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    /**
     * 创建Lambda查询包装器
     */
    public LambdaQueryWrapper<User> lambdaQuery() {
        return super.lambdaQuery();
    }
    
    /**
     * 根据年龄范围查找用户
     */
    public Future<List<User>> findByAgeRange(int minAge, int maxAge) {
        return lambdaQuery()
                .ge(User::getAge, minAge)
                .le(User::getAge, maxAge)
                .list();
    }
    
    /**
     * 查找活跃用户且余额大于指定值
     */
    public Future<List<User>> findActiveUsersWithHighBalance(double minBalance) {
        return lambdaQuery()
                .eq(User::getStatus, "ACTIVE")
                .ge(User::getBalance, minBalance)
                .eq(User::getEmailVerified, true)
                .list();
    }
    
    /**
     * 查找满足复杂条件的用户
     */
    public Future<List<User>> findUsersWithComplexCondition(String status, int minAge, double minBalance) {
        return lambdaQuery()
                .eq(User::getStatus, status)
                .and(wrapper -> wrapper
                        .ge(User::getAge, minAge)
                        .or(subWrapper -> subWrapper
                                .ge(User::getBalance, minBalance)
                                .eq(User::getEmailVerified, true)))
                .list();
    }
    
    /**
     * 分页查询用户
     */
    public Future<LambdaPageResult<User>> findUsersByPage(int page, int size, String status) {
        return lambdaPage(lambdaQuery()
                .eq(User::getStatus, status)
                .orderByDesc(User::getCreateTime), page, size);
    }
    
    /**
     * 统计活跃用户数量
     */
    public Future<Long> countActiveUsers() {
        return lambdaCount(User::getStatus, "ACTIVE");
    }
    
    /**
     * 检查邮箱是否存在
     */
    public Future<Boolean> existsByEmail(String email) {
        return lambdaExists(User::getEmail, email);
    }
    
    /**
     * 查询用户基本信息
     */
    public Future<List<User>> findUserBasicInfo() {
        return lambdaQuery()
                .select(User::getId, User::getUsername, User::getEmail, User::getAge)
                .eq(User::getStatus, "ACTIVE")
                .list();
    }
    
    /**
     * 批量更新用户状态
     */
    public Future<Integer> updateUserStatus(List<Long> userIds, String status) {
        // 创建一个临时用户对象用于更新
        User updateUser = new User();
        updateUser.setStatus(status);
        
        return lambdaUpdate(lambdaQuery().in(User::getId, userIds), updateUser);
    }
    
    /**
     * 插入用户
     */
    public Future<Optional<User>> insert(User user) {
        return super.insert(user);
    }
}
