package cn.qaiu.db.dsl.lambda;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;

/**
 * 简化的Service接口 - 专门用于Vert.x代理生成 只包含Vert.x @ProxyGen能够处理的方法
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface SimpleJService<T, ID> {

  // =================== 基础CRUD方法 ===================

  /**
   * 根据ID获取实体
   *
   * @param id 主键ID
   * @return 实体对象
   */
  Future<T> getById(ID id);

  /**
   * 获取所有实体
   *
   * @return 实体列表
   */
  Future<List<T>> getAll();

  /**
   * 分页查询实体
   *
   * @param page 页码
   * @param size 每页大小
   * @return 分页结果
   */
  Future<LambdaPageResult<T>> page(long page, long size);

  /**
   * 统计实体数量
   *
   * @return 数量
   */
  Future<Long> count();

  /**
   * 创建实体
   *
   * @param entity 实体信息
   * @return 创建的实体
   */
  Future<T> create(T entity);

  /**
   * 更新实体
   *
   * @param entity 实体信息
   * @return 是否更新成功
   */
  Future<Boolean> update(T entity);

  /**
   * 删除实体
   *
   * @param id 实体ID
   * @return 是否删除成功
   */
  Future<Boolean> delete(ID id);

  /**
   * 搜索实体
   *
   * @param keyword 关键词
   * @return 实体列表
   */
  Future<List<T>> search(String keyword);

  /**
   * 获取实体统计信息
   *
   * @return 统计信息
   */
  Future<JsonObject> getStatistics();
}
