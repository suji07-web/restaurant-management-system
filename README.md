# Restaurant Management System — "Spice Garden"

**IT23502 Web Programming – Mini Project (Unit IV: Advanced Frameworks, Unit V: Web Services)**

A small web application a restaurant can use to manage its **menu**, take **orders** at tables and generate **bills**.
It is built with Spring Boot, Spring MVC, JPA/Hibernate and REST, and has two independent services (microservices).

| Team member | Register number | Main contribution |
|---|---|---|
| _Name 1_ | _Reg no_ | _e.g. Entities, JPA/Hibernate, HQL, database_ |
| _Name 2_ | _Reg no_ | _e.g. MVC controllers, Thymeleaf pages, AOP_ |
| _Name 3_ | _Reg no_ | _e.g. REST APIs, billing microservice, Postman, Git/CI_ |

---

## 1. Problem statement

Small restaurants often track menus and orders on paper, which leads to wrong bills, no record of what sells well,
and no easy way to change prices. This project gives them a simple web application to:

- maintain the menu (food and beverages) with prices and availability,
- place orders for a table and follow their status (PLACED → PREPARING → SERVED → PAID),
- generate a bill with GST, service charge and discount, using a separate billing service,
- view a dashboard with revenue and the best-selling items,
- expose everything as a REST API so other apps (for example a mobile app) can use it.

## 2. Objectives

1. Build an MVC web application with Spring Boot and Thymeleaf.
2. Store data in PostgreSQL/MySQL using JPA + Hibernate (ORM), with full CRUD.
3. Show ORM relationships, an ID generation strategy, inheritance mapping and HQL queries.
4. Show dependency injection, BeanFactory/ApplicationContext and AOP (logging).
5. Provide REST APIs (GET, POST, PUT, PATCH, DELETE) and test them with Postman.
6. Split billing into a second microservice and call it over REST.
7. Keep the code on Git/GitHub, build it with Maven and run the tests automatically with GitHub Actions.

## 3. Technology used

| Layer | Technology |
|---|---|
| Language / build | Java 17, Maven (Maven Wrapper `mvnw` included) |
| Framework | Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring AOP |
| ORM | Hibernate 7 (JPA implementation) |
| Database | PostgreSQL (default) or MySQL; H2 in-memory for tests |
| View | Thymeleaf templates + CSS |
| REST client | Spring `RestClient` (service-to-service call) |
| Testing | JUnit 5, Spring Boot Test, MockMvc, Postman |
| DevOps | Git, GitHub, GitHub Actions (CI) |
| IDE | Spring Tool Suite (STS) |

## 4. Architecture (summary)

```
 Browser (HTML pages)          Postman / other apps (JSON)
          │                                │
          ▼                                ▼
 ┌──────────────────────── restaurant-service :8080 ─────────────────────────┐
 │  MVC Controllers (/menu, /orders)      REST Controllers (/api/...)        │
 │                 └──────────────┬───────────────┘                           │
 │                        Services (@Service)  ◄── LoggingAspect (AOP)        │
 │                 ┌──────────────┴───────────────┐                           │
 │      Repositories (JPA + HQL)            BillingClient (RestClient) ───────┼──► billing-service :8082
 │                 │                                                          │     POST /api/bills/calculate
 └─────────────────┼──────────────────────────────────────────────────────────┘
                   ▼
        PostgreSQL / MySQL  (menu_items, customer_orders, order_items)
```

Detailed diagrams (architecture, ER diagram, sequence diagram) are in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## 5. Project structure

```
College_Project/
├── restaurant-service/                 main application (port 8080)
│   ├── pom.xml
│   └── src/main/java/com/restaurant/
│       ├── RestaurantServiceApplication.java   @SpringBootApplication (entry point)
│       ├── model/        @Entity classes: MenuItem (abstract), FoodItem, Beverage, CustomerOrder, OrderItem
│       ├── repository/   Spring Data JPA repositories with HQL @Query methods
│       ├── service/      business logic (@Service, @Transactional) + BillingClient
│       ├── controller/   MVC controllers -> Thymeleaf pages
│       ├── api/          REST controllers -> JSON
│       ├── aspect/       LoggingAspect (AOP)
│       ├── config/       AppConfig (@Bean), DataSeeder (sample data)
│       ├── demo/         HibernateSessionDemo, BeanContainerDemo
│       ├── dto/          request/response/form objects
│       └── exception/    exception classes + global handlers
│   └── src/main/resources/
│       ├── application*.properties   settings per database (postgres / mysql / h2)
│       ├── templates/                Thymeleaf HTML pages
│       └── static/css/style.css
├── billing-service/                    second microservice (port 8082, no database)
├── database/                           schema (PostgreSQL + MySQL) and sample records
├── postman/                            Postman collection for all APIs
├── docs/                               architecture diagrams and viva notes
└── .github/workflows/build.yml         CI pipeline (GitHub Actions)
```

## 6. How to run locally

### Prerequisites
- **JDK 17 or newer.** Check with `java -version`. On macOS: `brew install --cask temurin@17`
- **PostgreSQL** (default) **or MySQL**, running on the standard port
- Maven is **not** required: use the included wrapper `./mvnw` (`mvnw.cmd` on Windows)
- Optional: Spring Tool Suite, Postman

### Step 1 – create the database
PostgreSQL:
```sql
CREATE DATABASE restaurant_db;
```
MySQL: nothing to do, the database is created automatically.

The tables are created by Hibernate on first start, and sample menu items and orders are inserted automatically.

### Step 2 – start the billing microservice (port 8082)
```bash
cd billing-service
./mvnw spring-boot:run
```

