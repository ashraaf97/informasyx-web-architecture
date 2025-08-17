FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline -B

# Copy the project source
COPY src src

# Build the project
RUN mvn package -DskipTests

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