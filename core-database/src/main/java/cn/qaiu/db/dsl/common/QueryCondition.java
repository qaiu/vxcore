package cn.qaiu.db.dsl.common;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jooq.Condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件构建器
 * 
 * 专为复杂查询设计，支持多种查询条件的组合
 * 提供统一的API来构建jOOQ Condition对象
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject(generateConverter = true)
public class QueryCondition implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 查询条件类型
     */
    public enum ConditionType {
        EQUAL("="),           // 等于
        NOT_EQUAL("!="),      // 不等于
        GREATER_THAN(">"),    // 大于
        GREATER_THAN_EQ(">="), // 大于等于
        LESS_THAN("<"),       // 小于
        LESS_THAN_EQ("<="),   // 小于等于
        LIKE("LIKE"),         // 模糊查询
        NOT_LIKE("NOT LIKE"), // 不匹配
        IN("IN"),             // 在集合中
        NOT_IN("NOT IN"),     // 不在集合中
        BETWEEN("BETWEEN"),   // 范围查询
        NOT_BETWEEN("NOT BETWEEN"), // 不在范围内
        IS_NULL("IS NULL"),   // 为空
        IS_NOT_NULL("IS NOT NULL"), // 不为空
        AND("AND"),           // 逻辑与
        OR("OR");             // 逻辑或
        
        private final String operator;
        
        ConditionType(String operator) {
            this.operator = operator;
        }
        
        public String getOperator() {
            return operator;
        }
    }
    
    /**
     * 条件组 - 支持嵌套条件
     */
    public static class ConditionGroup {
        private ConditionType connective; // 连接符（AND/OR）
        private List<QueryCondition> conditions;
        
        public ConditionGroup(ConditionType connective) {
            this.connective = connective;
            this.conditions = new ArrayList<>();
        }
        
        public ConditionGroup addCondition(QueryCondition condition) {
            conditions.add(condition);
            return this;
        }
        
        public ConditionGroup addCondition(String field, ConditionType type, Object value) {
            conditions.add(new QueryCondition(field, type, value));
            return this;
        }
        
        public ConditionGroup addBetweenCondition(String field, Object startValue, Object endValue) {
            return addCondition(field, ConditionType.BETWEEN, new Object[]{startValue, endValue});
        }
        
        public ConditionGroup addInCondition(String field, Object... values) {
            return addCondition(field, ConditionType.IN, values);
        }
        
        public ConditionGroup addLikeCondition(String field, String pattern) {
            return addCondition(field, ConditionType.LIKE, pattern);
        }
        
        // Getter methods
        public ConditionType getConnective() { return connective; }
        public List<QueryCondition> getConditions() { return conditions; }
    }
    
    /**
     * 字段名
     */
    private String field;
    
    /**
     * 条件类型
     */
    private ConditionType type;
    
    /**
     * 值（可以是单个值、数组或集合）
     */
    private Object value;
    
    /**
     * 子条件（用于嵌套条件）
     */
    private List<ConditionGroup> groups;
    
    /**
     * 默认构造函数
     */
    public QueryCondition() {
        this.groups = new ArrayList<>();
    }
    
    /**
     * 构造函数
     * 
     * @param field 字段名
     * @param type 条件类型
     * @param value 值
     */
    public QueryCondition(String field, ConditionType type, Object value) {
        this();
        this.field = field;
        this.type = type;
        this.value = value;
    }
    
    /**
     * JsonObject构造函数
     * 
     * @param json JSON对象
     */
    public QueryCondition(JsonObject json) {
        this.field = json.getString("field");
        this.type = ConditionType.valueOf(json.getString("type"));
        this.value = json.getValue("value");
        
        List<JsonObject> groupJsons = json.getJsonArray("groups") != null ? 
                json.getJsonArray("groups").getList() : new ArrayList<>();
        this.groups = new ArrayList<>();
        for (JsonObject groupJson : groupJsons) {
            // 简化处理，实际需要递归构造
            this.groups.add(new ConditionGroup(ConditionType.valueOf(groupJson.getString("connective"))));
        }
    }
    
    /**
     * 工厂方法：创建等于条件
     */
    public static QueryCondition eq(String field, Object value) {
        return new QueryCondition(field, ConditionType.EQUAL, value);
    }
    
    /**
     * 工厂方法：创建不等于条件
     */
    public static QueryCondition ne(String field, Object value) {
        return new QueryCondition(field, ConditionType.NOT_EQUAL, value);
    }
    
    /**
     * 工厂方法：创建大于条件
     */
    public static QueryCondition gt(String field, Object value) {
        return new QueryCondition(field, ConditionType.GREATER_THAN, value);
    }
    
    /**
     * 工厂方法：创建大于等于条件
     */
    public static QueryCondition gte(String field, Object value) {
        return new QueryCondition(field, ConditionType.GREATER_THAN_EQ, value);
    }
    
    /**
     * 工厂方法：创建小于条件
     */
    public static QueryCondition lt(String field, Object value) {
        return new QueryCondition(field, ConditionType.LESS_THAN, value);
    }
    
    /**
     * 工厂方法：创建小于等于条件
     */
    public static QueryCondition lte(String field, Object value) {
        return new QueryCondition(field, ConditionType.LESS_THAN_EQ, value);
    }
    
    /**
     * 工厂方法：创建模糊查询条件
     */
    public static QueryCondition like(String field, String pattern) {
        return new QueryCondition(field, ConditionType.LIKE, pattern);
    }
    
    /**
     * 工厂方法：创建IN条件
     */
    public static QueryCondition in(String field, Object... values) {
        return new QueryCondition(field, ConditionType.IN, values);
    }
    
    /**
     * 工厂方法：创建范围条件
     */
    public static QueryCondition between(String field, Object startValue, Object endValue) {
        return new QueryCondition(field, ConditionType.BETWEEN, new Object[]{startValue, endValue});
    }
    
    /**
     * 工厂方法：创建空值条件
     */
    public static QueryCondition isNull(String field) {
        return new QueryCondition(field, ConditionType.IS_NULL, null);
    }
    
    /**
     * 工厂方法：创建非空值条件
     */
    public static QueryCondition isNotNull(String field) {
        return new QueryCondition(field, ConditionType.IS_NOT_NULL, null);
    }
    
    /**
     * 添加条件组
     */
    public QueryCondition addGroup(ConditionGroup group) {
        groups.add(group);
        return this;
    }
    
    /**
     * 转换为JsonObject
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("field", field)
                .put("type", type)
                .put("value", value);
        
        if (!groups.isEmpty()) {
            JsonArray groupsJson = new JsonArray();
            for (ConditionGroup group : groups) {
                JsonObject groupJson = new JsonObject()
                        .put("connective", group.getConnective());
                // 简化处理，实际需要递归序列化
                groupsJson.add(groupJson);
            }
            json.put("groups", groupsJson);
        }
        
        return json;
    }
    
    /**
     * 创建AND条件组
     */
    public static ConditionGroup andGroup() {
        return new ConditionGroup(ConditionType.AND);
    }
    
    /**
     * 创建OR条件组
     */
    public static ConditionGroup orGroup() {
        return new ConditionGroup(ConditionType.OR);
    }
    
    // Getter and Setter methods
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public ConditionType getType() {
        return type;
    }
    
    public void setType(ConditionType type) {
        this.type = type;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public List<ConditionGroup> getGroups() {
        return groups;
    }
    
    public void setGroups(List<ConditionGroup> groups) {
        this.groups = groups;
    }
    
    @Override
    public String toString() {
        return "QueryCondition{" +
                "field='" + field + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", groupsCount=" + groups.size() +
                '}';
    }
}
