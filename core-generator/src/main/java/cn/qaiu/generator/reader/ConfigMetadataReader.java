package cn.qaiu.generator.reader;

import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.ForeignKeyInfo;
import cn.qaiu.vx.core.codegen.IndexInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 配置文件元数据读取器实现
 * 支持 JSON 和 YAML 格式的表结构定义
 * 
 * @author QAIU
 */
public class ConfigMetadataReader implements DatabaseMetadataReader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMetadataReader.class);
    
    private final String configFilePath;
    private final ObjectMapper objectMapper;
    private JsonNode configRoot;
    
    public ConfigMetadataReader(String configFilePath) {
        this.configFilePath = configFilePath;
        this.objectMapper = new ObjectMapper();
    }
    
    public ConfigMetadataReader(String configFilePath, boolean isYaml) {
        this.configFilePath = configFilePath;
        if (isYaml) {
            this.objectMapper = new ObjectMapper(new YAMLFactory());
        } else {
            this.objectMapper = new ObjectMapper();
        }
    }
    
    /**
     * 加载配置文件
     */
    private Future<Void> loadConfig() {
        if (configRoot != null) {
            return Future.succeededFuture();
        }
        
        return Future.future(promise -> {
            try {
                File configFile = new File(configFilePath);
                if (!configFile.exists()) {
                    promise.fail(new IOException("Config file not found: " + configFilePath));
                    return;
                }
                
                configRoot = objectMapper.readTree(configFile);
                LOGGER.info("Config file loaded: {}", configFilePath);
                promise.complete();
                
            } catch (IOException e) {
                LOGGER.error("Failed to load config file: {}", configFilePath, e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public Future<List<TableInfo>> readAllTables(String schema) {
        return loadConfig().compose(v -> {
            return Future.future(promise -> {
                try {
                    List<TableInfo> tables = new ArrayList<>();
                    
                    JsonNode tablesNode = configRoot.get("tables");
                    if (tablesNode != null && tablesNode.isArray()) {
                        for (JsonNode tableNode : tablesNode) {
                            TableInfo tableInfo = parseTableInfo(tableNode);
                            if (tableInfo != null) {
                                tables.add(tableInfo);
                            }
                        }
                    }
                    
                    LOGGER.info("Read {} tables from config file", tables.size());
                    promise.complete(tables);
                    
                } catch (Exception e) {
                    LOGGER.error("Failed to read tables from config", e);
                    promise.fail(e);
                }
            });
        });
    }
    
    @Override
    public Future<TableInfo> readTable(String tableName) {
        return loadConfig().compose(v -> {
            return Future.future(promise -> {
                try {
                    JsonNode tablesNode = configRoot.get("tables");
                    if (tablesNode != null && tablesNode.isArray()) {
                        for (JsonNode tableNode : tablesNode) {
                            String nodeTableName = tableNode.get("tableName").asText();
                            if (tableName.equals(nodeTableName)) {
                                TableInfo tableInfo = parseTableInfo(tableNode);
                                promise.complete(tableInfo);
                                return;
                            }
                        }
                    }
                    
                    promise.fail(new RuntimeException("Table not found: " + tableName));
                    
                } catch (Exception e) {
                    LOGGER.error("Failed to read table: {}", tableName, e);
                    promise.fail(e);
                }
            });
        });
    }
    
    @Override
    public Future<List<ColumnInfo>> readColumns(String tableName) {
        return readTable(tableName).map(tableInfo -> tableInfo.getColumns());
    }
    
    @Override
    public Future<List<ColumnInfo>> readColumns(String schema, String tableName) {
        return readColumns(tableName);
    }
    
    /**
     * 解析表信息
     */
    private TableInfo parseTableInfo(JsonNode tableNode) {
        try {
            String tableName = tableNode.get("tableName").asText();
            String comment = tableNode.has("comment") ? tableNode.get("comment").asText() : null;
            String schema = tableNode.has("schema") ? tableNode.get("schema").asText() : null;
            
            TableInfo tableInfo = new TableInfo(tableName, schema);
            tableInfo.setComment(comment);
            
            // 解析列信息
            JsonNode columnsNode = tableNode.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                List<ColumnInfo> columns = new ArrayList<>();
                for (JsonNode columnNode : columnsNode) {
                    ColumnInfo columnInfo = parseColumnInfo(columnNode);
                    if (columnInfo != null) {
                        columns.add(columnInfo);
                    }
                }
                tableInfo.setColumns(columns);
            }
            
            // 解析索引信息
            JsonNode indexesNode = tableNode.get("indexes");
            if (indexesNode != null && indexesNode.isArray()) {
                List<IndexInfo> indexes = new ArrayList<>();
                for (JsonNode indexNode : indexesNode) {
                    IndexInfo indexInfo = parseIndexInfo(indexNode);
                    if (indexInfo != null) {
                        indexes.add(indexInfo);
                    }
                }
                tableInfo.setIndexes(indexes);
            }
            
            // 解析外键信息
            JsonNode foreignKeysNode = tableNode.get("foreignKeys");
            if (foreignKeysNode != null && foreignKeysNode.isArray()) {
                List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
                for (JsonNode fkNode : foreignKeysNode) {
                    ForeignKeyInfo fkInfo = parseForeignKeyInfo(fkNode);
                    if (fkInfo != null) {
                        foreignKeys.add(fkInfo);
                    }
                }
                tableInfo.setForeignKeys(foreignKeys);
            }
            
            return tableInfo;
            
        } catch (Exception e) {
            LOGGER.error("Failed to parse table info", e);
            return null;
        }
    }
    
    /**
     * 解析列信息
     */
    private ColumnInfo parseColumnInfo(JsonNode columnNode) {
        try {
            String columnName = columnNode.get("columnName").asText();
            String columnType = columnNode.get("columnType").asText();
            String javaType = columnNode.has("javaType") ? columnNode.get("javaType").asText() : mapToJavaType(columnType);
            
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(columnName);
            columnInfo.setColumnType(columnType);
            // Java 类型映射在 EntityBuilder 中处理
            
            // 可选属性
            if (columnNode.has("columnSize")) {
                columnInfo.setColumnSize(columnNode.get("columnSize").asInt());
            }
            if (columnNode.has("nullable")) {
                columnInfo.setNullable(columnNode.get("nullable").asBoolean());
            }
            if (columnNode.has("defaultValue")) {
                columnInfo.setDefaultValue(columnNode.get("defaultValue").asText());
            }
            if (columnNode.has("comment")) {
                columnInfo.setComment(columnNode.get("comment").asText());
            }
            if (columnNode.has("primaryKey")) {
                columnInfo.setPrimaryKey(columnNode.get("primaryKey").asBoolean());
            }
            
            return columnInfo;
            
        } catch (Exception e) {
            LOGGER.error("Failed to parse column info", e);
            return null;
        }
    }
    
    /**
     * 解析索引信息
     */
    private IndexInfo parseIndexInfo(JsonNode indexNode) {
        try {
            String indexName = indexNode.get("indexName").asText();
            boolean unique = indexNode.has("unique") ? indexNode.get("unique").asBoolean() : false;
            
            IndexInfo indexInfo = new IndexInfo();
            indexInfo.setIndexName(indexName);
            indexInfo.setUnique(unique);
            
            // 解析索引列
            JsonNode columnsNode = indexNode.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                for (JsonNode columnNode : columnsNode) {
                    indexInfo.addColumnName(columnNode.asText());
                }
            }
            
            return indexInfo;
            
        } catch (Exception e) {
            LOGGER.error("Failed to parse index info", e);
            return null;
        }
    }
    
    /**
     * 解析外键信息
     */
    private ForeignKeyInfo parseForeignKeyInfo(JsonNode fkNode) {
        try {
            String fkName = fkNode.get("fkName").asText();
            String pkTableName = fkNode.get("referencedTableName").asText();
            String pkColumnName = fkNode.get("referencedColumnName").asText();
            String fkColumnName = fkNode.get("columnName").asText();
            
            ForeignKeyInfo fkInfo = new ForeignKeyInfo();
            fkInfo.setForeignKeyName(fkName);
        fkInfo.setReferencedTableName(pkTableName);
        fkInfo.addReferencedColumnName(pkColumnName);
        fkInfo.addColumnName(fkColumnName);
            
            return fkInfo;
            
        } catch (Exception e) {
            LOGGER.error("Failed to parse foreign key info", e);
            return null;
        }
    }
    
    /**
     * 数据库类型映射到Java类型
     */
    private String mapToJavaType(String dbType) {
        if (dbType == null) {
            return "String";
        }
        
        String lowerType = dbType.toLowerCase();
        
        if (lowerType.contains("varchar") || lowerType.contains("char") || 
            lowerType.contains("text") || lowerType.contains("json")) {
            return "String";
        } else if (lowerType.contains("int")) {
            if (lowerType.contains("bigint")) {
                return "Long";
            } else if (lowerType.contains("smallint") || lowerType.contains("tinyint")) {
                return "Short";
            } else {
                return "Integer";
            }
        } else if (lowerType.contains("decimal") || lowerType.contains("numeric")) {
            return "BigDecimal";
        } else if (lowerType.contains("float")) {
            return "Float";
        } else if (lowerType.contains("double")) {
            return "Double";
        } else if (lowerType.contains("boolean") || lowerType.contains("bit")) {
            return "Boolean";
        } else if (lowerType.contains("date")) {
            return "LocalDate";
        } else if (lowerType.contains("time")) {
            return "LocalTime";
        } else if (lowerType.contains("timestamp")) {
            return "LocalDateTime";
        } else if (lowerType.contains("blob") || lowerType.contains("binary")) {
            return "byte[]";
        } else {
            return "String";
        }
    }
    
    @Override
    public Future<Boolean> testConnection() {
        return loadConfig().map(v -> true);
    }
    
    @Override
    public Future<Void> close() {
        return Future.succeededFuture();
    }
}
