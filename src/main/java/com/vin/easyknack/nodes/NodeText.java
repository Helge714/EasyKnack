package com.vin.easyknack.nodes;

import java.util.Locale;
import java.util.ResourceBundle;

/** Stable node/property keys allow translations without changing saved documents. */
public final class NodeText {
    private static ResourceBundle TEXT = ResourceBundle.getBundle("nodes", com.vin.easyknack.ui.ApplicationSettings.locale(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    public static void reload(){TEXT=ResourceBundle.getBundle("nodes",com.vin.easyknack.ui.ApplicationSettings.locale(),ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));}
    private NodeText() { }
    private static final java.util.Map<String,String> KEYS=java.util.Collections.synchronizedMap(new java.util.IdentityHashMap<>());
    public static String text(String key, String fallback) {
        if(!TEXT.containsKey(key))return com.vin.easyknack.ui.AppText.literal(fallback);
        String value=TEXT.getString(key);KEYS.put(value,key);return value;
    }
    public static String translateExisting(String value){String key=KEYS.get(value);return key==null?value:text(key,value);}
}

