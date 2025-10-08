#!/bin/bash

# PostgreSQL DDL测试运行脚本
# 需要PostgreSQL服务器运行在127.0.0.1:5432

echo "=========================================="
echo "PostgreSQL DDL映射测试"
echo "=========================================="

# 检查PostgreSQL是否运行
echo "检查PostgreSQL连接..."
if ! pg_isready -h 127.0.0.1 -p 5432 -U postgres > /dev/null 2>&1; then
    echo "❌ PostgreSQL服务器未运行或无法连接"
    echo "请确保PostgreSQL运行在127.0.0.1:5432"
    echo "用户名: postgres, 密码: 空"
    exit 1
fi

echo "✅ PostgreSQL服务器连接正常"

# 检查测试数据库是否存在
echo "检查测试数据库..."
if ! psql -h 127.0.0.1 -p 5432 -U postgres -d testdb -c "SELECT 1;" > /dev/null 2>&1; then
    echo "创建测试数据库..."
    createdb -h 127.0.0.1 -p 5432 -U postgres testdb
    if [ $? -eq 0 ]; then
        echo "✅ 测试数据库创建成功"
    else
        echo "❌ 测试数据库创建失败"
        exit 1
    fi
else
    echo "✅ 测试数据库已存在"
fi

# 运行PostgreSQL DDL测试
echo "运行PostgreSQL DDL测试..."
mvn test -pl core-database -Dtest=PostgreSQLDdlTest

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL DDL测试通过"
else
    echo "❌ PostgreSQL DDL测试失败"
    exit 1
fi

# 运行PostgreSQL集成测试
echo "运行PostgreSQL集成测试..."
mvn test -pl core-database -Dtest=PostgreSQLIntegrationTest

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL集成测试通过"
else
    echo "❌ PostgreSQL集成测试失败"
    exit 1
fi

echo "=========================================="
echo "🎉 所有PostgreSQL测试通过！"
echo "=========================================="
