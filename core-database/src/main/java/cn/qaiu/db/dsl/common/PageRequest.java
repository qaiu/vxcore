package cn.qaiu.db.dsl.common;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * 分页请求对象
 * 
 * 专为Vert.x异步服务设计，支持序列化和反序列化
 * 避免使用泛型以确保在Future中正常传递
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject(generateConverter = true)
public class PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 页码，从1开始
     */
    private int pageNumber;
    
    /**
     * 每页大小
     */
    private int pageSize;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序方向：ASC或DESC
     */
    private String sortDirection;
    
    /**
     * 默认构造函数
     */
    public PageRequest() {
        this.pageNumber = 1;
        this.pageSize = 10;
        this.sortDirection = "ASC";
    }
    
    /**
     * JsonObject构造函数
     * 
     * @param json JSON对象
     */
    public PageRequest(JsonObject json) {
        this.pageNumber = json.getInteger("pageNumber", 1);
        this.pageSize = json.getInteger("pageSize", 10);
        this.sortField = json.getString("sortField");
        this.sortDirection = json.getString("sortDirection", "ASC");
    }
    
    /**
     * 转换为JsonObject
     */
    public JsonObject toJson() {
        return new JsonObject()
                .put("pageNumber", pageNumber)
                .put("pageSize", pageSize)
                .put("sortField", sortField)
                .put("sortDirection", sortDirection);
    }
    
    /**
     * 创建分页请求
     */
    public static PageRequest of(int pageNumber, int pageSize) {
        PageRequest page = new PageRequest();
        page.pageNumber = pageNumber;
        page.pageSize = pageSize;
        return page;
    }
    
    /**
     * 创建带排序的分页请求
     */
    public static PageRequest of(int pageNumber, int pageSize, String sortField, String sortDirection) {
        PageRequest page = new PageRequest();
        page.pageNumber = pageNumber;
        page.pageSize = pageSize;
        page.sortField = sortField;
        page.sortDirection = sortDirection;
        return page;
    }
    
    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }
    
    /**
     * 获取限制数量
     */
    public int getLimit() {
        return pageSize;
    }
    
    /**
     * 获取下一页的分页请求
     */
    public PageRequest nextPage() {
        return PageRequest.of(pageNumber + 1, pageSize, sortField, sortDirection);
    }
    
    /**
     * 获取上一页的分页请求
     */
    public PageRequest previousPage() {
        if (pageNumber <= 1) {
            return this;
        }
        return PageRequest.of(pageNumber - 1, pageSize, sortField, sortDirection);
    }
    
    /**
     * 检查是否有排序
     */
    public boolean hasSorting() {
        return sortField != null && !sortField.trim().isEmpty();
    }
    
    /**
     * 验证分页参数
     */
    public void validate() {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (pageSize > 1000) {
            throw new IllegalArgumentException("Page size cannot exceed 1000");
        }
        if (sortDirection != null && !"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        }
    }
    
    // Getter and Setter methods
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public String getSortField() {
        return sortField;
    }
    
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    @Override
    public String toString() {
        return "PageRequest{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", sortField='" + sortField + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequest that = (PageRequest) o;
        return pageNumber == that.pageNumber &&
                pageSize == that.pageSize &&
                java.util.Objects.equals(sortField, that.sortField) &&
                java.util.Objects.equals(sortDirection, that.sortDirection);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(pageNumber, pageSize, sortField, sortDirection);
    }
}
