# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Import SSL certificate into Java TrustStore
COPY docker/isrgrootx1.pem /tmp/isrgrootx1.pem
RUN keytool -importcert \
    -trustcacerts \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -noprompt \
    -alias isrgrootx1 \
    -file /tmp/isrgrootx1.pem

# Dynamic settings
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]