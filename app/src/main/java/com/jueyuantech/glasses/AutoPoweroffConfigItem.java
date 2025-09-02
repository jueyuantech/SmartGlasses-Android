package com.jueyuantech.glasses;

public class AutoPoweroffConfigItem {
    private String text;
    private int value;

    public AutoPoweroffConfigItem(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }
}