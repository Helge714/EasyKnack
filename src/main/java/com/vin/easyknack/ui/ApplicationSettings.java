package com.vin.easyknack.ui;
import java.nio.file.Path;
import java.util.Locale;
/** Compatibility alias preserving existing preferences and public source references. */
@Deprecated
public final class ApplicationSettings {
    private ApplicationSettings(){}
    static void previewLocale(Locale locale){com.vin.easyknack.app.ApplicationSettings.previewLocale(locale);}
    public static Locale locale(){return com.vin.easyknack.app.ApplicationSettings.locale();}
    public static String theme(){return com.vin.easyknack.app.ApplicationSettings.theme();}
    public static void save(String language,String theme)throws Exception{com.vin.easyknack.app.ApplicationSettings.save(language,theme);}
    public static int autosaveSeconds(){return com.vin.easyknack.app.ApplicationSettings.autosaveSeconds();}
    public static int recoveryDays(){return com.vin.easyknack.app.ApplicationSettings.recoveryDays();}
    public static double connectionCurvature(){return com.vin.easyknack.app.ApplicationSettings.connectionCurvature();}
    public static void save(String language,String theme,int seconds,int days,double curvature)throws Exception{com.vin.easyknack.app.ApplicationSettings.save(language,theme,seconds,days,curvature);}
    /** Legacy application preference; document settings now own feature availability. */
    @Deprecated public static boolean experimentalFeatures(){return com.vin.easyknack.app.ApplicationSettings.experimentalFeatures();}
    @Deprecated public static void save(String language,String theme,int seconds,int days,double curvature,boolean experimental)throws Exception{com.vin.easyknack.app.ApplicationSettings.save(language,theme,seconds,days,curvature,experimental);}
    public static Path examples(){return com.vin.easyknack.app.ApplicationSettings.examples();}
}
