package com.jueyuantech.glasses;

public class TransShowModeConfigItem {
    private String text;
    private int value;

    public TransShowModeConfigItem(String text, int value) {
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