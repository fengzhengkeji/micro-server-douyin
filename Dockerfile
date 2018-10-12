FROM openjdk:8-jre-alpine

ADD target/*.jar /data/app.jar

ENV MYSQL_URL=jdbc:mysql://mysql-userDTO:3306/userDTO \
    MYSQL_USERNAME=root \
    MYSQL_PASSWORD=Passw0rd

EXPOSE 8080

CMD java -jar \
    -server \
    -Xms300m \
    -Xmx300m \
    -XX:MaxMetaspaceSize=64m \
    /data/app.jar \
    --spring.datasource.url=$MYSQL_URL \
    --spring.datasource.username=$MYSQL_USERNAME \
    --spring.datasource.password=$MYSQL_PASSWORD