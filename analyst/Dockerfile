FROM maven:3.6.3-jdk-11 as builder

WORKDIR /app

COPY src/ src/
COPY pom.xml pom.xml

RUN mvn clean package -DskipTests
	
FROM openjdk:8-jre-alpine
COPY --from=builder /app/target/analyst-0.0.1-SNAPSHOT.jar /app/analyst.jar

WORKDIR /app

COPY log4j.properties log4j.properties
CMD java -jar analyst.jar $SERVER $TOPIC $NODENAME