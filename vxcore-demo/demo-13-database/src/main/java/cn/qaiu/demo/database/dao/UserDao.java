package cn.qaiu.demo.database.dao;

import cn.qaiu.demo.database.entity.User;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.SFunction;
import cn.qaiu.vx.core.annotations.Dao;

/**
 * 无参构造DAO验证 — 框架自动通过泛型获取User类型和JooqExecutor。
 * 继承 LambdaDao 以同时支持基础 CRUD 和 Lambda 查询。
 */
@Dao
public class UserDao extends LambdaDao<User, Long> {

    @Override
    protected <R> SFunction<User, R> getPrimaryKeyFieldLambda() {
        @SuppressWarnings("unchecked")
        SFunction<User, R> fn = (SFunction<User, R>) (SFunction<User, Long>) User::getId;
        return fn;
    }
}
