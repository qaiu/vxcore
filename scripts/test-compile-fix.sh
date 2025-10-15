#!/bin/bash

echo "🔧 测试编译修复..."

# 测试core模块编译
echo "📦 编译core模块..."
mvn clean compile -pl core -B -q
if [ $? -eq 0 ]; then
    echo "✅ core模块编译成功"
else
    echo "❌ core模块编译失败"
    exit 1
fi

# 测试core-database模块编译
echo "📦 编译core-database模块..."
mvn clean compile -pl core-database -B -q
if [ $? -eq 0 ]; then
    echo "✅ core-database模块编译成功"
else
    echo "❌ core-database模块编译失败"
    exit 1
fi

# 测试core-example模块编译
echo "📦 编译core-example模块..."
mvn clean compile -pl core-example -B -q
if [ $? -eq 0 ]; then
    echo "✅ core-example模块编译成功"
else
    echo "❌ core-example模块编译失败"
    exit 1
fi

# 测试整个项目编译
echo "📦 编译整个项目..."
mvn clean compile -B -q
if [ $? -eq 0 ]; then
    echo "✅ 整个项目编译成功"
else
    echo "❌ 整个项目编译失败"
    exit 1
fi

echo "🎉 所有模块编译成功！"