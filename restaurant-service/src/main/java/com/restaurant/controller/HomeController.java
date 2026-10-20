package com.restaurant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.restaurant.model.OrderStatus;
import com.restaurant.service.MenuService;
import com.restaurant.service.OrderService;

/**
 * MVC:
 *   Model      - data put into the "Model" object (and the @Entity classes)
 *   View       - the Thymeleaf HTML template (src/main/resources/templates/index.html)
 *   Controller - this class: handles the request, fills the model, returns the view name
 */
@Controller
public class HomeController {

    private final MenuService menuService;
    private final OrderService orderService;

    public HomeController(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("menuCount", menuService.count());
        model.addAttribute("orderCount", orderService.count());
        model.addAttribute("openOrders", orderService.findByStatus(OrderStatus.PLACED).size()
                + orderService.findByStatus(OrderStatus.PREPARING).size());
        model.addAttribute("revenue", orderService.totalRevenue());
        model.addAttribute("topItems", orderService.topSellingItems());
        model.addAttribute("recentOrders", orderService.findAll().stream().limit(5).toList());
        return "index";
    }
}
