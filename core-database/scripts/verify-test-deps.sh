#!/bin/bash

# 测试依赖验证脚本
# 验证JUnit和Vert.x JUnit依赖是否正确配置

echo "=========================================="
echo "测试依赖验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 进入项目目录
cd "$(dirname "$0")/.."

echo -e "${YELLOW}当前工作目录: $(pwd)${NC}"

# 检查pom.xml文件
echo -e "${YELLOW}检查pom.xml文件...${NC}"
if [ ! -f "core-database/pom.xml" ]; then
    echo -e "${RED}错误: core-database/pom.xml 文件不存在${NC}"
    exit 1
fi

# 检查JUnit 5依赖
echo -e "${YELLOW}检查JUnit 5依赖...${NC}"
if grep -q "junit-jupiter" core-database/pom.xml; then
    echo -e "${GREEN}✓ JUnit 5 依赖已配置${NC}"
    grep -A 3 "junit-jupiter" core-database/pom.xml | head -4
else
    echo -e "${RED}✗ JUnit 5 依赖未找到${NC}"
fi

# 检查Vert.x JUnit 5依赖
echo -e "${YELLOW}检查Vert.x JUnit 5依赖...${NC}"
if grep -q "vertx-junit5" core-database/pom.xml; then
    echo -e "${GREEN}✓ Vert.x JUnit 5 依赖已配置${NC}"
    grep -A 3 "vertx-junit5" core-database/pom.xml | head -4
else
    echo -e "${RED}✗ Vert.x JUnit 5 依赖未找到${NC}"
fi

# 检查Maven Surefire插件
echo -e "${YELLOW}检查Maven Surefire插件...${NC}"
if grep -q "maven-surefire-plugin" core-database/pom.xml; then
    echo -e "${GREEN}✓ Maven Surefire 插件已配置${NC}"
    grep -A 5 "maven-surefire-plugin" core-database/pom.xml | head -6
else
    echo -e "${RED}✗ Maven Surefire 插件未找到${NC}"
fi

# 检查测试文件
echo -e "${YELLOW}检查测试文件...${NC}"
test_count=$(find core-database/src/test -name "*.java" | wc -l)
echo -e "${GREEN}找到 $test_count 个测试文件${NC}"

# 显示测试文件列表
echo -e "${YELLOW}测试文件列表:${NC}"
find core-database/src/test -name "*.java" | while read file; do
    echo "  - $file"
done

# 尝试解析依赖
echo -e "${YELLOW}尝试解析Maven依赖...${NC}"
if mvn dependency:resolve -pl core-database -q 2>/dev/null; then
    echo -e "${GREEN}✓ Maven依赖解析成功${NC}"
else
    echo -e "${YELLOW}⚠ Maven依赖解析失败，可能需要先安装core模块${NC}"
    echo -e "${YELLOW}请运行: mvn install -pl core -DskipTests${NC}"
fi

echo -e "${GREEN}=========================================="
echo "依赖验证完成！"
echo "==========================================${NC}"

echo -e "${YELLOW}要运行测试，请执行:${NC}"
echo "1. mvn install -pl core -DskipTests"
echo "2. mvn test -pl core-database"
echo "3. mvn test -pl core-database -Dtest=SimpleDdlTest"
