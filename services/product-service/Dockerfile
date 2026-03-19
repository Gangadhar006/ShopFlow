FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S shopflow && adduser -S shopflow -G shopflow
COPY --from=builder /app/target/*.jar app.jar
USER shopflow
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
