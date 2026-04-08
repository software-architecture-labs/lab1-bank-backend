[![CI/CD Pipeline](https://github.com/software-architecture-labs/lab1-bank-backend/actions/workflows/build.yml/badge.svg)](https://github.com/software-architecture-labs/lab1-bank-backend/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=software-architecture-labs_lab1-bank-backend&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=software-architecture-labs_lab1-bank-backend)](https://sonarcloud.io/summary/new_code?id=software-architecture-labs_lab1-bank-backend)

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