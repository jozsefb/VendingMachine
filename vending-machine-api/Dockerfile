FROM maven:3.8.2-openjdk-17 AS build-env
WORKDIR /app

# Copy everything else
COPY ./ ./

RUN mvn package -ntp;

# Extract fat jar into layers for docker caching 
RUN java -Djarmode=layertools -jar /app/target/*.jar extract

# Build final runtime image
FROM maven:3.8.2-openjdk-17-slim
WORKDIR /app
COPY --from=build-env /app/dependencies/ ./
COPY --from=build-env /app/snapshot-dependencies/ ./
COPY --from=build-env /app/spring-boot-loader/ ./
COPY --from=build-env /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
