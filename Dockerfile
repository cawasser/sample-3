FROM openjdk:8-alpine

COPY target/uberjar/sample-3.jar /sample-3/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sample-3/app.jar"]
