package com.jueyuantech.glasses.ui.main;

public class ConfigItem {
    private String text;
    private int value;

    public ConfigItem(String text, int value) {
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