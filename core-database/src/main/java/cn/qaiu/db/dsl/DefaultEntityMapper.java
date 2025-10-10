package cn.qaiu.db.dsl;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认实体映射器实现
 * 
 * 基于 Vert.x CodeGen 风格的实体类进行映射
 * 支持各种数据类型的安全转换
 * 
 * @param <T> 实体类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DefaultEntityMapper<T extends BaseEntity> implements EntityMapper<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityMapper.class);
    
    private final Class<T> entityClass;
    private final Constructor<T> jsonConstructor;
    private volatile Method[] setters;
    
    /**
     * 构造函数
     * 
     * @param entityClass 实体类
     */
    public DefaultEntityMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        
        try {
            // 查找 JsonObject 构造函数
            this.jsonConstructor = entityClass.getConstructor(JsonObject.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                "Entity class " + entityClass.getName() + 
                " must have a constructor that takes JsonObject", e);
        }
        
        // 延迟初始化 getter/setter 方法
        initMethods();
    }
    
    /**
     * 初始化 getter/setter 方法
     */
    private void initMethods() {
        if (setters == null) {
            synchronized (this) {
                if (setters == null) {
                    setters = findSetterMethods();
                }
            }
        }
    }
    
    /**
     * 查找所有 setter 方法
     */
    private Method[] findSetterMethods() {
        Method[] methods = entityClass.getMethods();
        List<Method> setterList = new ArrayList<>();
        
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set") && 
                methodName.length() > 3 && 
                method.getParameterCount() == 1) {
                setterList.add(method);
            }
        }
        
        return setterList.toArray(new Method[0]);
    }
    
    @Override
    public T from(Row row) {
        try {
            // 使用 Vert.x 的 Row.toJson() 转换为 JsonObject
            JsonObject json = row.toJson();
            
            // 过滤掉不需要的字段（如序列化相关字段）
            JsonObject filteredJson = filterJsonForEntity(json);
            
            // 使用 JsonObject 构造函数创建实体
            T entity = jsonConstructor.newInstance(filteredJson);
            
            // Ver.x CodeGen 的 mapTo 方法可能更简单，但这里提供手动映射作为备选
            // T entity = json.mapTo(entityClass);
            
            LOGGER.debug("Mapped {} fields to {}: {}", filteredJson.size(), entityClass.getSimpleName(), 
                       filteredJson.fieldNames().stream().collect(java.util.stream.Collectors.toList()));
            return entity;
            
        } catch (Exception e) {
            LOGGER.error("Failed to map Row to {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从 JOQ Record 映射（用于复杂查询结果）
     * 
     * @param record JOQ Record
     * @return 实体对象
     */
    public T fromRecord(Record record) {
        try {
            // 将 JOQ Record 转换为 JsonObject
            JsonObject json = new JsonObject();
            
            for (Field<?> field : record.fields()) {
                Object value = record.get(field);
                String fieldName = field.getName();
                
                // 数据类型转换
                Object convertedValue = convertValue(value);
                json.put(fieldName, convertedValue);
            }
            
            // 创建实体对象
            T entity = jsonConstructor.newInstance(json);
            
            LOGGER.debug("Mapped JOQ Record to {}", entityClass.getSimpleName());
            return entity;
            
        } catch (Exception e) {
            LOGGER.error("Failed to map JOQ Record to {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 安全的值类型转换
     * 
     * @param value 原始值
     * @return 转换后的值
     */
    private Object convertValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // LocalDateTime 处理
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        
        // BigDecimal 处理
        if (value instanceof BigDecimal) {
            return value;
        }
        
        // 数字类型处理
        if (value instanceof Number) {
            return value;
        }
        
        // 字符串处理
        if (value instanceof String) {
            String strValue = (String) value;
            
            // 尝试解析为 LocalDateTime
            if (strValue.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                try {
                    return LocalDateTime.parse(strValue.replace(" ", "T"));
                } catch (Exception e) {
                    LOGGER.debug("Failed to parse LocalDateTime: {}", strValue);
                }
            }
            
            return strValue;
        }
        
        // 其他类型直接返回
        return value;
    }
    
    @Override
    public JsonObject toJson(T entity) {
        if (entity == null) {
            return null;
        }
        
        JsonObject json = entity.toJson();
        
        // 处理LocalDateTime序列化问题 - 转换为字符串
        JsonObject converted = new JsonObject();
        for (String key : json.fieldNames()) {
            Object value = json.getValue(key);
            if (value instanceof LocalDateTime) {
                converted.put(key, ((LocalDateTime) value).toString());
            } else {
                converted.put(key, value);
            }
        }
        
        return converted;
    }
    
    /**
     * 过滤JsonObject，移除不需要的字段
     * 
     * @param json 原始JsonObject
     * @return 过滤后的JsonObject
     */
    private JsonObject filterJsonForEntity(JsonObject json) {
        JsonObject filtered = new JsonObject();
        
        for (String key : json.fieldNames()) {
            // 过滤掉序列化相关的字段
            if (!key.equals("serialVersionUID") && 
                !key.equals("serial_version_u_i_d") &&
                !key.startsWith("$") && // 过滤掉内部字段
                !key.contains("serial")) { // 过滤掉包含serial的字段
                filtered.put(key, json.getValue(key));
            }
        }
        
        return filtered;
    }
    
    /**
     * 将JsonObject中的LocalDateTime转换为字符串
     * 
     * @param json 原始JsonObject
     * @return 转换后的JsonObject
     */
    private JsonObject convertLocalDateTimeToString(JsonObject json) {
        JsonObject converted = new JsonObject();
        
        for (String key : json.fieldNames()) {
            Object value = json.getValue(key);
            if (value instanceof LocalDateTime) {
                converted.put(key, ((LocalDateTime) value).toString());
            } else {
                converted.put(key, value);
            }
        }
        
        return converted;
    }
    
    /**
     * 批量映射从 RowSet
     * 
     * @param rowSet 数据库行集合
     * @return 实体列表
     */
    public List<T> fromRowSet(io.vertx.sqlclient.RowSet<io.vertx.sqlclient.Row> rowSet) {
        return fromMultiple(rowSet);
    }
    
    /**
     * 获取实体类
     * 
     * @return 实体类
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }
}
