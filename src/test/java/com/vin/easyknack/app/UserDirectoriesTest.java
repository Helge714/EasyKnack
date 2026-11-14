package com.vin.easyknack.app;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
class UserDirectoriesTest {
 @TempDir Path temp;
 @Test void userFoldersAreCreatedWithoutSeedingExamples()throws Exception{
  String old=System.getProperty("easyknack.documents");System.setProperty("easyknack.documents",temp.toString());
  try{
   ApplicationSettings.prepareUserDirectories();
   assertTrue(Files.isDirectory(temp.resolve("Projects")));assertTrue(Files.isDirectory(temp.resolve("Recordings")));
   assertFalse(Files.exists(temp.resolve("Examples")));assertFalse(Files.exists(temp.resolve("Beispiele")));
  }finally{if(old==null)System.clearProperty("easyknack.documents");else System.setProperty("easyknack.documents",old);}
 }
 @Test void packagedExamplesResolveNextToTheApplicationJar()throws Exception{
  Path jar=temp.resolve("easy-knack.jar");String name="com/vin/easyknack/app/ApplicationSettings.class";
  try(var output=new java.util.jar.JarOutputStream(Files.newOutputStream(jar));var input=ApplicationSettings.class.getClassLoader().getResourceAsStream(name)){
   output.putNextEntry(new java.util.jar.JarEntry(name));input.transferTo(output);output.closeEntry();
  }
  String old=System.getProperty("easyknack.examples");System.clearProperty("easyknack.examples");
  try(var loader=new java.net.URLClassLoader(new java.net.URL[]{jar.toUri().toURL()},ClassLoader.getPlatformClassLoader())){
   var settings=loader.loadClass("com.vin.easyknack.app.ApplicationSettings");
   assertEquals(temp.resolve("Examples"),settings.getMethod("examples").invoke(null));
   assertEquals(true,settings.getMethod("bundledExamples").invoke(null));
   assertFalse(Files.exists(temp.resolve("Examples")));
  }finally{if(old!=null)System.setProperty("easyknack.examples",old);}
 }
}
