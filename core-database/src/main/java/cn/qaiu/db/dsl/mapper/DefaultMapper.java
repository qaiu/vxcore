package cn.qaiu.db.dsl.mapper;

import cn.qaiu.db.ddl.DdlColumn;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 使用完全限定名避免与jOOQ Field冲突
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 默认实体映射器实现
 * 使用反射自动映射实体类
 */
public class DefaultMapper<T> implements EntityMapper<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMapper.class);

    private final Class<T> entityClass;
    private final String tableName;

    public DefaultMapper(Class<T> entityClass) {
        this(entityClass, null);
    }

    public DefaultMapper(Class<T> entityClass, String tableName) {
        this.entityClass = entityClass;
        this.tableName = tableName;
    }

    @Override
    public T fromRow(Row row) {
        if (row == null) {
            return null;
        }

        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            // 获取所有字段，包括继承的字段
            java.lang.reflect.Field[] fields = getAllFields(entityClass);
            
            for (java.lang.reflect.Field field : fields) {
                // 跳过不应该映射到数据库的字段
                if (shouldSkipField(field)) {
                    continue;
                }
                
                String columnName = getColumnName(field);
                if (columnName != null) {
                    try {
                        field.setAccessible(true);
                        Object value = getValueFromRow(row, columnName, field.getType());
                        LOGGER.debug("Field {} -> Column {} -> Value: {}", field.getName(), columnName, value);
                        if (value != null) {
                            field.set(entity, value);
                        }
                    } catch (Exception e) {
                        // 列不存在，跳过
                        LOGGER.debug("Column {} not found in row: {}", columnName, e.getMessage());
                    }
                }
            }

            // 调用实体的回调方法
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onLoad();
            }

            return entity;
        } catch (Exception e) {
            LOGGER.error("Failed to map row to entity", e);
            return null;
        }
    }

    @Override
    public Optional<T> fromSingle(RowSet<Row> rowSet) {
        List<T> entities = fromMultiple(rowSet);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

    @Override
    public List<T> fromMultiple(RowSet<Row> rowSet) {
        List<T> entities = new ArrayList<>();
        for (Row row : rowSet) {
            T entity = fromRow(row);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    @Override
    public JsonObject toJsonObject(T entity) {
        try {
            JsonObject json = new JsonObject();

            java.lang.reflect.Field[] fields = getAllFields(entityClass);
            for (java.lang.reflect.Field field : fields) {
                // 跳过不应该映射到数据库的字段
                if (shouldSkipField(field)) {
                    continue;
                }
                
                String columnName = getColumnName(field);
                if (columnName != null) {
                    field.setAccessible(true);
                    Object value = field.get(entity);

                    LOGGER.debug("Field: {}, Column: {}, Value: {}", field.getName(), columnName, value);
                    
                    if (value != null) {
                        // 直接使用数据库字段名，不进行转换
                        json.put(columnName, convertToJsonValue(value));
                    }
                }
            }

            return json;
        } catch (Exception e) {
            LOGGER.error("Failed to convert entity to json", e);
            return new JsonObject();
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * 判断是否应该跳过某个字段（不映射到数据库）
     */
    private boolean shouldSkipField(java.lang.reflect.Field field) {
        String fieldName = field.getName();
        
        // 跳过序列化相关字段
        if ("serialVersionUID".equals(fieldName)) {
            return true;
        }
        
        // 跳过静态字段
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        
        // 跳过final字段（通常是常量）
        if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
            return true;
        }
        
        // 跳过transient字段
        if (java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取所有字段，包括继承的字段
     */
    private java.lang.reflect.Field[] getAllFields(Class<?> clazz) {
        java.util.Set<java.lang.reflect.Field> allFields = new java.util.LinkedHashSet<>();
        
        // 递归获取所有父类的字段
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            java.lang.reflect.Field[] declaredFields = currentClass.getDeclaredFields();
            for (java.lang.reflect.Field field : declaredFields) {
                // 使用LinkedHashSet自动去重，保持顺序
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return allFields.toArray(new java.lang.reflect.Field[0]);
    }

    /**
     * 获取字段对应的列名
     * 默认使用下划线命名，符合数据库字段命名规范
     */
    private String getColumnName(java.lang.reflect.Field field) {
        return cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(field);
    }

    /**
     * 从数据库行获取值并转换为目标类型
     */
    private Object getValueFromRow(Row row, String columnName, Class<?> targetType) {
        try {
            // 尝试多种字段名格式：原始名、大写、小写
            String[] possibleNames = {
                columnName,
                columnName.toUpperCase(),
                columnName.toLowerCase()
            };
            
            Object value = null;
            String actualColumnName = null;
            
            // 尝试不同的字段名
            for (String name : possibleNames) {
                try {
                    // 首先尝试获取原始值
                    Object rawValue = null;
                    
                    // 尝试不同的获取方法
                    try {
                        rawValue = row.getValue(name);
                    } catch (Exception e1) {
                        try {
                            rawValue = row.getString(name);
                        } catch (Exception e2) {
                            // 如果都失败，继续下一个字段名
                            continue;
                        }
                    }
                    
                    // 使用增强的类型映射器进行转换
                    value = cn.qaiu.db.dsl.core.EnhancedTypeMapper.convertToType(rawValue, targetType);
                    
                    // 如果成功获取到值，就认为找到了字段
                    actualColumnName = name;
                    break;
                } catch (Exception e) {
                    // 继续尝试下一个字段名
                    continue;
                }
            }
            
            if (actualColumnName != null) {
                LOGGER.debug("Found column {} as {} for field {}", columnName, actualColumnName, targetType.getSimpleName());
                return value;
            } else {
                LOGGER.debug("Column {} not found in any format", columnName);
                return null;
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to get value for column {} of type {}", columnName, targetType.getName(), e);
            return null;
        }
    }

    /**
     * 转换值为Json兼容的类型
     */
    private Object convertToJsonValue(Object value) {
        if (value instanceof LocalDateTime) {
            return value.toString().replace("T", " ");
        } else if (value instanceof Enum) {
            return value.toString();
        }
        return value;
    }

}