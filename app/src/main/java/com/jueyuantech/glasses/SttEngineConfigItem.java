package com.jueyuantech.glasses;

public class SttEngineConfigItem {
    private String text;
    private String value;

    public SttEngineConfigItem(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}