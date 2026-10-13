package com.whatsonmyplate.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class InstructionStepDto {

    @NotNull(message = "Instruction step number is required")
    @Positive(message = "Instruction step number must be greater than zero")
    private Integer step;

    @NotBlank(message = "Instruction text is required")
    @Size(max = 2000, message = "Instruction text cannot exceed 2000 characters")
    private String text;

    public InstructionStepDto() {
    }

    public InstructionStepDto(Integer step, String text) {
        this.step = step;
        this.text = text;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
