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
    public static String documentationLanguage(){
        String language=System.getProperty("easyknack.documentation.language",PREFS.get("documentationLanguage","application"));
        return Set.of("application","de","en").contains(language)?language:"application";
    }
    public static Locale documentationLocale(){var language=documentationLanguage();return language.equals("application")?locale():Locale.forLanguageTag(language);}
    public static void saveDocumentationLanguage(String language)throws java.util.prefs.BackingStoreException{
        if(!Set.of("application","de","en").contains(language))throw new IllegalArgumentException("Unsupported documentation language");
        PREFS.put("documentationLanguage",language);PREFS.flush();
    }
    public static String theme(){return System.getProperty("easyknack.theme",PREFS.get("theme",""));}
    public static void save(String language,String theme)throws Exception{PREFS.put("language",language);PREFS.put("theme",theme);PREFS.flush();}
    public static int autosaveSeconds(){return Math.max(5,Math.min(3600,PREFS.getInt("autosaveSeconds",30)));}
    public static int recoveryDays(){return Math.max(1,Math.min(365,PREFS.getInt("recoveryDays",7)));}
    public static double connectionCurvature(){double v=PREFS.getDouble("connectionCurvature",0);return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
    public static void save(String language,String theme,int seconds,int days,double curvature)throws Exception{
        if(seconds<5||seconds>3600||days<1||days>365||!Double.isFinite(curvature)||curvature<0||curvature>1)throw new IllegalArgumentException("Invalid application settings");
        PREFS.putInt("autosaveSeconds",seconds);PREFS.putInt("recoveryDays",days);PREFS.putDouble("connectionCurvature",curvature);save(language,theme);
    }
    /** Legacy application preference; document settings now own feature availability. */
    @Deprecated public static boolean experimentalFeatures(){return Boolean.parseBoolean(System.getProperty("easyknack.experimental",Boolean.toString(PREFS.getBoolean("experimentalFeatures",false))));}
    @Deprecated public static void save(String language,String theme,int seconds,int days,double curvature,boolean experimental)throws Exception{
        save(language,theme,seconds,days,curvature);PREFS.putBoolean("experimentalFeatures",experimental);PREFS.flush();
    }
    public static Path documents(){
        String override=System.getProperty("easyknack.documents");
        return override!=null?Path.of(override):javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().toPath().resolve("EasyKnack");
    }
    public static Path projects(){return documents().resolve("Projects");}
    public static Path recordings(){return documents().resolve("Recordings");}
    public static void prepareUserDirectories()throws java.io.IOException{
        Files.createDirectories(projects());Files.createDirectories(recordings());
    }
    private static Path applicationLocation(){
        try{return Path.of(ApplicationSettings.class.getProtectionDomain().getCodeSource().getLocation().toURI());}
        catch(java.net.URISyntaxException failure){throw new IllegalStateException(failure);}
    }
    public static boolean bundledExamples(){return System.getProperty("easyknack.examples")==null&&!Files.isDirectory(applicationLocation());}
    public static Path examples(){
        String override=System.getProperty("easyknack.examples");
        if(override!=null)return Path.of(override);
        return applicationLocation().getParent().resolve("Examples");
    }
}
