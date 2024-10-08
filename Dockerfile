FROM maven:3.8.5 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package


FROM eclipse-temurin:17.0.12_7-jre-jammy

WORKDIR /app

EXPOSE 8080

COPY --from=builder /app/target/Wits-Project-Calendar-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "Wits-Project-Calendar-0.0.1-SNAPSHOT.jar"]

