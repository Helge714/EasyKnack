package com.vin.easyknack.ui;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.jogamp.opengl.GL3;
import com.vingame.glswing.ui.GLFontSpec;

/** Optional warming plus shared parsed pages; viewer instances retain their own layout and history. */
public final class DocumentationPreparation {
    private static final Cache CACHE=new Cache();
    private DocumentationPreparation(){}
    static final class Cache {
        private record Entry(String markdown,DocumentationPage.Document document){}
        private final Map<String,Entry> entries=new ConcurrentHashMap<>();
        DocumentationPage.Document document(String page,String markdown)throws IOException{
            var previous=entries.get(page);
            if(previous!=null&&previous.markdown().equals(markdown))return previous.document();
            var parsed=DocumentationPage.parse(markdown,page.contains("/de/")?"de":"en");
            entries.put(page,new Entry(markdown,parsed));return parsed;
        }
        Set<GLFontSpec> fonts(GLFontSpec base){
            var fonts=new LinkedHashSet<GLFontSpec>();fonts.add(base);
            for(var entry:entries.values())fonts.addAll(DocumentationPage.fonts(entry.document(),base));
            return fonts;
        }
    }
    static DocumentationPage.Document document(String page)throws IOException{
        // Read current text so cached copies never hide edits in a development checkout.
        return CACHE.document(page,NodeDocumentation.read(page));
    }
    public static void prepare(Consumer<String> detail,BiConsumer<Integer,Integer> progress){
        try{
            var pages=NodeDocumentation.documents();int completed=0;
            for(var page:pages.entrySet()){
                var doc=CACHE.document(page.getKey(),page.getValue());detail.accept(doc.title());progress.accept(++completed,pages.size());
            }
        }catch(IOException failure){
            // Optional preparation must not prevent the audio application from starting.
            System.err.println("Documentation preload unavailable: "+failure.getMessage());
        }
    }
    /** Fill Core's shared CPU atlas cache using the splash's current GL context. */
    public static void prepareFonts(GL3 gl,BiConsumer<Integer,Integer> progress){
        var specifications=CACHE.fonts(ApplicationTheme.current().theme().labelStyle().font());int completed=0;
        for(var specification:specifications){
            var font=specification.createFont();
            try{font.initialize(gl);}
            finally{font.dispose(gl);}
            progress.accept(++completed,specifications.size());
        }
    }
}
