-- =====================================================================
--  Restaurant Management System - database schema (MySQL)
--  Hibernate creates these tables automatically; this file is for the report.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

CREATE TABLE menu_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- GenerationType.IDENTITY
    item_type    VARCHAR(31)  NOT NULL,                                 -- discriminator: FOOD / BEVERAGE
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    price        DOUBLE       NOT NULL,
    category     VARCHAR(50)  NOT NULL,
    available    BIT          NOT NULL,
    vegetarian   BIT,             -- FoodItem only
    spice_level  VARCHAR(10),     -- FoodItem only
    volume_ml    INT,             -- Beverage only
    served_cold  BIT              -- Beverage only
);

CREATE TABLE customer_orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    table_number  INT          NOT NULL,
    status        ENUM('PLACED', 'PREPARING', 'SERVED', 'PAID', 'CANCELLED') NOT NULL,
    order_time    DATETIME(6)  NOT NULL,
    subtotal      DOUBLE       NOT NULL
);

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity     INT    NOT NULL,
    unit_price   DOUBLE NOT NULL,
    FOREIGN KEY (order_id)     REFERENCES customer_orders (id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
);
