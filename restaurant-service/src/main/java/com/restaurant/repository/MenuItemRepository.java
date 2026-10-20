package com.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;

/**
 * Spring Data JPA repository. JpaRepository already gives us the CRUD methods:
 * save(), findById(), findAll(), deleteById(), count() ...
 *
 * The @Query methods below are written in HQL/JPQL (Hibernate Query Language):
 * they use ENTITY and FIELD names (MenuItem, m.price), NOT table and column names.
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // HQL 1: available items up to a maximum price, cheapest first (named parameter :maxPrice)
    @Query("SELECT m FROM MenuItem m WHERE m.available = true AND m.price <= :maxPrice ORDER BY m.price")
    List<MenuItem> findAvailableUnderPrice(@Param("maxPrice") double maxPrice);

    // HQL 2: query on a SUBCLASS - only FoodItem rows (Hibernate adds WHERE item_type = 'FOOD')
    @Query("SELECT f FROM FoodItem f WHERE f.vegetarian = true ORDER BY f.name")
    List<FoodItem> findVegetarianItems();

    // HQL 3: case-insensitive search by name
    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.name")
    List<MenuItem> searchByName(@Param("keyword") String keyword);

    // Derived query - Spring generates the query from the method name
    List<MenuItem> findByAvailableTrueOrderByCategoryAscNameAsc();
}
