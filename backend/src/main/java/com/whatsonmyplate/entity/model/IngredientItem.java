package com.whatsonmyplate.entity.model;

import java.io.Serializable;

public class IngredientItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Double quantity;
    private String unit;

    public IngredientItem() {
    }

    public IngredientItem(String name, Double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
