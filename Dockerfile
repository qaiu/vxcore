# 多阶段构建 Dockerfile for VXCore

# 构建阶段
FROM maven:3.9.6-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件
COPY pom.xml .
COPY core/pom.xml core/
COPY core-database/pom.xml core-database/
COPY core-generator/pom.xml core-generator/
COPY core-example/pom.xml core-example/

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM openjdk:17-jre-slim

# 设置维护者信息
LABEL maintainer="QAIU <qaiu@qq.com>"
LABEL description="VXCore - 基于 Vert.x 的现代化 Java 框架"
LABEL version="1.0.0"

# 设置工作目录
WORKDIR /app

# 创建非 root 用户
RUN groupadd -r vxcore && useradd -r -g vxcore vxcore

# 安装必要的工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 复制构建的 JAR 文件
COPY --from=builder /app/core-example/target/core-example-*.jar app.jar

# 复制配置文件
COPY core-example/src/main/resources/application.yml application.yml

# 创建日志目录
RUN mkdir -p /app/logs && chown -R vxcore:vxcore /app

# 切换到非 root 用户
USER vxcore

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 设置 JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 默认命令
CMD ["--spring.profiles.active=docker"]