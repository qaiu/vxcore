#!/bin/bash

# DDL映射测试运行脚本
# 用于运行core-database模块的单元测试

echo "=========================================="
echo "DDL映射系统测试运行脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装或不在PATH中${NC}"
    exit 1
fi

# 检查Java版本
echo -e "${YELLOW}检查Java版本...${NC}"
java -version

# 进入项目目录
cd "$(dirname "$0")/.."

echo -e "${YELLOW}当前工作目录: $(pwd)${NC}"

# 清理并编译项目
echo -e "${YELLOW}清理并编译项目...${NC}"
mvn clean compile -pl core -q
if [ $? -ne 0 ]; then
    echo -e "${RED}编译core模块失败${NC}"
    exit 1
fi

mvn clean compile -pl core-database -q
if [ $? -ne 0 ]; then
    echo -e "${RED}编译core-database模块失败${NC}"
    exit 1
fi

# 运行单元测试
echo -e "${YELLOW}运行单元测试...${NC}"
mvn test -pl core-database

# 检查测试结果
if [ $? -eq 0 ]; then
    echo -e "${GREEN}=========================================="
    echo "所有测试通过！"
    echo "==========================================${NC}"
else
    echo -e "${RED}=========================================="
    echo "测试失败！"
    echo "==========================================${NC}"
    exit 1
fi

# 生成测试报告
echo -e "${YELLOW}生成测试报告...${NC}"
mvn surefire-report:report -pl core-database -q

echo -e "${GREEN}测试报告已生成: core-database/target/site/surefire-report.html${NC}"

# 运行特定测试套件
echo -e "${YELLOW}运行DDL映射测试套件...${NC}"
mvn test -pl core-database -Dtest=DdlMappingTestSuite

echo -e "${GREEN}=========================================="
echo "测试完成！"
echo "==========================================${NC}"
