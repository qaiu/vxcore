package cn.qaiu.db.dsl.common;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 分页请求和结果测试类
 *
 * @author QAIU
 */
@DisplayName("分页相关类测试")
class PageTest {

  @Nested
  @DisplayName("PageRequest 测试")
  class PageRequestTest {

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
      PageRequest request = new PageRequest();

      assertEquals(1, request.getPageNumber());
      assertEquals(10, request.getPageSize());
      assertEquals("ASC", request.getSortDirection());
      assertNull(request.getSortField());
    }

    @Test
    @DisplayName("测试JsonObject构造函数")
    void testJsonConstructor() {
      JsonObject json =
          new JsonObject()
              .put("pageNumber", 2)
              .put("pageSize", 20)
              .put("sortField", "name")
              .put("sortDirection", "DESC");

      PageRequest request = new PageRequest(json);

      assertEquals(2, request.getPageNumber());
      assertEquals(20, request.getPageSize());
      assertEquals("name", request.getSortField());
      assertEquals("DESC", request.getSortDirection());
    }

    @Test
    @DisplayName("测试toJson方法")
    void testToJson() {
      PageRequest request = PageRequest.of(3, 15, "id", "ASC");
      JsonObject json = request.toJson();

      assertEquals(3, json.getInteger("pageNumber"));
      assertEquals(15, json.getInteger("pageSize"));
      assertEquals("id", json.getString("sortField"));
      assertEquals("ASC", json.getString("sortDirection"));
    }

    @Test
    @DisplayName("测试静态工厂方法of")
    void testOfMethod() {
      PageRequest request = PageRequest.of(5, 25);

      assertEquals(5, request.getPageNumber());
      assertEquals(25, request.getPageSize());
    }

    @Test
    @DisplayName("测试带排序的静态工厂方法")
    void testOfMethodWithSorting() {
      PageRequest request = PageRequest.of(2, 10, "createdAt", "DESC");

      assertEquals(2, request.getPageNumber());
      assertEquals(10, request.getPageSize());
      assertEquals("createdAt", request.getSortField());
      assertEquals("DESC", request.getSortDirection());
    }

    @Test
    @DisplayName("测试getOffset计算")
    void testGetOffset() {
      assertEquals(0, PageRequest.of(1, 10).getOffset());
      assertEquals(10, PageRequest.of(2, 10).getOffset());
      assertEquals(90, PageRequest.of(10, 10).getOffset());
      assertEquals(0, PageRequest.of(1, 20).getOffset());
      assertEquals(20, PageRequest.of(2, 20).getOffset());
    }

    @Test
    @DisplayName("测试getLimit")
    void testGetLimit() {
      assertEquals(10, PageRequest.of(1, 10).getLimit());
      assertEquals(25, PageRequest.of(1, 25).getLimit());
    }

    @Test
    @DisplayName("测试nextPage")
    void testNextPage() {
      PageRequest request = PageRequest.of(1, 10, "id", "ASC");
      PageRequest next = request.nextPage();

      assertEquals(2, next.getPageNumber());
      assertEquals(10, next.getPageSize());
      assertEquals("id", next.getSortField());
      assertEquals("ASC", next.getSortDirection());
    }

    @Test
    @DisplayName("测试previousPage")
    void testPreviousPage() {
      PageRequest request = PageRequest.of(3, 10, "id", "ASC");
      PageRequest prev = request.previousPage();

      assertEquals(2, prev.getPageNumber());
      assertEquals(10, prev.getPageSize());

      // 测试第一页的情况
      PageRequest firstPage = PageRequest.of(1, 10);
      PageRequest stillFirst = firstPage.previousPage();
      assertEquals(1, stillFirst.getPageNumber());
    }

    @Test
    @DisplayName("测试hasSorting")
    void testHasSorting() {
      assertFalse(PageRequest.of(1, 10).hasSorting());
      assertTrue(PageRequest.of(1, 10, "name", "ASC").hasSorting());

      PageRequest requestWithEmptySort = new PageRequest();
      requestWithEmptySort.setSortField("");
      assertFalse(requestWithEmptySort.hasSorting());

      requestWithEmptySort.setSortField("   ");
      assertFalse(requestWithEmptySort.hasSorting());
    }

    @Test
    @DisplayName("测试validate - 有效参数")
    void testValidateValid() {
      PageRequest request = PageRequest.of(1, 100, "id", "ASC");
      assertDoesNotThrow(request::validate);

      request.setSortDirection("DESC");
      assertDoesNotThrow(request::validate);

      request.setSortDirection("asc");
      assertDoesNotThrow(request::validate);
    }

