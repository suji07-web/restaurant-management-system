package com.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.BillResponse;
import com.restaurant.dto.OrderRequest;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.CustomerOrder;
import com.restaurant.model.MenuItem;
import com.restaurant.model.OrderItem;
import com.restaurant.model.OrderStatus;
import com.restaurant.repository.CustomerOrderRepository;

/**
 * Business logic for orders.
 *
 * @Transactional: Spring opens a database transaction before the method runs and
 * commits it afterwards (or rolls back if an exception is thrown).
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final MenuService menuService;
    private final BillingClient billingClient;

    public OrderService(CustomerOrderRepository orderRepository, MenuService menuService,
                        BillingClient billingClient) {
        this.orderRepository = orderRepository;
        this.menuService = menuService;
        this.billingClient = billingClient;
    }

    public List<CustomerOrder> findAll() {
        return orderRepository.findAllByOrderByOrderTimeDesc();
    }

    public List<CustomerOrder> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public CustomerOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }

    @Transactional
    public CustomerOrder create(OrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item");
        }
        CustomerOrder order = new CustomerOrder(request.customerName(), request.tableNumber());
        for (OrderRequest.Line line : request.items()) {
            MenuItem menuItem = menuService.findById(line.menuItemId());
            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException("'" + menuItem.getName() + "' is not available right now");
            }
            order.addItem(new OrderItem(menuItem, line.quantity()));
        }
        return orderRepository.save(order); // cascade = ALL also inserts the order items
    }

    @Transactional
    public CustomerOrder updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = findById(id);
        order.setStatus(status); // no save() needed: the entity is "managed", Hibernate detects the change
        return order;
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.delete(findById(id));
    }

    /** Asks the billing microservice to calculate the bill for an order. */
    public BillResponse generateBill(Long id) {
        return billingClient.calculateBill(findById(id));
    }

    public double totalRevenue() {
        return orderRepository.sumSubtotalByStatus(OrderStatus.PAID);
    }

    public List<Object[]> topSellingItems() {
        return orderRepository.findTopSellingItems();
    }

    public long count() {
        return orderRepository.count();
    }
}
