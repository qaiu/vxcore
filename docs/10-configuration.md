# 配置管理

VXCore 提供了完善的配置管理功能，支持 YAML 配置、配置元数据、IDE 自动提示和配置验证。

## 🎯 配置注解

### @ConfigurationProperties

用于标记配置类，支持配置绑定和验证。

```java
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    
    @ConfigurationProperty(description = "服务器主机地址")
    private String host = "localhost";
    
    @ConfigurationProperty(description = "服务器端口")
    private int port = 8080;
    
    @ConfigurationProperty(description = "请求超时时间（毫秒）")
    private long timeout = 30000;
    
    // getters and setters
}
```

### @ConfigurationProperty

用于标记配置属性，提供元数据信息。

```java
@ConfigurationProperty(
    key = "server.host",
    description = "服务器主机地址",
    defaultValue = "localhost",
    type = String.class
)
private String host;
```

## 🔧 配置类定义

### 服务器配置

```java
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    
    @ConfigurationProperty(description = "服务器主机地址")
    private String host = "localhost";
    
    @ConfigurationProperty(description = "服务器端口")
    private int port = 8080;
    
    @ConfigurationProperty(description = "请求超时时间（毫秒）")
    private long timeout = 30000;
    
    @ConfigurationProperty(description = "是否启用SSL")
    private boolean sslEnabled = false;
    
    @ConfigurationProperty(description = "SSL证书路径")
    private String sslCertPath;
    
    @ConfigurationProperty(description = "SSL私钥路径")
    private String sslKeyPath;
    
    // getters and setters
}
```

### 数据库配置

```java
@ConfigurationProperties(prefix = "datasource")
public class DatabaseConfig {
    
    @ConfigurationProperty(description = "数据库连接URL")
    private String url;
    
    @ConfigurationProperty(description = "数据库用户名")
    private String username;
    
    @ConfigurationProperty(description = "数据库密码")
    private String password;
    
    @ConfigurationProperty(description = "数据库驱动类")
    private String driver;
    
    @ConfigurationProperty(description = "连接池最大连接数")
    private int maxPoolSize = 20;
    
    @ConfigurationProperty(description = "连接池最小连接数")
    private int minPoolSize = 5;
    
    @ConfigurationProperty(description = "连接超时时间（毫秒）")
    private long connectionTimeout = 30000;
    
    // getters and setters
}
```

### 代理配置

```java
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    
    @ConfigurationProperty(description = "是否启用代理")
    private boolean enabled = false;
    
    @ConfigurationProperty(description = "代理路由配置")
    private List<ProxyRoute> routes = new ArrayList<>();
    
    @ConfigurationProperty(description = "代理超时时间（毫秒）")
    private long timeout = 30000;
    
    // getters and setters
}

public class ProxyRoute {
    
    @ConfigurationProperty(description = "路径匹配规则")
    private String path;
    
    @ConfigurationProperty(description = "目标地址")
    private String target;
    
    @ConfigurationProperty(description = "代理类型")
    private String type = "http";
    
    // getters and setters
}
```

## 📝 配置元数据生成

### ConfigurationMetadataGenerator

```java
public class ConfigurationMetadataGenerator {
    
    /**
     * 生成配置元数据
     */
    public static void generateMetadata(String outputPath) {
        List<ConfigurationPropertyMetadata> metadata = new ArrayList<>();
        
        // 扫描配置类
        scanConfigurationClasses(metadata);
        
        // 生成JSON文件
        generateJsonFile(metadata, outputPath);
    }
    
    /**
     * 扫描配置类
     */
    private static void scanConfigurationClasses(List<ConfigurationPropertyMetadata> metadata) {
        // 扫描 @ConfigurationProperties 注解的类
        Set<Class<?>> configClasses = scanForConfigurationProperties();
        
        for (Class<?> configClass : configClasses) {
            ConfigurationProperties configProps = configClass.getAnnotation(ConfigurationProperties.class);
            String prefix = configProps.prefix();
            
            // 扫描配置属性
            scanConfigurationProperties(configClass, prefix, metadata);
        }
    }
    
    /**
     * 扫描配置属性
     */
    private static void scanConfigurationProperties(Class<?> configClass, String prefix, 
                                                   List<ConfigurationPropertyMetadata> metadata) {
        Field[] fields = configClass.getDeclaredFields();
        
        for (Field field : fields) {
            ConfigurationProperty prop = field.getAnnotation(ConfigurationProperty.class);
            if (prop != null) {
                ConfigurationPropertyMetadata meta = new ConfigurationPropertyMetadata();
                meta.setName(prefix + "." + field.getUsername());
                meta.setType(field.getType().getSimpleName());
                meta.setDescription(prop.description());
                meta.setDefaultValue(prop.defaultValue());
                meta.setRequired(prop.required());
                
                metadata.add(meta);
            }
        }
    }
    
    /**
     * 生成JSON文件
     */
    private static void generateJsonFile(List<ConfigurationPropertyMetadata> metadata, String outputPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            
            Map<String, Object> root = new HashMap<>();
            root.put("properties", metadata);
            
            mapper.writeValue(new File(outputPath), root);
        } catch (IOException e) {
            throw new RuntimeException("生成配置元数据失败", e);
        }
    }
}
```

