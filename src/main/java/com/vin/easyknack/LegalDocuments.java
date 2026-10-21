package com.vin.easyknack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Packaged legal documents, with a source-checkout fallback for incremental builds. */
public final class LegalDocuments {
    public static final String RESOURCE_PREFIX="/META-INF/easyknack/legal/";
    private LegalDocuments() { }

    public static String read(String name)throws IOException{
        if(!name.equals("LICENSE")&&!name.equals("THIRD-PARTY.md"))
            throw new IllegalArgumentException("Unknown legal document: "+name);
        try(var input=LegalDocuments.class.getResourceAsStream(RESOURCE_PREFIX+name)){
            if(input!=null)return text(new String(input.readAllBytes(),StandardCharsets.UTF_8));
        }
        var root=developmentRoot();
        if(root!=null&&Files.isRegularFile(root.resolve(name)))
            return text(Files.readString(root.resolve(name),StandardCharsets.UTF_8));
        throw new IOException("Bundled document missing: "+RESOURCE_PREFIX+name);
    }

    private static Path developmentRoot()throws IOException{
        var source=LegalDocuments.class.getProtectionDomain().getCodeSource();
        if(source==null)return null;
        try{
            var classes=Path.of(source.getLocation().toURI()).toAbsolutePath().normalize();
            if(!Files.isDirectory(classes)||!classes.endsWith(Path.of("target","classes")))return null;
            var root=classes.getParent().getParent();
            return Files.isRegularFile(root.resolve("pom.xml"))
                    &&Files.isRegularFile(root.resolve("src/main/java/com/vin/easyknack/LegalDocuments.java"))?root:null;
        }catch(java.net.URISyntaxException invalid){throw new IOException("Invalid application class location",invalid);}
    }
    private static String text(String value){return value.startsWith("\uFEFF")?value.substring(1):value;}
}
