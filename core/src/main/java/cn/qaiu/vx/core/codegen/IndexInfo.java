package cn.qaiu.vx.core.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引信息
 * 封装数据库索引的元数据信息
 * 
 * @author QAIU
 */
public class IndexInfo {
    
    private String indexName;
    private String tableName;
    private boolean unique = false;
    private boolean primaryKey = false;
    private List<String> columnNames = new ArrayList<>();
    private String comment;
    private String description;
    
    public IndexInfo() {
    }
    
    public IndexInfo(String indexName, String tableName) {
        this.indexName = indexName;
        this.tableName = tableName;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public IndexInfo setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public IndexInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public IndexInfo setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public IndexInfo setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public IndexInfo setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
        return this;
    }
    
    public IndexInfo addColumnName(String columnName) {
        this.columnNames.add(columnName);
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public IndexInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public IndexInfo setDescription(String description) {
        this.description = description;
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
     * 获取索引的注释
     * 
     * @return 索引注释
     */
    public String getIndexComment() {
        if (comment != null && !comment.trim().isEmpty()) {
            return comment;
        }
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return indexName;
    }
    
    /**
     * 检查是否为单列索引
     * 
     * @return 是否为单列索引
     */
    public boolean isSingleColumn() {
        return columnNames.size() == 1;
    }
    
    /**
     * 检查是否为多列索引
     * 
     * @return 是否为多列索引
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
     * 获取最后一个列名
     * 
     * @return 最后一个列名
     */
    public String getLastColumnName() {
        return columnNames.isEmpty() ? null : columnNames.get(columnNames.size() - 1);
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
    
    @Override
    public String toString() {
        return "IndexInfo{" +
                "indexName='" + indexName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", unique=" + unique +
                ", primaryKey=" + primaryKey +
                ", columnNames=" + columnNames +
                ", comment='" + comment + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
