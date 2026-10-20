package com.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.model.Beverage;
import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;
import com.restaurant.repository.MenuItemRepository;

/** Tests the HQL queries and the inheritance mapping. @Transactional rolls back after each test. */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository repository;

    @Test
    void idIsGeneratedAndSubclassIsLoadedBack() {
        MenuItem saved = repository.save(new Beverage("Test Juice", null, 60, "Drinks", 200, true));

        assertThat(saved.getId()).isNotNull(); // IDENTITY strategy
        MenuItem loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded).isInstanceOf(Beverage.class); // discriminator column worked
    }

    @Test
    void hqlFindAvailableUnderPrice() {
        List<MenuItem> items = repository.findAvailableUnderPrice(60);

        assertThat(items).isNotEmpty();
        assertThat(items).allMatch(i -> i.getPrice() <= 60 && i.isAvailable());
    }

    @Test
    void hqlFindVegetarianReturnsOnlyVegFood() {
        repository.save(new FoodItem("Test Mutton Curry", null, 350, "Main Course", false, "HOT"));

        List<FoodItem> veg = repository.findVegetarianItems();

        assertThat(veg).isNotEmpty();
        assertThat(veg).allMatch(FoodItem::isVegetarian);
        assertThat(veg).noneMatch(f -> f.getName().equals("Test Mutton Curry"));
    }

    @Test
    void hqlSearchIsCaseInsensitive() {
        assertThat(repository.searchByName("PANEER")).isNotEmpty();
    }
}
