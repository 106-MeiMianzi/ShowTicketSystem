# ========== 阶段1：构建（Build） ==========
# 使用带 Maven 3.9 + Eclipse Temurin JDK 17 的 Alpine 镜像作为构建环境，并命名为 build 阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
# 在容器内设置工作目录为 /app
WORKDIR /app

# 先只复制 pom.xml，便于利用 Docker 层缓存：依赖不变时不会重新下载
COPY pom.xml .
# 离线下载所有 Maven 依赖（-B 批处理模式，减少输出）
RUN mvn dependency:go-offline -B

# 复制项目源码到容器内的 ./src
COPY src ./src
# 打包项目，跳过测试（-DskipTests），生成 jar
RUN mvn package -DskipTests -B

# ========== 阶段2：运行（Runtime） ==========
# 使用仅含 JRE 17 的 Alpine 镜像，体积更小，仅用于运行
FROM eclipse-temurin:17-jre-alpine
# 运行阶段的工作目录
WORKDIR /app
# 从上一阶段 build 中复制打好的 jar 到当前镜像，并命名为 app.jar
COPY --from=build /app/target/*.jar app.jar
# 声明容器监听 8080 端口（文档用途，不会自动映射）
EXPOSE 8080
# 容器启动时执行的命令：用 java -jar 运行 app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
