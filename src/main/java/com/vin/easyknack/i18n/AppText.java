package com.vin.easyknack.i18n;
import com.vin.easyknack.app.ApplicationSettings;
import java.util.*;
/** Application messages, separate from immutable protocol IDs and user-authored names. */
public final class AppText {
    private static volatile ResourceBundle TEXT=ResourceBundle.getBundle("application",ApplicationSettings.locale(),ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    private static final Map<String,String> KEYS=Collections.synchronizedMap(new IdentityHashMap<>());
    public static void reload(){TEXT=ResourceBundle.getBundle("application",ApplicationSettings.locale(),ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));LITERALS.clear();}
    public static String translateExisting(String value){String key=KEYS.get(value);return key==null?com.vin.easyknack.nodes.NodeText.translateExisting(value):text(key);}
    private AppText(){}
    private static final java.util.concurrent.ConcurrentMap<String,String> LITERALS=new java.util.concurrent.ConcurrentHashMap<>();
    public static String literal(String source){
        if(source==null||source.isEmpty())return source;
        var cached=LITERALS.get(source);if(cached!=null)return cached;
        var resolved=resolve(source);LITERALS.putIfAbsent(source,resolved);return resolved;
    }
    private static String resolve(String value){try{
            String key="text."+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))).substring(0,16);
            if(TEXT.containsKey(key))return text(key);
            if(value.endsWith(" unit"))return format("unit.label",literal(value.substring(0,value.length()-5)));
            var suffix=java.util.regex.Pattern.compile("^(.*?)( \\[.*| \\d+)$").matcher(value);
            if(suffix.matches())return literal(suffix.group(1))+suffix.group(2);
            return value;
        }catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
    public static String text(String key){var value=TEXT.getString(key);KEYS.put(value,key);return value;}
    public static String format(String key,Object... args){return String.format(ApplicationSettings.locale(),text(key),args);}
    public static String identity(String text){return TEXT.keySet().stream().filter(k->TEXT.getString(k).equals(text)).sorted().findFirst().orElse(text);}
}
