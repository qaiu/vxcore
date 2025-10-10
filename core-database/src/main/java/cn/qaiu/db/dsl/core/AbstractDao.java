package cn.qaiu.db.dsl.core;

import cn.qaiu.db.dsl.interfaces.JooqDao;
import cn.qaiu.db.dsl.mapper.EntityMapper;
import cn.qaiu.db.dsl.mapper.DefaultMapper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * 抽象DAO基类 - 基于真正的 jOOQ DSL
 * 充分利用 jOOQ 的类型安全和 DSL 功能
 */
public abstract class AbstractDao<T, ID> implements JooqDao<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDao.class);

    protected final JooqExecutor executor;
    protected final JooqDslBuilder dslBuilder;
    protected final EntityMapper<T> entityMapper;
    protected final Class<T> entityClass;
    protected final String tableName;
    protected final String primaryKeyField;

    public AbstractDao(JooqExecutor executor, Class<T> entityClass) {
        this.executor = executor;
        this.dslBuilder = new JooqDslBuilder(executor.dsl());
        this.entityClass = entityClass;
        this.tableName = dslBuilder.getTableName(entityClass);
        this.primaryKeyField = dslBuilder.getPrimaryKey(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
    }

    @Override
    public Future<Optional<T>> insert(T entity) {
        try {
            // 调用实体的onCreate方法
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onCreate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);

            // 移除主键字段（插入时通常由数据库自动生成）
            data.remove(cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(primaryKeyField));

            Query insertQuery = dslBuilder.buildInsert(tableName, data);

            return executor.executeInsert(insertQuery)
                    .map(generatedId -> {
                        // 设置生成的主键
                        setId(entity, generatedId);
                        LOGGER.debug("Inserted entity to table {} with ID: {}", tableName, generatedId);
                        return Optional.of(entity);
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to insert entity", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> update(T entity) {
        try {
            ID entityId = getId(entity);
            if (entityId == null) {
                LOGGER.warn("Entity ID is null, cannot update");
                return Future.failedFuture(new IllegalArgumentException("Entity ID cannot be null for update"));
            }

            LOGGER.debug("Updating entity: ID={}, class={}", entityId, entityClass.getSimpleName());

            // 调用实体的onUpdate方法
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onUpdate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);
            LOGGER.debug("Entity data for update: {}", data.encode());

            Condition whereCondition = DSL.field(primaryKeyField).eq(entityId);
            Query updateQuery = dslBuilder.buildUpdate(tableName, data, whereCondition);

            return executor.executeUpdate(updateQuery)
                    .map(rowCount -> {
                        LOGGER.debug("Update query affected {} rows", rowCount);
                        if (rowCount > 0) {
                            LOGGER.debug("Updated entity in table {} with ID: {}", tableName, entityId);
                            return Optional.of(entity);
                        } else {
                            LOGGER.warn("No rows affected by update query for ID: {}", entityId);
                            return Optional.<T>empty();
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to update entity", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Boolean> delete(ID id) {
        if (id == null) {
            return Future.failedFuture(new IllegalArgumentException("ID cannot be null"));
        }

        try {
            Condition whereCondition = DSL.field(primaryKeyField).eq(id);
            Query deleteQuery = dslBuilder.buildDelete(tableName, whereCondition);

            return executor.executeUpdate(deleteQuery)
                    .map(rowCount -> {
                        LOGGER.debug("Deleted {} rows from table {} where {} = {}", 
                                rowCount, tableName, primaryKeyField, id);
                        return rowCount > 0;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to delete entity with ID: {}", id, e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> findById(ID id) {
        if (id == null) {
            return Future.succeededFuture(Optional.empty());
        }

        try {
            Condition condition = DSL.field(primaryKeyField).eq(id);
            Query selectQuery = dslBuilder.buildSelect(tableName, condition);

            return executor.executeQuery(selectQuery)
                    .map(rowSet -> entityMapper.fromSingle(rowSet));
        } catch (Exception e) {
            LOGGER.error("Failed to find entity by ID: {}", id, e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<List<T>> findAll() {
        try {
            Query selectQuery = dslBuilder.buildSelect(tableName, DSL.noCondition());

            return executor.executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find all entities", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<List<T>> findByCondition(Condition condition) {
        try {
            Query selectQuery = dslBuilder.buildSelect(tableName, condition);

            return executor.executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find entities by condition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Long> count() {
        try {
            Query countQuery = dslBuilder.buildCount(tableName, DSL.noCondition());

            return executor.executeQuery(countQuery)
                    .map(rowSet -> {
                        if (rowSet.size() > 0) {
                            return rowSet.iterator().next().getLong(0);
                        }
                        return 0L;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to count entities", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Long> count(Condition condition) {
        try {
            Query countQuery = dslBuilder.buildCount(tableName, condition);

            return executor.executeQuery(countQuery)
                    .map(rowSet -> {
                        if (rowSet.size() > 0) {
                            return rowSet.iterator().next().getLong(0);
                        }
                        return 0L;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to count entities by condition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Boolean> exists(ID id) {
        return count(DSL.field(primaryKeyField).eq(id))
                .map(count -> count > 0);
    }

    @Override
    public Future<Boolean> exists(Condition condition) {
        return count(condition)
                .map(count -> count > 0);
    }

    /**
     * 获取实体的ID值
     */
    @SuppressWarnings("unchecked")
    protected ID getId(T entity) {
        try {
            // 尝试使用getId方法
            Method getIdMethod = entityClass.getMethod("getId");
            ID id = (ID) getIdMethod.invoke(entity);
            LOGGER.debug("Got ID from entity {}: {}", entityClass.getSimpleName(), id);
            return id;
        } catch (Exception e) {
            LOGGER.warn("Failed to get ID from entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置实体的ID值
     */
    protected void setId(T entity, Long id) {
        try {
            // 尝试使用setId方法
            Method setIdMethod = entityClass.getMethod("setId", Long.class);
            setIdMethod.invoke(entity, id);
        } catch (Exception e) {
            LOGGER.warn("Failed to set ID for entity", e);
        }
    }

    /**
     * 获取表名（供子类使用）
     */
    protected String getTableName() {
        return tableName;
    }

    /**
     * 获取主键字段名（供子类使用）
     */
    protected String getPrimaryKeyField() {
        return primaryKeyField;
    }

    /**
     * 获取jOOQ DSL上下文（供子类使用）
     */
    protected DSLContext dsl() {
        return executor.dsl();
    }
    
    // =================== 批量操作方法 ===================
    
    /**
     * 批量插入实体
     * 
     * @param entities 要插入的实体列表
     * @return 插入成功的实体数量
     */
    public Future<Integer> batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch inserting {} entities to table {}", entities.size(), tableName);
            
            // 调用实体的onCreate方法
            entities.forEach(entity -> {
                if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                    ((cn.qaiu.db.dsl.BaseEntity) entity).onCreate();
                }
            });
            
            // 构建批量插入查询
            List<Query> insertQueries = entities.stream()
                .map(entity -> {
                    JsonObject data = entityMapper.toJsonObject(entity);
                    // 移除主键字段
                    data.remove(cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(primaryKeyField));
                    return dslBuilder.buildInsert(tableName, data);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量插入
            return executor.executeBatch(insertQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int i = 0; i < results.length; i++) {
                        if (results[i] > 0) {
                            successCount++;
                            // 设置生成的主键（如果有）
                            if (i < entities.size()) {
                                setId(entities.get(i), (long) results[i]);
                            }
                        }
                    }
                    LOGGER.debug("Batch insert completed: {} entities inserted", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch insert entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量更新实体
     * 
     * @param entities 要更新的实体列表
     * @return 更新成功的实体数量
     */
    public Future<Integer> batchUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch updating {} entities in table {}", entities.size(), tableName);
            
            // 调用实体的onUpdate方法
            entities.forEach(entity -> {
                if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                    ((cn.qaiu.db.dsl.BaseEntity) entity).onUpdate();
                }
            });
            
            // 构建批量更新查询
            List<Query> updateQueries = entities.stream()
                .map(entity -> {
                    ID entityId = getId(entity);
                    if (entityId == null) {
                        throw new IllegalArgumentException("Entity ID cannot be null for batch update");
                    }
                    
                    JsonObject data = entityMapper.toJsonObject(entity);
                    Condition whereCondition = DSL.field(primaryKeyField).eq(entityId);
                    return dslBuilder.buildUpdate(tableName, data, whereCondition);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量更新
            return executor.executeBatch(updateQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int result : results) {
                        if (result > 0) {
                            successCount++;
                        }
                    }
                    LOGGER.debug("Batch update completed: {} entities updated", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch update entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量删除实体
     * 
     * @param ids 要删除的ID列表
     * @return 删除成功的实体数量
     */
    public Future<Integer> batchDelete(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch deleting {} entities from table {}", ids.size(), tableName);
            
            // 构建批量删除查询
            List<Query> deleteQueries = ids.stream()
                .map(id -> {
                    Condition whereCondition = DSL.field(primaryKeyField).eq(id);
                    return dslBuilder.buildDelete(tableName, whereCondition);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量删除
            return executor.executeBatch(deleteQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int result : results) {
                        if (result > 0) {
                            successCount++;
                        }
                    }
                    LOGGER.debug("Batch delete completed: {} entities deleted", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch delete entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量删除实体（根据条件）
     * 
     * @param condition 删除条件
     * @return 删除成功的实体数量
     */
    public Future<Integer> batchDeleteByCondition(Condition condition) {
        if (condition == null) {
            return Future.failedFuture(new IllegalArgumentException("Condition cannot be null"));
        }
        
        try {
            LOGGER.debug("Batch deleting entities from table {} with condition", tableName);
            
            Query deleteQuery = dslBuilder.buildDelete(tableName, condition);
            
            return executor.executeUpdate(deleteQuery)
                .map(rowCount -> {
                    LOGGER.debug("Batch delete by condition completed: {} entities deleted", rowCount);
                    return rowCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch delete entities by condition", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量插入或更新实体（UPSERT）
     * 如果实体ID存在则更新，否则插入
     * 
     * @param entities 要插入或更新的实体列表
     * @return 操作成功的实体数量
     */
    public Future<Integer> batchUpsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch upserting {} entities in table {}", entities.size(), tableName);
            
            // 分离需要插入和更新的实体
            List<T> toInsert = new java.util.ArrayList<>();
            List<T> toUpdate = new java.util.ArrayList<>();
            
            for (T entity : entities) {
                ID entityId = getId(entity);
                if (entityId == null) {
                    toInsert.add(entity);
                } else {
                    // 检查实体是否存在
                    exists(entityId)
                        .onSuccess(exists -> {
                            if (exists) {
                                toUpdate.add(entity);
                            } else {
                                toInsert.add(entity);
                            }
                        });
                }
            }
            
            // 执行批量插入和更新
            Future<Integer> insertFuture = batchInsert(toInsert);
            Future<Integer> updateFuture = batchUpdate(toUpdate);
            
            return Future.all(insertFuture, updateFuture)
                .map(compositeFuture -> {
                    Integer insertCount = insertFuture.result();
                    Integer updateCount = updateFuture.result();
                    int totalCount = (insertCount != null ? insertCount : 0) + 
                                   (updateCount != null ? updateCount : 0);
                    LOGGER.debug("Batch upsert completed: {} inserted, {} updated, total: {}", 
                               insertCount, updateCount, totalCount);
                    return totalCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch upsert entities", e);
            return Future.failedFuture(e);
        }
    }
}