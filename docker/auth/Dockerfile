FROM eclipse-temurin:21-jre
WORKDIR /app
COPY aicloud-auth/aicloud-auth-server/target/aicloud-auth-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 48081
ENTRYPOINT ["java","-jar","/app/app.jar"]
