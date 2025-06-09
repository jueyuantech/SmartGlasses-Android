package com.jueyuantech.glasses.bean;

import java.util.List;

public class LanguageTag {

    /**
     * tag : en-US
     * language : English
     * region : United States
     * description : US English
     * listeningStr : Listening...
     * title : 简体中文
     */

    private String tag;
    private String language;
    private String region;
    private String description;
    private String listeningStr;
    private String title;
    private boolean selected;
    private List<LanguageTag> target;

    public LanguageTag() {

    }

    public LanguageTag(String tag, String language, String region, String description, String listeningStr, String title) {
        this.tag = tag;
        this.language = language;
        this.region = region;
        this.description = description;
        this.listeningStr = listeningStr;
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getListeningStr() {
        return listeningStr;
    }

    public void setListeningStr(String listeningStr) {
        this.listeningStr = listeningStr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<LanguageTag> getTarget() {
        return target;
    }

    public void setTarget(List<LanguageTag> target) {
        this.target = target;
    }
}