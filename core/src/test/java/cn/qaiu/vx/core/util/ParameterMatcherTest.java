package cn.qaiu.vx.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import cn.qaiu.vx.core.annotations.param.PathVariable;
import cn.qaiu.vx.core.annotations.param.RequestBody;
import cn.qaiu.vx.core.annotations.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParameterMatcher参数匹配器测试")
class ParameterMatcherTest {

  @Mock private RoutingContext ctx;
  @Mock private HttpServerRequest request;
  @Mock private HttpServerResponse response;
  @Mock private io.vertx.ext.web.RequestBody requestBodyObj;

  private Map<String, String> pathParams;
  private MultiMap queryParams;

  @BeforeEach
  void setUp() {
    pathParams = new HashMap<>();
    queryParams = MultiMap.caseInsensitiveMultiMap();
    lenient().when(ctx.pathParams()).thenReturn(pathParams);
    lenient().when(ctx.queryParams()).thenReturn(queryParams);
    lenient().when(ctx.body()).thenReturn(null);
    lenient().when(ctx.request()).thenReturn(request);
    lenient().when(ctx.response()).thenReturn(response);
  }

  @Nested
  @DisplayName("findBestMatch测试")
  class FindBestMatchTest {

    @Test
    @DisplayName("空方法列表返回null")
    void emptyList_returnsNull() {
      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.emptyList(), ctx);
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("单方法列表直接返回score=100")
    void singleMethod_returnsWithScore100() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("noParams");
      List<Method> methods = Collections.singletonList(m);

      ParameterMatcher.MatchResult result = ParameterMatcher.findBestMatch(methods, ctx);

      assertThat(result).isNotNull();
      assertThat(result.getMethod()).isEqualTo(m);
      assertThat(result.getScore()).isEqualTo(100);
      assertThat(result.getParameters()).isEmpty();
    }

    @Test
    @DisplayName("多方法列表选择最高得分方法")
    void multipleMethod_selectsBestScore() throws Exception {
      Method withPath = TestHandlers.class.getDeclaredMethod("withPathVar", String.class);
      Method withQuery = TestHandlers.class.getDeclaredMethod("withQueryParam", String.class);
      List<Method> methods = Arrays.asList(withPath, withQuery);

      // 提供路径参数，让withPath方法得分更高
      pathParams.put("id", "123");

      ParameterMatcher.MatchResult result = ParameterMatcher.findBestMatch(methods, ctx);

      assertThat(result).isNotNull();
      assertThat(result.getMethod()).isEqualTo(withPath);
    }

