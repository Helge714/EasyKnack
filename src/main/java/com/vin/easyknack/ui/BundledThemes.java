package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;

/** Enumerates application resources both from Maven classes and the packaged JAR. */
final class BundledThemes {
    record Choice(String resource){
        public String toString(){return resource.substring(1).replaceFirst("\\.gltheme$","");}
        String id(){return "classpath:"+resource;}
    }
    static List<Choice> available(){
        var names=new TreeSet<String>();names.add("/easyknack.gltheme");
        try{
            var location=Path.of(BundledThemes.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(Files.isDirectory(location))try(var files=Files.walk(location)){
                files.filter(Files::isRegularFile).filter(p->p.toString().endsWith(".gltheme")).forEach(p->names.add("/"+location.relativize(p).toString().replace('\\','/')));
            }else try(var jar=new JarFile(location.toFile())){
                jar.stream().filter(e->!e.isDirectory()&&e.getName().endsWith(".gltheme")).forEach(e->names.add("/"+e.getName()));
            }
        }catch(Exception failure){throw new IllegalStateException("Cannot list bundled themes",failure);}
        return names.stream().map(Choice::new).toList();
    }
}
