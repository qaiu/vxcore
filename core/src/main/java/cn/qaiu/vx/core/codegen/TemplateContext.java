package cn.qaiu.vx.core.codegen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板上下文
 * 封装模板渲染所需的数据
 * 
 * @author QAIU
 */
public class TemplateContext {
    
    private final Map<String, Object> data;
    
    public TemplateContext() {
        this.data = new HashMap<>();
        initDefaultData();
    }
    
    public TemplateContext(Map<String, Object> data) {
        this.data = new HashMap<>(data);
        initDefaultData();
    }
    
    /**
     * 初始化默认数据
     */
    private void initDefaultData() {
        // 添加生成时间
        data.put("generatedTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 添加生成日期
        data.put("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // 添加作者信息
        data.put("author", "QAIU");
        
        // 添加版本信息
        data.put("version", "1.0.0");
    }
    
    /**
     * 添加数据
     * 
     * @param key 键
     * @param value 值
     * @return 当前上下文
     */
    public TemplateContext put(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    /**
     * 获取数据
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return data.get(key);
    }
    
    /**
     * 获取字符串数据
     * 
     * @param key 键
     * @return 字符串值
     */
    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取字符串数据，带默认值
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 字符串值
     */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 检查是否包含键
     * 
     * @param key 键
     * @return 是否包含
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
    
    /**
     * 获取所有数据
     * 
     * @return 数据映射
     */
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    /**
     * 设置实体信息
     * 
     * @param entityInfo 实体信息
     * @return 当前上下文
     */
    public TemplateContext setEntityInfo(EntityInfo entityInfo) {
        data.put("entity", entityInfo);
        return this;
    }
    
    /**
     * 设置表信息
     * 
     * @param tableInfo 表信息
     * @return 当前上下文
     */
    public TemplateContext setTableInfo(TableInfo tableInfo) {
        data.put("table", tableInfo);
        return this;
    }
    
    /**
     * 设置包信息
     * 
     * @param packageInfo 包信息
     * @return 当前上下文
     */
    public TemplateContext setPackageInfo(PackageInfo packageInfo) {
        data.put("package", packageInfo);
        return this;
    }
    
    /**
     * 设置生成配置
     * 
     * @param config 生成配置
     * @return 当前上下文
     */
    public TemplateContext setGeneratorConfig(GeneratorConfig config) {
        data.put("config", config);
        return this;
    }
    
    /**
     * 获取实体信息
     * 
     * @return 实体信息
     */
    public EntityInfo getEntityInfo() {
        return (EntityInfo) data.get("entity");
    }
    
    /**
     * 获取表信息
     * 
     * @return 表信息
     */
    public TableInfo getTableInfo() {
        return (TableInfo) data.get("table");
    }
    
    /**
     * 获取包信息
     * 
     * @return 包信息
     */
    public PackageInfo getPackageInfo() {
        return (PackageInfo) data.get("package");
    }
    
    /**
     * 获取生成配置
     * 
     * @return 生成配置
     */
    public GeneratorConfig getGeneratorConfig() {
        return (GeneratorConfig) data.get("config");
    }
    
    /**
     * 创建子上下文
     * 
     * @param prefix 前缀
     * @return 子上下文
     */
    public TemplateContext createSubContext(String prefix) {
        Map<String, Object> subData = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            subData.put(prefix + "." + entry.getKey(), entry.getValue());
        }
        
        return new TemplateContext(subData);
    }
    
    /**
     * 合并另一个上下文
     * 
     * @param other 其他上下文
     * @return 当前上下文
     */
    public TemplateContext merge(TemplateContext other) {
        if (other != null) {
            data.putAll(other.data);
        }
        return this;
    }
    
    /**
     * 清空数据
     * 
     * @return 当前上下文
     */
    public TemplateContext clear() {
        data.clear();
        initDefaultData();
        return this;
    }
    
    /**
     * 获取数据大小
     * 
     * @return 数据大小
     */
    public int size() {
        return data.size();
    }
    
    /**
     * 检查是否为空
     * 
     * @return 是否为空
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public String toString() {
        return "TemplateContext{" +
                "data=" + data +
                '}';
    }
}
