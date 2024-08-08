FROM openjdk:19
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone
COPY target/mysql-demo-0.0.1-SNAPSHOT.jar /mysqldemo.jar
ENTRYPOINT ["java", "-jar", "/mysqldemo.jar"]