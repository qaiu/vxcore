#!/bin/bash

###############################################################################
# CI配置验证脚本
# 
# 验证GitHub Actions CI配置是否正确设置
###############################################################################

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CI配置验证${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查1: GitHub workflows目录存在
echo -e "${YELLOW}检查1: GitHub workflows目录...${NC}"
if [ -d ".github/workflows" ]; then
    echo -e "${GREEN}✓ .github/workflows 目录存在${NC}"
else
    echo -e "${RED}✗ .github/workflows 目录不存在${NC}"
    exit 1
fi
echo ""

# 检查2: ci.yml文件存在
echo -e "${YELLOW}检查2: CI工作流文件...${NC}"
if [ -f ".github/workflows/ci.yml" ]; then
    echo -e "${GREEN}✓ ci.yml 文件存在${NC}"
    
    # 检查是否包含CI环境变量
    if grep -q "CI: true" .github/workflows/ci.yml; then
        echo -e "${GREEN}✓ ci.yml 配置了 CI=true 环境变量${NC}"
    else
        echo -e "${RED}✗ ci.yml 未配置 CI=true 环境变量${NC}"
    fi
else
    echo -e "${RED}✗ ci.yml 文件不存在${NC}"
    exit 1
fi
echo ""

# 检查3: pom.xml中的profile配置
echo -e "${YELLOW}检查3: Maven Profile配置...${NC}"
if [ -f "core-database/pom.xml" ]; then
    echo -e "${GREEN}✓ core-database/pom.xml 存在${NC}"
    
    if grep -q '<id>ci</id>' core-database/pom.xml; then
        echo -e "${GREEN}✓ 找到 ci profile${NC}"
    else
        echo -e "${RED}✗ 未找到 ci profile${NC}"
        exit 1
    fi
    
    if grep -q '<id>local</id>' core-database/pom.xml; then
        echo -e "${GREEN}✓ 找到 local profile${NC}"
    else
        echo -e "${RED}✗ 未找到 local profile${NC}"
        exit 1
    fi
    
    if grep -q 'MySQL\*Test.java' core-database/pom.xml; then
        echo -e "${GREEN}✓ 配置了MySQL测试排除${NC}"
    else
        echo -e "${RED}✗ 未配置MySQL测试排除${NC}"
    fi
    
    if grep -q 'PostgreSQL\*Test.java' core-database/pom.xml; then
        echo -e "${GREEN}✓ 配置了PostgreSQL测试排除${NC}"
    else
        echo -e "${RED}✗ 未配置PostgreSQL测试排除${NC}"
    fi
else
    echo -e "${RED}✗ core-database/pom.xml 不存在${NC}"
    exit 1
fi
echo ""

# 检查4: 测试文件存在
echo -e "${YELLOW}检查4: 测试文件...${NC}"
MYSQL_TESTS=$(find core-database/src/test -name "*MySQL*Test.java" 2>/dev/null | wc -l)
PGSQL_TESTS=$(find core-database/src/test -name "*PostgreSQL*Test.java" 2>/dev/null | wc -l)

echo -e "${GREEN}✓ 找到 $MYSQL_TESTS 个MySQL测试文件${NC}"
echo -e "${GREEN}✓ 找到 $PGSQL_TESTS 个PostgreSQL测试文件${NC}"
echo ""

# 检查5: CI测试脚本
echo -e "${YELLOW}检查5: CI测试脚本...${NC}"
if [ -f "scripts/test-ci-mode.sh" ]; then
    echo -e "${GREEN}✓ test-ci-mode.sh 存在${NC}"
    
    if [ -x "scripts/test-ci-mode.sh" ]; then
        echo -e "${GREEN}✓ test-ci-mode.sh 有执行权限${NC}"
    else
        echo -e "${YELLOW}⚠ test-ci-mode.sh 没有执行权限，正在添加...${NC}"
        chmod +x scripts/test-ci-mode.sh
        echo -e "${GREEN}✓ 已添加执行权限${NC}"
    fi
else
    echo -e "${RED}✗ test-ci-mode.sh 不存在${NC}"
    exit 1
fi
echo ""

# 检查6: 文档
echo -e "${YELLOW}检查6: 文档...${NC}"
if [ -f "docs/CI_TEST_CONFIGURATION.md" ]; then
    echo -e "${GREEN}✓ CI配置文档存在${NC}"
else
    echo -e "${YELLOW}⚠ CI配置文档不存在${NC}"
fi

if [ -f ".github/workflows/README.md" ]; then
    echo -e "${GREEN}✓ Workflows说明文档存在${NC}"
else
    echo -e "${YELLOW}⚠ Workflows说明文档不存在${NC}"
fi
echo ""

# 检查7: 其他workflow文件的CI配置
echo -e "${YELLOW}检查7: 其他workflow文件的CI配置...${NC}"
for workflow in .github/workflows/*.yml; do
    if [ -f "$workflow" ]; then
        filename=$(basename "$workflow")
        if grep -q "CI: true" "$workflow" 2>/dev/null; then
            echo -e "${GREEN}✓ $filename 已配置 CI=true${NC}"
        else
            if [[ "$filename" != "ci.yml" ]]; then
                echo -e "${YELLOW}⚠ $filename 未配置 CI=true${NC}"
            fi
        fi
    fi
done
echo ""

# 检查8: Maven验证
echo -e "${YELLOW}检查8: Maven配置验证...${NC}"
if mvn validate -pl core-database -q 2>/dev/null; then
    echo -e "${GREEN}✓ Maven配置验证通过${NC}"
else
    echo -e "${RED}✗ Maven配置验证失败${NC}"
    exit 1
fi
echo ""

# 检查9: 测试CI模式（可选）
echo -e "${YELLOW}检查9: 测试CI Profile激活...${NC}"
export CI=true
ACTIVE_PROFILES=$(mvn help:active-profiles -pl core-database 2>/dev/null | grep -A 10 "Active Profiles" || echo "")
if echo "$ACTIVE_PROFILES" | grep -q "ci"; then
    echo -e "${GREEN}✓ CI Profile在CI=true时自动激活${NC}"
else
    echo -e "${YELLOW}⚠ 无法确认CI Profile激活状态（这可能是正常的）${NC}"
fi
unset CI
echo ""

# 总结
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  验证完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}所有关键配置检查通过！${NC}"
echo ""
echo -e "${YELLOW}下一步操作：${NC}"
echo "1. 测试CI模式: ${BLUE}./scripts/test-ci-mode.sh${NC}"
echo "2. 测试完整模式: ${BLUE}mvn test${NC}"
echo "3. 提交到Git: ${BLUE}git add . && git commit -m 'ci: 配置CI测试排除'${NC}"
echo "4. 推送到GitHub: ${BLUE}git push${NC}"
echo ""

