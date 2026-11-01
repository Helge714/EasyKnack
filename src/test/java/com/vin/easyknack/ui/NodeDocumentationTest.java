package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.NodeRegistry;

class NodeDocumentationTest {
    @TempDir Path temporary;

    @Test void incrementalBuildReadsVerifiedCheckoutWhenCopiedResourcesAreMissingOrStale()throws Exception{
        Path project=temporary.resolve("checkout"),classes=project.resolve("target/classes");
        Files.createDirectories(classes);Files.writeString(project.resolve("pom.xml"),"<project/>");
        Path owner=project.resolve("src/main/java/com/vin/easyknack/ui/NodeDocumentation.java");
        Files.createDirectories(owner.getParent());Files.writeString(owner,"class NodeDocumentation {}");
        Path page=project.resolve("docs/nodes/en/example.md");Files.createDirectories(page.getParent());Files.writeString(page,"# Current reference");
        Path source=NodeDocumentation.developmentDocs(classes);assertEquals(project.resolve("docs"),source);
        assertEquals("# Current reference",NodeDocumentation.read("nodes/en/example.md",source,resource->null));
        assertEquals("# Current reference",NodeDocumentation.read("nodes/en/example.md",source,resource->new java.io.ByteArrayInputStream("# Stale copy".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        assertNull(NodeDocumentation.developmentDocs(project));
        Path jar=project.resolve("easyknack.jar");Files.writeString(jar,"");assertNull(NodeDocumentation.developmentDocs(jar));
        assertThrows(java.io.IOException.class,()->NodeDocumentation.read("../../secret.md",source,resource->null));
        assertEquals("# Bundled",NodeDocumentation.read("nodes/en/example.md",null,resource->new java.io.ByteArrayInputStream("# Bundled".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    @Test void packagedTranslationsRemainCompleteWithoutCheckoutFallback()throws Exception{
        for(var entry:NodeDocumentation.catalog().entrySet())for(String language:List.of("en","de"))
            assertTrue(NodeDocumentation.read("nodes/"+language+"/"+entry.getValue()+".md",null,NodeDocumentation.class::getResourceAsStream).startsWith("#"),entry.getKey());
    }

    @Test void actualNodeMenusAreEnabledAndActiveReferenceTracksSelectionAndGroups()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var editor=controller.workspace().editor();
            for(var view:editor.getNodes()){
                var item=view.getContextMenu().getItems().stream().filter(i->i.getText().equals(AppText.text("help.nodeDocumentation"))).findFirst().orElseThrow();
                assertTrue(item.isEnabled(),view.getTitle());editor.setSelectedNode(view);
                var model=controller.workspace().currentGraph().snapshot().findNode(AudioNodeFactory.idOf(view).orElseThrow()).orElseThrow();
                assertEquals(NodeDocumentation.reference(model.typeId(),Locale.ENGLISH),NodeDocumentation.activeReference(editor.getActiveNode(),Locale.ENGLISH));
            }
            var fixture=GroupChoicesTest.fixture(false);controller.openDocument("Group",fixture.doc());
            var group=controller.workspace().editor().getNodes().stream().filter(v->v instanceof GroupViewFactory.GroupView).findFirst().orElseThrow();
            controller.workspace().editor().setSelectedNode(group);
            assertEquals("nodes/en/node-group.md",NodeDocumentation.activeReference(group,Locale.ENGLISH));
            assertTrue(group.getContextMenu().getItems().stream().filter(i->i.getText().equals(AppText.text("help.nodeDocumentation"))).findFirst().orElseThrow().isEnabled());
            assertEquals("nodes/de/index.md",NodeDocumentation.activeReference(null,Locale.GERMAN));
        }catch(Exception failure){throw new AssertionError(failure);}});
    }

    @Test void publicNodesAndGroupsResolveByIdentityInBothLanguages()throws Exception{
        for(var definition:NodeRegistry.builtinDefinitions())if(definition.isListedInCatalog()){
            for(var locale:List.of(Locale.ENGLISH,Locale.GERMAN)){
                var page=NodeDocumentation.reference(definition.registration().id(),locale);
                assertNotNull(page,definition.registration().id());
                assertTrue(NodeDocumentation.read(page).startsWith("#"));
            }
        }
        for(String id:List.of("group:ordinary","group:instance","group:formula","group:spectral","easyknack.spectral.bincontext","easyknack.spectral.binoutput"))
            assertNotNull(NodeDocumentation.reference(id,Locale.GERMAN),id);
        assertEquals("nodes/en/oscillator.md",NodeDocumentation.reference("easyknack.audio.oscillator",Locale.FRENCH));
        assertNull(NodeDocumentation.reference("unregistered.plugin",Locale.ENGLISH));
    }

    @Test void exportedBundleIsPortableAndAllLocalLinksAndAnchorsExist()throws Exception{
        var pages=NodeDocumentation.documents();var root=NodeDocumentation.export(temporary,pages);
        assertTrue(pages.size()>200);
        assertFalse(pages.keySet().stream().anyMatch(p->p.contains("development/")||p.contains("-v3.md")));
        var ids=new HashMap<Path,Set<String>>();var html=new HashMap<Path,String>();
        for(String page:pages.keySet()){
            Path file=root.resolve(page.replaceFirst("\\.md$",".html"));String text=Files.readString(file);html.put(file,text);
            assertFalse(text.contains("file:///"),page);assertFalse(text.contains("javascript:"),page);
            var found=new HashSet<String>();var matcher=Pattern.compile("\\bid=\"([^\"]+)\"").matcher(text);
            while(matcher.find())assertTrue(found.add(matcher.group(1)),page+" duplicate anchor "+matcher.group(1));
            ids.put(file,found);
        }
        for(var page:html.entrySet()){
            var links=Pattern.compile("href=\"([^\"]+)\"").matcher(page.getValue());
            while(links.find()){
                String[] parts=links.group(1).split("#",2);Path dest=page.getKey().getParent().resolve(parts[0]).normalize();
                if(parts[0].isEmpty())dest=page.getKey();
                assertTrue(dest.startsWith(root),links.group(1));assertTrue(Files.isRegularFile(dest),page.getKey()+" -> "+links.group(1));
                if(parts.length==2)assertTrue(ids.get(dest).contains(parts[1]),page.getKey()+" -> "+links.group(1));
            }
        }
        var oscillator=html.get(root.resolve("nodes/en/oscillator.html"));
        assertTrue(oscillator.contains("<th>"));assertTrue(oscillator.contains("<html lang=\"en\""));
        assertTrue(html.get(root.resolve("nodes/de/oscillator.html")).contains("<html lang=\"de\""));
        assertEquals(root,NodeDocumentation.export(temporary,pages));
        var changed=new TreeMap<>(pages);changed.put("nodes/en/oscillator.md",pages.get("nodes/en/oscillator.md")+"\nChanged.\n");
        assertNotEquals(root,NodeDocumentation.export(temporary,changed));
    }

    @Test void linksCannotEscapeTheBundleOrActivateMarkup(){
        for(String url:List.of("../../../../private.md","file:///secret.md","C:/private.md","javascript:alert(1)","//server/share.md","..\\private.md","%2e%2e/private.md"))
            assertNull(NodeDocumentation.target("nodes/en/oscillator.md",url),url);
        String html=HelpDocuments.html("# Test\n\n<a id=\"safe\"></a> <script>bad()</script> `sin(x)` [x](javascript:alert)\n","de",url->null);
        assertTrue(html.contains("<a id=\"safe\"></a>"));assertFalse(html.contains("<script>"));assertTrue(html.contains("<code>sin(x)</code>"));
    }
}
