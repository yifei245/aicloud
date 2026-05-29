FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=20 -Djava.security.egd=file:/dev/urandom -Dfile.encoding=UTF-8"
RUN groupadd --system --gid 10001 aicloud \
    && useradd --system --uid 10001 --gid aicloud --home-dir /app --shell /usr/sbin/nologin aicloud \
    && mkdir -p /app/logs/nacos /tmp \
    && chown -R aicloud:aicloud /app /tmp
COPY --chown=aicloud:aicloud ${JAR_FILE} app.jar
USER 10001:10001
EXPOSE 49000
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
