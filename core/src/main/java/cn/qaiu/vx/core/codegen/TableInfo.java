package cn.qaiu.vx.core.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息
 * 封装数据库表的元数据信息
 * 
 * @author QAIU
 */
public class TableInfo {
    
    private String tableName;
    private String schema;
    private String catalog;
    private String description;
    private String comment;
    private List<ColumnInfo> columns = new ArrayList<>();
    private List<IndexInfo> indexes = new ArrayList<>();
    private List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
    
    public TableInfo() {
    }
    
    public TableInfo(String tableName) {
        this.tableName = tableName;
    }
    
    public TableInfo(String tableName, String schema) {
        this.tableName = tableName;
        this.schema = schema;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public TableInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public TableInfo setSchema(String schema) {
        this.schema = schema;
        return this;
    }
    
    public String getCatalog() {
        return catalog;
    }
    
    public TableInfo setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public TableInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public TableInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public TableInfo setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
        return this;
    }
    
    public TableInfo addColumn(ColumnInfo column) {
        this.columns.add(column);
        return this;
    }
    
    public List<IndexInfo> getIndexes() {
        return indexes;
    }
    
    public TableInfo setIndexes(List<IndexInfo> indexes) {
        this.indexes = indexes;
        return this;
    }
    
    public TableInfo addIndex(IndexInfo index) {
        this.indexes.add(index);
        return this;
    }
    
    public List<ForeignKeyInfo> getForeignKeys() {
        return foreignKeys;
    }
    
    public TableInfo setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
        this.foreignKeys = foreignKeys;
        return this;
    }
    
    public TableInfo addForeignKey(ForeignKeyInfo foreignKey) {
        this.foreignKeys.add(foreignKey);
        return this;
    }
    
    /**
     * 获取主键列
     * 
     * @return 主键列列表
     */
    public List<ColumnInfo> getPrimaryKeyColumns() {
        return columns.stream()
                .filter(ColumnInfo::isPrimaryKey)
                .toList();
    }
    
    /**
     * 获取非主键列
     * 
     * @return 非主键列列表
     */
    public List<ColumnInfo> getNonPrimaryKeyColumns() {
        return columns.stream()
                .filter(column -> !column.isPrimaryKey())
                .toList();
    }
    
    /**
     * 获取列数量
     * 
     * @return 列数量
     */
    public int getColumnCount() {
        return columns.size();
    }
    
    /**
     * 检查是否有主键
     * 
     * @return 是否有主键
     */
    public boolean hasPrimaryKey() {
        return !getPrimaryKeyColumns().isEmpty();
    }
    
    /**
     * 根据列名查找列
     * 
     * @param columnName 列名
     * @return 列信息
     */
    public ColumnInfo getColumnByName(String columnName) {
        return columns.stream()
                .filter(column -> columnName.equals(column.getColumnName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据列类型查找列
     * 
     * @param columnType 列类型
     * @return 列列表
     */
    public List<ColumnInfo> getColumnsByType(String columnType) {
        return columns.stream()
                .filter(column -> columnType.equals(column.getColumnType()))
                .toList();
    }
    
    /**
     * 获取唯一索引
     * 
     * @return 唯一索引列表
     */
    public List<IndexInfo> getUniqueIndexes() {
        return indexes.stream()
                .filter(IndexInfo::isUnique)
                .toList();
    }
    
    /**
     * 获取普通索引
     * 
     * @return 普通索引列表
     */
    public List<IndexInfo> getNormalIndexes() {
        return indexes.stream()
                .filter(index -> !index.isUnique())
                .toList();
    }
    
    /**
     * 获取完整的表名
     * 
     * @return 完整表名
     */
    public String getFullTableName() {
        StringBuilder fullName = new StringBuilder();
        
        if (catalog != null && !catalog.trim().isEmpty()) {
            fullName.append(catalog).append(".");
        }
        
        if (schema != null && !schema.trim().isEmpty()) {
            fullName.append(schema).append(".");
        }
        
        fullName.append(tableName);
        
        return fullName.toString();
    }
    
    /**
     * 获取表的注释
     * 
     * @return 表注释
     */
    public String getTableComment() {
        if (comment != null && !comment.trim().isEmpty()) {
            return comment;
        }
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return tableName;
    }
    
    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", schema='" + schema + '\'' +
                ", catalog='" + catalog + '\'' +
                ", description='" + description + '\'' +
                ", comment='" + comment + '\'' +
                ", columns=" + columns.size() +
                ", indexes=" + indexes.size() +
                ", foreignKeys=" + foreignKeys.size() +
                '}';
    }
}
