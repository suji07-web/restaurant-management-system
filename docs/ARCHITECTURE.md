# Architecture

GitHub and VS Code render these diagrams automatically. For the report, paste a diagram into https://mermaid.live and export it as PNG.

## 1. System architecture (microservices)

```mermaid
flowchart LR
    U[Browser<br/>HTML pages] -->|HTTP| RS
    P[Postman /<br/>other clients] -->|HTTP + JSON| RS

    subgraph RS[restaurant-service : 8080]
        direction TB
        MVC[MVC Controllers<br/>MenuController, OrderController, HomeController]
        API[REST Controllers<br/>/api/menu, /api/orders, /api/demo]
        SVC[Services<br/>MenuService, OrderService]
        AOP{{LoggingAspect<br/>AOP}}
        REPO[Repositories<br/>Spring Data JPA + HQL]
        BC[BillingClient<br/>RestClient]
        MVC --> SVC
        API --> SVC
        AOP -.logs every call.-> SVC
        SVC --> REPO
        SVC --> BC
    end

    REPO -->|JDBC via Hibernate| DB[(PostgreSQL / MySQL<br/>restaurant_db)]
    BC -->|POST /api/bills/calculate<br/>JSON| BS

    subgraph BS[billing-service : 8082]
        direction TB
        BCTRL[BillingController] --> BSVC[BillingService<br/>GST, service charge, discount]
    end
```

The two services are **separate Spring Boot applications**: separate `pom.xml`, separate process, separate port.
They only talk to each other through REST/JSON. If billing-service is stopped, the restaurant app keeps working
and only the "Generate bill" feature shows an error (HTTP 503 in the API).

## 2. Layered design inside restaurant-service

```mermaid
flowchart TB
    V[View - Thymeleaf templates] --- C[Controller layer - @Controller / @RestController]
    C --> S[Service layer - @Service, @Transactional]
    S --> R[Repository layer - JpaRepository + @Query HQL]
    R --> H[Hibernate ORM]
    H --> D[(Database)]
    M[Model - @Entity classes] -.used by all layers.- S
```

## 3. Entity–Relationship diagram

```mermaid
erDiagram
    MENU_ITEMS ||--o{ ORDER_ITEMS : "appears in"
    CUSTOMER_ORDERS ||--|{ ORDER_ITEMS : "contains"

    MENU_ITEMS {
        bigint id PK "IDENTITY"
        varchar item_type "discriminator FOOD or BEVERAGE"
        varchar name
        varchar description
        double price
        varchar category
        boolean available
        boolean vegetarian "FoodItem only"
        varchar spice_level "FoodItem only"
        int volume_ml "Beverage only"
        boolean served_cold "Beverage only"
    }
    CUSTOMER_ORDERS {
        bigint id PK "IDENTITY"
        varchar customer_name
        int table_number
        varchar status "PLACED PREPARING SERVED PAID CANCELLED"
        timestamp order_time
        double subtotal
    }
    ORDER_ITEMS {
        bigint id PK "IDENTITY"
        bigint order_id FK
        bigint menu_item_id FK
        int quantity
        double unit_price
    }
```

## 4. Class diagram (entities and inheritance)

```mermaid
classDiagram
    class MenuItem {
        <<abstract>>
        Long id
        String name
        String description
        double price
        String category
        boolean available
        getItemType()* String
    }
    class FoodItem {
        boolean vegetarian
        String spiceLevel
    }
    class Beverage {
        Integer volumeMl
        boolean servedCold
    }
    class CustomerOrder {
        Long id
        String customerName
        int tableNumber
        OrderStatus status
        LocalDateTime orderTime
        double subtotal
        addItem(OrderItem)
    }
    class OrderItem {
        Long id
        int quantity
        double unitPrice
        getLineTotal() double
    }
    MenuItem <|-- FoodItem
    MenuItem <|-- Beverage
    CustomerOrder "1" *-- "many" OrderItem : items
    OrderItem "many" --> "1" MenuItem : menuItem
```

## 5. Sequence: generating a bill (service-to-service REST call)

```mermaid
sequenceDiagram
    actor User
    participant OC as OrderController (8080)
    participant OS as OrderService
    participant DB as Database
    participant BC as BillingClient
    participant BS as billing-service (8082)

    User->>OC: POST /orders/5/bill
    OC->>OS: generateBill(5)
    OS->>DB: SELECT order 5 (Hibernate)
    DB-->>OS: CustomerOrder (subtotal 1080)
    OS->>BC: calculateBill(order)
    BC->>BS: POST /api/bills/calculate {"orderId":5,"subtotal":1080}
    BS-->>BC: 200 {"gst":54,"serviceCharge":108,"discount":108,"total":1134}
    BC-->>OS: BillResponse
    OS-->>OC: BillResponse
    OC-->>User: orders/detail.html with the bill
```