### Step 3 – start the restaurant service (port 8080)
Open a second terminal:
```bash
cd restaurant-service

# PostgreSQL (default). Set your DB login if it is not postgres/postgres:
DB_USERNAME=postgres DB_PASSWORD=postgres ./mvnw spring-boot:run

# or MySQL
DB_USERNAME=root DB_PASSWORD=root ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# or H2 in-memory (no database installation needed, for a quick try)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
On Windows use `mvnw.cmd` and set the variables with `set DB_USERNAME=postgres` first.

### Step 4 – open the app
- Web application: http://localhost:8080
- REST API example: http://localhost:8080/api/menu
- Billing service: http://localhost:8082/api/bills/rates

### Running from Spring Tool Suite (STS)
1. `File → Import → Maven → Existing Maven Projects`, select the `College_Project` folder and tick both projects.
2. For a database other than postgres/postgres, set `DB_USERNAME` / `DB_PASSWORD` under
   `Run Configurations → Environment`, or edit `application-postgres.properties`.
3. Open the **Boot Dashboard**, select `billing-service` and press **(Re)start**, then do the same for `restaurant-service`.

### Build a JAR and run the tests (Maven)
```bash
cd restaurant-service
./mvnw clean package          # compiles, runs all tests, creates target/restaurant-service-0.0.1-SNAPSHOT.jar
java -jar target/restaurant-service-0.0.1-SNAPSHOT.jar
```
The tests use the H2 in-memory database, so they run without PostgreSQL/MySQL.

## 7. Web pages

| URL | Page |
|---|---|
| `/` | Dashboard: counts, revenue, best sellers (HQL), recent orders |
| `/menu` | Menu list with search, price filter and vegetarian filter |
| `/menu/new`, `/menu/{id}/edit` | Add or edit a food item or beverage (with validation) |
| `/orders` | Orders, filterable by status |
| `/orders/new` | Place an order (choose quantities) |
| `/orders/{id}` | Order details, change status, **generate bill** (calls billing-service), delete |

## 8. REST API

Base URL `http://localhost:8080`. Import [postman/Restaurant-API.postman_collection.json](postman/Restaurant-API.postman_collection.json) into Postman to get every request ready to run.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/menu` | All menu items (`?maxPrice=100` or `?q=paneer`) |
| GET | `/api/menu/vegetarian` | Vegetarian food (HQL on a subclass) |
| GET | `/api/menu/{id}` | One item (404 if not found) |
| POST | `/api/menu` | Create an item; body needs `"type": "FOOD"` or `"BEVERAGE"` → **201 Created** |
| PUT | `/api/menu/{id}` | Update an item |
| DELETE | `/api/menu/{id}` | Delete an item → 204 (400 if it is used in an order) |
| GET | `/api/orders` | All orders (`?status=PAID`) |
| GET | `/api/orders/{id}` | One order with its items |
| POST | `/api/orders` | Place an order → **201 Created** |
| PATCH | `/api/orders/{id}/status?status=SERVED` | Change the status |
| GET | `/api/orders/{id}/bill` | Bill, calculated by **billing-service** (503 if it is down) |
| GET | `/api/orders/stats` | Revenue and best sellers |
| DELETE | `/api/orders/{id}` | Delete an order |
| GET | `/api/demo/hibernate` | SessionFactory / Session / Transaction demo |
| GET | `/api/demo/beans` | BeanFactory vs ApplicationContext demo |
| GET | `:8082/api/bills/rates` | Billing service: tax rates |
| POST | `:8082/api/bills/calculate` | Billing service: calculate a bill |

Example – place an order:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Priya","tableNumber":6,"items":[{"menuItemId":4,"quantity":2},{"menuItemId":12,"quantity":1}]}'
```

## 9. Where each syllabus requirement is implemented

| Requirement | Where |
|---|---|
| Spring Boot app created in STS | Both projects were generated with Spring Initializr (the same generator STS uses) and can be imported into STS |
| MVC | `controller/*Controller.java` + `templates/*.html` + `model/` |
| JPA + Hibernate, CRUD, annotations | `model/*.java` (`@Entity`, `@Table`, `@Column` …), `repository/`, `service/` |
| ORM mapping | `CustomerOrder` `@OneToMany` ↔ `OrderItem` `@ManyToOne`; `OrderItem` `@ManyToOne` → `MenuItem` |
| ID generation strategy | `@GeneratedValue(strategy = GenerationType.IDENTITY)` on every entity |
| HQL queries (7) | `MenuItemRepository`, `CustomerOrderRepository`, `demo/HibernateSessionDemo` |
| Inheritance mapping | `MenuItem` ← `FoodItem`, `Beverage` with `InheritanceType.SINGLE_TABLE` |
| SessionFactory, Session, Transaction | `demo/HibernateSessionDemo.java` → `GET /api/demo/hibernate` |
| Dependency injection | Constructor injection in every service and controller; `config/AppConfig.java` `@Bean` |
| BeanFactory vs ApplicationContext | `demo/BeanContainerDemo.java` → `GET /api/demo/beans` |
| AOP (logging) | `aspect/LoggingAspect.java` (see the `[AOP]` lines in the console) |
| REST APIs, GET/POST/PUT/PATCH/DELETE | `api/MenuRestController.java`, `api/OrderRestController.java` |
| Postman testing | `postman/` collection; automated tests in `src/test` |
| Second microservice, different port, REST call | `billing-service` (8082) ← `service/BillingClient.java` |
| Git, GitHub, Maven, DevOps | `.gitignore`, `mvnw`, `.github/workflows/build.yml` |
| Database schema + sample records | `database/` |

Explanations of all concepts for the viva: [docs/VIVA-NOTES.md](docs/VIVA-NOTES.md).
