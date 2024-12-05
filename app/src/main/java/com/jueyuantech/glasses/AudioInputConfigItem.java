package com.jueyuantech.glasses;

public class AudioInputConfigItem {
    private String text;
    private int value;

    public AudioInputConfigItem(String text, int value) {
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