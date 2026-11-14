package com.vin.easyknack.ui;

import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;

/** Enumerates application resources both from Maven classes and the packaged JAR. */
final class BundledThemes {
    record Choice(String resource,String name){
        public String toString(){return name;}
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
        return names.stream().map(BundledThemes::choice).flatMap(Optional::stream)
                .sorted(Comparator.comparing((Choice c)->!c.resource().equals(ApplicationTheme.DEFAULT_RESOURCE))
                        .thenComparing(Choice::name,String.CASE_INSENSITIVE_ORDER).thenComparing(Choice::resource)).toList();
    }
    private static Optional<Choice> choice(String resource){
        try(var stream=BundledThemes.class.getResourceAsStream(resource)){
            if(stream==null)throw new IOException("Missing theme resource: "+resource);
            var properties=new Properties();properties.load(new InputStreamReader(stream,StandardCharsets.UTF_8));
            String type=properties.getProperty("theme.type","").trim();
            if(type.equalsIgnoreCase("colors")||type.equalsIgnoreCase("metrics"))return Optional.empty();
            String fallback=resource.substring(1).replaceFirst("\\.gltheme$","");
            String name=properties.getProperty("theme.name",fallback).trim();
            return Optional.of(new Choice(resource,name.isEmpty()?fallback:name));
        }catch(IOException failure){throw new IllegalStateException("Cannot read bundled theme: "+resource,failure);}
    }
}
