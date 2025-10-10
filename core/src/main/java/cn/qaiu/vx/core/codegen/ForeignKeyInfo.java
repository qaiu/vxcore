package cn.qaiu.vx.core.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 外键信息
 * 封装数据库外键的元数据信息
 * 
 * @author QAIU
 */
public class ForeignKeyInfo {
    
    private String foreignKeyName;
    private String tableName;
    private String referencedTableName;
    private List<String> columnNames = new ArrayList<>();
    private List<String> referencedColumnNames = new ArrayList<>();
    private String comment;
    private String description;
    
    public ForeignKeyInfo() {
    }
    
    public ForeignKeyInfo(String foreignKeyName, String tableName, String referencedTableName) {
        this.foreignKeyName = foreignKeyName;
        this.tableName = tableName;
        this.referencedTableName = referencedTableName;
    }
    
    public String getForeignKeyName() {
        return foreignKeyName;
    }
    
    public ForeignKeyInfo setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
        return this;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public ForeignKeyInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    public String getReferencedTableName() {
        return referencedTableName;
    }
    
    public ForeignKeyInfo setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
        return this;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public ForeignKeyInfo setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
        return this;
    }
    
    public ForeignKeyInfo addColumnName(String columnName) {
        this.columnNames.add(columnName);
        return this;
    }
    
    public List<String> getReferencedColumnNames() {
        return referencedColumnNames;
    }
    
    public ForeignKeyInfo setReferencedColumnNames(List<String> referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
        return this;
    }
    
    public ForeignKeyInfo addReferencedColumnName(String referencedColumnName) {
        this.referencedColumnNames.add(referencedColumnName);
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public ForeignKeyInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ForeignKeyInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * 设置主键表名
     * 
     * @param pkTableName 主键表名
     * @return ForeignKeyInfo实例
     */
    public ForeignKeyInfo setPkTableName(String pkTableName) {
        this.referencedTableName = pkTableName;
        return this;
    }
    
    /**
     * 设置主键列名
     * 
     * @param pkColumnName 主键列名
     * @return ForeignKeyInfo实例
     */
    public ForeignKeyInfo setPkColumnName(String pkColumnName) {
        if (this.referencedColumnNames.isEmpty()) {
            this.referencedColumnNames.add(pkColumnName);
        } else {
            this.referencedColumnNames.set(0, pkColumnName);
        }
        return this;
    }
    
    /**
     * 设置外键表名
     * 
     * @param fkTableName 外键表名
     * @return ForeignKeyInfo实例
     */
    public ForeignKeyInfo setFkTableName(String fkTableName) {
        this.tableName = fkTableName;
        return this;
    }
    
    /**
     * 设置外键列名
     * 
     * @param fkColumnName 外键列名
     * @return ForeignKeyInfo实例
     */
    public ForeignKeyInfo setFkColumnName(String fkColumnName) {
        if (this.columnNames.isEmpty()) {
            this.columnNames.add(fkColumnName);
        } else {
            this.columnNames.set(0, fkColumnName);
        }
        return this;
    }
    
    /**
     * 获取列数量
     * 
     * @return 列数量
     */
    public int getColumnCount() {
        return columnNames.size();
    }
    
    /**
     * 获取外键的注释
     * 
     * @return 外键注释
     */
    public String getForeignKeyComment() {
        if (comment != null && !comment.trim().isEmpty()) {
            return comment;
        }
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return foreignKeyName;
    }
    
    /**
     * 检查是否为单列外键
     * 
     * @return 是否为单列外键
     */
    public boolean isSingleColumn() {
        return columnNames.size() == 1;
    }
    
    /**
     * 检查是否为多列外键
     * 
     * @return 是否为多列外键
     */
    public boolean isMultiColumn() {
        return columnNames.size() > 1;
    }
    
    /**
     * 获取第一个列名
     * 
     * @return 第一个列名
     */
    public String getFirstColumnName() {
        return columnNames.isEmpty() ? null : columnNames.get(0);
    }
    
    /**
     * 获取第一个引用列名
     * 
     * @return 第一个引用列名
     */
    public String getFirstReferencedColumnName() {
        return referencedColumnNames.isEmpty() ? null : referencedColumnNames.get(0);
    }
    
    /**
     * 检查是否包含指定列
     * 
     * @param columnName 列名
     * @return 是否包含
     */
    public boolean containsColumn(String columnName) {
        return columnNames.contains(columnName);
    }
    
    /**
     * 检查是否引用指定列
     * 
     * @param referencedColumnName 引用列名
     * @return 是否引用
     */
    public boolean referencesColumn(String referencedColumnName) {
        return referencedColumnNames.contains(referencedColumnName);
    }
    
    /**
     * 获取列名和引用列名的映射
     * 
     * @return 列名映射
     */
    public String getColumnMapping() {
        if (columnNames.size() != referencedColumnNames.size()) {
            return null;
        }
        
        StringBuilder mapping = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                mapping.append(", ");
            }
            mapping.append(columnNames.get(i))
                   .append(" -> ")
                   .append(referencedColumnNames.get(i));
        }
        
        return mapping.toString();
    }
    
    @Override
    public String toString() {
        return "ForeignKeyInfo{" +
                "foreignKeyName='" + foreignKeyName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", referencedTableName='" + referencedTableName + '\'' +
                ", columnNames=" + columnNames +
                ", referencedColumnNames=" + referencedColumnNames +
                ", comment='" + comment + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
