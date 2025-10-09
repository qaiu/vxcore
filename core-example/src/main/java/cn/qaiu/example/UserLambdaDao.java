package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 用户Lambda DAO类
 * 继承LambdaDao，提供类型安全的Lambda查询功能
 * 
 * @author qaiu
 */
public class UserLambdaDao extends LambdaDao<User, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(UserLambdaDao.class);
    
    public UserLambdaDao(JooqExecutor executor) {
        super(executor, User.class);
        logger.debug("初始化UserLambdaDao");
    }
    
    /**
     * 根据用户名查询用户
     */
    public Future<Optional<User>> findByUsername(String username) {
        logger.debug("根据用户名查询: {}", username);
        return lambdaOne(User::getUsername, username);
    }
    
    /**
     * 根据邮箱查询用户
     */
    public Future<Optional<User>> findByEmail(String email) {
        logger.debug("根据邮箱查询: {}", email);
        return lambdaOne(User::getEmail, email);
    }
    
    /**
     * 查询活跃用户
     */
    public Future<List<User>> findActiveUsers() {
        logger.debug("查询活跃用户");
        return lambdaList(User::getStatus, User.UserStatus.ACTIVE);
    }
    
    /**
     * 查询指定年龄范围的用户
     */
    public Future<List<User>> findByAgeRange(int minAge, int maxAge) {
        logger.debug("查询年龄范围: {}-{}", minAge, maxAge);
        return lambdaList(lambdaQuery()
                .between(User::getAge, minAge, maxAge)
                .orderByAsc(User::getAge));
    }
    
    /**
     * 查询余额大于等于指定金额的用户
     */
    public Future<List<User>> findByMinBalance(BigDecimal minBalance) {
        logger.debug("查询余额大于等于: {}", minBalance);
        return lambdaList(lambdaQuery()
                .ge(User::getBalance, minBalance)
                .orderByDesc(User::getBalance));
    }
    
    /**
     * 查询邮箱已验证的用户
     */
    public Future<List<User>> findVerifiedUsers() {
        logger.debug("查询邮箱已验证的用户");
        return lambdaList(User::getEmailVerified, true);
    }
    
    /**
     * 模糊查询用户名
     */
    public Future<List<User>> findByUsernameLike(String keyword) {
        logger.debug("模糊查询用户名: {}", keyword);
        return lambdaList(lambdaQuery()
                .like(User::getUsername, "%" + keyword + "%")
                .orderByAsc(User::getUsername));
    }
    
    /**
     * 复杂条件查询：活跃且邮箱已验证且余额大于指定金额
     */
    public Future<List<User>> findActiveVerifiedRichUsers(BigDecimal minBalance) {
        logger.debug("查询活跃、已验证、余额大于{}的用户", minBalance);
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .eq(User::getEmailVerified, true)
                .ge(User::getBalance, minBalance)
                .orderByDesc(User::getBalance));
    }
    
    /**
     * 嵌套条件查询
     */
    public Future<List<User>> findComplexConditionUsers() {
        logger.debug("复杂嵌套条件查询");
        return lambdaList(lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .and(wrapper -> wrapper
                        .ge(User::getAge, 18)
                        .or(subWrapper -> subWrapper
                                .ge(User::getBalance, new BigDecimal("1000.00"))
                                .eq(User::getEmailVerified, true)))
                .orderByDesc(User::getCreateTime));
    }
    
    /**
     * 分页查询活跃用户
     */
    public Future<LambdaPageResult<User>> findActiveUsersByPage(long current, long size) {
        logger.debug("分页查询活跃用户: 第{}页, 每页{}条", current, size);
        return lambdaPage(lambdaQuery()
                .eq(User::getStatus, User.UserStatus.ACTIVE)
                .orderByDesc(User::getCreateTime), current, size);
    }
    
    /**
     * 统计查询
     */
    public Future<Long> countActiveUsers() {
        logger.debug("统计活跃用户数量");
        return lambdaCount(lambdaQuery().eq(User::getStatus, User.UserStatus.ACTIVE));
    }
    
    /**
     * 检查邮箱是否存在
     */
    public Future<Boolean> existsByEmail(String email) {
        logger.debug("检查邮箱是否存在: {}", email);
        return lambdaExists(User::getEmail, email);
    }
    
    /**
     * 字段选择查询：只查询基本信息
     */
    public Future<List<User>> findBasicInfo() {
        logger.debug("查询用户基本信息");
        return lambdaList(lambdaQuery()
                .select(User::getId, User::getUsername, User::getEmail, User::getStatus)
                .orderByAsc(User::getUsername));
    }
    
    /**
     * 批量更新用户状态
     */
    public Future<Integer> updateUsersStatus(List<Long> userIds, User.UserStatus status) {
        logger.debug("批量更新用户状态: {} -> {}", userIds, status);
        return lambdaUpdate(lambdaQuery().in(User::getId, userIds), 
                createUserWithStatus(status));
    }
    
    /**
     * 删除非活跃用户
     */
    public Future<Integer> deleteInactiveUsers() {
        logger.debug("删除非活跃用户");
        return lambdaDelete(lambdaQuery().ne(User::getStatus, User.UserStatus.ACTIVE));
    }
    
    /**
     * 创建用于更新的用户对象（只设置状态字段）
     */
    private User createUserWithStatus(User.UserStatus status) {
        User user = new User();
        user.setStatus(status);
        return user;
    }
}
