package com.restaurant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * An order placed at a table.
 * ("Order" is a reserved word in SQL, so the class is CustomerOrder and the table is "customer_orders".)
 *
 * ORM MAPPING: one CustomerOrder has many OrderItems  (@OneToMany <-> @ManyToOne in OrderItem).
 *   cascade = ALL       -> saving/deleting the order also saves/deletes its items
 *   orphanRemoval=true  -> an item removed from the list is deleted from the database
 */
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime = LocalDateTime.now();

    @Column(nullable = false)
    private double subtotal;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public CustomerOrder() {
    }

    public CustomerOrder(String customerName, int tableNumber) {
        this.customerName = customerName;
        this.tableNumber = tableNumber;
    }

    /** Adds an item and keeps both sides of the relationship in sync. */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
        recalculateSubtotal();
    }

    public void recalculateSubtotal() {
        this.subtotal = items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
