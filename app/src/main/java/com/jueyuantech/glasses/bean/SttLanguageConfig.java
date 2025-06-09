package com.jueyuantech.glasses.bean;

import java.util.List;

public class SttLanguageConfig {

    private String name;
    private List<LanguageTag> transcribe;
    private List<LanguageTag> translate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LanguageTag> getTranscribe() {
        return transcribe;
    }

    public void setTranscribe(List<LanguageTag> transcribe) {
        this.transcribe = transcribe;
    }

    public List<LanguageTag> getTranslate() {
        return translate;
    }

    public void setTranslate(List<LanguageTag> translate) {
        this.translate = translate;
    }
}