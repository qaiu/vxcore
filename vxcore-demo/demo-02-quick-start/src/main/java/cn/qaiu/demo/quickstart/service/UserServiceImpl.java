package cn.qaiu.demo.quickstart.service;

import cn.qaiu.demo.quickstart.entity.User;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 用户服务实现 (内存存储)
 * 
 * 验证 02-quick-start.md 中的服务层逻辑
 * 使用 ConcurrentHashMap 模拟数据库
 */
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserServiceImpl() {
        // 预置测试数据
        User demo = new User("demo-user", "demo@example.com");
        demo.setId(idGen.getAndIncrement());
        demo.setCreateTime(LocalDateTime.now().format(FMT));
        demo.setUpdateTime(LocalDateTime.now().format(FMT));
        userStore.put(demo.getId(), demo);
    }

    @Override
    public Future<List<User>> findAllUsers() {
        log.info("findAllUsers, count={}", userStore.size());
        return Future.succeededFuture(new ArrayList<>(userStore.values()));
    }

    @Override
    public Future<Optional<User>> findUserById(Long id) {
        log.info("findUserById: {}", id);
        return Future.succeededFuture(Optional.ofNullable(userStore.get(id)));
    }

    @Override
    public Future<User> createUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
        }
        user.setId(idGen.getAndIncrement());
        user.setCreateTime(LocalDateTime.now().format(FMT));
        user.setUpdateTime(LocalDateTime.now().format(FMT));
        userStore.put(user.getId(), user);
        log.info("createUser: {}", user);
        return Future.succeededFuture(user);
    }

    @Override
    public Future<User> updateUser(User user) {
        if (user.getId() == null || !userStore.containsKey(user.getId())) {
            return Future.failedFuture(new IllegalArgumentException("用户不存在"));
        }
        user.setUpdateTime(LocalDateTime.now().format(FMT));
        userStore.put(user.getId(), user);
        log.info("updateUser: {}", user);
        return Future.succeededFuture(user);
    }

    @Override
    public Future<Boolean> deleteUser(Long id) {
        User removed = userStore.remove(id);
        log.info("deleteUser: {}, found={}", id, removed != null);
        return Future.succeededFuture(removed != null);
    }

    @Override
    public Future<List<User>> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllUsers();
        }
        List<User> result = userStore.values().stream()
                .filter(u -> u.getName() != null && u.getName().contains(keyword))
                .collect(Collectors.toList());
        log.info("searchUsers: keyword={}, found={}", keyword, result.size());
        return Future.succeededFuture(result);
    }
}
