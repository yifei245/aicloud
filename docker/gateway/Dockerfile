FROM eclipse-temurin:21-jre
WORKDIR /app
COPY aicloud-gateway/aicloud-gateway-server/target/aicloud-gateway-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 48080
ENTRYPOINT ["java","-jar","/app/app.jar"]
