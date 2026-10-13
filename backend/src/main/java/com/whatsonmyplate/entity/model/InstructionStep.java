package com.whatsonmyplate.entity.model;

import java.io.Serializable;

public class InstructionStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer step;
    private String text;

    public InstructionStep() {
    }

    public InstructionStep(Integer step, String text) {
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