### 配置元数据文件

```json
{
  "properties": [
    {
      "name": "server.host",
      "type": "String",
      "description": "服务器主机地址",
      "defaultValue": "localhost"
    },
    {
      "name": "server.port",
      "type": "int",
      "description": "服务器端口",
      "defaultValue": "8080"
    },
    {
      "name": "server.timeout",
      "type": "long",
      "description": "请求超时时间（毫秒）",
      "defaultValue": "30000"
    },
    {
      "name": "datasource.url",
      "type": "String",
      "description": "数据库连接URL",
      "required": true
    },
    {
      "name": "datasource.username",
      "type": "String",
      "description": "数据库用户名",
      "required": true
    },
    {
      "name": "datasource.password",
      "type": "String",
      "description": "数据库密码",
      "required": true
    }
  ]
}
```

## 🔧 配置绑定器

### ConfigurationPropertyBinder

```java
public class ConfigurationPropertyBinder {
    
    /**
     * 绑定配置到对象
     */
    public static <T> T bind(JsonObject config, Class<T> configClass) {
        try {
            T instance = configClass.getDeclaredConstructor().newInstance();
            bindProperties(config, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("配置绑定失败", e);
        }
    }
    
    /**
     * 绑定属性
     */
    private static void bindProperties(JsonObject config, Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            ConfigurationProperty prop = field.getAnnotation(ConfigurationProperty.class);
            if (prop != null) {
                String key = prop.key();
                if (key.isEmpty()) {
                    key = field.getUsername();
                }
                
                Object value = getValue(config, key, field.getType());
                if (value != null) {
                    setFieldValue(instance, field, value);
                }
            }
        }
    }
    
    /**
     * 获取配置值
     */
    private static Object getValue(JsonObject config, String key, Class<?> type) {
        if (type == String.class) {
            return config.getString(key);
        } else if (type == int.class || type == Integer.class) {
            return config.getInteger(key);
        } else if (type == long.class || type == Long.class) {
            return config.getLong(key);
        } else if (type == boolean.class || type == Boolean.class) {
            return config.getBoolean(key);
        } else if (type == double.class || type == Double.class) {
            return config.getDouble(key);
        } else if (type == List.class) {
            return config.getJsonArray(key);
        } else if (type == Map.class) {
            return config.getJsonObject(key);
        }
        
        return null;
    }
    
    /**
     * 设置字段值
     */
    private static void setFieldValue(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置字段值失败", e);
        }
    }
}
```

## 📊 配置验证

### 配置验证器

```java
public class ConfigurationValidator {
    
    /**
     * 验证配置
     */
    public static void validate(Object config) {
        Field[] fields = config.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            ConfigurationProperty prop = field.getAnnotation(ConfigurationProperty.class);
            if (prop != null) {
                validateField(config, field, prop);
            }
        }
    }
    
    /**
     * 验证字段
     */
    private static void validateField(Object config, Field field, ConfigurationProperty prop) {
        try {
            field.setAccessible(true);
            Object value = field.get(config);
            
            // 必填验证
            if (prop.required() && value == null) {
                throw new ValidationException("配置项 " + prop.key() + " 不能为空");
            }
            
            // 类型验证
            if (value != null) {
                validateValue(value, prop);
            }
            
        } catch (IllegalAccessException e) {
            throw new RuntimeException("配置验证失败", e);
        }
    }
    
    /**
     * 验证值
     */
    private static void validateValue(Object value, ConfigurationProperty prop) {
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.trim().isEmpty()) {
                throw new ValidationException("配置项 " + prop.key() + " 不能为空字符串");
            }
        } else if (value instanceof Number) {
            Number numValue = (Number) value;
            if (numValue.doubleValue() < 0) {
                throw new ValidationException("配置项 " + prop.key() + " 不能为负数");
            }
        }
    }
}
```

