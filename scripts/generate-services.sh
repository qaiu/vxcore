#!/bin/bash

###############################################################################
# Service接口生成脚本
# 
# 基于实体类自动生成Service接口
###############################################################################

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Service接口生成工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENTITY_DIR="$PROJECT_ROOT/core-example/src/main/java/cn/qaiu/example/entity"
SERVICE_DIR="$PROJECT_ROOT/core-example/src/main/java/cn/qaiu/example/service"
PACKAGE_NAME="cn.qaiu.example"

echo -e "${YELLOW}项目根目录:${NC} $PROJECT_ROOT"
echo -e "${YELLOW}实体目录:${NC} $ENTITY_DIR"
echo -e "${YELLOW}服务目录:${NC} $SERVICE_DIR"
echo ""

# 检查实体目录
if [ ! -d "$ENTITY_DIR" ]; then
    echo -e "${RED}❌ 实体目录不存在: $ENTITY_DIR${NC}"
    exit 1
fi

# 扫描实体类
echo -e "${YELLOW}扫描实体类...${NC}"
ENTITY_FILES=($(find "$ENTITY_DIR" -name "*.java" -type f | grep -v "Test" | sort))
ENTITY_NAMES=()

for file in "${ENTITY_FILES[@]}"; do
    filename=$(basename "$file")
    entity_name="${filename%.java}"
    ENTITY_NAMES+=("$entity_name")
    echo -e "${GREEN}✓${NC} 发现实体: $entity_name"
done

echo ""
echo -e "${YELLOW}找到 ${#ENTITY_NAMES[@]} 个实体类${NC}"
echo ""

# 生成Service接口
echo -e "${YELLOW}开始生成Service接口...${NC}"

for entity_name in "${ENTITY_NAMES[@]}"; do
    service_name="${entity_name}Service"
    service_file="$SERVICE_DIR/${service_name}.java"
    
    # 检查是否已存在
    if [ -f "$service_file" ]; then
        echo -e "${YELLOW}⚠️${NC} $service_name 已存在，跳过"
        continue
    fi
    
    echo -e "${BLUE}🔧${NC} 生成 $service_name..."
    
    # 创建Service接口内容
    cat > "$service_file" << EOF
package $PACKAGE_NAME.service;

import cn.qaiu.db.dsl.lambda.SimpleJService;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import $PACKAGE_NAME.entity.$entity_name;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * ${entity_name}服务接口
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ProxyGen
public interface $service_name extends SimpleJService<$entity_name, Long> {

    /**
     * 根据ID获取${entity_name}
     * 
     * @param id ${entity_name}ID
     * @return ${entity_name}信息
     */
    Future<$entity_name> get${entity_name}ById(Long id);

    /**
     * 获取所有${entity_name}
     * 
     * @return ${entity_name}列表
     */
    Future<List<$entity_name>> getAll${entity_name}s();

    /**
     * 分页查询${entity_name}
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Future<LambdaPageResult<$entity_name>> find${entity_name}s(long page, long size);

    /**
     * 统计${entity_name}数量
     * 
     * @return 数量
     */
    Future<Long> count${entity_name}s();

    /**
     * 创建${entity_name}
     * 
     * @param ${entity_name,} ${entity_name}信息
     * @return 创建的${entity_name}
     */
    Future<$entity_name> create${entity_name}($entity_name ${entity_name,});

    /**
     * 更新${entity_name}
     * 
     * @param ${entity_name,} ${entity_name}信息
     * @return 是否更新成功
     */
    Future<Boolean> update${entity_name}($entity_name ${entity_name,});

    /**
     * 删除${entity_name}
     * 
     * @param id ${entity_name}ID
     * @return 是否删除成功
     */
    Future<Boolean> delete${entity_name}(Long id);

    /**
     * 搜索${entity_name}
     * 
     * @param keyword 关键词
     * @return ${entity_name}列表
     */
    Future<List<$entity_name>> search${entity_name}s(String keyword);

    /**
     * 获取${entity_name}统计信息
     * 
     * @return 统计信息
     */
    Future<JsonObject> get${entity_name}Statistics();
}
EOF
    
    echo -e "${GREEN}✅${NC} 成功生成 $service_name"
done

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}🎉 Service接口生成完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 显示生成的文件
echo -e "${YELLOW}生成的文件:${NC}"
for entity_name in "${ENTITY_NAMES[@]}"; do
    service_name="${entity_name}Service"
    service_file="$SERVICE_DIR/${service_name}.java"
    if [ -f "$service_file" ]; then
        echo -e "${GREEN}✓${NC} $service_file"
    fi
done

echo ""
echo -e "${YELLOW}下一步:${NC}"
echo "1. 检查生成的Service接口"
echo "2. 根据需要添加自定义方法"
echo "3. 实现Service接口"
echo "4. 运行Vert.x代码生成: mvn compile"
echo ""
echo -e "${BLUE}💡 提示:${NC} 生成的Service接口会自动被Vert.x代理生成器处理，"
echo "   生成对应的 VertxEBProxy 类。"

