FROM maven:3.9.16-eclipse-temurin-21-noble AS build

WORKDIR /workspace/backend-java

COPY backend-java/pom.xml .
COPY database /workspace/database

RUN mvn dependency:go-offline

COPY backend-java/src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

RUN groupadd --system peakprogress \
    && useradd --system --gid peakprogress peakprogress

COPY --from=build \
    --chown=peakprogress:peakprogress \
    /workspace/backend-java/target/*.jar \
    /app/app.jar

USER peakprogress

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]