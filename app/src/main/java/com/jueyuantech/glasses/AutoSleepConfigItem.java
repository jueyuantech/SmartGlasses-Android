package com.jueyuantech.glasses;

public class AutoSleepConfigItem {
    private String text;
    private int value;

    public AutoSleepConfigItem(String text, int value) {
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