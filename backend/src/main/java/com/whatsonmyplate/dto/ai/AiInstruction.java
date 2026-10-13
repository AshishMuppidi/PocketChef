package com.whatsonmyplate.dto.ai;

public class AiInstruction {

    private Integer step;
    private String text;

    public AiInstruction() {
    }

    public AiInstruction(Integer step, String text) {
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
