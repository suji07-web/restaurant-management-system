package com.restaurant.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Backing object for the "New order" HTML form: menuItemId -> quantity. */
public class OrderForm {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Min(value = 1, message = "Table number must be 1 or more")
    private int tableNumber = 1;

    private Map<Long, Integer> quantities = new HashMap<>();

    /** Converts the form into the same request object the REST API uses. */
    public OrderRequest toRequest() {
        List<OrderRequest.Line> lines = new ArrayList<>();
        quantities.forEach((menuItemId, qty) -> {
            if (qty != null && qty > 0) {
                lines.add(new OrderRequest.Line(menuItemId, qty));
            }
        });
        return new OrderRequest(customerName, tableNumber, lines);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Map<Long, Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(Map<Long, Integer> quantities) {
        this.quantities = quantities;
    }
}
