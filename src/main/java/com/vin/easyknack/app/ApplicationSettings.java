package com.vin.easyknack.app;

import java.nio.file.*;
import java.util.*;
import java.util.prefs.Preferences;

/** User preferences never modify documents or command-line overrides. */
public final class ApplicationSettings {
    private static final Preferences PREFS=Preferences.userRoot().node("/com/vin/easyknack/ui");
    private ApplicationSettings(){}
    private static volatile Locale previewLocale;
    public static void previewLocale(Locale locale){previewLocale=locale;}
    public static Locale locale(){if(previewLocale!=null)return previewLocale;return Locale.forLanguageTag(System.getProperty("easyknack.language",PREFS.get("language",Locale.getDefault().getLanguage().equals("de")?"de":"en")));}
    public static String theme(){return System.getProperty("easyknack.theme",PREFS.get("theme",""));}
    public static void save(String language,String theme)throws Exception{PREFS.put("language",language);PREFS.put("theme",theme);PREFS.flush();}
    public static int autosaveSeconds(){return Math.max(5,Math.min(3600,PREFS.getInt("autosaveSeconds",30)));}
    public static int recoveryDays(){return Math.max(1,Math.min(365,PREFS.getInt("recoveryDays",7)));}
    public static double connectionCurvature(){double v=PREFS.getDouble("connectionCurvature",1);return Double.isFinite(v)?Math.max(0,Math.min(1,v)):1;}
    public static void save(String language,String theme,int seconds,int days,double curvature)throws Exception{
        if(seconds<5||seconds>3600||days<1||days>365||!Double.isFinite(curvature)||curvature<0||curvature>1)throw new IllegalArgumentException("Invalid application settings");
        PREFS.putInt("autosaveSeconds",seconds);PREFS.putInt("recoveryDays",days);PREFS.putDouble("connectionCurvature",curvature);save(language,theme);
    }
    /** Legacy application preference; document settings now own feature availability. */
    @Deprecated public static boolean experimentalFeatures(){return Boolean.parseBoolean(System.getProperty("easyknack.experimental",Boolean.toString(PREFS.getBoolean("experimentalFeatures",false))));}
    @Deprecated public static void save(String language,String theme,int seconds,int days,double curvature,boolean experimental)throws Exception{
        save(language,theme,seconds,days,curvature);PREFS.putBoolean("experimentalFeatures",experimental);PREFS.flush();
    }
    public static Path examples(){return Path.of(System.getProperty("easyknack.examples",Path.of(System.getProperty("user.home"),"Documents","EasyKnack","Beispiele").toString()));}
}
