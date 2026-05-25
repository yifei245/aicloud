FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system aicloud && useradd --system --gid aicloud --home-dir /app --shell /usr/sbin/nologin aicloud \
    && mkdir -p /app/logs/nacos \
    && chown -R aicloud:aicloud /app
COPY --chown=aicloud:aicloud aicloud-gateway/aicloud-gateway-server/target/aicloud-gateway-server-1.0.0-SNAPSHOT.jar app.jar
USER aicloud
EXPOSE 48080
ENTRYPOINT ["java","-jar","/app/app.jar"]
