
# QnR Technical Assessment

## Tech Stack

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **Spring Security 6**
- **PostgresSQL**
- **Docker** for containerization
- **Redis** for caching
- **JUnit** for Unit Testing
- **Test containers** for Integration Testing
- **Maven** for build management
- **Lombok** for reducing boilerplate code
- **Swagger UI** for API documentation

---

## Design Overview

This project is designed to provide a simple yet robust API that integrates Spring Boot, Spring Security, PostgreSQL, Redis, and Docker for containerization and caching. The application ensures proper validation, security with JWT, and the use of Docker for efficient testing and deployment.
The structure is layers like the **Controller layer** which manage the Rest application end point. After there is the **Service layer** which is the business layer and finally the **DAO layer** which is responsible for the data access layer of the application.

<p align="center"> 
<img src="https://github.com/BardisRenos/QnR/blob/main/images/Architecture.png" width="750" height="450" style=centerme>
</p>

---

## What This Application Does

This application is an **Order Management System** that allows users to create, update, retrieve, and manage orders while
ensuring **security**, **efficiency**, and **scalability**. It provides **user authentication**, **order filtering**, **caching
for performance optimization**, and **RESTful APIs** for seamless interaction with external systems.

---

## Core Functionalities

### User Authentication & Security
- Users can **register**, **log in**, and access the system securely.
- Implements **JWT-based authentication** to protect endpoints.

### Order Management
- Users can **create**, **update**, **delete**, and **fetch** orders.
- Supports **filtering** by **order status** and **creation date**.
- Includes **pagination** for efficient data retrieval.

### Database & Caching for Performance
- Stores all **user** and **order data** in **PostgreSQL**.
- **Redis caching** is used to improve response times for frequently accessed data, reducing the load on the database.

### Dockerized Deployment & API Integration
- Runs inside **Docker containers** for easy setup and deployment.
- Exposes **RESTful API endpoints** for seamless integration with external systems and services.

### API Documentation & Testing
- **Swagger UI** is provided for testing API endpoints and visualizing the API structure.
- Includes **Postman collections** for quick API testing and debugging.
- Supports **unit and integration testing** using **JUnit** and **Testcontainers**, ensuring robustness and reliability of the application.


---

## Setup Instructions

### 1. Initialize PostgresSQL Database

Follow these steps to initialize the PostgresSQL database and set up necessary tables and sample data:

#### Step 1: Start Docker Containers

Ensure that the `docker-compose.yml` file is present and configured properly. Then, start the services using Docker Compose:

```bash
./start.sh
```
This starts the application with all its services in Docker containers.


#### Step 2: Connect to PostgresSQL Database

After the Docker container is up, connect to the PostgresSQL database by running:

```bash
docker exec -it postgres psql -U postgres -d mydatabase
```

If there is a need to run only the postgresSql database then first starts the database only:

```bash
start_progres.sh
```

To check the databases need to type :
```
\l
```
To connect with the database 
```
\c
```
Finally, to see the db tables then type:
```
\dt
```

---

## Database Schema

The following SQL queries create the necessary tables and indexes for the application:

### Database Tables 

#### Orders table
This table stores the order details, including a unique order ID, description, status, and the creation date.

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_id INTEGER UNIQUE,
    description VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_order_id ON orders (order_id);
```

#### Users Table
The users table stores information about the users in the system. 
Each user is assigned a user_id and has a username, role (such as "Admin" or "User"), 
and password which is stored securely.

```sql
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE INDEX idx_users_role ON users (role);

```

---

#### Step 3: Create Database Tables and Insert Data

When the postgres container is up the  `- /home/renos/Downloads/QnRProject/qnr/init-scripts:/docker-entrypoint-initdb.d/`
indicates that container reads from the folder **init-scripts** and execute files with **.sql**.
This script will create the necessary tables, indexes, and insert sample data into the database.

---

### 2. Build and Run the Application (Locally)

To build the project and install dependencies, run the following Maven command:

```bash
mvn clean install
```

Alternatively, to build the project without running the tests, use:

```bash
mvn clean install -DskipTests
```

To start the Spring Boot application, execute:

```bash
mvn spring-boot:run
```

---

### 3. Change Server Port (For local use)

By default, the application runs on port `8080`. To change the port to `8088`, update the `application.properties` file:

```properties
server.port=8088
```

---

### 4. Docker Integration (For Docker container use)

The application can also be run inside a Docker container. If Docker Compose is used, the application and its services
(PostgresSQL, Redis) will be managed in containers. The application will be available at port `9088` when using Docker Compose.

---

### 5. Redis Cache Integration

To verify if Redis caching is working properly:

1. Enter the Redis Docker container:

    ```bash
    docker exec -it redis-cache-db sh
    ```

2. Authenticate with the Redis password (default: `renos1987`):

   ```bash
   redis-cli
   ```   
    ```bash
    AUTH renos1987
    ```

3. Monitor Redis activity:

    ```bash
    MONITOR
    ```

This will allow you to observe real-time data caching in Redis.

---

### 6. API Endpoints

API endpoints are available in the **endpoints** folder, along with a Postman collection. The collection also includes environment variables for both local and Docker-based setups:

There are 7 end points for the Order and 5 end point for the Users.

The local url is saved as {{url}} and the docker url is saved as {{docker_url}}. Just is needed to choose in Postman 
from the top right the Environments drop down list.
- **Local URL:** `http://localhost:8088`
- **Docker URL:** `http://localhost:9088`

There is a folder **API-Documentation** which has 3 .json files. Need to import into Postman in order to test the application.

---
### 7. Advanced Query with Filter and Pagination
The custom query retrieves Order entities with filtering and pagination support.
It allows filtering by status and create date, and supports pagination.
If no pagination parameters are provided, it defaults to page 0 and page size 10.

---

### 8. Validations

The system ensures that the `OrderDto` and `UserDtoNoPass` are *NotBlank*. These validations are enforced to maintain data integrity within the application.

---

### 9. Security

The application leverages **Spring Security** with **JWT** (JSON Web Tokens) for user authentication and authorization.
Also, another worth mentioning the application register a new **User** then the password is saved into the users database table in BCryptPasswordEncoder with 10 rounds of encryption.

1. Users are authenticated by checking their credentials in the database.
2. After successful authentication, a JWT token is returned.
3. This token is used for subsequent requests to authenticate API calls.
4. The given token is applied into the Authorization option in Postman by choosing the Type as Bearer Token option.

---

### 10. Swagger UI

To view and interact with the API endpoints, the application provides a **Swagger UI** interface. Visit the following URL:

```
http://localhost:8088/swagger-ui/index.html
```

This interface allows you to test all available endpoints with ease.

---

### 11. Unit & Integration Tests

The application includes both **unit tests** for individual methods and **integration tests** for testing the complete system. It uses **Docker containers** for integration testing, ensuring that all components are properly tested in isolation and together as part of the integration process.

If you want to run all test cases then need to type this command:
```bash
mvn test
```
If you need to run only the integration tests then there is the command:

```bash
mvn verify -P integration-tests
```

---
### 12. Test Coverage

There is an option to check whether the source code is covered by running in Intellij run by coverage.

<p align="center"> 
<img src="https://github.com/BardisRenos/QnR/blob/main/images/coverageTest.png" width="1150" height="450" style=centerme>
</p>
