#!/bin/bash

# MySQL DDL测试运行脚本
# 用于运行MySQL相关的DDL测试

echo "=========================================="
echo "MySQL DDL测试运行脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Java环境
echo -e "${YELLOW}检查Java环境...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到Java环境${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}错误: 需要Java 17或更高版本，当前版本: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}Java版本检查通过: $JAVA_VERSION${NC}"

# 检查Maven环境
echo -e "${YELLOW}检查Maven环境...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到Maven环境${NC}"
    exit 1
fi
echo -e "${GREEN}Maven环境检查通过${NC}"

# 检查MySQL连接
echo -e "${YELLOW}检查MySQL连接...${NC}"
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="testuser"
MYSQL_PASS="testpass"

# 使用nc检查端口连通性
if ! nc -z "$MYSQL_HOST" "$MYSQL_PORT" 2>/dev/null; then
    echo -e "${RED}错误: 无法连接到MySQL服务器 $MYSQL_HOST:$MYSQL_PORT${NC}"
    echo -e "${YELLOW}请检查网络连接和服务器状态${NC}"
    exit 1
fi
echo -e "${GREEN}MySQL服务器连接检查通过${NC}"

# 编译项目
echo -e "${YELLOW}编译项目...${NC}"
if ! mvn clean compile test-compile -q; then
    echo -e "${RED}错误: 项目编译失败${NC}"
    exit 1
fi
echo -e "${GREEN}项目编译成功${NC}"

# 运行MySQL测试
echo -e "${YELLOW}运行MySQL DDL测试...${NC}"
echo "=========================================="

# 运行MySQL简单测试
echo -e "${YELLOW}1. 运行MySQL简单测试...${NC}"
if mvn test -Dtest=MySQLSimpleTest -q; then
    echo -e "${GREEN}✓ MySQL简单测试通过${NC}"
else
    echo -e "${RED}✗ MySQL简单测试失败${NC}"
    exit 1
fi

# 运行MySQL集成测试
echo -e "${YELLOW}2. 运行MySQL集成测试...${NC}"
if mvn test -Dtest=MySQLIntegrationTest -q; then
    echo -e "${GREEN}✓ MySQL集成测试通过${NC}"
else
    echo -e "${RED}✗ MySQL集成测试失败${NC}"
    exit 1
fi

# 运行所有MySQL相关测试
echo -e "${YELLOW}3. 运行所有MySQL相关测试...${NC}"
if mvn test -Dtest="*MySQL*Test" -q; then
    echo -e "${GREEN}✓ 所有MySQL测试通过${NC}"
else
    echo -e "${RED}✗ 部分MySQL测试失败${NC}"
    exit 1
fi

echo "=========================================="
echo -e "${GREEN}所有MySQL DDL测试完成！${NC}"
echo "=========================================="

# 显示测试报告
echo -e "${YELLOW}测试报告位置:${NC}"
echo "- Surefire报告: target/surefire-reports/"
echo "- 测试日志: logs/"

# 清理测试数据（可选）
read -p "是否清理测试数据？(y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}清理测试数据...${NC}"
    # 这里可以添加清理测试数据的SQL命令
    echo -e "${GREEN}测试数据清理完成${NC}"
fi

echo -e "${GREEN}MySQL DDL测试脚本执行完成！${NC}"
