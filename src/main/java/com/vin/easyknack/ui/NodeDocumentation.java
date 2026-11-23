package com.vin.easyknack.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.GLNode;

/** Bundled offline references with native navigation and a portable HTML export for tooling. */
final class NodeDocumentation {
    static final String PREFIX="/META-INF/easyknack/docs/";
    private static final Pattern LINK=Pattern.compile("\\[[^]]*]\\(([^)]+)\\)");
    // The maintained catalog's identity/slug fields are deliberately adjacent.
    private static final Pattern ENTRY=Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"slug\"\\s*:\\s*\"([a-z0-9-]+)\"");
    private static Map<String,String> slugs;
    private static final Map<GLNode,String> identities=Collections.synchronizedMap(new WeakHashMap<>());
    private static Path exported;
    private NodeDocumentation(){}

    static String language(Locale locale){return locale.getLanguage().equals("de")?"de":"en";}
    static String localizedPage(String page,Locale locale){return localizedPage(page,locale,NodeDocumentation::exists);}
    static String localizedPage(String page,Locale locale,java.util.function.Predicate<String> exists){
        for(String prefix:List.of("nodes/","user/")){
            if(!(page.startsWith(prefix+"de/")||page.startsWith(prefix+"en/")))continue;
            String suffix=page.substring(prefix.length()+3),translated=prefix+language(locale)+"/"+suffix;
            if(exists.test(translated))return translated;
            String english=prefix+"en/"+suffix;return exists.test(english)?english:page;
        }
        return page;
    }
    static synchronized Map<String,String> catalog()throws IOException{
        if(slugs==null){
            var found=new LinkedHashMap<String,String>();var match=ENTRY.matcher(read("nodes/catalog.json"));
            while(match.find())if(found.put(match.group(1),match.group(2))!=null)throw new IOException("Duplicate documentation identity");
            if(found.isEmpty())throw new IOException("Node documentation catalog is empty");
            slugs=Map.copyOf(found);
        }
        return slugs;
    }
    static String reference(String id,Locale locale)throws IOException{
        var slug=catalog().get(id);if(slug==null)return null;
        String page="nodes/"+language(locale)+"/"+slug+".md";
        if(exists(page))return page;
        page="nodes/en/"+slug+".md";return exists(page)?page:null;
    }
    static void attach(GLNode node,String id){
        identities.put(node,id);
        // GLSwing creates its built-in menu items on first access, before the node is
        // parented. Use the app language explicitly so GLSwing's English default does
        // not leak into items it never refreshes, such as Reset name.
        node.setGLLocalizer(com.vingame.glswing.ui.GLLocalizer.forLocale(ApplicationSettings.locale()));
        try{
            var menu=node.getContextMenu();menu.addSeparator();
            var item=menu.addItem(AppText.text("help.nodeDocumentation"),event->{
                try{String page=reference(id,ApplicationSettings.documentationLocale());if(page!=null)open(node,page);}
                catch(IOException failure){HelpMenu.show(node,"help.nodeDocumentation",failure.getMessage());}
            });
            try{item.setEnabled(reference(id,ApplicationSettings.documentationLocale())!=null);}
            catch(IOException failure){item.setEnabled(false);}
        }finally{node.clearGLLocalizer();}
    }
    static void openIndex(GLContainer owner){open(owner,"nodes/"+language(ApplicationSettings.documentationLocale())+"/index.md");}
    static String activeReference(GLNode active,Locale locale)throws IOException{
        if(active==null)return "nodes/"+language(locale)+"/index.md";
        String id=identities.get(active);return id==null?null:reference(id,locale);
    }
    static void openActive(GLContainer owner,GLNode active){
        try{String page=activeReference(active,ApplicationSettings.documentationLocale());
            if(page==null)HelpMenu.show(owner,"help.nodeDocumentation",AppText.text("help.noNodeReference"));
            else open(owner,page);
        }catch(IOException failure){HelpMenu.show(owner,"help.nodeDocumentation",failure.getMessage());}
    }
    static void open(GLContainer owner,String page){
        DocumentationViewer.show(owner,page);
    }
    static String read(String page)throws IOException{
        return read(page,developmentDocs(),NodeDocumentation.class::getResourceAsStream);
    }
    static String read(String page,Path source,java.util.function.Function<String,java.io.InputStream> bundled)throws IOException{
        if(!validResource(page))throw new IOException("Invalid help resource: "+page);
        if(source!=null&&Files.isRegularFile(source.resolve(page)))return text(Files.readString(source.resolve(page),StandardCharsets.UTF_8));
        try(var in=bundled.apply(PREFIX+page)){
            if(in==null)throw new IOException("Bundled document missing: "+page);
            return text(new String(in.readAllBytes(),StandardCharsets.UTF_8));
        }
    }
    private static String text(String text){return text.startsWith("\uFEFF")?text.substring(1):text;}
    private static boolean validResource(String page){return page!=null&&(page.equals("nodes/catalog.json")||page.equals(target(page,"")));}
    static boolean exists(String page){
        if(!validResource(page))return false;
        Path source=developmentDocs();
        return source!=null&&Files.isRegularFile(source.resolve(page))||NodeDocumentation.class.getResource(PREFIX+page)!=null;
    }
    private static Path developmentDocs(){
        try{
            var source=NodeDocumentation.class.getProtectionDomain().getCodeSource();
            return source==null?null:developmentDocs(Path.of(source.getLocation().toURI()));
        }catch(java.net.URISyntaxException|IllegalArgumentException failure){return null;}
    }
    static Path developmentDocs(Path classes){
        classes=classes.toAbsolutePath().normalize();
        if(!Files.isDirectory(classes)||!classes.endsWith(Path.of("target","classes")))return null;
        Path root=classes.getParent().getParent();
        return Files.isRegularFile(root.resolve("pom.xml"))
            &&Files.isRegularFile(root.resolve("src/main/java/com/vin/easyknack/ui/NodeDocumentation.java"))
            &&Files.isDirectory(root.resolve("docs"))?root.resolve("docs"):null;
    }

    /** Return a normalized path only within the documented resource tree, never a URI or a host path. */
    static String target(String source,String link){
        if(link.contains(":")||link.contains("\\")||link.startsWith("/")||link.contains("?")||link.contains("%"))return null;
        String path=link.split("#",2)[0];
        try{
            var parent=Path.of(source).getParent();
            String result=(path.isEmpty()?Path.of(source):(parent==null?Path.of(path):parent.resolve(path))).normalize().toString().replace('\\','/');
            if(result.startsWith("../")||!result.endsWith(".md"))return null;
            if(result.contains("/")&&!result.startsWith("nodes/")&&!result.startsWith("user/"))return null;
            return result;
        }catch(InvalidPathException failure){return null;}
    }
    static Map<String,String> documents()throws IOException{
        var pages=new TreeMap<String,String>();var queue=new ArrayDeque<String>();
        queue.addAll(List.of("user/README.md","nodes/en/index.md","nodes/de/index.md"));
        while(!queue.isEmpty()){
            String page=queue.removeFirst();if(pages.containsKey(page))continue;
            String markdown=read(page);pages.put(page,markdown);
            var links=LINK.matcher(markdown);
            while(links.find()){
                String dest=target(page,links.group(1));
                if(dest!=null&&exists(dest)&&!pages.containsKey(dest))queue.addLast(dest);
            }
        }
        return Collections.unmodifiableMap(pages);
    }
    static String resolveLink(String source,String link,Set<String> pages){
        String dest=target(source,link);if(dest==null||!pages.contains(dest))return null;
        String anchor=link.contains("#")?link.substring(link.indexOf('#')):"";
        if(!anchor.isEmpty()&&!anchor.matches("#[a-z0-9-]+"))return null;
        Path parent=Path.of(source).getParent();if(parent==null)parent=Path.of("");
        return parent.relativize(Path.of(dest)).toString().replace('\\','/').replaceFirst("\\.md$",".html")+anchor;
    }
    static Path export(Path root,Map<String,String> pages)throws IOException{
        String identity;
        try{
            var hash=MessageDigest.getInstance("SHA-256");hash.update("help-renderer-1\n".getBytes(StandardCharsets.UTF_8));
            for(var entry:new TreeMap<>(pages).entrySet()){hash.update(entry.getKey().getBytes(StandardCharsets.UTF_8));hash.update((byte)0);hash.update(entry.getValue().getBytes(StandardCharsets.UTF_8));hash.update((byte)0);}
            identity=HexFormat.of().formatHex(hash.digest()).substring(0,24);
        }catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
        Path folder=root.resolve(identity);Files.createDirectories(folder);
        for(var entry:pages.entrySet()){
            String source=entry.getKey();Path output=folder.resolve(source.replaceFirst("\\.md$",".html"));
            if(Files.isRegularFile(output))continue;
            String lang=source.contains("/de/")?"de":"en";
            String html=HelpDocuments.html(entry.getValue(),lang,link->resolveLink(source,link,pages.keySet()));
            Path parent=Path.of(source).getParent();if(parent==null)parent=Path.of("");
            String index=parent.relativize(Path.of("nodes/"+lang+"/index.html")).toString().replace('\\','/');
            String manual=parent.relativize(Path.of("user/"+lang+"/index.html")).toString().replace('\\','/');
            html=html.replace("<body>","<body><nav><a href=\""+index+"\">"+(lang.equals("de")?"Node-Verzeichnis":"Node index")+"</a><a href=\""+manual+"\">"+(lang.equals("de")?"Handbuch":"User guide")+"</a></nav>");
            Files.createDirectories(output.getParent());
            Path temp=Files.createTempFile(output.getParent(),"help-",".tmp");
            try{Files.writeString(temp,html,StandardCharsets.UTF_8);Files.move(temp,output,StandardCopyOption.REPLACE_EXISTING);}
            finally{Files.deleteIfExists(temp);}
        }
        return folder;
    }
    static synchronized Path page(String source)throws IOException{
        if(target(source,"")==null||!exists(source))throw new IOException("Unknown help page: "+source);
        if(exported==null)exported=export(Path.of(System.getProperty("user.home"),".easyknack","help","references"),documents());
        Path path=exported.resolve(source.replaceFirst("\\.md$",".html"));
        if(!Files.isRegularFile(path))throw new IOException("Unindexed help page: "+source);
        return path;
    }
}
