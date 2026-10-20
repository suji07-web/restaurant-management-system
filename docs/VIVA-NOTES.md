# Viva Notes – Concepts and where they are in our code

Each section explains one concept briefly and points to the file that shows it.
File paths are relative to `restaurant-service/src/main/java/com/restaurant/` unless stated otherwise.

---

## Demo script (about 8 minutes)

1. Start `billing-service`, then `restaurant-service`. Show the two ports (8082, 8080) in the console.
2. **Dashboard** `/` → counts, revenue (HQL SUM), best sellers (HQL GROUP BY).
3. **Menu** → add a food item, edit it, try saving with an empty name (validation), use the filters (HQL), try deleting an item that is used in an order (it is refused).
4. **New order** → place an order → order page → change status → **Generate bill** (call to the microservice).
5. Stop billing-service → Generate bill again → friendly error. This shows the services are independent.
6. **Postman** → GET `/api/menu`, POST `/api/menu`, POST `/api/orders`, GET `/api/orders/1/bill`, a 404 and a 400.
7. Open `/api/demo/hibernate` and `/api/demo/beans`; show the `[AOP]` lines and the Hibernate SQL in the console.
8. Show the database tables (`menu_items` with the `item_type` column), the GitHub repo and the green GitHub Actions run.

---

# UNIT IV – Advanced Frameworks

## 1. MVC (Model–View–Controller)
Splits the application into three parts so each part can change on its own:
- **Model**: the data → `model/*.java` entities, plus the `Model` object passed to the view.
- **View**: what the user sees → `templates/*.html` (Thymeleaf).
- **Controller**: receives the request, calls the service, chooses the view → `controller/MenuController.java`.

