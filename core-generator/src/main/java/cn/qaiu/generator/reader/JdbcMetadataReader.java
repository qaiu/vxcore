package cn.qaiu.generator.reader;

import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.ForeignKeyInfo;
import cn.qaiu.vx.core.codegen.IndexInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * JDBC 数据库元数据读取器实现
 * 支持 MySQL、PostgreSQL、H2 数据库
 * 
 * @author QAIU
 */
public class JdbcMetadataReader implements DatabaseMetadataReader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcMetadataReader.class);
    
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
    
    private Connection connection;
    
    public JdbcMetadataReader(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = determineDriverClass(url);
    }
    
    public JdbcMetadataReader(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }
    
    /**
     * 根据URL确定驱动类
     */
    private String determineDriverClass(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        } else {
            throw new IllegalArgumentException("Unsupported database URL: " + url);
        }
    }
    
    /**
     * 获取数据库连接
     */
    private Future<Connection> getConnection() {
        if (connection != null && !isConnectionClosed()) {
            return Future.succeededFuture(connection);
        }
        
        return Future.future(promise -> {
            try {
                Class.forName(driverClassName);
                connection = DriverManager.getConnection(url, username, password);
                LOGGER.info("Database connection established: {}", url);
                promise.complete(connection);
            } catch (Exception e) {
                LOGGER.error("Failed to establish database connection: {}", url, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 检查连接是否已关闭
     */
    private boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
    
    @Override
    public Future<List<TableInfo>> readAllTables(String schema) {
        return getConnection().compose(conn -> {
            return Future.future(promise -> {
                try {
                    DatabaseMetaData metaData = conn.getMetaData();
                    List<TableInfo> tables = new ArrayList<>();
                    
                    try (ResultSet rs = metaData.getTables(null, schema, null, new String[]{"TABLE"})) {
                        while (rs.next()) {
                            String tableName = rs.getString("TABLE_NAME");
                            String tableComment = rs.getString("REMARKS");
                            
                            TableInfo tableInfo = new TableInfo(tableName, schema);
                            tableInfo.setComment(tableComment);
                            
                            // 读取列信息
                            List<ColumnInfo> columns = readColumnsSync(conn, schema, tableName);
                            tableInfo.setColumns(columns);
                            
                            // 读取索引信息
                            List<IndexInfo> indexes = readIndexesSync(conn, schema, tableName);
                            tableInfo.setIndexes(indexes);
                            
                            // 读取外键信息
                            List<ForeignKeyInfo> foreignKeys = readForeignKeysSync(conn, schema, tableName);
                            tableInfo.setForeignKeys(foreignKeys);
                            
                            tables.add(tableInfo);
                        }
                    }
                    
                    LOGGER.info("Read {} tables from schema: {}", tables.size(), schema);
                    promise.complete(tables);
                    
                } catch (SQLException e) {
                    LOGGER.error("Failed to read tables from schema: {}", schema, e);
                    promise.fail(e);
                }
            });
        });
    }
    
    @Override
    public Future<TableInfo> readTable(String tableName) {
        return readTable(null, tableName);
    }
    
    /**
     * 读取指定表的信息
     */
    public Future<TableInfo> readTable(String schema, String tableName) {
        return getConnection().compose(conn -> {
            return Future.future(promise -> {
                try {
                    DatabaseMetaData metaData = conn.getMetaData();
                    
                    try (ResultSet rs = metaData.getTables(null, schema, tableName, new String[]{"TABLE"})) {
                        if (rs.next()) {
                            String comment = rs.getString("REMARKS");
                            
                            TableInfo tableInfo = new TableInfo(tableName, schema);
                            tableInfo.setComment(comment);
                            
                            // 读取列信息
                            List<ColumnInfo> columns = readColumnsSync(conn, schema, tableName);
                            tableInfo.setColumns(columns);
                            
                            // 读取索引信息
                            List<IndexInfo> indexes = readIndexesSync(conn, schema, tableName);
                            tableInfo.setIndexes(indexes);
                            
                            // 读取外键信息
                            List<ForeignKeyInfo> foreignKeys = readForeignKeysSync(conn, schema, tableName);
                            tableInfo.setForeignKeys(foreignKeys);
                            
                            promise.complete(tableInfo);
                        } else {
                            promise.fail(new SQLException("Table not found: " + tableName));
                        }
                    }
                    
                } catch (SQLException e) {
                    LOGGER.error("Failed to read table: {}", tableName, e);
                    promise.fail(e);
                }
            });
        });
    }
    
    @Override
    public Future<List<ColumnInfo>> readColumns(String tableName) {
        return readColumns(null, tableName);
    }
    
    @Override
    public Future<List<ColumnInfo>> readColumns(String schema, String tableName) {
        return getConnection().compose(conn -> {
            return Future.future(promise -> {
                try {
                    List<ColumnInfo> columns = readColumnsSync(conn, schema, tableName);
                    promise.complete(columns);
                } catch (SQLException e) {
                    LOGGER.error("Failed to read columns for table: {}", tableName, e);
                    promise.fail(e);
                }
            });
        });
    }
    
    /**
     * 同步读取列信息
     */
    private List<ColumnInfo> readColumnsSync(Connection conn, String schema, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        List<ColumnInfo> columns = new ArrayList<>();
        
        try (ResultSet rs = metaData.getColumns(null, schema, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int nullable = rs.getInt("NULLABLE");
                String defaultValue = rs.getString("COLUMN_DEF");
                String comment = rs.getString("REMARKS");
                
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setColumnName(columnName);
                columnInfo.setColumnType(typeName);
                // Java 类型映射在 EntityBuilder 中处理
                columnInfo.setColumnSize(columnSize);
                columnInfo.setNullable(nullable == DatabaseMetaData.columnNullable);
                columnInfo.setDefaultValue(defaultValue);
                columnInfo.setComment(comment);
                
                columns.add(columnInfo);
            }
        }
        
        // 读取主键信息
        try (ResultSet rs = metaData.getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                columns.stream()
                    .filter(col -> columnName.equals(col.getColumnName()))
                    .findFirst()
                    .ifPresent(col -> col.setPrimaryKey(true));
            }
        }
        
        return columns;
    }
    
    /**
     * 同步读取索引信息
     */
    private List<IndexInfo> readIndexesSync(Connection conn, String schema, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        List<IndexInfo> indexes = new ArrayList<>();
        
        try (ResultSet rs = metaData.getIndexInfo(null, schema, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                boolean unique = !rs.getBoolean("NON_UNIQUE");
                String columnName = rs.getString("COLUMN_NAME");
                
                // 查找是否已存在该索引
                IndexInfo existingIndex = indexes.stream()
                    .filter(idx -> indexName.equals(idx.getIndexName()))
                    .findFirst()
                    .orElse(null);
                
                if (existingIndex == null) {
                    IndexInfo indexInfo = new IndexInfo();
                    indexInfo.setIndexName(indexName);
                    indexInfo.setUnique(unique);
                    indexInfo.setTableName(tableName);
                    indexInfo.addColumnName(columnName);
                    indexes.add(indexInfo);
                } else {
                    existingIndex.addColumnName(columnName);
                }
            }
        }
        
        return indexes;
    }
    
    /**
     * 同步读取外键信息
     */
    private List<ForeignKeyInfo> readForeignKeysSync(Connection conn, String schema, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
        
        try (ResultSet rs = metaData.getImportedKeys(null, schema, tableName)) {
            while (rs.next()) {
                String fkName = rs.getString("FK_NAME");
                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumnName = rs.getString("PKCOLUMN_NAME");
                String fkColumnName = rs.getString("FKCOLUMN_NAME");
                
                ForeignKeyInfo foreignKeyInfo = new ForeignKeyInfo();
                foreignKeyInfo.setForeignKeyName(fkName);
                foreignKeyInfo.setReferencedTableName(pkTableName);
                foreignKeyInfo.addReferencedColumnName(pkColumnName);
                foreignKeyInfo.setTableName(tableName);
                foreignKeyInfo.addColumnName(fkColumnName);
                
                foreignKeys.add(foreignKeyInfo);
            }
        }
        
        return foreignKeys;
    }
    
    /**
     * 数据库类型映射到Java类型
     */
    private String mapToJavaType(int sqlType, String typeName) {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return "String";
            case Types.INTEGER:
                return "Integer";
            case Types.BIGINT:
                return "Long";
            case Types.SMALLINT:
            case Types.TINYINT:
                return "Short";
            case Types.DECIMAL:
            case Types.NUMERIC:
                return "BigDecimal";
            case Types.FLOAT:
            case Types.REAL:
                return "Float";
            case Types.DOUBLE:
                return "Double";
            case Types.BOOLEAN:
            case Types.BIT:
                return "Boolean";
            case Types.DATE:
                return "LocalDate";
            case Types.TIME:
                return "LocalTime";
            case Types.TIMESTAMP:
                return "LocalDateTime";
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "byte[]";
            default:
                // 根据类型名称进行特殊处理
                if (typeName != null) {
                    String lowerTypeName = typeName.toLowerCase();
                    if (lowerTypeName.contains("text")) {
                        return "String";
                    } else if (lowerTypeName.contains("json")) {
                        return "String";
                    } else if (lowerTypeName.contains("uuid")) {
                        return "String";
                    }
                }
                return "String";
        }
    }
    
    @Override
    public Future<Boolean> testConnection() {
        return getConnection().map(conn -> {
            try {
                return !conn.isClosed();
            } catch (SQLException e) {
                return false;
            }
        });
    }
    
    @Override
    public Future<Void> close() {
        return Future.future(promise -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed");
                }
                promise.complete();
            } catch (SQLException e) {
                LOGGER.error("Failed to close database connection", e);
                promise.fail(e);
            }
        });
    }
}
