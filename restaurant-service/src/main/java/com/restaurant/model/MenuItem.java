package com.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Base class for everything on the menu.
 *
 * INHERITANCE MAPPING (Hibernate):
 *   Strategy = SINGLE_TABLE -> FoodItem and Beverage are stored in ONE table "menu_items".
 *   The column "item_type" (discriminator) tells Hibernate which subclass a row belongs to.
 *   Columns that belong only to one subclass (e.g. spice_level) are NULL for the other.
 *
 * ID GENERATION:
 *   GenerationType.IDENTITY -> the database generates the id (AUTO_INCREMENT in MySQL,
 *   IDENTITY/SERIAL in PostgreSQL).
 *
 * The Jackson annotations let the REST API send/receive the correct subclass using a "type" field.
 */
@Entity
@Table(name = "menu_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodItem.class, name = "FOOD"),
        @JsonSubTypes.Type(value = Beverage.class, name = "BEVERAGE")
})
public abstract class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false)
    private double price;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private boolean available = true;

    protected MenuItem() {
        // required by JPA
    }

    protected MenuItem(String name, String description, double price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    /** "FOOD" or "BEVERAGE" - used by the HTML pages. */
    @JsonIgnore
    public abstract String getItemType();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
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
}
