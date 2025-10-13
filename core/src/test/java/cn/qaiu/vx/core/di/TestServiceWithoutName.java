package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;

/**
 * 测试用的Service实现（没有指定name）
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Service  // 没有指定name，应该使用类名首字母小写
public class TestServiceWithoutName implements TestServiceWithoutNameInterface {
    
    @Override
    public Future<String> process(String input) {
        return Future.succeededFuture("processed-" + input);
    }
}

/**
 * 测试服务接口（无名称）
 */
@io.vertx.codegen.annotations.ProxyGen
interface TestServiceWithoutNameInterface {
    Future<String> process(String input);
}
