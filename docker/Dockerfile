# Build stage: compile app AND import certificate
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Import certificate into TrustStore during build
COPY isrgrootx1.pem /tmp/isrgrootx1.pem
RUN keytool -importcert \
    -trustcacerts \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -noprompt \
    -alias isrgrootx1 \
    -file /tmp/isrgrootx1.pem

# Runtime stage: just copy the JAR and modified TrustStore
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /opt/java/openjdk/lib/security/cacerts /opt/java/openjdk/lib/security/cacerts

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]