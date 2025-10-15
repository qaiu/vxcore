#!/bin/bash

###############################################################################
# CI模式测试脚本
# 
# 这个脚本在本地模拟GitHub Actions CI环境，排除MySQL和PostgreSQL测试
# 
# 用法:
#   ./scripts/test-ci-mode.sh              # 运行所有模块的CI测试
#   ./scripts/test-ci-mode.sh core         # 只测试core模块
#   ./scripts/test-ci-mode.sh core-database # 只测试core-database模块
###############################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  CI模式测试 - 排除外部数据库测试${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 设置CI环境变量
export CI=true
export DB_TYPE=h2

# 显示Maven版本
echo -e "${YELLOW}Maven版本:${NC}"
mvn --version
echo ""

# 清理之前的构建
echo -e "${YELLOW}清理之前的构建...${NC}"
mvn clean -q
echo -e "${GREEN}✓ 清理完成${NC}"
echo ""

# 编译项目
echo -e "${YELLOW}编译项目...${NC}"
mvn compile -B
echo -e "${GREEN}✓ 编译完成${NC}"
echo ""

# 运行测试
if [ -z "$1" ]; then
    # 运行所有模块测试
    echo -e "${YELLOW}运行所有模块测试 (CI模式 - 排除MySQL、PostgreSQL和JDBC连接问题测试)...${NC}"
    mvn test -B -Pci
else
    # 运行指定模块测试
    echo -e "${YELLOW}运行 $1 模块测试 (CI模式 - 排除MySQL、PostgreSQL和JDBC连接问题测试)...${NC}"
    mvn test -B -pl "$1" -Pci
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  测试完成!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 显示测试结果位置
echo -e "${YELLOW}测试报告位置:${NC}"
find . -path "*/target/surefire-reports" -type d | while read dir; do
    echo "  - $dir"
done
echo ""

# 统计测试结果
echo -e "${YELLOW}测试统计:${NC}"
find . -name "TEST-*.xml" -type f | while read xml; do
    if [ -f "$xml" ]; then
        tests=$(grep -oP 'tests="\K[0-9]+' "$xml" | head -1)
        failures=$(grep -oP 'failures="\K[0-9]+' "$xml" | head -1)
        errors=$(grep -oP 'errors="\K[0-9]+' "$xml" | head -1)
        skipped=$(grep -oP 'skipped="\K[0-9]+' "$xml" | head -1)
        
        testfile=$(basename "$xml")
        if [ "$failures" = "0" ] && [ "$errors" = "0" ]; then
            echo -e "  ${GREEN}✓${NC} $testfile: $tests 个测试通过"
        else
            echo -e "  ${RED}✗${NC} $testfile: $tests 个测试, $failures 失败, $errors 错误"
        fi
    fi
done

echo ""
echo -e "${YELLOW}注意:${NC} 以下测试已被排除:"
echo "  - MySQL*Test.java (外部数据库依赖)"
echo "  - PostgreSQL*Test.java (外部数据库依赖)"
echo "  - LambdaQueryTest.java (JDBC连接配置问题，待修复)"
echo ""
echo "如需运行完整测试，请使用: ${YELLOW}mvn test${NC}"
echo "如需单独测试LambdaQueryTest，请使用: ${YELLOW}mvn test -Dtest=LambdaQueryTest${NC}"