    @Test
    @DisplayName("特殊类型参数(RoutingContext等)直接注入")
    void specialTypeParams_injectedDirectly() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withSpecialTypes",
          RoutingContext.class, HttpServerRequest.class, HttpServerResponse.class);

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result).isNotNull();
      Object[] params = result.getParameters();
      assertThat(params[0]).isSameAs(ctx);
      assertThat(params[1]).isSameAs(request);
      assertThat(params[2]).isSameAs(response);
    }
  }

  @Nested
  @DisplayName("PathVariable参数提取测试")
  class PathVariableTest {

    @Test
    @DisplayName("PathVariable有值时提取路径参数")
    void pathVariable_withValue_extracted() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withPathVar", String.class);
      pathParams.put("id", "42");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo("42");
    }

    @Test
    @DisplayName("PathVariable自定义名称提取路径参数")
    void pathVariable_customName_extracted() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withNamedPathVar", String.class);
      pathParams.put("userId", "100");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo("100");
    }

    @Test
    @DisplayName("PathVariable缺失且required=true时多方法模式异常被捕获跳过该方法")
    void pathVariable_missingRequired_multiMethodSkipsIt() throws Exception {
      Method withRequired = TestHandlers.class.getDeclaredMethod("withPathVar", String.class);
      Method withOptional = TestHandlers.class.getDeclaredMethod("withOptionalPathVar", String.class);
      List<Method> methods = Arrays.asList(withRequired, withOptional);
      // 不提供路径参数

      ParameterMatcher.MatchResult result = ParameterMatcher.findBestMatch(methods, ctx);

      // 必需路径参数缺失时得分为-1，可选得分为0（unmatched -10），选出分数较高者
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("PathVariable缺失且required=false时返回null")
    void pathVariable_missingOptional_returnsNull() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withOptionalPathVar", String.class);
      // 不提供路径参数

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result).isNotNull();
      assertThat(result.getParameters()[0]).isNull();
    }
  }

  @Nested
  @DisplayName("RequestBody参数提取测试")
  class RequestBodyTest {

    @Test
    @DisplayName("有请求体时提取JsonObject")
    void requestBody_withBody_returnsJsonObject() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withBody", JsonObject.class);
      JsonObject body = new JsonObject().put("key", "value");
      when(ctx.body()).thenReturn(requestBodyObj);
      when(requestBodyObj.asJsonObject()).thenReturn(body);

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo(body);
    }

    @Test
    @DisplayName("无请求体且required=false时返回null")
    void requestBody_missingOptional_returnsNull() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withOptionalBody", JsonObject.class);
      // ctx.body() is null (set in setUp)

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isNull();
    }
  }

  @Nested
  @DisplayName("RequestParam参数提取测试")
  class RequestParamTest {

    @Test
    @DisplayName("查询参数存在时提取值")
    void requestParam_present_extracted() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withQueryParam", String.class);
      queryParams.add("name", "Alice");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo("Alice");
    }

    @Test
    @DisplayName("查询参数缺失且有默认值时返回默认值")
    void requestParam_missingWithDefault_returnsDefault() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withDefaultParam", String.class);
      // 不提供name参数

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo("defaultValue");
    }

    @Test
    @DisplayName("查询参数缺失且required=false时返回null")
    void requestParam_missingOptional_returnsNull() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withOptionalParam", String.class);

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isNull();
    }

    @Test
    @DisplayName("自定义参数名映射")
    void requestParam_customName_extracted() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("withNamedParam", String.class);
      queryParams.add("username", "Bob");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result.getParameters()[0]).isEqualTo("Bob");
    }
  }

  @Nested
  @DisplayName("无注解参数按名称匹配测试")
  class NoAnnotationParamTest {

    @Test
    @DisplayName("无注解参数通过路径参数名匹配")
    void noAnnotation_matchedByPathParam() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("noAnnotation", String.class);
      pathParams.put("name", "test");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("无注解参数通过查询参数名匹配")
    void noAnnotation_matchedByQueryParam() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("noAnnotation", String.class);
      queryParams.add("name", "test");

      ParameterMatcher.MatchResult result =
          ParameterMatcher.findBestMatch(Collections.singletonList(m), ctx);

      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("MatchResult访问器测试")
  class MatchResultTest {

    @Test
    @DisplayName("MatchResult正确存储方法、参数和得分")
    void matchResult_storesData() throws Exception {
      Method m = TestHandlers.class.getDeclaredMethod("noParams");
      Object[] params = new Object[0];
      int score = 42;

      ParameterMatcher.MatchResult result = new ParameterMatcher.MatchResult(m, params, score);

      assertThat(result.getMethod()).isEqualTo(m);
      assertThat(result.getParameters()).isSameAs(params);
      assertThat(result.getScore()).isEqualTo(42);
    }
  }

  @Nested
  @DisplayName("多方法评分竞争测试")
  class MultiMethodScoringTest {

    @Test
    @DisplayName("请求体存在时RequestBody方法得分更高")
    void withRequestBody_bodyMethodScoresHigher() throws Exception {
      Method bodyMethod = TestHandlers.class.getDeclaredMethod("withBody", JsonObject.class);
      Method queryMethod = TestHandlers.class.getDeclaredMethod("withQueryParam", String.class);
      List<Method> methods = Arrays.asList(queryMethod, bodyMethod);

      JsonObject body = new JsonObject().put("key", "val");
      when(ctx.body()).thenReturn(requestBodyObj);
      when(requestBodyObj.asJsonObject()).thenReturn(body);

      ParameterMatcher.MatchResult result = ParameterMatcher.findBestMatch(methods, ctx);

      assertThat(result).isNotNull();
      assertThat(result.getMethod()).isEqualTo(bodyMethod);
    }

    @Test
    @DisplayName("必需路径参数缺失得-1，可选路径参数缺失得0，选分数更高的方法")
    void requiredMissingVsOptionalMissing_picksOptional() throws Exception {
      Method withRequired = TestHandlers.class.getDeclaredMethod("withPathVar", String.class);
      Method withOptional = TestHandlers.class.getDeclaredMethod("withOptionalPathVar", String.class);
      List<Method> methods = Arrays.asList(withRequired, withOptional);

      // 不提供路径参数：
      // - withRequired(@PathVariable required=true) → calculateMatchScore返回-1
      // - withOptional(@PathVariable required=false) → 不加分也不扣分 → score=0
      // 0 > -1，所以选 withOptional
      ParameterMatcher.MatchResult result = ParameterMatcher.findBestMatch(methods, ctx);
      assertThat(result).isNotNull();
      assertThat(result.getMethod()).isEqualTo(withOptional);
    }
  }

  // ===== 测试用辅助方法类 =====

  @SuppressWarnings("unused")
  static class TestHandlers {
    public void noParams() {}

    public void withSpecialTypes(
        RoutingContext ctx, HttpServerRequest req, HttpServerResponse resp) {}

    public void withPathVar(@PathVariable String id) {}

    public void withNamedPathVar(@PathVariable("userId") String id) {}

    public void withOptionalPathVar(@PathVariable(required = false) String id) {}

    public void withBody(@RequestBody JsonObject body) {}

    public void withOptionalBody(@RequestBody(required = false) JsonObject body) {}

    public void withQueryParam(@RequestParam String name) {}

    public void withDefaultParam(@RequestParam(defaultValue = "defaultValue") String name) {}

    public void withOptionalParam(@RequestParam(required = false) String name) {}

    public void withNamedParam(@RequestParam("username") String name) {}

    public void noAnnotation(String name) {}
  }
}
