package com.vin.easyknack;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Map;
import java.util.jar.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegalDocumentsTest {
    @TempDir Path directory;
    private Path fixture()throws IOException{
        var classes=Files.createDirectories(directory.resolve("project/target/classes"));
        var root=classes.getParent().getParent();Files.writeString(root.resolve("pom.xml"),"<project/>");
        var source=root.resolve("src/main/java/com/vin/easyknack/LegalDocuments.java");
        Files.createDirectories(source.getParent());Files.writeString(source,"// source checkout marker");
        Files.writeString(root.resolve("LICENSE"),"\uFEFFSource license");
        Files.writeString(root.resolve("THIRD-PARTY.md"),"Source notices");
        copyClass(classes,LegalDocuments.class);return classes;
    }
    private static void copyClass(Path classes,Class<?> type)throws IOException{
        String name=type.getName().replace('.','/')+".class";
        var target=classes.resolve(name);Files.createDirectories(target.getParent());
        try(var input=type.getResourceAsStream("/"+name)){Files.copy(input,target);}
    }
    private static URLClassLoader loader(Path source)throws IOException{
        return new URLClassLoader(new java.net.URL[]{source.toUri().toURL()},ClassLoader.getPlatformClassLoader());
    }
    private static String read(URLClassLoader loader,String name)throws Exception{
        return (String)loader.loadClass(LegalDocuments.class.getName()).getMethod("read",String.class).invoke(null,name);
    }
    @Test void sourceCheckoutWorksAfterGeneratedResourcesAreCleaned()throws Exception{
        var classes=fixture();copyClass(classes,com.vin.easyknack.plugins.PluginLicensing.class);
        try(var loader=loader(classes)){
            assertNull(loader.getResource("META-INF/easyknack/legal/LICENSE"));
            assertEquals("Source license",read(loader,"LICENSE"));assertEquals("Source notices",read(loader,"THIRD-PARTY.md"));
            var files=(Map<?,?>)loader.loadClass("com.vin.easyknack.plugins.PluginLicensing").getMethod("files").invoke(null);
            assertEquals("Source license",files.get("LICENSE"));
            var error=assertThrows(InvocationTargetException.class,()->read(loader,"../LICENSE"));
            assertInstanceOf(IllegalArgumentException.class,error.getCause());
        }
    }
    @Test void bundledDocumentTakesPrecedenceOverSourceFiles()throws Exception{
        var classes=fixture();var resource=classes.resolve("META-INF/easyknack/legal/LICENSE");
        Files.createDirectories(resource.getParent());Files.writeString(resource,"Bundled license");
        try(var loader=loader(classes)){assertEquals("Bundled license",read(loader,"LICENSE"));}
    }
    @Test void packagedApplicationNeverReadsANearbyCheckout()throws Exception{
        var classes=fixture();
        for(boolean bundled:new boolean[]{false,true}){
            var jar=directory.resolve("project/app-"+bundled+".jar");
            try(var output=new JarOutputStream(Files.newOutputStream(jar))){
                String name=LegalDocuments.class.getName().replace('.','/')+".class";
                output.putNextEntry(new JarEntry(name));Files.copy(classes.resolve(name),output);output.closeEntry();
                if(bundled){output.putNextEntry(new JarEntry("META-INF/easyknack/legal/LICENSE"));output.write("Jar license".getBytes(java.nio.charset.StandardCharsets.UTF_8));output.closeEntry();}
            }
            try(var loader=loader(jar)){
                if(bundled)assertEquals("Jar license",read(loader,"LICENSE"));
                else assertInstanceOf(IOException.class,assertThrows(InvocationTargetException.class,()->read(loader,"LICENSE")).getCause());
            }
        }
    }
}
