## 基础镜像
#FROM openjdk:17-jdk-slim
## 设定时区
##ENV TZ=Asia/Shanghai：定义环境变量 TZ（时区）为 Asia/Shanghai（上海）。
# #RUN 后面的命令是在容器内部执行的 Linux 命令：
# #ln -snf /usr/share/zoneinfo/$TZ /etc/localtime：创建软链接，将系统时区配置文件指向上海时区的时区信息。
# #echo $TZ > /etc/timezone：将时区写入 /etc/timezone 文件（Debian 系统识别时区的方式）。
#ENV TZ=Asia/Shanghai
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#
## 指定工作目录
##设置容器内的工作目录（后续的文件操作、命令执行都会在这个目录下进行，类似 cd /app）。
#WORKDIR /app
#
## 将jar包添加到工作目录
##target/AuthenticationService-0.0.1-SNAPSHOT.jar：本地 JAR 包的路径（相对于 Dockerfile 所在目录），通常是 Maven/Gradle 编译后输出的路径（target 目录是 Maven 的默认输出目录）。
# #.：表示复制到容器内的当前工作目录（即 /app，因为前面用 WORKDIR 指定了）。
# #等效于 COPY target/xxx.jar /app/xxx.jar（ADD 与 COPY 功能类似，这里用 COPY 也可以）。
#ADD target/AuthenticationService-0.0.1-SNAPSHOT.jar .
#
## 暴露端口 声明容器运行时会监听的端口（仅为声明，不实际映射端口，方便使用者了解服务端口）。
#EXPOSE 8080
#
## 启动命令
##ENTRYPOINT 是 Docker 中用于指定容器启动命令的指令（与 CMD 类似，但 ENTRYPOINT 更适合固定启动命令，CMD 可被覆盖）。
## --spring.profiles.active 是 Spring Boot 中用于激活特定配置文件的命令行参数。
## 通过设置这个参数，你可以指定应用程序在启动时使用哪个配置文件。这在开发、测试和生产环境中非常有用，因为每个环境可能需要不同的配置。
#ENTRYPOINT ["java","-jar","/app/AuthenticationService-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]

FROM eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]