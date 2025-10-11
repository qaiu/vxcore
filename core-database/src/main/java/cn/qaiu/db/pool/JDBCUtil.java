package cn.qaiu.db.pool;

/**
 * JDBC工具类
 * 
 * @author QAIU
 */
public class JDBCUtil {
    public static JDBCType getJDBCType(String deviceName) {
        switch (deviceName) {
            case "com.mysql.cj.jdbc.Driver":
            case "com.mysql.jdbc.Driver":
                return JDBCType.MySQL;
            case "org.h2.Driver":
                return JDBCType.H2DB;
        }
        throw new RuntimeException("不支持的SQL驱动类型: " + deviceName);
    }
}
