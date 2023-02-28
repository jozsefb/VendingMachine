FROM maven:3.8.2-openjdk-17 AS build-env
WORKDIR /app

COPY . ./

# Start tests
ENTRYPOINT ["mvn", "test"]
