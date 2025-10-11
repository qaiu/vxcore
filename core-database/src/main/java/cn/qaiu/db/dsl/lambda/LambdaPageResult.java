package cn.qaiu.db.dsl.lambda;

// import io.vertx.codegen.annotations.DataObject; // 未使用
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.List;

/**
 * Lambda分页查询结果
 * 用于Lambda查询的分页结果封装
 * 
 * @param <T> 数据类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class LambdaPageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 默认构造函数
     */
    public LambdaPageResult() {
    }
    
    /**
     * 构造函数
     * 
     * @param records 数据列表
     * @param total 总记录数
     * @param current 当前页码
     * @param size 每页大小
     */
    public LambdaPageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size; // 向上取整
    }
    
    /**
     * JsonObject构造函数
     * 
     * @param json JSON对象
     */
    public LambdaPageResult(JsonObject json) {
        // 简单的JSON转换实现
        this.current = json.getLong("current");
        this.size = json.getLong("size");
        this.total = json.getLong("total");
        this.pages = json.getLong("pages");
        // records需要特殊处理，这里简化实现
    }
    
    /**
     * 转换为JsonObject
     * 
     * @return JSON对象
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("records", records);
        json.put("total", total);
        json.put("current", current);
        json.put("size", size);
        json.put("pages", pages);
        return json;
    }
    
    // Getters and Setters
    
    public List<T> getRecords() {
        return records;
    }
    
    public void setRecords(List<T> records) {
        this.records = records;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public Long getCurrent() {
        return current;
    }
    
    public void setCurrent(Long current) {
        this.current = current;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public Long getPages() {
        return pages;
    }
    
    public void setPages(Long pages) {
        this.pages = pages;
    }
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return current < pages;
    }
    
    /**
     * 获取上一页页码
     */
    public Long getPrevious() {
        return hasPrevious() ? current - 1 : current;
    }
    
    /**
     * 获取下一页页码
     */
    public Long getNext() {
        return hasNext() ? current + 1 : current;
    }
    
    @Override
    public String toString() {
        return "LambdaPageResult{" +
                "records=" + records +
                ", total=" + total +
                ", current=" + current +
                ", size=" + size +
                ", pages=" + pages +
                '}';
    }
}
