How to deploy the project : 
- cd into the directory at the root of the project. In the folder called  **Project_8_maven** 
- in the terminal, execute  **mvn clean package -f TourGuide/pom.xml**  to build the -jar. The -jar will be built in the target folder. 
- run "docker build --tag=tourguide-app:latest . "  to build the docker image. 
- then run "docker run -d --name tourguide-container -p 8080:8080 tourguide-app"  to run the container from the image. 
- you may now access the endpoints. Follow the following link to test via Postman:
https://www.postman.com/altimetry-geoscientist-15115209/workspace/p8/collection/18469543-5b6b7f40-7bb9-4ecf-8179-713c3111b068?action=share&creator=18469543


