FROM openjdk:10-ea
EXPOSE 8080
ADD target/lab2-2026.jar lab2-2026.jar
ENTRYPOINT ["java","-jar","/lab2-2026.jar"]