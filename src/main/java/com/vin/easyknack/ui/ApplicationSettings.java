package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import java.util.prefs.Preferences;

/** User preferences never modify documents or command-line overrides. */
public final class ApplicationSettings {
    private static final Preferences PREFS=Preferences.userNodeForPackage(ApplicationSettings.class);
    private ApplicationSettings(){}
    public static Locale locale(){return Locale.forLanguageTag(System.getProperty("easyknack.language",PREFS.get("language",Locale.getDefault().getLanguage().equals("de")?"de":"en")));}
    public static String theme(){return System.getProperty("easyknack.theme",PREFS.get("theme",""));}
    public static void save(String language,String theme)throws Exception{PREFS.put("language",language);PREFS.put("theme",theme);PREFS.flush();}
    public static Path examples(){return Path.of(System.getProperty("easyknack.examples",Path.of(System.getProperty("user.home"),"Documents","EasyKnack","Beispiele").toString()));}
}
