package com.whatsonmyplate.dto.ai;

public class AdjustedIngredient {

    private String name;
    private Double originalQuantity;
    private String originalUnit;
    private Double adjustedQuantity;
    private String adjustedUnit;
    private String changeReason;

    public AdjustedIngredient() {
    }

    public AdjustedIngredient(String name, Double originalQuantity, String originalUnit, Double adjustedQuantity, String adjustedUnit, String changeReason) {
        this.name = name;
        this.originalQuantity = originalQuantity;
        this.originalUnit = originalUnit;
        this.adjustedQuantity = adjustedQuantity;
        this.adjustedUnit = adjustedUnit;
        this.changeReason = changeReason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(Double originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public String getOriginalUnit() {
        return originalUnit;
    }

    public void setOriginalUnit(String originalUnit) {
        this.originalUnit = originalUnit;
    }

    public Double getAdjustedQuantity() {
        return adjustedQuantity;
    }

    public void setAdjustedQuantity(Double adjustedQuantity) {
        this.adjustedQuantity = adjustedQuantity;
    }

    public String getAdjustedUnit() {
        return adjustedUnit;
    }

    public void setAdjustedUnit(String adjustedUnit) {
        this.adjustedUnit = adjustedUnit;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}
