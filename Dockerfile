FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Copy maven executable and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download all dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Build the project
RUN ./mvnw package -DskipTests

# Extract the JAR layers for better caching
RUN mkdir -p target/extracted
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Create the final image
FROM eclipse-temurin:17-jre

# Create a non-root user
RUN useradd -m -s /bin/bash appuser
USER appuser
VOLUME /tmp
ARG EXTRACTED=/workspace/app/target/extracted

# Copy layers from build stage
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

# Set the entrypoint
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"] 