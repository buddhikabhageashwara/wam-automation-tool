FROM gradle:7-jdk17 AS build


VOLUME /tmp

EXPOSE 8085

RUN mkdir /app


ADD ./build/libs/flow.chart.game.api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "./app.jar"]