## 🚀 使用示例

### 1. 配置文件

```yaml
# application.yml
server:
  host: localhost
  port: 8080
  timeout: 30000
  ssl:
    enabled: false
    cert-path: /path/to/cert.pem
    key-path: /path/to/key.pem

datasource:
  url: jdbc:h2:mem:testdb
  username: sa
  password: ""
  driver: org.h2.Driver
  max-pool-size: 20
  min-pool-size: 5
  connection-timeout: 30000

proxy:
  enabled: true
  timeout: 30000
  routes:
    - path: /api/v1/*
      target: http://backend:8080
      type: http
    - path: /ws/*
      target: ws://backend:8080
      type: websocket

logging:
  level:
    root: INFO
    cn.qaiu.vx: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. 配置加载

```java
@Component
public class ConfigurationLoader {
    
    private ServerConfig serverConfig;
    private DatabaseConfig databaseConfig;
    private ProxyConfig proxyConfig;
    
    @PostConstruct
    public void loadConfigurations() {
        // 加载配置文件
        JsonObject config = ConfigUtil.loadConfig("application.yml");
        
        // 绑定配置
        serverConfig = ConfigurationPropertyBinder.bind(
            config.getJsonObject("server"), ServerConfig.class);
        databaseConfig = ConfigurationPropertyBinder.bind(
            config.getJsonObject("datasource"), DatabaseConfig.class);
        proxyConfig = ConfigurationPropertyBinder.bind(
            config.getJsonObject("proxy"), ProxyConfig.class);
        
        // 验证配置
        ConfigurationValidator.validate(serverConfig);
        ConfigurationValidator.validate(databaseConfig);
        ConfigurationValidator.validate(proxyConfig);
    }
    
    public ServerConfig getServerConfig() {
        return serverConfig;
    }
    
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
    
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }
}
```

### 3. 配置使用

```java
@RouteHandler("/api")
public class ConfigController {
    
    @Autowired
    private ConfigurationLoader configLoader;
    
    @RouteMapping(value = "/config", method = HttpMethod.GET)
    public Future<JsonResult> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("server", configLoader.getServerConfig());
        config.put("database", configLoader.getDatabaseConfig());
        config.put("proxy", configLoader.getProxyConfig());
        
        return Future.succeededFuture(JsonResult.success(config));
    }
}
```

## 🔍 IDE 支持

### 1. 自动提示

配置元数据文件 `META-INF/spring-configuration-metadata.json` 可以让 IDE 提供配置项的自动提示：

```json
{
  "properties": [
    {
      "name": "server.host",
      "type": "java.lang.String",
      "description": "服务器主机地址",
      "defaultValue": "localhost"
    },
    {
      "name": "server.port",
      "type": "java.lang.Integer",
      "description": "服务器端口",
      "defaultValue": 8080
    }
  ]
}
```

### 2. 配置验证

IDE 可以根据配置元数据提供配置验证：

- 必填项检查
- 类型检查
- 默认值提示
- 描述信息显示

## 📚 最佳实践

### 1. 配置分层

```java
// 基础配置
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String name;
    private String version;
    private String environment;
}

// 服务器配置
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    private String host;
    private int port;
    private SslConfig ssl;
}

// SSL配置
public class SslConfig {
    private boolean enabled;
    private String certPath;
    private String keyPath;
}
```

### 2. 配置验证

```java
@ConfigurationProperties(prefix = "datasource")
public class DatabaseConfig {
    
    @ConfigurationProperty(required = true)
    private String url;
    
    @ConfigurationProperty(required = true)
    private String username;
    
    @ConfigurationProperty(required = true)
    private String password;
    
    @PostConstruct
    public void validate() {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("数据库URL不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("数据库用户名不能为空");
        }
        if (password == null) {
            throw new ValidationException("数据库密码不能为空");
        }
    }
}
```

### 3. 配置热更新

```java
@Component
public class ConfigWatcher {
    
    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        // 处理配置变更
        if (event.getKey().startsWith("server.")) {
            // 重新加载服务器配置
            reloadServerConfig();
        } else if (event.getKey().startsWith("datasource.")) {
            // 重新加载数据库配置
            reloadDatabaseConfig();
        }
    }
}
```

## 📚 相关文档

- [路由注解指南](08-routing-annotations.md)
- [异常处理机制](09-exception-handling.md)
- [Lambda查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md)
- [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md)
