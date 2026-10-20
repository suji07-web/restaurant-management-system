package com.restaurant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurant.dto.OrderForm;
import com.restaurant.dto.OrderRequest;
import com.restaurant.exception.BillingServiceException;
import com.restaurant.model.CustomerOrder;
import com.restaurant.model.OrderStatus;
import com.restaurant.service.MenuService;
import com.restaurant.service.OrderService;

import jakarta.validation.Valid;

/** HTML pages for orders. */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final MenuService menuService;

    public OrderController(OrderService orderService, MenuService menuService) {
        this.orderService = orderService;
        this.menuService = menuService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status, Model model) {
        model.addAttribute("orders", status == null ? orderService.findAll() : orderService.findByStatus(status));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "orders/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new OrderForm());
        model.addAttribute("menuItems", menuService.findAvailable());
        return "orders/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") OrderForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        OrderRequest request = form.toRequest();
        if (request.items().isEmpty()) {
            result.reject("noItems", "Select at least one item (quantity 1 or more)");
        }
        if (result.hasErrors()) {
            model.addAttribute("menuItems", menuService.findAvailable());
            return "orders/form";
        }
        CustomerOrder order = orderService.create(request);
        redirect.addFlashAttribute("success", "Order #" + order.getId() + " placed");
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status,
                               RedirectAttributes redirect) {
        orderService.updateStatus(id, status);
        redirect.addFlashAttribute("success", "Status changed to " + status);
        return "redirect:/orders/" + id;
    }

    /** Calls the billing microservice and shows the bill. */
    @PostMapping("/{id}/bill")
    public String bill(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("bill", orderService.generateBill(id));
        } catch (BillingServiceException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        // load the order AFTER the billing call: a failed call rolls back the transaction,
        // which would detach an order loaded earlier (its items could then not be lazily loaded)
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        orderService.delete(id);
        redirect.addFlashAttribute("success", "Order #" + id + " deleted");
        return "redirect:/orders";
    }
}
