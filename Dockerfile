# ---------- Build stage ----------
FROM maven:3.9.5-eclipse-temurin-21 as build
WORKDIR /build

# Copy maven files first (to cache dependencies)
COPY smrs-service/pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY smrs-service/src ./src
RUN mvn -B -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
ARG JAR_FILE=/build/target/*.jar
WORKDIR /app

# copy the fat jar from build stage
COPY --from=build ${JAR_FILE} app.jar

# Optional: set JVM options via env
ENV JAVA_OPTS="-Xms256m -Xmx512m -server"

EXPOSE 8080

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
