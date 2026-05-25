FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE
RUN groupadd --system aicloud && useradd --system --gid aicloud --home-dir /app --shell /usr/sbin/nologin aicloud \
    && mkdir -p /app/logs/nacos \
    && chown -R aicloud:aicloud /app
COPY --chown=aicloud:aicloud ${JAR_FILE} app.jar
USER aicloud
EXPOSE 49000
ENTRYPOINT ["java","-jar","/app/app.jar"]
