package com.restaurant.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.restaurant.dto.OrderRequest;
import com.restaurant.model.Beverage;
import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;
import com.restaurant.model.OrderStatus;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.service.OrderService;

/**
 * Inserts sample records the first time the application starts (only if the menu table is empty).
 * CommandLineRunner.run() is called by Spring Boot right after the application has started.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MenuItemRepository menuItemRepository;
    private final OrderService orderService;

    public DataSeeder(MenuItemRepository menuItemRepository, OrderService orderService) {
        this.menuItemRepository = menuItemRepository;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        if (menuItemRepository.count() > 0) {
            return;
        }
        log.info("Empty database - inserting sample menu items and orders");

        List<MenuItem> menu = menuItemRepository.saveAll(List.of(
                new FoodItem("Paneer Tikka", "Grilled cottage cheese with spices", 220, "Starter", true, "MEDIUM"),
                new FoodItem("Chicken 65", "Spicy deep-fried chicken", 240, "Starter", false, "HOT"),
                new FoodItem("Veg Spring Roll", "Crispy rolls with vegetable filling", 160, "Starter", true, "MILD"),
                new FoodItem("Butter Chicken", "Chicken in creamy tomato gravy", 320, "Main Course", false, "MILD"),
                new FoodItem("Paneer Butter Masala", "Paneer in rich tomato gravy", 280, "Main Course", true, "MILD"),
                new FoodItem("Veg Biryani", "Basmati rice with vegetables", 210, "Main Course", true, "MEDIUM"),
                new FoodItem("Chicken Biryani", "Hyderabadi dum biryani", 260, "Main Course", false, "HOT"),
                new FoodItem("Butter Naan", "Tandoor bread with butter", 50, "Breads", true, "MILD"),
                new FoodItem("Gulab Jamun", "Two pieces, served warm", 90, "Dessert", true, "MILD"),
                new Beverage("Masala Chai", "Indian spiced tea", 40, "Drinks", 150, false),
                new Beverage("Filter Coffee", "South Indian filter coffee", 50, "Drinks", 150, false),
                new Beverage("Sweet Lassi", "Chilled yoghurt drink", 80, "Drinks", 300, true),
                new Beverage("Fresh Lime Soda", "Sweet or salted", 70, "Drinks", 300, true)));

        Long paneerTikka = menu.get(0).getId();
        Long butterChicken = menu.get(3).getId();
        Long naan = menu.get(7).getId();
        Long vegBiryani = menu.get(5).getId();
        Long lassi = menu.get(11).getId();
        Long chai = menu.get(9).getId();
        Long gulabJamun = menu.get(8).getId();

        var o1 = orderService.create(new OrderRequest("Arjun", 3, List.of(
                new OrderRequest.Line(paneerTikka, 1),
                new OrderRequest.Line(butterChicken, 2),
                new OrderRequest.Line(naan, 4))));
        orderService.updateStatus(o1.getId(), OrderStatus.PAID);

        var o2 = orderService.create(new OrderRequest("Meera", 5, List.of(
                new OrderRequest.Line(vegBiryani, 2),
                new OrderRequest.Line(lassi, 2))));
        orderService.updateStatus(o2.getId(), OrderStatus.SERVED);

        orderService.create(new OrderRequest("Karthik", 1, List.of(
                new OrderRequest.Line(chai, 2),
                new OrderRequest.Line(gulabJamun, 1))));
    }
}
