FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /app/serverpayment/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
