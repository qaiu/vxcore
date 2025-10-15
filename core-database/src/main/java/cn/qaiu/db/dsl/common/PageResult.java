package cn.qaiu.db.dsl.common;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
 * 
 * 专为Vert.x异步服务设计，支持序列化和反序列化
 * 数据以JsonArray形式存储以避免泛型序列化问题
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject(generateConverter = true)
public class PageResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分页请求信息
     */
    private PageRequest pageRequest;
    
    /**
     * 数据列表（JsonArray形式）
     */
    private JsonArray data;
    
    /**
     * 总记录数
     */
    private long totalRecords;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 是否为第一页
     */
    private boolean firstPage;
    
    /**
     * 是否为最后一页
     */
    private boolean lastPage;
    
    /**
     * 是否有下一页
     */
    private boolean hasNextPage;
    
    /**
     * 是否有上一页
     */
    private boolean hasPreviousPage;
    
    /**
     * 默认构造函数
     */
    public PageResult() {
        this.data = new JsonArray();
        this.totalRecords = 0;
        this.totalPages = 0;
        this.firstPage = true;
        this.lastPage = true;
        this.hasNextPage = false;
        this.hasPreviousPage = false;
    }
    
    /**
     * JsonObject构造函数
     * 
     * @param json JSON对象
     */
    public PageResult(JsonObject json) {
        this.pageRequest = json.getJsonObject("pageRequest") != null ? 
                new PageRequest(json.getJsonObject("pageRequest")) : new PageRequest();
        this.data = json.getJsonArray("data", new JsonArray());
        this.totalRecords = json.getLong("totalRecords", 0L);
        this.totalPages = json.getInteger("totalPages", 0);
        this.firstPage = json.getBoolean("firstPage", true);
        this.lastPage = json.getBoolean("lastPage", true);
        this.hasNextPage = json.getBoolean("hasNextPage", false);
        this.hasPreviousPage = json.getBoolean("hasPreviousPage", false);
    }
    
    /**
     * 构造函数
     * 
     * @param pageRequest 分页请求
     * @param data 数据列表
     * @param totalRecords 总记录数
     */
    public PageResult(PageRequest pageRequest, JsonArray data, long totalRecords) {
        this.pageRequest = pageRequest;
        this.data = data != null ? data : new JsonArray();
        this.totalRecords = totalRecords;
        this.totalPages = calculateTotalPages(totalRecords, pageRequest.getPageSize());
        this.firstPage = pageRequest.getPageNumber() == 1;
        this.lastPage = pageRequest.getPageNumber() >= totalPages;
        this.hasNextPage = pageRequest.getPageNumber() < totalPages;
        this.hasPreviousPage = pageRequest.getPageNumber() > 1;
    }
    
    /**
     * 静态工厂方法
     */
    public static PageResult of(PageRequest pageRequest, List<JsonObject> data, long totalRecords) {
        JsonArray jsonArray = new JsonArray();
        if (data != null) {
            data.forEach(jsonArray::add);
        }
        return new PageResult(pageRequest, jsonArray, totalRecords);
    }
    
    /**
     * 静态工厂方法（空结果）
     */
    public static PageResult empty(PageRequest pageRequest) {
        return new PageResult(pageRequest, new JsonArray(), 0);
    }
    
    /**
     * 计算总页数
     */
    private int calculateTotalPages(long totalRecords, int pageSize) {
        if (totalRecords == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
    
    /**
     * 转换为JsonObject
     */
    public JsonObject toJson() {
        return new JsonObject()
                .put("pageRequest", pageRequest != null ? pageRequest.toJson() : null)
                .put("data", data)
                .put("totalRecords", totalRecords)
                .put("totalPages", totalPages)
                .put("firstPage", firstPage)
                .put("lastPage", lastPage)
                .put("hasNextPage", hasNextPage)
                .put("hasPreviousPage", hasPreviousPage);
    }
    
    /**
     * 获取当前页码
     */
    public int getCurrentPage() {
        return pageRequest != null ? pageRequest.getPageNumber() : 1;
    }
    
    /**
     * 获取每页大小
     */
    public int getPageSize() {
        return pageRequest != null ? pageRequest.getPageSize() : 10;
    }
    
    /**
     * 获取当前页数据条数
     */
    public int getCurrentPageSize() {
        return data.size();
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    /**
     * 获取下一页的分页请求
     */
    public PageRequest getNextPageRequest() {
        return hasNextPage && pageRequest != null ? pageRequest.nextPage() : null;
    }
    
    /**
     * 获取上一页的分页请求
     */
    public PageRequest getPreviousPageRequest() {
        return hasPreviousPage && pageRequest != null ? pageRequest.previousPage() : null;
    }
    
    /**
     * 获取页码列表（用于分页组件）
     * 
     * @param maxDisplayPages 最大显示页码数
     */
    public List<Integer> getPageNumbers(int maxDisplayPages) {
        List<Integer> pages = new java.util.ArrayList<>();
        
        if (totalPages <= maxDisplayPages) {
            // 页码数量不超过最大显示数，显示所有页码
            for (int i = 1; i <= totalPages; i++) {
                pages.set(i - 1, i);
            }
        } else {
            // 页码数量超过最大显示数，显示部分页码
            int currentPage = getCurrentPage();
            int halfDisplay = maxDisplayPages / 2;
            
            int startPage = Math.max(1, currentPage - halfDisplay);
            int endPage = Math.min(totalPages, startPage + maxDisplayPages - 1);
            
            // 调整起始页码
            if (endPage - startPage + 1 < maxDisplayPages) {
                startPage = Math.max(1, endPage - maxDisplayPages + 1);
            }
            
            for (int i = startPage; i <= endPage; i++) {
                pages.add(i);
            }
        }
        
        return pages;
    }
    
    /**
     * 转换为实体列表（需要在调用方进行类型转换）
     * 
     * @return JsonArray形式的数据列表
     */
    public JsonArray getDataAsJsonArray() {
        return data;
    }
    
    /**
     * 转换为实体列表（需要在调用方进行类型转换）
     * 
     * @return List形式的数据列表
     */
    @SuppressWarnings("unchecked")
    public List<JsonObject> getDataAsList() {
        return data.getList();
    }
    
    // Getter and Setter methods
    public PageRequest getPageRequest() {
        return pageRequest;
    }
    
    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
    
    public JsonArray getData() {
        return data;
    }
    
    public void setData(JsonArray data) {
        this.data = data;
    }
    
    public long getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
        // 重新计算相关属性
        this.totalPages = calculateTotalPages(totalRecords, getPageSize());
        this.lastPage = getCurrentPage() >= totalPages;
        this.hasNextPage = getCurrentPage() < totalPages;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirstPage() {
        return firstPage;
    }
    
    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }
    
    public boolean isLastPage() {
        return lastPage;
    }
    
    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }
    
    public boolean isHasNextPage() {
        return hasNextPage;
    }
    
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
    
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }
    
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }
    
    @Override
    public String toString() {
        return "PageResult{" +
                "currentPage=" + getCurrentPage() +
                ", pageSize=" + getPageSize() +
                ", totalRecords=" + totalRecords +
                ", totalPages=" + totalPages +
                ", currentPageSize=" + getCurrentPageSize() +
                ", firstPage=" + firstPage +
                ", lastPage=" + lastPage +
                ", hasNextPage=" + hasNextPage +
                ", hasPreviousPage=" + hasPreviousPage +
                '}';
    }
}