    @Test
    @DisplayName("测试validate - 无效页码")
    void testValidateInvalidPageNumber() {
      PageRequest request = new PageRequest();
      request.setPageNumber(0);

      assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    @DisplayName("测试validate - 无效页大小")
    void testValidateInvalidPageSize() {
      PageRequest request = new PageRequest();
      request.setPageSize(0);
      assertThrows(IllegalArgumentException.class, request::validate);

      request.setPageSize(1001);
      assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    @DisplayName("测试validate - 无效排序方向")
    void testValidateInvalidSortDirection() {
      PageRequest request = new PageRequest();
      request.setSortDirection("INVALID");

      assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    @DisplayName("测试equals和hashCode")
    void testEqualsAndHashCode() {
      PageRequest r1 = PageRequest.of(1, 10, "id", "ASC");
      PageRequest r2 = PageRequest.of(1, 10, "id", "ASC");
      PageRequest r3 = PageRequest.of(2, 10, "id", "ASC");

      assertEquals(r1, r2);
      assertEquals(r1.hashCode(), r2.hashCode());
      assertNotEquals(r1, r3);

      assertEquals(r1, r1);
      assertNotEquals(r1, null);
      assertNotEquals(r1, "not a PageRequest");
    }

    @Test
    @DisplayName("测试toString")
    void testToString() {
      PageRequest request = PageRequest.of(1, 10, "id", "ASC");
      String str = request.toString();

      assertTrue(str.contains("pageNumber=1"));
      assertTrue(str.contains("pageSize=10"));
      assertTrue(str.contains("sortField='id'"));
      assertTrue(str.contains("sortDirection='ASC'"));
    }

    @Test
    @DisplayName("测试Setter方法")
    void testSetters() {
      PageRequest request = new PageRequest();

      request.setPageNumber(5);
      assertEquals(5, request.getPageNumber());

      request.setPageSize(50);
      assertEquals(50, request.getPageSize());

      request.setSortField("updatedAt");
      assertEquals("updatedAt", request.getSortField());

      request.setSortDirection("DESC");
      assertEquals("DESC", request.getSortDirection());
    }
  }

  @Nested
  @DisplayName("PageResult 测试")
  class PageResultTest {

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
      PageResult result = new PageResult();

      assertNotNull(result.getData());
      assertEquals(0, result.getData().size());
      assertEquals(0, result.getTotalRecords());
      assertEquals(0, result.getTotalPages());
      assertTrue(result.isFirstPage());
      assertTrue(result.isLastPage());
      assertFalse(result.isHasNextPage());
      assertFalse(result.isHasPreviousPage());
    }

    @Test
    @DisplayName("测试JsonObject构造函数")
    void testJsonConstructor() {
      JsonArray data =
          new JsonArray().add(new JsonObject().put("id", 1)).add(new JsonObject().put("id", 2));

      JsonObject json =
          new JsonObject()
              .put("pageRequest", new JsonObject().put("pageNumber", 2).put("pageSize", 10))
              .put("data", data)
              .put("totalRecords", 100L)
              .put("totalPages", 10)
              .put("firstPage", false)
              .put("lastPage", false)
              .put("hasNextPage", true)
              .put("hasPreviousPage", true);

      PageResult result = new PageResult(json);

      assertEquals(2, result.getData().size());
      assertEquals(100L, result.getTotalRecords());
      assertEquals(10, result.getTotalPages());
      assertFalse(result.isFirstPage());
      assertFalse(result.isLastPage());
      assertTrue(result.isHasNextPage());
      assertTrue(result.isHasPreviousPage());
    }

    @Test
    @DisplayName("测试setter方法")
    void testSetters() {
      PageResult result = new PageResult();

      result.setTotalRecords(50);
      assertEquals(50, result.getTotalRecords());

      result.setTotalPages(5);
      assertEquals(5, result.getTotalPages());

      result.setFirstPage(false);
      assertFalse(result.isFirstPage());

      result.setLastPage(false);
      assertFalse(result.isLastPage());

      result.setHasNextPage(true);
      assertTrue(result.isHasNextPage());

      result.setHasPreviousPage(true);
      assertTrue(result.isHasPreviousPage());

      JsonArray data = new JsonArray().add("test");
      result.setData(data);
      assertEquals(1, result.getData().size());

      PageRequest pageRequest = PageRequest.of(1, 10);
      result.setPageRequest(pageRequest);
      assertEquals(pageRequest, result.getPageRequest());
    }
  }
}
