#!/bin/bash

# DDL映射测试运行脚本（简化版）
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

# 检查测试文件
echo -e "${YELLOW}检查测试文件...${NC}"
if [ ! -d "core-database/src/test" ]; then
    echo -e "${RED}测试目录不存在${NC}"
    exit 1
fi

echo -e "${GREEN}测试文件检查完成${NC}"

# 显示测试文件列表
echo -e "${YELLOW}测试文件列表:${NC}"
find core-database/src/test -name "*.java" | while read file; do
    echo "  - $file"
done

echo -e "${YELLOW}测试配置文件:${NC}"
find core-database/src/test/resources -name "*" | while read file; do
    echo "  - $file"
done

echo -e "${GREEN}=========================================="
echo "测试环境检查完成！"
echo "==========================================${NC}"

echo -e "${YELLOW}要运行测试，请执行以下命令:${NC}"
echo "1. 安装依赖: mvn install -pl core -DskipTests"
echo "2. 运行测试: mvn test -pl core-database"
echo "3. 运行特定测试: mvn test -pl core-database -Dtest=DdlTableSimpleTest"

echo -e "${GREEN}=========================================="
echo "脚本执行完成！"
echo "==========================================${NC}"
