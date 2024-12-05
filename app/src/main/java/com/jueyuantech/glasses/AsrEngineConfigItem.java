package com.jueyuantech.glasses;

public class AsrEngineConfigItem {
    private String text;
    private String value;

    public AsrEngineConfigItem(String text, String value) {
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