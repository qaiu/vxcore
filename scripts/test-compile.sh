#!/bin/bash

echo "Testing VXCore compilation..."

# 测试core模块编译
echo "Compiling core module..."
mvn clean compile -pl core -B

if [ $? -eq 0 ]; then
    echo "✅ Core module compiled successfully"
else
    echo "❌ Core module compilation failed"
    exit 1
fi

# 测试core-database模块编译
echo "Compiling core-database module..."
mvn clean compile -pl core-database -B

if [ $? -eq 0 ]; then
    echo "✅ Core-database module compiled successfully"
else
    echo "❌ Core-database module compilation failed"
    exit 1
fi

# 测试core-example模块编译
echo "Compiling core-example module..."
mvn clean compile -pl core-example -B

if [ $? -eq 0 ]; then
    echo "✅ Core-example module compiled successfully"
else
    echo "❌ Core-example module compilation failed"
    exit 1
fi

# 测试整个项目编译
echo "Compiling entire project..."
mvn clean compile -B

if [ $? -eq 0 ]; then
    echo "✅ Entire project compiled successfully"
else
    echo "❌ Project compilation failed"
    exit 1
fi

echo "🎉 All compilation tests passed!"