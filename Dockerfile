FROM openjdk:21-oracle

COPY build/libs/spring-proxy-parser-1.0.jar /opt/app.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/app.jar
