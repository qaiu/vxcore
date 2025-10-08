package cn.qaiu.db.dsl;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有数据实体的基类
 * 
 * 结合 Vert.x CodeGen 和现有 DDL 系统，提供统一的数据对象基类
 * 支持：
 * - Vert.x DataObject 序列化
 * - JSON 转换
 * - DDL 注解兼容（@DdlTable, @DdlColumn）
 * - JOOQ DSL 查询支持
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject(generateConverter = true)
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 所有实体基础字段
     */
    protected Long id;
    protected LocalDateTime createTime;
    protected LocalDateTime updateTime;
    
    /**
     * 无参构造函数（Jackson/JSON 反序列化需要）
     */
    protected BaseEntity() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * JsonObject 构造函数（Vert.x CodeGen 风格）
     * 
     * @param json JSON 对象
     */
    public BaseEntity(JsonObject json) {
        this.id = json.getLong("id");
        
        // JsonObject 没有直接的 getLocalDateTime 方法，需要手动转换
        String createTimeStr = json.getString("createTime");
        if (createTimeStr != null) {
            try {
                this.createTime = LocalDateTime.parse(createTimeStr.replace(" ", "T"));
            } catch (Exception e) {
                this.createTime = LocalDateTime.now();
            }
        }
        
        String updateTimeStr = json.getString("updateTime");
        if (updateTimeStr != null) {
            try {
                this.updateTime = LocalDateTime.parse(updateTimeStr.replace(" ", "T"));
            } catch (Exception e) {
                this.updateTime = LocalDateTime.now();
            }
        }
        
        // 确保时间字段不为null
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 转换为 JsonObject（Vert.x CodeGen 风格）
     * 
     * @return JSON 对象
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject()
            .put("id", id)
            .put("createTime", createTime != null ? createTime.toString() : null)
            .put("updateTime", updateTime != null ? updateTime.toString() : null);
        
        // 子类可以重写此方法添加额外字段
        fillJson(json);
        
        return json;
    }
    
    /**
     * 子类重写此方法添加自己的字段到 JSON
     * 
     * @param json JSON 对象
     */
    protected void fillJson(JsonObject json) {
        // 默认空实现，子类重写
    }
    
    /**
     * 更新记录前调用（自动设置 updateTime）
     */
    public void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 创建记录前调用（自动设置创建时间）
     */
    public void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取表名（可以从 DDL 注解中提取）
     * 
     * @return 表名
     */
    public String getTableName() {
        // 默认使用类名的下划线形式
        String className = this.getClass().getSimpleName();
        return camelToSnake(className);
    }
    
    /**
     * 获取主键字段名
     * 
     * @return 主键字段名
     */
    public String getPrimaryKeyColumn() {
        return "id";
    }
    
    /**
     * 获取主键值
     * 
     * @return 主键值
     */
    public Long getPrimaryKeyValue() {
        return id;
    }
    
    /**
     * 设置主键值
     * 
     * @param primaryKey 主键值
     */
    public void setPrimaryKeyValue(Long primaryKey) {
        this.id = primaryKey;
    }
    
    /**
     * Java 驼峰命名转数据库下划线命名
     * 
     * @param camelCase 驼峰命名字符串
     * @return 下划线命名字符串
     */
    protected String camelToSnake(String camelCase) {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!isFirst) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
            isFirst = false;
        }
        
        return result.toString();
    }
    
    // ========== 基础字段的 getter/setter ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    // ========== Object 方法重写 ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BaseEntity that = (BaseEntity) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + 
               "{id=" + id + 
               ", createTime=" + createTime + 
               ", updateTime=" + updateTime + '}';
    }

    /**
     * 生命周期回调：实体加载时调用
     */
    public void onLoad() {
        // 默认实现为空，实体类可以重写此方法
    }
}
