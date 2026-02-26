package cn.qaiu.db.dsl.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.jooq.ExecuteContext;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SqlAuditTest {

  @BeforeEach
  void setUp() {
    SqlAuditListener.resetStatistics();
  }

  @AfterEach
  void tearDown() {
    SqlAuditListener.resetStatistics();
  }

  @Nested
  class SqlAuditListenerTest {

    @Test
    void incrementQueryCount_andGetStatistics() {
      SqlAuditListener.incrementQueryCount("SELECT * FROM users");
      SqlAuditListener.incrementQueryCount("SELECT * FROM users");

      SqlAuditListener.QueryStatistics stats =
          SqlAuditListener.getQueryStatistics("SELECT * FROM users");
      assertThat(stats).isNotNull();
      assertThat(stats.getExecutionCount()).isEqualTo(2);
      assertThat(stats.getSql()).isEqualTo("SELECT * FROM users");
    }

    @Test
    void getStatistics_unknownSql_returnsZeroCounts() {
      SqlAuditListener.QueryStatistics stats =
          SqlAuditListener.getQueryStatistics("UNKNOWN SQL");
      assertThat(stats).isNotNull();
      assertThat(stats.getExecutionCount()).isEqualTo(0);
      assertThat(stats.getTotalExecutionTime()).isEqualTo(0);
      assertThat(stats.getErrorCount()).isEqualTo(0);
    }

    @Test
    void getAllStatistics_afterMultipleQueries() {
      SqlAuditListener.incrementQueryCount("SELECT 1");
      SqlAuditListener.incrementQueryCount("SELECT 2");
      SqlAuditListener.incrementQueryCount("SELECT 1");

      Map<String, SqlAuditListener.QueryStatistics> all = SqlAuditListener.getAllStatistics();
      assertThat(all).hasSize(2);
      assertThat(all.get("SELECT 1").getExecutionCount()).isEqualTo(2);
      assertThat(all.get("SELECT 2").getExecutionCount()).isEqualTo(1);
    }

    @Test
    void resetStatistics_clearsAll() {
      SqlAuditListener.incrementQueryCount("SELECT 1");
      SqlAuditListener.resetStatistics();
      assertThat(SqlAuditListener.getAllStatistics()).isEmpty();
    }

    @Test
    void queryStatistics_averageExecutionTime() {
      SqlAuditListener.QueryStatistics stats =
          new SqlAuditListener.QueryStatistics("SELECT 1", 10, 500, 2);
      assertThat(stats.getAverageExecutionTime()).isEqualTo(50.0);
      assertThat(stats.getErrorRate()).isEqualTo(0.2);
      assertThat(stats.toString()).contains("SELECT 1");
    }

    @Test
    void queryStatistics_zeroCounts() {
      SqlAuditListener.QueryStatistics stats =
          new SqlAuditListener.QueryStatistics("SELECT 1", 0, 0, 0);
      assertThat(stats.getAverageExecutionTime()).isEqualTo(0.0);
      assertThat(stats.getErrorRate()).isEqualTo(0.0);
    }
  }

  @Nested
  class SqlAuditStatisticsTest {

    @Test
    void getAllStatistics_empty() {
      assertThat(SqlAuditStatistics.getAllStatistics()).isEmpty();
    }

    @Test
    void getStatistics_afterManualIncrement() {
      SqlAuditListener.incrementQueryCount("SELECT * FROM orders");
      SqlAuditListener.QueryStatistics stats =
          SqlAuditStatistics.getStatistics("SELECT * FROM orders");
      assertThat(stats).isNotNull();
      assertThat(stats.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void printAllStatistics_empty_noErrors() {
      SqlAuditStatistics.printAllStatistics();
    }

    @Test
    void printAllStatistics_withData() {
      SqlAuditListener.incrementQueryCount("SELECT * FROM users");
      SqlAuditStatistics.printAllStatistics();
    }

    @Test
    void printSlowQueries_noSlow() {
      SqlAuditListener.incrementQueryCount("SELECT 1");
      SqlAuditStatistics.printSlowQueries(100);
    }

    @Test
    void printErrorQueries_noErrors() {
      SqlAuditListener.incrementQueryCount("SELECT 1");
      SqlAuditStatistics.printErrorQueries();
    }

    @Test
    void resetStatistics() {
      SqlAuditListener.incrementQueryCount("SELECT 1");
      SqlAuditStatistics.resetStatistics();
      assertThat(SqlAuditStatistics.getAllStatistics()).isEmpty();
    }

    @Test
    void exportStatisticsAsJson_empty() {
      String json = SqlAuditStatistics.exportStatisticsAsJson();
      assertThat(json).contains("\"statistics\"");
    }

    @Test
    void exportStatisticsAsJson_withData() {
      SqlAuditListener.incrementQueryCount("SELECT * FROM users");
      String json = SqlAuditStatistics.exportStatisticsAsJson();
      assertThat(json).contains("\"statistics\"");
      assertThat(json).contains("SELECT * FROM users");
      assertThat(json).contains("executionCount");
    }
  }

  @Nested
  class SqlAuditListenerExecuteTest {

    @Mock ExecuteContext mockCtx;
    @Mock Query mockQuery;

    private AutoCloseable mocks;

    @BeforeEach
    void init() {
      mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void close() throws Exception {
      mocks.close();
    }

    @Test
    void executeStart_withQuery_recordsCount() {
      when(mockCtx.query()).thenReturn(mockQuery);
      when(mockQuery.getSQL()).thenReturn("SELECT * FROM users");
      when(mockCtx.data()).thenReturn(null);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);

      SqlAuditListener.QueryStatistics stats =
          SqlAuditListener.getQueryStatistics("SELECT * FROM users");
      assertThat(stats.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void executeStart_withNullQuery_noError() {
      when(mockCtx.query()).thenReturn(null);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);
      // 不抛出异常即可
    }

    @Test
    void executeEnd_afterStart_recordsTiming() {
      when(mockCtx.query()).thenReturn(mockQuery);
      when(mockQuery.getSQL()).thenReturn("SELECT 1");
      when(mockCtx.data()).thenReturn(null);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);
      listener.executeEnd(mockCtx);
      // 不抛出异常，且计数记录正确
      SqlAuditListener.QueryStatistics stats = SqlAuditListener.getQueryStatistics("SELECT 1");
      assertThat(stats.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void executeEnd_withNullQuery_noError() {
      when(mockCtx.query()).thenReturn(null);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);
      listener.executeEnd(mockCtx);
    }

    @Test
    void executeEnd_withoutPriorStart_noError() {
      when(mockCtx.query()).thenReturn(mockQuery);
      when(mockQuery.getSQL()).thenReturn("SELECT 1");

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeEnd(mockCtx);
      // 没有先调用executeStart，startTime为null，应该安全跳过
    }

    @Test
    void exception_afterStart_recordsError() {
      when(mockCtx.query()).thenReturn(mockQuery);
      when(mockQuery.getSQL()).thenReturn("SELECT bad");
      when(mockCtx.data()).thenReturn(null);
      RuntimeException ex = new RuntimeException("SQL error");
      when(mockCtx.exception()).thenReturn(ex);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);
      listener.exception(mockCtx);

      SqlAuditListener.QueryStatistics stats = SqlAuditListener.getQueryStatistics("SELECT bad");
      assertThat(stats.getErrorCount()).isEqualTo(1);
    }

    @Test
    void exception_withNullQuery_noError() {
      when(mockCtx.query()).thenReturn(null);
      when(mockCtx.data()).thenReturn(null);

      SqlAuditListener listener = new SqlAuditListener();
      listener.executeStart(mockCtx);
      listener.exception(mockCtx);
    }

    @Test
    void exception_withoutPriorStart_noError() {
      SqlAuditListener listener = new SqlAuditListener();
      listener.exception(mockCtx);
    }
  }
}
