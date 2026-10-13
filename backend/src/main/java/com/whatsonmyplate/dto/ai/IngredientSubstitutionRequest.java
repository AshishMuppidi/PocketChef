package com.whatsonmyplate.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IngredientSubstitutionRequest {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 150, message = "Ingredient name must not exceed 150 characters")
    private String ingredientName;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    public IngredientSubstitutionRequest() {
    }

    public IngredientSubstitutionRequest(String ingredientName, String reason) {
        this.ingredientName = ingredientName;
        this.reason = reason;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
