# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create uploads directory with proper permissions
RUN mkdir -p /app/uploads && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create volume for file uploads
VOLUME /app/uploads

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]