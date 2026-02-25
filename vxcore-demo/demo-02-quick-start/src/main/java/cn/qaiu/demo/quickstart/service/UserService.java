package cn.qaiu.demo.quickstart.service;

import cn.qaiu.demo.quickstart.entity.User;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * 验证 02-quick-start.md 第六步中的服务层
 * 使用内存存储模拟数据库操作
 */
public interface UserService {

    Future<List<User>> findAllUsers();

    Future<Optional<User>> findUserById(Long id);

    Future<User> createUser(User user);

    Future<User> updateUser(User user);

    Future<Boolean> deleteUser(Long id);

    Future<List<User>> searchUsers(String keyword);
}
