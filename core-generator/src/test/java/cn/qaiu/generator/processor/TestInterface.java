package cn.qaiu.generator.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 测试接口 - 用于验证接口支持功能
 * 
 * 功能：
 * 1. 测试接口方法生成
 * 2. 测试泛型参数解析
 * 3. 测试父接口方法继承
 * 4. 验证 Future 返回类型生成
 * 
 * @author vxcore
 * @version 1.0
 */
@GenerateServiceGen(idType = Long.class, generateProxy = true)
public interface TestInterface {
    
    /**
     * 查找用户
     * @return Future 包装的用户信息
     */
    Future<JsonObject> findUser();
    
    /**
     * 查找所有用户
     * @return Future 包装的用户列表
     */
    Future<List<JsonObject>> findAllUsers();
    
    /**
     * 保存用户
     * @return Future 包装的保存结果
     */
    Future<Void> saveUser();
    
    /**
     * 删除用户
     * @return Future 包装的删除结果
     */
    Future<Boolean> deleteUser();
    
    /**
     * 更新用户
     * @return Future 包装的更新结果
     */
    Future<Integer> updateUser();
}
