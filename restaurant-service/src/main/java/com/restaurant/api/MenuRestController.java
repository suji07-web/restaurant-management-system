package com.restaurant.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;
import com.restaurant.service.MenuService;

import jakarta.validation.Valid;

/**
 * REST API for the menu. @RestController = @Controller + @ResponseBody,
 * so return values are converted to JSON instead of being treated as view names.
 *
 *   GET    /api/menu                  all items          (?maxPrice=200 or ?q=paneer)
 *   GET    /api/menu/vegetarian       vegetarian food
 *   GET    /api/menu/{id}             one item
 *   POST   /api/menu                  create   (JSON body with "type": "FOOD" or "BEVERAGE")
 *   PUT    /api/menu/{id}             update
 *   DELETE /api/menu/{id}             delete
 */
@RestController
@RequestMapping("/api/menu")
public class MenuRestController {

    private final MenuService menuService;

    public MenuRestController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItem> getAll(@RequestParam(required = false) Double maxPrice,
                                 @RequestParam(required = false) String q) {
        if (maxPrice != null) {
            return menuService.findAvailableUnderPrice(maxPrice);
        }
        if (q != null && !q.isBlank()) {
            return menuService.search(q);
        }
        return menuService.findAll();
    }

    @GetMapping("/vegetarian")
    public List<FoodItem> getVegetarian() {
        return menuService.findVegetarian();
    }

    @GetMapping("/{id}")
    public MenuItem getOne(@PathVariable Long id) {
        return menuService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MenuItem> create(@Valid @RequestBody MenuItem item) {
        MenuItem saved = menuService.create(item);
        return ResponseEntity.created(URI.create("/api/menu/" + saved.getId())).body(saved); // 201 Created
    }

    @PutMapping("/{id}")
    public MenuItem update(@PathVariable Long id, @Valid @RequestBody MenuItem item) {
        return menuService.update(id, item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
