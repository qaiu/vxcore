package cn.qaiu.vx.core.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体信息
 * 封装实体类的元数据信息
 * 
 * @author QAIU
 */
public class EntityInfo {
    
    private String className;
    private String tableName;
    private String description;
    private String packageName;
    private List<FieldInfo> fields = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private String author = "QAIU";
    private String version = "1.0.0";
    
    public EntityInfo() {
    }
    
    public EntityInfo(String className, String tableName) {
        this.className = className;
        this.tableName = tableName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public EntityInfo setClassName(String className) {
        this.className = className;
        return this;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public EntityInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public EntityInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public EntityInfo setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }
    
    public List<FieldInfo> getFields() {
        return fields;
    }
    
    public EntityInfo setFields(List<FieldInfo> fields) {
        this.fields = fields;
        return this;
    }
    
    public EntityInfo addField(FieldInfo field) {
        this.fields.add(field);
        return this;
    }
    
    public List<String> getImports() {
        return imports;
    }
    
    public EntityInfo setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }
    
    public EntityInfo addImport(String importClass) {
        if (!this.imports.contains(importClass)) {
            this.imports.add(importClass);
        }
        return this;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public EntityInfo setAuthor(String author) {
        this.author = author;
        return this;
    }
    
    public String getVersion() {
        return version;
    }
    
    public EntityInfo setVersion(String version) {
        this.version = version;
        return this;
    }
    
    /**
     * 获取主键字段
     * 
     * @return 主键字段
     */
    public FieldInfo getPrimaryKeyField() {
        return fields.stream()
                .filter(FieldInfo::isPrimaryKey)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取非主键字段
     * 
     * @return 非主键字段列表
     */
    public List<FieldInfo> getNonPrimaryKeyFields() {
        return fields.stream()
                .filter(field -> !field.isPrimaryKey())
                .toList();
    }
    
    /**
     * 获取字段数量
     * 
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields.size();
    }
    
    /**
     * 检查是否有主键
     * 
     * @return 是否有主键
     */
    public boolean hasPrimaryKey() {
        return getPrimaryKeyField() != null;
    }
    
    /**
     * 获取字段名列表
     * 
     * @return 字段名列表
     */
    public List<String> getFieldNames() {
        return fields.stream()
                .map(FieldInfo::getFieldName)
                .toList();
    }
    
    /**
     * 获取字段类型列表
     * 
     * @return 字段类型列表
     */
    public List<String> getFieldTypes() {
        return fields.stream()
                .map(FieldInfo::getFieldType)
                .toList();
    }
    
    /**
     * 根据字段名查找字段
     * 
     * @param fieldName 字段名
     * @return 字段信息
     */
    public FieldInfo getFieldByName(String fieldName) {
        return fields.stream()
                .filter(field -> fieldName.equals(field.getFieldName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据字段类型查找字段
     * 
     * @param fieldType 字段类型
     * @return 字段列表
     */
    public List<FieldInfo> getFieldsByType(String fieldType) {
        return fields.stream()
                .filter(field -> fieldType.equals(field.getFieldType()))
                .toList();
    }
    
    /**
     * 获取包路径
     * 
     * @return 包路径
     */
    public String getPackagePath() {
        if (packageName == null) {
            return null;
        }
        return packageName.replace('.', '/');
    }
    
    /**
     * 获取完整类名
     * 
     * @return 完整类名
     */
    public String getFullClassName() {
        if (packageName == null || packageName.trim().isEmpty()) {
            return className;
        }
        return packageName + "." + className;
    }
    
    /**
     * 获取文件路径
     * 
     * @return 文件路径
     */
    public String getFilePath() {
        String packagePath = getPackagePath();
        if (packagePath == null) {
            return className + ".java";
        }
        return packagePath + "/" + className + ".java";
    }
    
    @Override
    public String toString() {
        return "EntityInfo{" +
                "className='" + className + '\'' +
                ", tableName='" + tableName + '\'' +
                ", description='" + description + '\'' +
                ", packageName='" + packageName + '\'' +
                ", fields=" + fields.size() +
                ", imports=" + imports.size() +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
