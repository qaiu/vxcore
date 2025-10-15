#!/bin/bash

echo "🔍 Checking VXCore module dependencies..."

# 检查各模块的依赖关系
echo ""
echo "📋 Module Dependencies:"
echo "========================"

echo ""
echo "1. Core module dependencies:"
mvn dependency:tree -pl core -Dverbose=false | grep "cn.qaiu" || echo "  No internal module dependencies"

echo ""
echo "2. Core-database module dependencies:"
mvn dependency:tree -pl core-database -Dverbose=false | grep "cn.qaiu" || echo "  No internal module dependencies"

echo ""
echo "3. Core-generator module dependencies:"
mvn dependency:tree -pl core-generator -Dverbose=false | grep "cn.qaiu" || echo "  No internal module dependencies"

echo ""
echo "4. Core-example module dependencies:"
mvn dependency:tree -pl core-example -Dverbose=false | grep "cn.qaiu" || echo "  No internal module dependencies"

echo ""
echo "🔧 Testing compilation order:"
echo "============================="

# 测试编译顺序
echo ""
echo "1. Compiling core module..."
mvn clean compile -pl core -B -q
if [ $? -eq 0 ]; then
    echo "  ✅ Core module compiled successfully"
else
    echo "  ❌ Core module compilation failed"
    exit 1
fi

echo ""
echo "2. Compiling core-database module..."
mvn clean compile -pl core-database -B -q
if [ $? -eq 0 ]; then
    echo "  ✅ Core-database module compiled successfully"
else
    echo "  ❌ Core-database module compilation failed"
    exit 1
fi

echo ""
echo "3. Compiling core-generator module..."
mvn clean compile -pl core-generator -B -q
if [ $? -eq 0 ]; then
    echo "  ✅ Core-generator module compiled successfully"
else
    echo "  ❌ Core-generator module compilation failed"
    exit 1
fi

echo ""
echo "4. Compiling core-example module..."
mvn clean compile -pl core-example -B -q
if [ $? -eq 0 ]; then
    echo "  ✅ Core-example module compiled successfully"
else
    echo "  ❌ Core-example module compilation failed"
    exit 1
fi

echo ""
echo "5. Compiling entire project..."
mvn clean compile -B -q
if [ $? -eq 0 ]; then
    echo "  ✅ Entire project compiled successfully"
else
    echo "  ❌ Project compilation failed"
    exit 1
fi

echo ""
echo "🎉 All dependency checks passed!"
echo "✅ No circular dependencies found"
echo "✅ All modules compile successfully"
echo "✅ Dependency order is correct"