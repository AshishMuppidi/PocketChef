package com.whatsonmyplate.dto.pantry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PantryItemCreateRequest {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 150, message = "Ingredient name must not exceed 150 characters")
    private String name;

    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    private LocalDate expirationDate;

    public PantryItemCreateRequest() {
    }

    public PantryItemCreateRequest(String name, Double quantity, String unit, LocalDate expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expirationDate = expirationDate;
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

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
