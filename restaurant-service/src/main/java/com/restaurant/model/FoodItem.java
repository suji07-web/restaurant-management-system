package com.restaurant.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A dish. Rows in "menu_items" with item_type = 'FOOD'. */
@Entity
@DiscriminatorValue("FOOD")
public class FoodItem extends MenuItem {

    @Column(name = "vegetarian")
    private boolean vegetarian;

    /** MILD, MEDIUM or HOT */
    @Column(name = "spice_level", length = 10)
    private String spiceLevel;

    public FoodItem() {
    }

    public FoodItem(String name, String description, double price, String category,
                    boolean vegetarian, String spiceLevel) {
        super(name, description, price, category);
        this.vegetarian = vegetarian;
        this.spiceLevel = spiceLevel;
    }

    @Override
    public String getItemType() {
        return "FOOD";
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
}
