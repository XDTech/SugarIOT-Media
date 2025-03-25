FROM openjdk:17-oracle


# 设置工作目录
WORKDIR /app

# 复制 Spring Boot 应用
COPY target/*.jar app.jar



# 创建用于挂载的配置目录
VOLUME /app/config

# 运行 MediaServer 并启动 Spring Boot 应用
CMD [ "sh", "-c", "java -jar app.jar" ]
