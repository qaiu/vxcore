package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;

/**
 * 测试用的Service实现
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Service(name = "testService")
public class TestService implements TestServiceInterface {

    @Override
    public Future<String> getValue(String key) {
        return Future.succeededFuture("test-value-" + key);
    }
}

/**
 * 测试服务接口
 */
@io.vertx.codegen.annotations.ProxyGen
interface TestServiceInterface {
    Future<String> getValue(String key);
}
