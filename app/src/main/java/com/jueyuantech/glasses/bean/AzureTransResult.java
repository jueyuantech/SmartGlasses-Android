package com.jueyuantech.glasses.bean;

import java.util.List;

public class AzureTransResult {

    private List<TranslationsBean> translations;

    public List<TranslationsBean> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationsBean> translations) {
        this.translations = translations;
    }

    public static class TranslationsBean {
        /**
         * text : , the farmer of the wharf spent a penny to get Mr. Nan Guo's prediction.
         * to : en
         */

        private String text;
        private String to;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
