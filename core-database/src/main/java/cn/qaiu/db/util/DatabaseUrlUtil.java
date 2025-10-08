package cn.qaiu.db.util;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.sqlclient.SqlConnection;
import org.postgresql.jdbc.PgConnection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUrlUtil {

    public static String getDatabaseIdentifier(java.sql.Connection conn) {
        if (conn == null) return "Unknown";

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbProduct = metaData.getDatabaseProductName().toLowerCase();

            // PostgreSQL 特殊处理
            if (dbProduct.contains("postgresql")) {
                try {
                    PgConnection unwrap = conn.unwrap(PgConnection.class);
                    return trimCredentials("postgresql://" + unwrap.getURL() + "/" + unwrap.getSchema());
                } catch (Exception ignored) {
                }
            }

            String url = metaData.getURL();

            // 优先从URL解析
            if (url != null && !url.isEmpty()) {
                return trimCredentials(simplifyUrl(url, dbProduct));
            }

            // TDengine 特殊处理
            if (dbProduct.contains("taos")) {
                return trimCredentials(extractTaosInfo(conn));
            }

            // 尝试获取 ApplicationName
            try {
                Properties clientInfo = conn.getClientInfo();
                String appName = clientInfo.getProperty("ApplicationName");
                if (appName != null) {
                    return trimCredentials(trimToFirstSpace(appName));
                }
            } catch (Exception ignored) {
            }

            return trimCredentials(dbProduct);

        } catch (SQLException e) {
            return "Unknown (Error: " + e.getMessage() + ")";
        }
    }

    private static String simplifyUrl(String url, String dbProduct) {
        try {
            url = url.trim();

            // 去除 query 参数
            int q = url.indexOf("?");
            if (q != -1) url = url.substring(0, q);

            // 去除用户名/密码等敏感信息
            if (url.contains("@")) {
                int prefix = url.indexOf("//") + 2;
                int at = url.indexOf("@");
                if (prefix < at) {
                    url = url.substring(0, prefix) + url.substring(at + 1);
                }
            }

            // Oracle SID 截取
            if (dbProduct.contains("oracle")) {
                int idx = url.lastIndexOf(":");
                return idx != -1 ? url.substring(0, idx) : url;
            }

            return url;

        } catch (Exception e) {
            return url;
        }
    }

    private static String extractTaosInfo(Connection conn) {
        try {
            Properties clientInfo = conn.getClientInfo();
            String applicationName = clientInfo.getProperty("ApplicationName");
            String host = clientInfo.getProperty("host");
            String dbname = clientInfo.getProperty("dbname");

            if (applicationName != null) {
                return trimToFirstSpace(applicationName);
            } else if (host != null && dbname != null) {
                return dbname + "@" + host;
            } else if (host != null) {
                return host;
            } else if (dbname != null) {
                return dbname;
            } else {
                return "TDengine (no info)";
            }
        } catch (Exception e) {
            return "TDengine (error: " + e.getMessage() + ")";
        }
    }

    private static String trimToFirstSpace(String input) {
        int index = input.indexOf(" ");
        return index != -1 ? input.substring(0, index) : input;
    }

    // 去掉URL里认证信息比如 ?后面的所有参数
    private static String trimCredentials(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        int queryIndex = url.indexOf("?");
        if (queryIndex != -1) {
            return url.substring(0, queryIndex);
        }
        return url;
    }

    /**
     * 从 Vert.x 连接池获取 JDBC URL
     * 
     * @param vertxConn Vert.x 连接对象
     * @return JDBC URL
     */
    public static String getJdbcUrl(SqlConnection vertxConn) {
        if (vertxConn == null) {
            return "Unknown";
        }

        try {
            // 方法1: 尝试使用 unwrap 方法获取原生 JDBC 连接
            try {
                Method unwrapMethod = vertxConn.getClass().getMethod("unwrap", Class.class);
                @SuppressWarnings("resource") // 不关闭连接，因为它是从 Vert.x 连接池借用的
                java.sql.Connection jdbcConn = (java.sql.Connection) unwrapMethod.invoke(vertxConn, java.sql.Connection.class);
                DatabaseMetaData metaData = jdbcConn.getMetaData();
                return metaData.getURL();
            } catch (Exception e) {
                // 继续尝试其他方法
            }

            // 方法2: 通过反射访问内部连接对象
            // 根据路径: ((ConnectionImpl) ((SqlConnectionBase) conn).conn).getJDBCConnection()
            try {
                // 获取 SqlConnectionBase 的 conn 字段
                Field connField = findField(vertxConn.getClass(), "conn");
                if (connField != null) {
                    connField.setAccessible(true);
                    Object connectionImpl = connField.get(vertxConn);
                    
                    if (connectionImpl != null) {
                        // 调用 getJDBCConnection() 方法
                        Method getJdbcConnectionMethod = findMethod(connectionImpl.getClass(), "getJDBCConnection");
                        if (getJdbcConnectionMethod != null) {
                            getJdbcConnectionMethod.setAccessible(true);
                            @SuppressWarnings("resource") // 不关闭连接，因为它是从 Vert.x 连接池借用的
                            java.sql.Connection jdbcConn = (java.sql.Connection) getJdbcConnectionMethod.invoke(connectionImpl);
                            DatabaseMetaData metaData = jdbcConn.getMetaData();
                            return metaData.getURL();
                        }
                    }
                }
            } catch (Exception e) {
                // 继续尝试其他方法
            }

            // 方法3: 尝试其他可能的字段名
            String[] possibleFieldNames = {"jdbcConnection", "connection", "delegate", "wrapped"};
            for (String fieldName : possibleFieldNames) {
                try {
                    Field field = findField(vertxConn.getClass(), fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object value = field.get(vertxConn);
                        if (value instanceof java.sql.Connection) {
                            @SuppressWarnings("resource") // 不关闭连接，因为它是从 Vert.x 连接池借用的
                            java.sql.Connection jdbcConn = (java.sql.Connection) value;
                            DatabaseMetaData metaData = jdbcConn.getMetaData();
                            return metaData.getURL();
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个字段
                }
            }

            return "Unknown (Unable to extract JDBC URL)";

        } catch (Exception e) {
            return "Unknown (Error: " + e.getMessage() + ")";
        }
    }

    /**
     * 从 Vert.x 连接池获取数据库标识符
     * 
     * @param vertxConn Vert.x 连接对象
     * @return 数据库标识符
     */
    public static String getDatabaseIdentifierFromVertx(SqlConnection vertxConn) {
        try {
            String jdbcUrl = getJdbcUrl(vertxConn);
            if ("Unknown".equals(jdbcUrl) || jdbcUrl.startsWith("Unknown")) {
                return jdbcUrl;
            }
            
            // 使用现有的逻辑处理 URL
            return trimCredentials(simplifyUrl(jdbcUrl, ""));
        } catch (Exception e) {
            return "Unknown (Error: " + e.getMessage() + ")";
        }
    }

    /**
     * 递归查找字段
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 递归查找方法
     */
    private static Method findMethod(Class<?> clazz, String methodName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    public static JDBCType getJDBCType(SqlConnection vertxConn) {
        String jdbcUrl = getJdbcUrl(vertxConn);
        if (jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUrl.startsWith("Unknown")) {
            return null;
        }
        return JDBCType.getJDBCTypeByURL(jdbcUrl);
    }
}
