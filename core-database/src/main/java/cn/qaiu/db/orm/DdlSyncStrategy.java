package cn.qaiu.db.orm;

/**
 * DDL同步策略枚举
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public enum DdlSyncStrategy {
    
    /**
     * 自动同步（默认）
     * 自动创建表、添加字段、修改字段、删除多余字段
     */
    AUTO,
    
    /**
     * 仅创建
     * 只创建不存在的表，不修改已有表结构
     */
    CREATE,
    
    /**
     * 仅更新
     * 更新表结构但不删除字段
     */
    UPDATE,
    
    /**
     * 仅验证
     * 验证实体与表结构是否匹配，不执行任何DDL操作
     */
    VALIDATE,
    
    /**
     * 禁用
     * 不执行任何DDL操作
     */
    NONE
}
