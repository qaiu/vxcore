package cn.qaiu.vx.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodMatcherTest {

  private Map<String, String> pathParams;
  private MultiMap queryParams;

  @BeforeEach
  void setUp() {
    pathParams = new HashMap<>();
    queryParams = MultiMap.caseInsensitiveMultiMap();
  }

  @Nested
  class CalculateScoreTest {

    @Test
    void specialTypeParams_shouldBeSkipped() throws Exception {
      Method m = TestController.class.getDeclaredMethod("withSpecialTypes",
          RoutingContext.class, HttpServerRequest.class, HttpServerResponse.class);
      int score = MethodMatcher.calculateScore(m, pathParams, queryParams, false);
      assertThat(score).isEqualTo(0);
    }

    @Test
    void noAnnotationParams_matchedByPathParam() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noAnnotation", String.class, int.class);
      pathParams.put("name", "test");
      int score = MethodMatcher.calculateScore(m, pathParams, queryParams, false);
      assertThat(score).isGreaterThan(0);
    }

    @Test
    void noAnnotationParams_matchedByQueryParam() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noAnnotation", String.class, int.class);
      queryParams.add("name", "test");
      int score = MethodMatcher.calculateScore(m, pathParams, queryParams, false);
      assertThat(score).isGreaterThan(0);
    }

    @Test
    void noAnnotationParams_notMatched_negativePenalty() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noAnnotation", String.class, int.class);
      int score = MethodMatcher.calculateScore(m, pathParams, queryParams, false);
      assertThat(score).isLessThan(0);
    }

    @Test
    void emptyParamMethod_scoreZero() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noParams");
      int score = MethodMatcher.calculateScore(m, pathParams, queryParams, false);
      assertThat(score).isEqualTo(0);
    }
  }

  @Nested
  class IsFullyMatchedTest {

    @Test
    void specialTypeParams_fullyMatched() throws Exception {
      Method m = TestController.class.getDeclaredMethod("withSpecialTypes",
          RoutingContext.class, HttpServerRequest.class, HttpServerResponse.class);
      boolean matched = MethodMatcher.isFullyMatched(m, pathParams, queryParams, false);
      assertThat(matched).isTrue();
    }

    @Test
    void noAnnotationParams_allPresent_matched() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noAnnotation", String.class, int.class);
      pathParams.put("name", "test");
      queryParams.add("age", "25");
      boolean matched = MethodMatcher.isFullyMatched(m, pathParams, queryParams, false);
      assertThat(matched).isTrue();
    }

    @Test
    void noAnnotationParams_missing_notMatched() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noAnnotation", String.class, int.class);
      boolean matched = MethodMatcher.isFullyMatched(m, pathParams, queryParams, false);
      assertThat(matched).isFalse();
    }

    @Test
    void emptyParamMethod_fullyMatched() throws Exception {
      Method m = TestController.class.getDeclaredMethod("noParams");
      boolean matched = MethodMatcher.isFullyMatched(m, pathParams, queryParams, false);
      assertThat(matched).isTrue();
    }
  }

  @SuppressWarnings("unused")
  static class TestController {
    public void withSpecialTypes(RoutingContext ctx, HttpServerRequest req, HttpServerResponse resp) {}
    public void noAnnotation(String name, int age) {}
    public void noParams() {}
  }
}
