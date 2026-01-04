FROM eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 10010
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]