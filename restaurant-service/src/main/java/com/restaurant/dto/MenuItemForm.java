package com.restaurant.dto;

import com.restaurant.model.Beverage;
import com.restaurant.model.FoodItem;
import com.restaurant.model.MenuItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Backing object for the "Add / Edit menu item" HTML form.
 * One form is used for both subclasses; "type" decides which entity is created.
 */
public class MenuItemForm {

    private Long id;

    @NotBlank
    private String type = "FOOD";

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotBlank(message = "Category is required")
    private String category;

    private boolean available = true;

    // FoodItem fields
    private boolean vegetarian;
    private String spiceLevel = "MEDIUM";

    // Beverage fields
    private Integer volumeMl;
    private boolean servedCold;

    public static MenuItemForm fromEntity(MenuItem item) {
        MenuItemForm form = new MenuItemForm();
        form.id = item.getId();
        form.type = item.getItemType();
        form.name = item.getName();
        form.description = item.getDescription();
        form.price = item.getPrice();
        form.category = item.getCategory();
        form.available = item.isAvailable();
        if (item instanceof FoodItem food) {
            form.vegetarian = food.isVegetarian();
            form.spiceLevel = food.getSpiceLevel();
        } else if (item instanceof Beverage drink) {
            form.volumeMl = drink.getVolumeMl();
            form.servedCold = drink.isServedCold();
        }
        return form;
    }

    public MenuItem toEntity() {
        MenuItem item = "BEVERAGE".equals(type)
                ? new Beverage(name, description, price, category, volumeMl, servedCold)
                : new FoodItem(name, description, price, category, vegetarian, spiceLevel);
        item.setId(id);
        item.setAvailable(available);
        return item;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public String getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(String spiceLevel) {
        this.spiceLevel = spiceLevel;
    }

    public Integer getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(Integer volumeMl) {
        this.volumeMl = volumeMl;
    }

    public boolean isServedCold() {
        return servedCold;
    }

    public void setServedCold(boolean servedCold) {
        this.servedCold = servedCold;
    }
}
