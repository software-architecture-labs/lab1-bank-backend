[![CI/CD Pipeline](https://github.com/software-architecture-labs/lab1-bank-backend/actions/workflows/build.yml/badge.svg)](https://github.com/software-architecture-labs/lab1-bank-backend/actions/workflows/build.yml)
# Banking System API - Software Architecture Laboratory

This repository contains a RESTful API developed with **Spring Boot** for banking operations management, focusing on customer administration and secure financial transaction processing.

---

## Technical Stack

* **Java 17** (LTS)
* **Spring Boot 3.5.11**
* **Spring Data JPA**: For data persistence and object-relational mapping.
* **MySQL**: Relational database management system.
* **Lombok**: Library for reducing boilerplate code (Getters, Setters, Builders).
* **JUnit 5 & Mockito**: Frameworks for unit testing and object mocking.
* **Springdoc-OpenAPI (Swagger)**: Interactive technical documentation and endpoint testing.

---

## Configuration and Installation

### Prerequisites
1.  **JDK 17** installed and configured in system environment variables.
2.  **MySQL Server** instance active.
3.  Manual database creation:
    ```sql
    CREATE DATABASE lab1banco;
    ```

### Environment Setup
The application utilizes the `src/main/resources/application.properties` file. Ensure that the following credentials match your local environment:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lab1banco
spring.datasource.username=your_user
spring.datasource.password=your_password