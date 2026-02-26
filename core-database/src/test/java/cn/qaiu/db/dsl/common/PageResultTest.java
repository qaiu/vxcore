package cn.qaiu.db.dsl.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Nested
  class ConstructorTest {

    @Test
    void defaultConstructor() {
      PageResult pr = new PageResult();
      assertThat(pr.getTotalRecords()).isEqualTo(0);
      assertThat(pr.getTotalPages()).isEqualTo(0);
      assertThat(pr.isFirstPage()).isTrue();
      assertThat(pr.isLastPage()).isTrue();
      assertThat(pr.isHasNextPage()).isFalse();
      assertThat(pr.isHasPreviousPage()).isFalse();
      assertThat(pr.isEmpty()).isTrue();
    }

    @Test
    void jsonConstructor_withPageRequest() {
      JsonObject json = new JsonObject()
          .put("pageRequest", new JsonObject().put("pageNumber", 2).put("pageSize", 5))
          .put("data", new JsonArray().add(new JsonObject().put("id", 1)))
          .put("totalRecords", 50L)
          .put("totalPages", 10)
          .put("firstPage", false)
          .put("lastPage", false)
          .put("hasNextPage", true)
          .put("hasPreviousPage", true);

      PageResult pr = new PageResult(json);
      assertThat(pr.getTotalRecords()).isEqualTo(50);
      assertThat(pr.getTotalPages()).isEqualTo(10);
      assertThat(pr.isFirstPage()).isFalse();
      assertThat(pr.isHasNextPage()).isTrue();
      assertThat(pr.getCurrentPageSize()).isEqualTo(1);
    }

    @Test
    void jsonConstructor_withoutPageRequest() {
      JsonObject json = new JsonObject()
          .put("totalRecords", 10L);
      PageResult pr = new PageResult(json);
      assertThat(pr.getTotalRecords()).isEqualTo(10);
    }

    @Test
    void fullConstructor_firstPage() {
      PageRequest req = PageRequest.of(1, 10);
      JsonArray data = new JsonArray().add(new JsonObject().put("id", 1));
      PageResult pr = new PageResult(req, data, 25);

      assertThat(pr.getCurrentPage()).isEqualTo(1);
      assertThat(pr.getPageSize()).isEqualTo(10);
      assertThat(pr.getTotalRecords()).isEqualTo(25);
      assertThat(pr.getTotalPages()).isEqualTo(3);
      assertThat(pr.isFirstPage()).isTrue();
      assertThat(pr.isLastPage()).isFalse();
      assertThat(pr.isHasNextPage()).isTrue();
      assertThat(pr.isHasPreviousPage()).isFalse();
    }

    @Test
    void fullConstructor_lastPage() {
      PageRequest req = PageRequest.of(3, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 25);

      assertThat(pr.isFirstPage()).isFalse();
      assertThat(pr.isLastPage()).isTrue();
      assertThat(pr.isHasNextPage()).isFalse();
      assertThat(pr.isHasPreviousPage()).isTrue();
    }

    @Test
    void fullConstructor_nullData() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = new PageResult(req, null, 0);
      assertThat(pr.getData()).isNotNull();
      assertThat(pr.isEmpty()).isTrue();
    }

    @Test
    void fullConstructor_zeroRecords() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 0);
      assertThat(pr.getTotalPages()).isEqualTo(0);
    }
  }

  @Nested
  class FactoryMethodTest {

    @Test
    void of_withData() {
      PageRequest req = PageRequest.of(1, 10);
      List<JsonObject> items = Arrays.asList(
          new JsonObject().put("id", 1),
          new JsonObject().put("id", 2));
      PageResult pr = PageResult.of(req, items, 100);

      assertThat(pr.getCurrentPageSize()).isEqualTo(2);
      assertThat(pr.getTotalRecords()).isEqualTo(100);
    }

    @Test
    void of_nullData() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = PageResult.of(req, null, 0);
      assertThat(pr.isEmpty()).isTrue();
    }

    @Test
    void empty_factory() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = PageResult.empty(req);

      assertThat(pr.isEmpty()).isTrue();
      assertThat(pr.getTotalRecords()).isEqualTo(0);
    }
  }

  @Nested
  class NavigationTest {

    @Test
    void getNextPageRequest_hasNext() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 25);

      PageRequest next = pr.getNextPageRequest();
      assertThat(next).isNotNull();
      assertThat(next.getPageNumber()).isEqualTo(2);
    }

    @Test
    void getNextPageRequest_noNext() {
      PageRequest req = PageRequest.of(3, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 25);

      assertThat(pr.getNextPageRequest()).isNull();
    }

    @Test
    void getPreviousPageRequest_hasPrevious() {
      PageRequest req = PageRequest.of(2, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 25);

      PageRequest prev = pr.getPreviousPageRequest();
      assertThat(prev).isNotNull();
      assertThat(prev.getPageNumber()).isEqualTo(1);
    }

    @Test
    void getPreviousPageRequest_noPrevious() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 25);

      assertThat(pr.getPreviousPageRequest()).isNull();
    }
  }

  @Nested
  class SerializationTest {

    @Test
    void toJson() {
      PageRequest req = PageRequest.of(2, 10);
      JsonArray data = new JsonArray().add(new JsonObject().put("id", 1));
      PageResult pr = new PageResult(req, data, 50);

      JsonObject json = pr.toJson();
      assertThat(json.getLong("totalRecords")).isEqualTo(50);
      assertThat(json.getInteger("totalPages")).isEqualTo(5);
      assertThat(json.getJsonObject("pageRequest")).isNotNull();
      assertThat(json.getJsonArray("data")).hasSize(1);
    }

    @Test
    void toJson_nullPageRequest() {
      PageResult pr = new PageResult();
      JsonObject json = pr.toJson();
      assertThat(json.getJsonObject("pageRequest")).isNull();
    }
  }

  @Nested
  class AccessorTest {

    @Test
    void getCurrentPage_withoutPageRequest() {
      PageResult pr = new PageResult();
      assertThat(pr.getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getPageSize_withoutPageRequest() {
      PageResult pr = new PageResult();
      assertThat(pr.getPageSize()).isEqualTo(10);
    }

    @Test
    void setTotalRecords_recalculatesPages() {
      PageRequest req = PageRequest.of(1, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 5);
      assertThat(pr.getTotalPages()).isEqualTo(1);
      assertThat(pr.isLastPage()).isTrue();

      pr.setTotalRecords(25);
      assertThat(pr.getTotalPages()).isEqualTo(3);
      assertThat(pr.isHasNextPage()).isTrue();
      assertThat(pr.isLastPage()).isFalse();
    }

    @Test
    void getDataAsList() {
      PageRequest req = PageRequest.of(1, 10);
      JsonArray data = new JsonArray().add(new JsonObject().put("id", 1));
      PageResult pr = new PageResult(req, data, 1);

      assertThat(pr.getDataAsList()).hasSize(1);
      assertThat(pr.getDataAsJsonArray()).hasSize(1);
    }

    @Test
    void setters() {
      PageResult pr = new PageResult();
      pr.setFirstPage(false);
      pr.setLastPage(false);
      pr.setHasNextPage(true);
      pr.setHasPreviousPage(true);
      pr.setTotalPages(5);
      pr.setData(new JsonArray());
      pr.setPageRequest(PageRequest.of(2, 20));

      assertThat(pr.isFirstPage()).isFalse();
      assertThat(pr.isLastPage()).isFalse();
      assertThat(pr.isHasNextPage()).isTrue();
      assertThat(pr.isHasPreviousPage()).isTrue();
      assertThat(pr.getTotalPages()).isEqualTo(5);
    }

    @Test
    void toString_containsRelevantInfo() {
      PageRequest req = PageRequest.of(2, 10);
      PageResult pr = new PageResult(req, new JsonArray(), 50);
      String str = pr.toString();
      assertThat(str).contains("currentPage=2");
      assertThat(str).contains("totalRecords=50");
    }
  }

  @Nested
  class PageRequestTest {

    @Test
    void defaultValues() {
      PageRequest pr = new PageRequest();
      assertThat(pr.getPageNumber()).isEqualTo(1);
      assertThat(pr.getPageSize()).isEqualTo(10);
      assertThat(pr.getSortDirection()).isEqualTo("ASC");
    }

    @Test
    void jsonConstructor() {
      JsonObject json = new JsonObject()
          .put("pageNumber", 3)
          .put("pageSize", 20)
          .put("sortField", "name")
          .put("sortDirection", "DESC");
      PageRequest pr = new PageRequest(json);

      assertThat(pr.getPageNumber()).isEqualTo(3);
      assertThat(pr.getPageSize()).isEqualTo(20);
      assertThat(pr.getSortField()).isEqualTo("name");
      assertThat(pr.getSortDirection()).isEqualTo("DESC");
    }

    @Test
    void toJson() {
      PageRequest pr = PageRequest.of(2, 15, "id", "DESC");
      JsonObject json = pr.toJson();
      assertThat(json.getInteger("pageNumber")).isEqualTo(2);
      assertThat(json.getInteger("pageSize")).isEqualTo(15);
    }

    @Test
    void getOffset() {
      assertThat(PageRequest.of(1, 10).getOffset()).isEqualTo(0);
      assertThat(PageRequest.of(3, 10).getOffset()).isEqualTo(20);
    }

    @Test
    void getLimit() {
      assertThat(PageRequest.of(1, 25).getLimit()).isEqualTo(25);
    }

    @Test
    void nextPage() {
      PageRequest next = PageRequest.of(1, 10).nextPage();
      assertThat(next.getPageNumber()).isEqualTo(2);
    }

    @Test
    void previousPage() {
      PageRequest prev = PageRequest.of(3, 10).previousPage();
      assertThat(prev.getPageNumber()).isEqualTo(2);
    }

    @Test
    void previousPage_firstPage_returnsSelf() {
      PageRequest pr = PageRequest.of(1, 10);
      assertThat(pr.previousPage()).isSameAs(pr);
    }

    @Test
    void hasSorting() {
      assertThat(PageRequest.of(1, 10).hasSorting()).isFalse();
      assertThat(PageRequest.of(1, 10, "name", "ASC").hasSorting()).isTrue();
    }

    @Test
    void validate_valid() {
      PageRequest.of(1, 10).validate();
      PageRequest.of(1, 10, "name", "ASC").validate();
      PageRequest.of(1, 10, "name", "DESC").validate();
    }

    @Test
    void validate_invalidPageNumber() {
      PageRequest pr = new PageRequest();
      pr.setPageNumber(0);
      try {
        pr.validate();
        assertThat(false).isTrue();
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).contains("Page number");
      }
    }

    @Test
    void validate_invalidPageSize() {
      PageRequest pr = new PageRequest();
      pr.setPageSize(0);
      try {
        pr.validate();
        assertThat(false).isTrue();
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).contains("Page size");
      }
    }

    @Test
    void validate_pageSizeTooLarge() {
      PageRequest pr = new PageRequest();
      pr.setPageSize(1001);
      try {
        pr.validate();
        assertThat(false).isTrue();
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).contains("Page size");
      }
    }

    @Test
    void validate_invalidSortDirection() {
      PageRequest pr = PageRequest.of(1, 10, "name", "INVALID");
      try {
        pr.validate();
        assertThat(false).isTrue();
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).contains("Sort direction");
      }
    }

    @Test
    void equalsAndHashCode() {
      PageRequest a = PageRequest.of(1, 10, "name", "ASC");
      PageRequest b = PageRequest.of(1, 10, "name", "ASC");
      PageRequest c = PageRequest.of(2, 10);

      assertThat(a).isEqualTo(b);
      assertThat(a.hashCode()).isEqualTo(b.hashCode());
      assertThat(a).isNotEqualTo(c);
      assertThat(a).isNotEqualTo(null);
      assertThat(a).isEqualTo(a);
    }

    @Test
    void toString_test() {
      PageRequest pr = PageRequest.of(1, 10);
      assertThat(pr.toString()).contains("pageNumber=1");
    }
  }
}
