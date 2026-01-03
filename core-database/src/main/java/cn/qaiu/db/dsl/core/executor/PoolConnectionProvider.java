package cn.qaiu.db.dsl.core.executor;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 基于Vert.x Pool的连接提供者
 * 将Vert.x Pool适配为jOOQ的ConnectionProvider
 * 
 * @author qaiu
 */
public class PoolConnectionProvider implements ConnectionProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PoolConnectionProvider.class);
    
    private final Pool pool;
    
    public PoolConnectionProvider(Pool pool) {
        this.pool = pool;
    }
    
    @Override
    public Connection acquire() throws DataAccessException {
        try {
            // 从Vert.x Pool获取连接并转换为JDBC Connection
            CompletableFuture<Connection> future = new CompletableFuture<>();
            
            pool.getConnection()
                .onSuccess(sqlConnection -> {
                    try {
                        // 对于JDBCPool，我们需要通过反射获取底层JDBC连接
                        if (pool.getClass().getName().contains("JDBCPool")) {
                            // 使用反射获取JDBC连接
                            java.lang.reflect.Field connectionField = sqlConnection.getClass().getDeclaredField("connection");
                            connectionField.setAccessible(true);
                            Connection jdbcConnection = (Connection) connectionField.get(sqlConnection);
                            future.complete(jdbcConnection);
                        } else {
                            // 对于其他类型的Pool，抛出异常
                            future.completeExceptionally(new DataAccessException("Unsupported pool type: " + pool.getClass().getName()));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(new DataAccessException("Failed to get JDBC connection", e));
                    }
                })
                .onFailure(future::completeExceptionally);
            
            // 等待连接获取完成
            return future.get(10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            throw new DataAccessException("Failed to acquire connection from pool", e);
        }
    }
    
    @Override
    public void release(Connection connection) throws DataAccessException {
        try {
            // 关闭JDBC连接，Vert.x Pool会自动管理
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to close connection: {}", e.getMessage());
        }
    }
}
