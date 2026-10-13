FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S aicloud && adduser -S aicloud -G aicloud && mkdir -p /app/data && chown -R aicloud:aicloud /app
ARG JAR_FILE
COPY --chown=aicloud:aicloud ${JAR_FILE} app.jar
USER aicloud
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
