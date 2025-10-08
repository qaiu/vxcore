# Debug Scripts Directory

这个目录包含用于调试和测试jOOQ DSL框架的各种脚本文件。

## 文件说明

### 字段映射相关
- `debug_field_mapping.java` - 测试字段名映射功能
- `debug_field_mapping_simple.java` - 简化版字段映射测试

### ID字段相关
- `debug_id_issue.java` - 测试ID字段映射问题
- `debug_id_mapping.java` - ID字段映射测试
- `debug_id_mapping_detailed.java` - 详细ID字段映射测试

### 插入操作相关
- `debug_insert.java` - 测试插入操作
- `debug_simple_insert.java` - 简化插入测试
- `debug_test_insert.java` - 完整插入测试

### 查询操作相关
- `debug_query_test.java` - 查询操作测试
- `debug_insert_query.java` - 插入后查询测试

### 表结构相关
- `debug_table_name.java` - 表名和主键测试
- `debug_sql.java` - SQL生成测试

## 使用方法

这些脚本主要用于开发过程中的调试和问题排查。运行前需要：

1. 确保项目已编译：`mvn compile`
2. 运行脚本：
   ```bash
   javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" debug_script_name.java
   java -cp ".:target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" debug_script_name
   ```

## 注意事项

- 这些文件仅用于开发调试，不应包含在生产代码中
- 运行这些脚本可能会创建临时的数据库连接和表
- 某些脚本可能需要特定的数据库环境配置

## 清理

定期清理这些调试文件，保持项目目录的整洁：
```bash
rm -f debug-scripts/*.class
```
