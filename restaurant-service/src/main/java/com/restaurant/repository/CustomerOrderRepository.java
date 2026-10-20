package com.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.model.CustomerOrder;
import com.restaurant.model.OrderStatus;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByOrderTimeDesc();

    // HQL 4: orders with a given status, newest first
    @Query("SELECT o FROM CustomerOrder o WHERE o.status = :status ORDER BY o.orderTime DESC")
    List<CustomerOrder> findByStatus(@Param("status") OrderStatus status);

    // HQL 5: aggregate function - total amount of orders with a given status
    @Query("SELECT COALESCE(SUM(o.subtotal), 0) FROM CustomerOrder o WHERE o.status = :status")
    double sumSubtotalByStatus(@Param("status") OrderStatus status);

    // HQL 6: GROUP BY + JOIN through the relationship - best selling items
    @Query("SELECT oi.menuItem.name, SUM(oi.quantity) FROM OrderItem oi "
            + "GROUP BY oi.menuItem.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingItems();

    // HQL 7: how many order lines use a menu item (used before deleting a menu item)
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId")
    long countOrderLinesForMenuItem(@Param("menuItemId") Long menuItemId);
}
