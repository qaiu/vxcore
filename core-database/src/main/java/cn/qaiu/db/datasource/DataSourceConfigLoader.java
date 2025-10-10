package cn.qaiu.db.datasource;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.io.IOException; // 未使用
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 数据源配置加载器
 * 支持从多种配置源加载数据源配置
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceConfigLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfigLoader.class);
    
    private final Vertx vertx;
    private final DataSourceManager dataSourceManager;
    
    public DataSourceConfigLoader(Vertx vertx) {
        this.vertx = vertx;
        this.dataSourceManager = DataSourceManager.getInstance(vertx);
    }
    
    /**
     * 从JsonObject加载数据源配置
     */
    public Future<Void> loadFromJsonObject(JsonObject config) {
        return Future.future(promise -> {
            try {
                JsonObject datasources = config.getJsonObject("datasources");
                if (datasources == null) {
                    LOGGER.warn("No datasources configuration found");
                    promise.complete();
                    return;
                }
                
                Future<Void> allFutures = Future.succeededFuture();
                for (String name : datasources.fieldNames()) {
                    JsonObject dsConfig = datasources.getJsonObject(name);
                    allFutures = allFutures.compose(v -> 
                        dataSourceManager.registerDataSource(name, dsConfig));
                }
                
                allFutures.onComplete(promise);
            } catch (Exception e) {
                LOGGER.error("Failed to load datasource config from JsonObject", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 从文件加载数据源配置
     */
    public Future<Void> loadFromFile(String configPath) {
        return Future.future(promise -> {
            vertx.fileSystem().readFile(configPath)
                .onSuccess(buffer -> {
                    try {
                        JsonObject config = buffer.toJsonObject();
                        loadFromJsonObject(config).onComplete(promise);
                    } catch (Exception e) {
                        LOGGER.error("Failed to parse config file: {}", configPath, e);
                        promise.fail(e);
                    }
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to read config file: {}", configPath, error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 从类路径加载数据源配置
     */
    public Future<Void> loadFromClasspath(String configPath) {
        return Future.future(promise -> {
            try {
                Path path = Paths.get(getClass().getClassLoader().getResource(configPath).toURI());
                String content = Files.readString(path);
                JsonObject config = new JsonObject(content);
                loadFromJsonObject(config).onComplete(promise);
            } catch (Exception e) {
                LOGGER.error("Failed to load config from classpath: {}", configPath, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 从环境变量加载数据源配置
     */
    public Future<Void> loadFromEnvironment() {
        return Future.future(promise -> {
            try {
                JsonObject config = new JsonObject();
                JsonObject datasources = new JsonObject();
                
                // 检查环境变量
                String defaultUrl = System.getenv("DATASOURCE_DEFAULT_URL");
                String defaultUser = System.getenv("DATASOURCE_DEFAULT_USER");
                String defaultPassword = System.getenv("DATASOURCE_DEFAULT_PASSWORD");
                
                if (defaultUrl != null) {
                    JsonObject defaultDs = new JsonObject()
                        .put("type", "jdbc")
                        .put("jdbcUrl", defaultUrl)
                        .put("user", defaultUser != null ? defaultUser : "")
                        .put("password", defaultPassword != null ? defaultPassword : "");
                    datasources.put("default", defaultDs);
                }
                
                // 检查其他数据源
                for (int i = 1; i <= 10; i++) {
                    String url = System.getenv("DATASOURCE_" + i + "_URL");
                    if (url != null) {
                        String name = System.getenv("DATASOURCE_" + i + "_NAME");
                        if (name == null) {
                            name = "datasource" + i;
                        }
                        
                        JsonObject ds = new JsonObject()
                            .put("type", "jdbc")
                            .put("jdbcUrl", url)
                            .put("user", System.getenv("DATASOURCE_" + i + "_USER"))
                            .put("password", System.getenv("DATASOURCE_" + i + "_PASSWORD"));
                        datasources.put(name, ds);
                    }
                }
                
                if (!datasources.isEmpty()) {
                    config.put("datasources", datasources);
                    loadFromJsonObject(config).onComplete(promise);
                } else {
                    LOGGER.info("No datasource configuration found in environment variables");
                    promise.complete();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load datasource config from environment", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 加载默认配置
     */
    public Future<Void> loadDefaultConfig() {
        return Future.future(promise -> {
            // 尝试从多个位置加载配置
            Future<Void> loadFuture = loadFromClasspath("datasource.json")
                .recover(v -> loadFromClasspath("application.json"))
                .recover(v -> loadFromClasspath("config.json"))
                .recover(v -> loadFromEnvironment());
            
            loadFuture.onComplete(promise);
        });
    }
    
    /**
     * 初始化所有数据源
     */
    public Future<Void> initializeAllDataSources() {
        return dataSourceManager.initializeAllDataSources();
    }
}
