# ==============================================================================
# Stage 1: The Forge (Builder)
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set the working directory to the root of the project inside the container
WORKDIR /build

# Copy the ENTIRE repository (parent POM and all microservice folders) into the forge
COPY . .

# Forge the weapon.
# -pl Microservices/Gateway tells Maven to ONLY compile the Gateway.
# -am (also make) tells Maven to automatically compile the Parent POM it depends on.
RUN mvn clean package -pl Microservices/Gateway -am -DskipTests

# ==============================================================================
# Stage 2: The Scabbard (Runtime)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# DevSecOps: Create a non-root user
RUN addgroup -S petreca && adduser -S spring -G petreca

# Magic: Extract ONLY the Gateway's finished .jar from the heavy builder stage
COPY --from=builder /build/Microservices/Gateway/target/*.jar app.jar

# Drop privileges
USER spring:petreca

EXPOSE 9999

# The Ignition spell
ENTRYPOINT ["java", "-jar", "app.jar"]