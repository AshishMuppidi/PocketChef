package com.whatsonmyplate.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SpiceAdjustmentRequest {

    @NotBlank(message = "Target spice level is required")
    @Pattern(regexp = "(?i)MILD|MEDIUM|SPICY", message = "Target spice level must be MILD, MEDIUM, or SPICY")
    private String targetSpiceLevel;

    public SpiceAdjustmentRequest() {
    }

    public SpiceAdjustmentRequest(String targetSpiceLevel) {
        this.targetSpiceLevel = targetSpiceLevel;
    }

    public String getTargetSpiceLevel() {
        return targetSpiceLevel;
    }

    public void setTargetSpiceLevel(String targetSpiceLevel) {
        this.targetSpiceLevel = targetSpiceLevel;
    }
}
