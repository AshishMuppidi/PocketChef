package com.whatsonmyplate.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class IngredientItemDto {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 150, message = "Ingredient name cannot exceed 150 characters")
    private String name;

    @NotNull(message = "Ingredient quantity is required")
    @Positive(message = "Ingredient quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "Ingredient unit is required")
    @Size(max = 50, message = "Ingredient unit cannot exceed 50 characters")
    private String unit;

    public IngredientItemDto() {
    }

    public IngredientItemDto(String name, Double quantity, String unit) {
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
