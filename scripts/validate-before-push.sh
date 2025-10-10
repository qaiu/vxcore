#!/bin/bash
set -e

echo "🔍 开始提交前验证..."

# 1. 编译检查
echo "📦 检查编译..."
mvn clean compile -pl core,core-database -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译通过"

# 2. 代码风格检查（跳过，未配置Spotless）
echo "🎨 跳过代码风格检查（未配置Spotless插件）"

# 3. 静态分析（跳过，未配置SpotBugs）
echo "🔍 跳过静态分析（未配置SpotBugs插件）"

# 4. 单元测试
echo "🧪 运行单元测试..."
mvn test -pl core-database -q
if [ $? -ne 0 ]; then
    echo "❌ 单元测试失败"
    exit 1
fi
echo "✅ 单元测试通过"

# 5. 测试覆盖率（跳过，未配置JaCoCo）
echo "📊 跳过测试覆盖率检查（未配置JaCoCo插件）"

# 6. 构建验证
echo "🏗️ 验证构建..."
mvn package -pl core,core-database -q -DskipTests=true
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi
echo "✅ 构建验证通过"

echo "🎉 所有验证通过，可以安全推送！"
