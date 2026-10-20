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

import com.restaurant.dto.MenuItemForm;
import com.restaurant.service.MenuService;

import jakarta.validation.Valid;

/** HTML pages for the menu (CRUD). */
@Controller
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(required = false) Boolean veg,
                       Model model) {
        if (q != null && !q.isBlank()) {
            model.addAttribute("items", menuService.search(q));
        } else if (maxPrice != null) {
            model.addAttribute("items", menuService.findAvailableUnderPrice(maxPrice));
        } else if (Boolean.TRUE.equals(veg)) {
            model.addAttribute("items", menuService.findVegetarian());
        } else {
            model.addAttribute("items", menuService.findAll());
        }
        model.addAttribute("q", q);
        model.addAttribute("maxPrice", maxPrice);
        return "menu/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new MenuItemForm());
        return "menu/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", MenuItemForm.fromEntity(menuService.findById(id)));
        return "menu/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") MenuItemForm form, BindingResult result,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "menu/form"; // show the form again with error messages
        }
        if (form.getId() == null) {
            menuService.create(form.toEntity());
            redirect.addFlashAttribute("success", "Added '" + form.getName() + "' to the menu");
        } else {
            menuService.update(form.getId(), form.toEntity());
            redirect.addFlashAttribute("success", "Updated '" + form.getName() + "'");
        }
        return "redirect:/menu"; // Post/Redirect/Get pattern
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            menuService.delete(id);
            redirect.addFlashAttribute("success", "Menu item deleted");
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/menu";
    }
}
