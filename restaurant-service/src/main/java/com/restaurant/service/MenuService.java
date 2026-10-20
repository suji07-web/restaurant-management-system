package com.restaurant.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;
import com.restaurant.repository.CustomerOrderRepository;
import com.restaurant.repository.MenuItemRepository;

/**
 * Business logic for the menu.
 *
 * DEPENDENCY INJECTION: the repositories are passed in through the constructor.
 * Spring (the ApplicationContext / IoC container) creates them and injects them for us -
 * this class never calls "new MenuItemRepository()".
 */
@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CustomerOrderRepository orderRepository;

    public MenuService(MenuItemRepository menuItemRepository, CustomerOrderRepository orderRepository) {
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
    }

    public List<MenuItem> findAll() {
        return menuItemRepository.findAll(Sort.by("category", "name"));
    }

    public List<MenuItem> findAvailable() {
        return menuItemRepository.findByAvailableTrueOrderByCategoryAscNameAsc();
    }

    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item " + id + " not found"));
    }

    public List<MenuItem> search(String keyword) {
        return menuItemRepository.searchByName(keyword);
    }

    public List<MenuItem> findAvailableUnderPrice(double maxPrice) {
        return menuItemRepository.findAvailableUnderPrice(maxPrice);
    }

    public List<FoodItem> findVegetarian() {
        return menuItemRepository.findVegetarianItems();
    }

    @Transactional
    public MenuItem create(MenuItem item) {
        item.setId(null); // let the database generate the id (IDENTITY strategy)
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem update(Long id, MenuItem changes) {
        MenuItem existing = findById(id);
        if (!existing.getItemType().equals(changes.getItemType())) {
            throw new IllegalArgumentException("The type of a menu item (FOOD/BEVERAGE) cannot be changed");
        }
        changes.setId(id);
        return menuItemRepository.save(changes); // save() with an existing id = UPDATE (merge)
    }

    @Transactional
    public void delete(Long id) {
        MenuItem item = findById(id);
        long usedIn = orderRepository.countOrderLinesForMenuItem(id);
        if (usedIn > 0) {
            throw new IllegalStateException("'" + item.getName() + "' is used in " + usedIn
                    + " order line(s) and cannot be deleted. Mark it as unavailable instead.");
        }
        menuItemRepository.delete(item);
    }

    public long count() {
        return menuItemRepository.count();
    }
}
