# Library-Management System

A multi-role (admin, librarian, basic user) web application built with Spring MVC 5, JDBC and Thymeleaf.  The project started on Spring-Data-JPA and was migrated to plain JDBC with a handcrafted `ConnectionPool`, manual transactions and Testcontainers-powered integration tests.

---
## Tech Stack
* Java 11 (builds on 21/23 as well)
* Maven
* Spring 5 (MVC, Security, JDBC, Tx)
* Thymeleaf 3 + Spring Security extras
* PostgreSQL (H2 for tests) • Testcontainers
* Log4j 2 with AOP request/DAO logging
* Bootstrap 5 UI + i18n (EN / RU)
* JUnit 5, Mockito, JaCoCo, Spotless (google-java-format)

---
## Project Structure  
`src/main/java`   application code  
`src/main/webapp`  Thymeleaf templates & static resources  
`src/test/java`    unit & integration tests  

---
## Getting Started
### Prerequisites
1. **Java 11+** (LTS recommended).  
2. **Maven 3.9+**  
3. PostgreSQL running locally on `localhost:5432` _or_ adjust credentials in `src/main/resources/application.properties`.

### Local database
```
# create user & db
createuser -P postgres
createdb library-managment -O postgres
```
The schema is created automatically by the first run (plain `CREATE TABLE` scripts executed by liquibase-like helper).

---
## Build & Run
```bash
# package the WAR and run Jetty from Maven
git clone https://github.com/your-name/library-management.git
cd library-management
mvn clean package jetty:run
```
The app will be available at http://localhost:8080 and immediately redirect to `/dashboard` (login page if not authenticated).

Login credentials for local dev:
* **admin / admin** – ADMIN + BASIC roles
* **librarian / lib** – LIBRARIAN + BASIC roles
* **user / user** – BASIC role only

See `data.sql` for full seed list.

---
## Configuration
All runtime options live in `src/main/resources/application.properties`  
Environment variables `DB_USERNAME` / `DB_PASSWORD` override the default Postgres creds.
```
jdbc.driverClassName=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:5432/library-managment
jdbc.username=${DB_USERNAME:postgres}
jdbc.password=${DB_PASSWORD:1111}

# connection-pool
pool.initialSize=5
pool.maxSize=20
pool.borrowTimeoutMillis=30000
```

For tests we use an in-memory H2 (see `src/test/resources/application.properties`).

---
## Tests & Coverage
```bash
mvn spotless:apply   # format
mvn test             # unit + integration tests (requires Docker for Testcontainers)
# coverage report:
open target/site/jacoco/index.html
```
CI fails if Spotless or JaCoCo (<50 %) thresholds are violated.

---
## Notable Features
* Custom `ConnectionPool` with blocking take/offer and transaction-aware close.
* Manual transaction API (`JdbcTransactionManager`) and AOP logging around DAO calls.
* Role-based dashboards (`/dashboard-*`) with collapsible Bootstrap panels.
* EN / RU toggle via request `lang` param, backed by message bundles.
* Spotless & google-java-format ensure unified code style.

---
## Contributing
1. Fork ➜ create feature branch ➜ commit (+ tests & Javadoc) ➜ pull-request.  
2. Pre-commit hook: `mvn spotless:apply`.

---
© 2025 Library-Management Team. Licensed under MIT.
