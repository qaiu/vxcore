package cn.qaiu.demo.database.service;

import cn.qaiu.demo.database.dao.UserDao;
import cn.qaiu.demo.database.entity.User;
import cn.qaiu.vx.core.annotations.Service;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao = new UserDao();

    public Future<Optional<User>> createUser(User user) {
        return userDao.insert(user);
    }

    public Future<Optional<User>> findById(Long id) {
        return userDao.findById(id);
    }

    public Future<List<User>> findAll() {
        return userDao.findAll();
    }

    public Future<Optional<User>> updateUser(User user) {
        return userDao.update(user);
    }

    public Future<Boolean> deleteUser(Long id) {
        return userDao.delete(id);
    }

    /**
     * Lambda 查询：按名称模糊搜索，按 ID 降序排列
     */
    public Future<List<User>> searchByName(String name) {
        return userDao.lambdaQuery()
                .like(User::getName, name)
                .orderByDesc(User::getId)
                .list();
    }

    /**
     * Lambda 查询：按状态精确匹配
     */
    public Future<List<User>> findByStatus(String status) {
        return userDao.lambdaQuery()
                .eq(User::getStatus, status)
                .list();
    }
}
