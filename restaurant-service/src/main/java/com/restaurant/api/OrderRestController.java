package com.restaurant.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.BillResponse;
import com.restaurant.dto.OrderRequest;
import com.restaurant.model.CustomerOrder;
import com.restaurant.model.OrderStatus;
import com.restaurant.service.OrderService;

import jakarta.validation.Valid;

/**
 * REST API for orders.
 *
 *   GET    /api/orders                 all orders  (?status=PLACED)
 *   GET    /api/orders/{id}            one order
 *   POST   /api/orders                 place an order
 *   PATCH  /api/orders/{id}/status     change status  (?status=SERVED)
 *   GET    /api/orders/{id}/bill       bill calculated by the billing microservice
 *   GET    /api/orders/stats           revenue + best sellers
 *   DELETE /api/orders/{id}            delete
 */
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<CustomerOrder> getAll(@RequestParam(required = false) OrderStatus status) {
        return status == null ? orderService.findAll() : orderService.findByStatus(status);
    }

    @GetMapping("/{id}")
    public CustomerOrder getOne(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> create(@Valid @RequestBody OrderRequest request) {
        CustomerOrder order = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(order);
    }

    @PatchMapping("/{id}/status")
    public CustomerOrder updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    @GetMapping("/{id}/bill")
    public BillResponse bill(@PathVariable Long id) {
        return orderService.generateBill(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOrders", orderService.count());
        stats.put("revenueFromPaidOrders", orderService.totalRevenue());
        stats.put("topSellingItems", orderService.topSellingItems().stream()
                .map(row -> Map.of("item", row[0], "quantitySold", row[1]))
                .toList());
        return stats;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
