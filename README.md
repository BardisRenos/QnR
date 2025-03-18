# QnR
Technical Assessment 



## Tech Stack

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **Spring Security 6**
- **PostgreSQL**
- **Docker**
- **Redis** for caching
- **JUnit** for Unit Testing
- **Docker testcontainers** for Integration testing
- **Maven** for build management
- **Lombok** for boilerplate code reduction
- **Swagger UI** for API documentation

## Design 

## Setup Instructions

### 1. Initialize PostgresSQL Database

To initialize the PostgresSQL database and create necessary tables, indexes, and sample data, follow these steps:

#### Step 1: Start Docker Containers

Ensure that the `docker-compose.yml` file is running and the necessary Docker containers are up.
If you're using Docker Compose, run the following command to start the services:

```bash
docker-compose up -d
```

#### Step 2: Connect to the PostgresSQL Database

After the Docker container is up, connect to the PostgresSQL database by running:

```bash
docker exec -it pgdbrating psql -U postgres -d mydatabase
```

#### Step 3: Create Database Tables and Insert Data

Once connected to the PostgresSQL CLI, switch to the `mydatabase` database and execute the
following SQL script to create the necessary tables and insert sample data. Tha data are located at the 
folder `init-scripts` where can create the tables and the indexes, and finally to initial the database tables with
some data.

### 2. Build and Run the Application

To clean and install the Maven dependencies, run:

```bash
mvn clean install
```
or

To build the project without running the tests.
```bash
mvn clean install -DskipTests
```

To start the Spring Boot application, use the following command:

```bash
mvn spring-boot:run
```

### 3. Change Server Port

By default, the application runs on port `8080`. To change the port to `8081`, modify the `application.properties` file:

```
server.port=8081
```

### 4. Docker container & Docker compose

The application may run also in a Docker container at the port 9088. The application uses also
an orchestrator the services PostgresSql, Redis Cache, and the actual application

### 5. Redis Cache Integration

To check if Redis caching is working properly, follow these steps:

1. Enter the Redis Docker container:

    ```bash
    docker exec -it <container_name> sh
    ```

2. Authenticate with the Redis password (default password: `renos1987`):

    ```bash
    AUTH renos1987
    ```

3. Monitor Redis activity:

    ```bash
    MONITOR
    ```

This will allow you to see if the data is being collected and cached in Redis.

### 6. API Endpoints

The Rest end points are at the endpoints folder. A Postman collection. Also, has Environments variables such as 
**DockerUrl** and **SimpleUrl**. Like,
`url`= http://localhost:8088 and `docer_url`= http://localhost:9088

### 7. Validations

There are some validation points. Some Dto's are

The system checks and validates the entity givenRating and ratedEntity as NotNull.

### 8. Security 

### 9. Swagger UI

To view and interact with the API endpoints via Swagger UI, visit:

```
http://localhost:8088/swagger-ui/index.html
```

### 10. Unit & Integration tests

The application has unit tests for each layer and each (functional) method. Also, the application uses Docker containers
in order to test all the components.