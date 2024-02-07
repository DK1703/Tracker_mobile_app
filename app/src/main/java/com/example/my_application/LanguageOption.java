package com.example.my_application;

public class LanguageOption {
    private final String languageName;
    private final String languageCode;

    public LanguageOption(String languageName, String languageCode) {
        this.languageName = languageName;
        this.languageCode = languageCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}

