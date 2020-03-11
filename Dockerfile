FROM maven:3.6.3-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package


FROM openjdk:11
COPY --from=build /usr/src/app/target/api_gateway-0.0.1-SNAPSHOT.jar /usr/app/api_gateway-0.0.1-SNAPSHOT.jar
EXPOSE 8762
ENTRYPOINT ["java","-jar","/usr/app/api_gateway-0.0.1-SNAPSHOT.jar"]
