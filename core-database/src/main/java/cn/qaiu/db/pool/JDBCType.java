package cn.qaiu.db.pool;

import org.apache.commons.lang3.StringUtils;

/**
 * JDBC数据库类型枚举
 * 定义支持的数据库类型及其JDBC URL前缀
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 * @since 2023/10/10 14:06
 */
public enum JDBCType {
    /** MySQL数据库 */
    MySQL("jdbc:mysql:"),
    /** H2数据库 */
    H2DB("jdbc:h2:"),
    /** PostgreSQL数据库 */
    PostgreSQL("jdbc:postgresql:");
    /** JDBC URL 前缀 */
    private final String urlPrefix;

    /**
     * 构造函数
     * 
     * @param urlPrefix JDBC URL前缀
     */
    JDBCType(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    /**
     * 获取 JDBC URL 前缀
     * 
     * @return JDBC URL前缀
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * 根据 JDBC URL 获取 JDBC 类型
     * 
     * @param jdbcURL JDBC连接URL
     * @return 对应的JDBC类型
     * @throws RuntimeException 不支持的SQL类型时抛出
     */
    public static JDBCType getJDBCTypeByURL(String jdbcURL) {
        for (JDBCType jdbcType : values()) {
            if (StringUtils.startsWithIgnoreCase(jdbcURL, jdbcType.getUrlPrefix())) {
                return jdbcType;
            }
        }
        throw new RuntimeException("不支持的SQL类型: " + jdbcURL);
    }
}
