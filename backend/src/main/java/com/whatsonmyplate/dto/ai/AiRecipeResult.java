package com.whatsonmyplate.dto.ai;

import com.whatsonmyplate.entity.enums.SpiceLevel;

import java.util.ArrayList;
import java.util.List;

public class AiRecipeResult {

    private String dishName;
    private String description;
    private List<AiIngredient> ingredients = new ArrayList<>();
    private List<AiInstruction> instructions = new ArrayList<>();
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer servings;
    private SpiceLevel spiceLevel;
    private Double confidence;

    public AiRecipeResult() {
    }

    public AiRecipeResult(String dishName, String description, List<AiIngredient> ingredients, List<AiInstruction> instructions, Integer prepTimeMinutes, Integer cookTimeMinutes, Integer servings, SpiceLevel spiceLevel, Double confidence) {
        this.dishName = dishName;
        this.description = description;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings;
        this.spiceLevel = spiceLevel;
        this.confidence = confidence;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AiIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<AiIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<AiInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<AiInstruction> instructions) {
        this.instructions = instructions;
    }

    public Integer getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(Integer prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public SpiceLevel getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(SpiceLevel spiceLevel) {
        this.spiceLevel = spiceLevel;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