Flow: Browser → **DispatcherServlet** (Spring's front controller) → `@GetMapping` method → returns `"menu/list"` → Thymeleaf renders `templates/menu/list.html`.
We also use the **Post/Redirect/Get** pattern: after a form POST we `redirect:` so refreshing the page does not submit the form twice.

## 2. ORM, JPA and Hibernate
- **ORM (Object-Relational Mapping)** maps Java classes to tables and objects to rows, so we work with objects and do not have to write SQL for CRUD.
- **JPA (Jakarta Persistence API)** is the *specification*: a set of interfaces and annotations (`@Entity`, `@Id`, `EntityManager`).
- **Hibernate** is the *implementation* of JPA that actually generates and runs the SQL. Spring Boot uses Hibernate by default.
- **Spring Data JPA** builds on top of JPA: we only write an interface (`MenuItemRepository extends JpaRepository`) and Spring creates the class at runtime with `save`, `findById`, `findAll`, `delete`, `count`, and so on.

Annotations used (`model/MenuItem.java`, `model/CustomerOrder.java`, `model/OrderItem.java`):
`@Entity`, `@Table(name=…)`, `@Id`, `@GeneratedValue`, `@Column(nullable, length)`, `@Enumerated(EnumType.STRING)`,
`@OneToMany(mappedBy, cascade, orphanRemoval)`, `@ManyToOne`, `@JoinColumn`, `@Inheritance`, `@DiscriminatorColumn`, `@DiscriminatorValue`.

**ORM mapping in our project**
- `CustomerOrder` **1 — many** `OrderItem`: `@OneToMany(mappedBy = "order")` on one side and `@ManyToOne @JoinColumn(name="order_id")` on the other. It is *bidirectional*; the owning side is `OrderItem` because it has the foreign key.
- `OrderItem` **many — 1** `MenuItem`: unidirectional `@ManyToOne` → column `menu_item_id`.
- `cascade = CascadeType.ALL`: saving an order also saves its items. `orphanRemoval = true`: an item removed from the list is deleted.

**CRUD** → `service/MenuService.java`: create = `save()` (INSERT), read = `findById/findAll` (SELECT), update = `save()` with an existing id (UPDATE), delete = `delete()` (DELETE).
Turn on `spring.jpa.show-sql=true` (already on) to see the SQL Hibernate generates.

## 3. ID generation strategies
`@GeneratedValue(strategy = …)`:

| Strategy | How the id is produced | Notes |
|---|---|---|
| `IDENTITY` ✅ *(used by us)* | The database auto-increments the column (MySQL `AUTO_INCREMENT`, PostgreSQL `GENERATED … AS IDENTITY`) | Simple, works on both of our databases. Hibernate must INSERT immediately to learn the id, so batch inserts are not possible |
| `SEQUENCE` | Hibernate asks a DB sequence for the next value (`@SequenceGenerator`) | Efficient (can pre-allocate ids); PostgreSQL/Oracle |
| `TABLE` | A separate table stores the next id | Works everywhere but slow (locking) |
| `AUTO` | Hibernate chooses based on the database | Default |
| `UUID` | Random universally unique id | Good for distributed systems |

## 4. Hibernate inheritance mapping
`MenuItem` (abstract) is the parent; `FoodItem` and `Beverage` extend it.

| Strategy | Tables | Pros / cons |
|---|---|---|
| **`SINGLE_TABLE`** ✅ *(used)* | 1 table `menu_items` + discriminator column `item_type` | Fastest, no joins; subclass columns must allow NULL |
| `JOINED` | parent table + one table per subclass, joined by id | Normalised; needs joins |
| `TABLE_PER_CLASS` | one complete table per concrete class | No joins for one type; polymorphic queries need UNION |
| `@MappedSuperclass` | no parent table; only shares fields | Not a real entity, so no polymorphic queries |

Proof: `SELECT item_type, name FROM menu_items;` shows FOOD and BEVERAGE rows in one table.
`repository.findById(id)` gives back a `FoodItem` or a `Beverage` object, and Hibernate decides which by reading `item_type`.

## 5. HQL (Hibernate Query Language)
HQL looks like SQL but uses **entity class and field names**, not table and column names. It is database-independent: Hibernate translates it to the MySQL or PostgreSQL dialect.
JPQL (the JPA standard) is a subset of HQL; Spring's `@Query` accepts it.

Our HQL queries:
1. `SELECT m FROM MenuItem m WHERE m.available = true AND m.price <= :maxPrice ORDER BY m.price` – named parameter
2. `SELECT f FROM FoodItem f WHERE f.vegetarian = true` – query on a **subclass** (Hibernate adds `item_type='FOOD'`)
3. `… WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))` – functions
4. `SELECT o FROM CustomerOrder o WHERE o.status = :status ORDER BY o.orderTime DESC`
5. `SELECT COALESCE(SUM(o.subtotal), 0) FROM CustomerOrder o WHERE o.status = :status` – aggregate
6. `SELECT oi.menuItem.name, SUM(oi.quantity) FROM OrderItem oi GROUP BY oi.menuItem.name …` – implicit join + GROUP BY
7. `SELECT COUNT(oi) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId`
8. Native Hibernate API: `session.createQuery("FROM MenuItem m WHERE m.price < :limit", MenuItem.class)` in `demo/HibernateSessionDemo.java`

SQL vs HQL: `SELECT * FROM menu_items WHERE price < 100` (table/columns) vs `FROM MenuItem m WHERE m.price < 100` (class/fields).

## 6. SessionFactory, Session, Transaction → `demo/HibernateSessionDemo.java`, `GET /api/demo/hibernate`
- **SessionFactory**: created **once** at start-up and shared by the whole application. It is heavy and thread-safe, holds the configuration and all entity mappings, and creates Sessions. (In JPA terms: `EntityManagerFactory`.)
- **Session**: short-lived and **not** thread-safe, one per unit of work. It wraps a JDBC connection and holds the first-level cache (the *persistence context*). Methods: `persist`, `get/find`, `merge`, `remove`, `createQuery`. (In JPA terms: `EntityManager`.)
- **Transaction**: `beginTransaction()` … `commit()`, or `rollback()` on error. All operations succeed together or none do (ACID).

In normal code Spring handles all three for us: `@Transactional` opens the transaction and commits or rolls back automatically (see `service/OrderService.java`).
Entity states: **transient** (new object) → **persistent/managed** (inside a Session, changes saved automatically – see `updateStatus`, which needs no `save()`) → **detached** (Session closed) → **removed**.

## 7. Spring Framework, IoC and Dependency Injection
- **IoC (Inversion of Control)**: the framework creates and connects the objects (*beans*), not our code.
- **DI (Dependency Injection)**: a class receives its dependencies from outside. We use **constructor injection** everywhere, for example:
  ```java
  public OrderService(CustomerOrderRepository orderRepository, MenuService menuService, BillingClient billingClient)
  ```
  `OrderService` never writes `new MenuService()`. Spring sees the constructor and passes in the beans.
  Benefits: loose coupling, and easy testing (in `RestApiTest` we replace `BillingClient` with a mock using `@MockitoBean`).
- Beans are created from `@Component`, `@Service`, `@Repository`, `@Controller`/`@RestController` and `@Bean` methods (`config/AppConfig.java`).
- Other injection types: setter injection and field injection (`@Autowired` on a field; not recommended).
- Bean scopes: `singleton` (default, one instance), `prototype`, `request`, `session`.

## 8. BeanFactory vs ApplicationContext → `demo/BeanContainerDemo.java`, `GET /api/demo/beans`

| BeanFactory | ApplicationContext |
|---|---|
| Basic IoC container (root interface) | Extends BeanFactory, so it does everything BeanFactory does and more |
| Creates beans **lazily**, on `getBean()` | Creates singleton beans **eagerly** at start-up (errors show early) |
| DI only | + annotation processing, AOP, events, i18n messages, `Environment`/properties |
| Very light, rarely used directly | Used by Spring Boot (`AnnotationConfigServletWebServerApplicationContext`) |

The demo endpoint shows this: with a plain `DefaultListableBeanFactory` the constructor runs only after `getBean()`, but with an `ApplicationContext` it runs during `refresh()`.
It also lists our own beans and proves `MenuService` is a singleton.

## 9. AOP (Aspect-Oriented Programming) → `aspect/LoggingAspect.java`
AOP separates **cross-cutting concerns** (logging, security, transactions, timing) from business logic.
- **Aspect**: the module (`LoggingAspect`)
- **Join point**: a point during execution, i.e. a method call
- **Pointcut**: an expression that selects join points → `execution(* com.restaurant.service..*(..))`
- **Advice**: the code to run → `@Around` (before + after, measures time) and `@AfterThrowing`. Other kinds: `@Before`, `@After`, `@AfterReturning`
- **Weaving**: Spring wraps the bean in a **proxy** (the `MenuService$$SpringCGLIB$$0` shown by `/api/demo/beans`)

Practical use in our app: every service call is logged with its arguments and execution time, for example
`[AOP] --> OrderService.findById(..) args=[999]` and `[AOP] !!! … threw ResourceNotFoundException`.
`@Transactional` itself is also implemented with AOP.

## 10. Spring Boot and STS
- **Spring Boot** makes Spring quick to use:
  - **auto-configuration**: it sees Hibernate and PostgreSQL on the classpath and configures the DataSource and JPA itself
  - **starter dependencies** (`spring-boot-starter-webmvc`, `-data-jpa`, …)
  - an **embedded Tomcat** server, so there is no WAR deployment
  - `application.properties` and **profiles** (`postgres`, `mysql`, `h2`)
  - **Actuator** (`/actuator/health`)
  - `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- **STS (Spring Tool Suite)** is an Eclipse-based IDE for Spring: the *New Spring Starter Project* wizard (Spring Initializr), the **Boot Dashboard** to start and stop several apps, and property auto-completion.

---

# UNIT V – Web Services

## 11. Web services
A **web service** lets two applications talk over a network (HTTP) using a standard format (JSON or XML), regardless of language or platform.
Example: our restaurant-service (client) calls billing-service (provider) over HTTP with JSON.

## 12. REST basics
**REST (REpresentational State Transfer)** is an architectural style:
- Everything is a **resource** with a URL: `/api/menu/5`
- The **HTTP method** says what to do:
  - GET reads
  - POST creates
  - PUT replaces or updates
  - PATCH partially updates
  - DELETE removes
- **Stateless**: every request carries all the information it needs; the server keeps no client session.
- **Representation**: usually JSON.
- **Status codes**:
  - 200 OK
  - 201 Created (our POSTs)
  - 204 No Content (our DELETEs)
  - 400 Bad Request (validation)
  - 404 Not Found
  - 500 Server Error
  - 503 Service Unavailable (billing service down)

Safe methods: GET. Idempotent methods (repeating them gives the same result): GET, PUT, DELETE. POST is **not** idempotent.

## 13. Spring REST → `api/*.java`
- `@RestController` = `@Controller` + `@ResponseBody`: the return value is converted to JSON by Jackson.
- `@RequestMapping("/api/menu")`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`
- `@PathVariable` (`/api/menu/{id}`), `@RequestParam` (`?maxPrice=100`), `@RequestBody` (JSON → Java object)
- `ResponseEntity` to set the status code and headers (`201 Created` + `Location` header)
- `@Valid` + Bean Validation (`@NotBlank`, `@Positive`) → 400 when the data is invalid
- `@RestControllerAdvice` (`exception/ApiExceptionHandler.java`) turns exceptions into JSON errors
- Polymorphic JSON: `@JsonTypeInfo` on `MenuItem` reads `"type": "FOOD"` and creates a `FoodItem`
- Calling another service: `RestClient` (`service/BillingClient.java`)

## 14. REST vs SOAP

| | REST | SOAP |
|---|---|---|
| Type | Architectural style | Protocol (W3C standard) |
| Data format | JSON (also XML, text …) | XML only (SOAP envelope) |
| Transport | HTTP | HTTP, SMTP, JMS … |
| Contract | Optional (OpenAPI/Swagger) | Required **WSDL** |
| Operations | HTTP methods on resources (URLs) | Operations defined in the WSDL, always POST |
| Speed / size | Light, fast, cacheable | Heavier (XML envelope) |
| Security | HTTPS, OAuth, JWT | WS-Security built in |
| Used for | Web/mobile APIs, microservices | Banking, enterprise, strict contracts |
| In Spring | Spring Web MVC (`@RestController`) | **Spring Web Services** (`@Endpoint`, contract-first, XSD) |

## 15. Monolithic vs microservice architecture

| Monolithic | Microservices |
|---|---|
| One application, one deployable (one JAR) | Many small services, each deployed on its own |
| One shared database | Each service owns its data |
| Scale the whole app | Scale only the busy service |
| One bug or crash can stop everything | Failures are isolated (our app works while billing is down) |
| One technology stack | Each service can use different technology |
| Simple to develop at first; hard to maintain when large | More complex: network calls, monitoring, deployment |

**Our project:** `restaurant-service` (menu, orders, database) and `billing-service` (bill calculation, no database). Each is a separate Spring Boot app on its own port, and they communicate with REST. Advantages shown:
- independent deployment (we can change the GST rate and restart only billing)
- fault isolation
- each service has a single responsibility

## 16. Spring Cloud
A set of tools for the common problems of microservices. Relevant features:
- **Service discovery – Eureka**: services register themselves by name. Then `BillingClient` could call `http://billing-service` instead of hard-coding `localhost:8082`. *This is the most relevant feature for our project.*
- **API Gateway – Spring Cloud Gateway**: a single entry point (routing, authentication, rate limiting)
- **Config Server**: central configuration for all services (for example the GST rate)
- **OpenFeign**: a declarative REST client (an interface instead of `RestClient` code)
- **Circuit breaker – Resilience4j**: if billing is down, fail fast and return a fallback
- **LoadBalancer**: spreads calls across several instances of a service

We did not add Spring Cloud because the project has only two services. We explain it as the next step.

## 17. DevOps
**DevOps** combines Development and Operations: a culture and a set of tools that automate building, testing and deploying software so changes reach users quickly and reliably.
Advantages:
- faster releases
- fewer bugs, because tests run automatically
- quick feedback
- better collaboration
- reliable, repeatable deployments

**CI** (Continuous Integration) builds and tests every push. **CD** (Continuous Delivery/Deployment) releases automatically.

What we used:
- **Git**: version control, with history of every change. Each member works on a branch and merges.
- **GitHub**: remote repository, collaboration and pull requests.
- **Maven**: build tool and dependency management. Lifecycle: `validate → compile → test → package → verify → install → deploy`. `./mvnw clean package` builds the JAR and runs the tests. The Maven Wrapper means Maven does not need to be installed.
- **GitHub Actions** (`.github/workflows/build.yml`): CI pipeline that builds both services and runs all tests on every push.

Other DevOps tools:

| Area | Tools |
|---|---|
| CI/CD | Jenkins, GitLab CI |
| Containers | Docker |
| Orchestration | Kubernetes |
| Infrastructure as code | Terraform, Ansible |
| Monitoring | Prometheus, Grafana; Spring Boot Actuator gives health/metrics endpoints |
| Code quality | SonarQube |

---

## Likely viva questions (short answers)

- **Why `CustomerOrder` and not `Order`?** `ORDER` is a reserved SQL word (`ORDER BY`).
- **Why store `unitPrice` in `OrderItem`?** If the menu price changes later, old orders and bills must not change.
- **What does `mappedBy` mean?** The relationship is owned by the other side (`OrderItem.order`), so the foreign key `order_id` is in `order_items`.
- **Why `@JsonIgnore` on `OrderItem.order`?** Otherwise JSON loops forever: order → items → order → …
- **Lazy vs eager?** `@OneToMany` is LAZY by default (items are loaded only when used); `@ManyToOne` is EAGER by default.
- **What does `ddl-auto=update` do?** Hibernate creates or alters tables to match the entities. Other values: `create`, `create-drop`, `validate`, `none`.
- **What is `@Transactional(readOnly = true)` on the class?** All methods are read-only by default; write methods override it with `@Transactional`.
- **How does the order page bind quantities?** Inputs named `quantities[5]` bind to `Map<Long,Integer>` in `OrderForm`.
- **What happens if billing-service is down?** `BillingClient` catches `RestClientException` and throws `BillingServiceException`. The web page shows a message and the API returns 503.
- **Why do the tests use H2?** So they run anywhere (including GitHub Actions) without installing PostgreSQL.
- **How do you change the database?** Use a Spring profile: `-Dspring-boot.run.profiles=mysql`. Only the properties change, not the code; that is the benefit of JPA/ORM.
