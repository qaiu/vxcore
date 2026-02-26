package cn.qaiu.db.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DatabaseUrlUtilTest {

  @Mock private Connection mockConn;
  @Mock private DatabaseMetaData mockMeta;

  private AutoCloseable mocks;

  @BeforeEach
  void initMocks() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void closeMocks() throws Exception {
    mocks.close();
  }

  @Nested
  class GetDatabaseIdentifierNullTest {

    @Test
    void nullConnection_returnsUnknown() {
      assertThat(DatabaseUrlUtil.getDatabaseIdentifier(null)).isEqualTo("Unknown");
    }
  }

  @Nested
  @DisplayName("通过真实Connection测试URL处理逻辑")
  class GetDatabaseIdentifierWithConnectionTest {

    @Test
    @DisplayName("MySQL URL中的查询参数被去除")
    void mysqlUrl_queryParamsStripped() throws Exception {
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("MySQL");
      when(mockMeta.getURL()).thenReturn("jdbc:mysql://localhost:3306/mydb?useSSL=false&charset=utf8");

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isEqualTo("jdbc:mysql://localhost:3306/mydb");
    }

    @Test
    @DisplayName("URL中的用户名密码被去除")
    void url_credentialsStripped() throws Exception {
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("MySQL");
      when(mockMeta.getURL()).thenReturn("jdbc:mysql://user:password@localhost:3306/mydb");

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isEqualTo("jdbc:mysql://localhost:3306/mydb");
    }

    @Test
    @DisplayName("Oracle URL截取SID之前的部分")
    void oracleUrl_truncatedAtLastColon() throws Exception {
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("Oracle Database");
      when(mockMeta.getURL()).thenReturn("jdbc:oracle:thin:@localhost:1521:mydb");

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).doesNotEndWith(":mydb");
    }

    @Test
    @DisplayName("URL为null时尝试获取ApplicationName")
    void url_null_fallsBackToAppName() throws Exception {
      Properties props = new Properties();
      props.setProperty("ApplicationName", "myapp v1.0");
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("somedb");
      when(mockMeta.getURL()).thenReturn(null);
      when(mockConn.getClientInfo()).thenReturn(props);

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isEqualTo("myapp");
    }

    @Test
    @DisplayName("URL和ApplicationName都为null时返回数据库产品名")
    void url_null_appName_null_returnsProductName() throws Exception {
      Properties props = new Properties();
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("MyCustomDB");
      when(mockMeta.getURL()).thenReturn(null);
      when(mockConn.getClientInfo()).thenReturn(props);

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isEqualTo("mycustomdb");
    }

    @Test
    @DisplayName("URL为空字符串时尝试其他来源")
    void emptyUrl_fallsBack() throws Exception {
      Properties props = new Properties();
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("h2");
      when(mockMeta.getURL()).thenReturn("");
      when(mockConn.getClientInfo()).thenReturn(props);

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getMetaData抛出SQLException时返回Unknown(Error)")
    void getMetaData_throwsSQLException_returnsUnknownError() throws Exception {
      when(mockConn.getMetaData()).thenThrow(new SQLException("DB error"));

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).startsWith("Unknown (Error:");
    }

    @Test
    @DisplayName("URL有@符号且有//前缀时去除认证信息")
    void url_withAtSign_credentialsRemoved() throws Exception {
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("postgresql");
      when(mockMeta.getURL()).thenReturn("jdbc:postgresql://user:pass@host:5432/db");

      // PostgreSQL会尝试unwrap，失败后走URL解析路径
      when(mockConn.unwrap(any())).thenThrow(new SQLException("not supported"));

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).doesNotContain("user:pass");
    }

    @Test
    @DisplayName("简单URL不含@和?直接返回")
    void simpleUrl_noQueryNoCredentials() throws Exception {
      when(mockConn.getMetaData()).thenReturn(mockMeta);
      when(mockMeta.getDatabaseProductName()).thenReturn("h2");
      when(mockMeta.getURL()).thenReturn("jdbc:h2:mem:testdb");

      String result = DatabaseUrlUtil.getDatabaseIdentifier(mockConn);
      assertThat(result).isEqualTo("jdbc:h2:mem:testdb");
    }
  }

  @Nested
  class GetJdbcUrlNullTest {

    @Test
    void nullConnection_returnsUnknown() {
      assertThat(DatabaseUrlUtil.getJdbcUrl(null)).isEqualTo("Unknown");
    }
  }

  @Nested
  class GetDatabaseIdentifierFromVertxTest {

    @Test
    void nullConnection_returnsUnknown() {
      String result = DatabaseUrlUtil.getDatabaseIdentifierFromVertx(null);
      assertThat(result).startsWith("Unknown");
    }
  }

  @Nested
  class GetJDBCTypeTest {

    @Test
    void nullConnection_returnsNull() {
      assertThat(DatabaseUrlUtil.getJDBCType(null)).isNull();
    }
  }

  @Nested
  class GetJDBCTypeFromPoolTest {

    @Test
    void nullPoolConnection_handledGracefully() {
      assertThat(DatabaseUrlUtil.getJdbcUrl(null)).isEqualTo("Unknown");
      assertThat(DatabaseUrlUtil.getDatabaseIdentifierFromVertx(null)).startsWith("Unknown");
    }
  }
}
