package cn.qaiu.db.dsl.lambda;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LambdaPageResultTest {

  @Test
  void defaultConstructor() {
    LambdaPageResult<String> pr = new LambdaPageResult<>();
    assertThat(pr.getRecords()).isNull();
    assertThat(pr.getTotal()).isNull();
  }

  @Test
  void fullConstructor() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Arrays.asList("a", "b"), 20L, 1L, 10L);
    assertThat(pr.getRecords()).hasSize(2);
    assertThat(pr.getTotal()).isEqualTo(20);
    assertThat(pr.getCurrent()).isEqualTo(1);
    assertThat(pr.getSize()).isEqualTo(10);
    assertThat(pr.getPages()).isEqualTo(2);
  }

  @Test
  void jsonConstructor() {
    JsonObject json = new JsonObject()
        .put("current", 2L).put("size", 10L).put("total", 50L).put("pages", 5L);
    LambdaPageResult<String> pr = new LambdaPageResult<>(json);
    assertThat(pr.getCurrent()).isEqualTo(2);
    assertThat(pr.getSize()).isEqualTo(10);
  }

  @Test
  void toJson() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Arrays.asList("a"), 10L, 1L, 5L);
    JsonObject json = pr.toJson();
    assertThat(json.getLong("total")).isEqualTo(10);
    assertThat(json.getLong("pages")).isEqualTo(2);
  }

  @Test
  void hasPrevious_firstPage() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Collections.emptyList(), 50L, 1L, 10L);
    assertThat(pr.hasPrevious()).isFalse();
    assertThat(pr.getPrevious()).isEqualTo(1);
  }

  @Test
  void hasPrevious_secondPage() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Collections.emptyList(), 50L, 2L, 10L);
    assertThat(pr.hasPrevious()).isTrue();
    assertThat(pr.getPrevious()).isEqualTo(1);
  }

  @Test
  void hasNext_lastPage() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Collections.emptyList(), 50L, 5L, 10L);
    assertThat(pr.hasNext()).isFalse();
    assertThat(pr.getNext()).isEqualTo(5);
  }

  @Test
  void hasNext_notLastPage() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Collections.emptyList(), 50L, 3L, 10L);
    assertThat(pr.hasNext()).isTrue();
    assertThat(pr.getNext()).isEqualTo(4);
  }

  @Test
  void setters() {
    LambdaPageResult<String> pr = new LambdaPageResult<>();
    pr.setRecords(Arrays.asList("x"));
    pr.setTotal(100L);
    pr.setCurrent(5L);
    pr.setSize(20L);
    pr.setPages(5L);
    assertThat(pr.getRecords()).hasSize(1);
    assertThat(pr.getTotal()).isEqualTo(100);
  }

  @Test
  void toStringOutput() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Arrays.asList("a"), 10L, 1L, 5L);
    assertThat(pr.toString()).contains("total=10");
  }

  @Test
  void pagesCalculation_notEvenDivision() {
    LambdaPageResult<String> pr = new LambdaPageResult<>(
        Collections.emptyList(), 15L, 1L, 10L);
    assertThat(pr.getPages()).isEqualTo(2);
  }
}
