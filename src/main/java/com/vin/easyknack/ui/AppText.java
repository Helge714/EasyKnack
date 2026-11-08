package com.vin.easyknack.ui;

/** Compatibility alias; language policy lives outside the UI package. */
@Deprecated
public final class AppText {
    private AppText() { }
    public static void reload(){com.vin.easyknack.i18n.AppText.reload();}
    static String translateExisting(String value){return com.vin.easyknack.i18n.AppText.translateExisting(value);}
    public static String literal(String value){return com.vin.easyknack.i18n.AppText.literal(value);}
    public static String identity(String value){return com.vin.easyknack.i18n.AppText.identity(value);}
    public static String text(String key){return com.vin.easyknack.i18n.AppText.text(key);}
    public static String format(String key,Object... values){return com.vin.easyknack.i18n.AppText.format(key,values);}
}
