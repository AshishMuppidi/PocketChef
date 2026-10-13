package com.whatsonmyplate.dto.ai;

public class IngredientSubstitutionResult {

    private String originalIngredient;
    private String substitute;
    private String quantity;
    private String reason;
    private String instructions;
    private Double confidence;

    public IngredientSubstitutionResult() {
    }

    public IngredientSubstitutionResult(String originalIngredient, String substitute, String quantity, String reason, String instructions, Double confidence) {
        this.originalIngredient = originalIngredient;
        this.substitute = substitute;
        this.quantity = quantity;
        this.reason = reason;
        this.instructions = instructions;
        this.confidence = confidence;
    }

    public String getOriginalIngredient() {
        return originalIngredient;
    }

    public void setOriginalIngredient(String originalIngredient) {
        this.originalIngredient = originalIngredient;
    }

    public String getSubstitute() {
        return substitute;
    }

    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
