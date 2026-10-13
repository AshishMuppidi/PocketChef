package com.whatsonmyplate.dto.ai;

import java.util.ArrayList;
import java.util.List;

public class SpiceAdjustmentResult {

    private Long recipeId;
    private String originalSpiceLevel;
    private String targetSpiceLevel;
    private List<AdjustedIngredient> adjustedIngredients = new ArrayList<>();
    private String notes;
    private Double confidence;

    public SpiceAdjustmentResult() {
    }

    public SpiceAdjustmentResult(Long recipeId, String originalSpiceLevel, String targetSpiceLevel, List<AdjustedIngredient> adjustedIngredients, String notes, Double confidence) {
        this.recipeId = recipeId;
        this.originalSpiceLevel = originalSpiceLevel;
        this.targetSpiceLevel = targetSpiceLevel;
        this.adjustedIngredients = adjustedIngredients != null ? adjustedIngredients : new ArrayList<>();
        this.notes = notes;
        this.confidence = confidence;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getOriginalSpiceLevel() {
        return originalSpiceLevel;
    }

    public void setOriginalSpiceLevel(String originalSpiceLevel) {
        this.originalSpiceLevel = originalSpiceLevel;
    }

    public String getTargetSpiceLevel() {
        return targetSpiceLevel;
    }

    public void setTargetSpiceLevel(String targetSpiceLevel) {
        this.targetSpiceLevel = targetSpiceLevel;
    }

    public List<AdjustedIngredient> getAdjustedIngredients() {
        return adjustedIngredients;
    }

    public void setAdjustedIngredients(List<AdjustedIngredient> adjustedIngredients) {
        this.adjustedIngredients = adjustedIngredients != null ? adjustedIngredients : new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
