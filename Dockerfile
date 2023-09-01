FROM eclipse-temurin:17-jdk
EXPOSE 8080
VOLUME /tmp
CMD ls
COPY TourGuide/target/tourguide-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]