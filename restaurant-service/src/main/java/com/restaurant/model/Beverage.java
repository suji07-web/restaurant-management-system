package com.restaurant.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A drink. Rows in "menu_items" with item_type = 'BEVERAGE'. */
@Entity
@DiscriminatorValue("BEVERAGE")
public class Beverage extends MenuItem {

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "served_cold")
    private boolean servedCold;

    public Beverage() {
    }

    public Beverage(String name, String description, double price, String category,
                    Integer volumeMl, boolean servedCold) {
        super(name, description, price, category);
        this.volumeMl = volumeMl;
        this.servedCold = servedCold;
    }

    @Override
    public String getItemType() {
        return "BEVERAGE";
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
