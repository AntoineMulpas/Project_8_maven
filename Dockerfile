FROM eclipse-temurin:17-jdk

VOLUME /tmp
COPY TourGuide/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]