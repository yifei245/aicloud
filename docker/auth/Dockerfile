FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system aicloud && useradd --system --gid aicloud --home-dir /app --shell /usr/sbin/nologin aicloud \
    && mkdir -p /app/logs/nacos \
    && chown -R aicloud:aicloud /app
COPY --chown=aicloud:aicloud aicloud-auth/aicloud-auth-server/target/aicloud-auth-server-1.0.0-SNAPSHOT.jar app.jar
USER aicloud
EXPOSE 48081
ENTRYPOINT ["java","-jar","/app/app.jar"]
