#   Java 8 (openjdk:8)
FROM maven:3.8-openjdk-8 AS builder

# 设置工作目录
WORKDIR /app

# 只有当 pom.xml 文件变化时，才会重新下载依赖。
COPY pom.xml .

# 下载所有项目依赖
RUN mvn dependency:go-offline

# 复制项目的源代码
COPY src ./src

# 运行 Maven 打包命令，生成可执行的 JAR 文件
RUN mvn clean package -DskipTests

#  只包含 Java 8 JRE)
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 从builder容器,只复制最终 JAR 文件过来
COPY --from=builder /app/target/*.jar app.jar

# 声明容器端口
EXPOSE 8080

# 定义容器启动时要执行的命令
ENTRYPOINT ["java", "-jar", "tsp-application.jar"]