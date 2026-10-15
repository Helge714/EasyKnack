package com.vin.easyknack.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsPreviewTest {
    @Test void languagePreviewDoesNotPersistPreferences(){
        var previous=ApplicationSettings.locale();var selected=previous.getLanguage().equals("de")?java.util.Locale.ENGLISH:java.util.Locale.GERMAN;
        try{
            ApplicationSettings.previewLocale(selected);AppText.reload();
            assertEquals(selected,ApplicationSettings.locale());
            assertEquals(selected.getLanguage().equals("de")?"Neu":"New",AppText.text("new"));
        }finally{ApplicationSettings.previewLocale(null);AppText.reload();}
        assertEquals(previous,ApplicationSettings.locale());
    }
}
