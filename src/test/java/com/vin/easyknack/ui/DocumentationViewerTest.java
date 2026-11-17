package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class DocumentationViewerTest {
    @Test void sharedCoreSearchHighlightsScrollsAndNavigatesAndClearsOnNewPage()throws Exception {
        var holder=new AtomicReference<DocumentationViewer>();
        SwingUtilities.invokeAndWait(()->{var panel=new DocumentationViewer(()->{});panel.setBounds(0,0,800,320);holder.set(panel);});
        var panel=holder.get();await(()->panel.openPage("user/en/formula-language.md"));
        SwingUtilities.invokeAndWait(()->{
            assertTrue(panel.find.isEnabled());
            assertTrue(panel.content.startSearch("buffer.read",com.vingame.glswing.ui.GLTextSearchOptions.DEFAULT)>0);
            assertFalse(panel.content.searchHighlights().isEmpty());assertTrue(panel.scroll.getVerticalScrollOffset()>0);
            panel.content.startSearch("float",com.vingame.glswing.ui.GLTextSearchOptions.DEFAULT);
            assertEquals(0,panel.content.getActiveSearchMatchIndex());
            var next=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),java.awt.event.KeyEvent.KEY_PRESSED,0,0,java.awt.event.KeyEvent.VK_F3,java.awt.event.KeyEvent.CHAR_UNDEFINED));
            assertTrue(panel.capturesKeyPress(next));assertEquals(1,panel.content.getActiveSearchMatchIndex());
            var previous=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),java.awt.event.KeyEvent.KEY_PRESSED,0,java.awt.event.InputEvent.SHIFT_DOWN_MASK,java.awt.event.KeyEvent.VK_F3,java.awt.event.KeyEvent.CHAR_UNDEFINED));
            assertTrue(panel.capturesKeyPress(previous));assertEquals(0,panel.content.getActiveSearchMatchIndex());
        });
        await(()->panel.openPage("user/en/rates.md"));
        SwingUtilities.invokeAndWait(()->{assertEquals(0,panel.content.getSearchMatchCount());assertTrue(panel.content.searchHighlights().isEmpty());});
    }
    @Test void formulaBuiltinNavigationAnchorsExistInBothLanguages()throws Exception {
        SwingUtilities.invokeAndWait(()->{try{
            for(String language:List.of("de","en")){
                var page=new DocumentationPage(link->{});page.show(DocumentationPage.parse(NodeDocumentation.read("user/"+language+"/formula-language.md"),language));
                var anchors=new ArrayList<>(com.vin.easyknack.formula.FormulaCompiler.FUNCTIONS.keySet());anchors.add("buffer-read");
                for(String name:anchors)assertTrue(page.anchor(name.toLowerCase(Locale.ROOT))>0,language+"/"+name);
            }
        }catch(Exception e){throw new AssertionError(e);}});
    }
    @Test void formulaHelpOpensAtAnAnchorInTheIndependentDocumentationLanguage()throws Exception {
        String override=System.getProperty("easyknack.documentation.language");System.setProperty("easyknack.documentation.language","de");
        try{
            var holder=new AtomicReference<DocumentationViewer>();
            SwingUtilities.invokeAndWait(()->{var panel=new DocumentationViewer(()->{});panel.setBounds(0,0,800,320);holder.set(panel);});
            var panel=holder.get();await(()->panel.openPage("user/en/formula-language.md","sinewave"));
            SwingUtilities.invokeAndWait(()->{assertEquals("user/de/formula-language.md",panel.position().page());assertEquals(panel.content.anchor("sinewave"),panel.position().y(),1);});
        }finally{if(override==null)System.clearProperty("easyknack.documentation.language");else System.setProperty("easyknack.documentation.language",override);}
    }
    @Test void linksUseCoreHandCursorAndReleaseItOnExitOrPageReplacement()throws Exception{
        SwingUtilities.invokeAndWait(()->{try{
            var page=new DocumentationPage(link->{});page.show(DocumentationPage.parse("# Test\n\n[Link](index.md) Plain text\n","en"));
            class Pointer implements com.vingame.glswing.input.GLPointerController {
                Object owner;com.vingame.glswing.input.GLPointerCursor cursor;int changes;
                public boolean isAvailable(){return true;}
                public void setCursor(Object owner,com.vingame.glswing.input.GLPointerCursor cursor){this.owner=owner;this.cursor=cursor;changes++;}
                public void clearCursor(Object owner){if(this.owner==owner){this.owner=null;cursor=null;changes++;}}
            }
            var pointer=new Pointer();var host=new java.awt.Canvas();
            var run=page.runs().stream().filter(r->r.link()!=null).findFirst().orElseThrow();
            var pick=new com.vingame.glswing.input.GLPickResult(1,page,0,0,run.x()+run.width()/2,run.y()+run.height()/2,1,0,0,1,0,0);
            var event=new com.vingame.glswing.input.GLMouseEvent(new java.awt.event.MouseEvent(host,java.awt.event.MouseEvent.MOUSE_MOVED,0,0,0,0,0,false),pick,0,0,null,pointer);
            page.mouseEntered(event);assertEquals(com.vingame.glswing.input.GLPointerCursor.HAND,pointer.cursor);
            int changes=pointer.changes;page.mouseMoved(event);assertEquals(changes,pointer.changes);
            page.mouseExited(event);assertNull(pointer.cursor);
            page.mouseMoved(event);page.show(new DocumentationPage.Document("",List.of()));assertNull(pointer.cursor);
        }catch(Exception failure){throw new AssertionError(failure);}});
    }
    @Test void nativeModelPreservesTablesEquationsUnicodeAndInlineLinks()throws Exception{
        var document=DocumentationPage.parse(NodeDocumentation.read("nodes/de/additive-oscillator.md"),"de");
        assertTrue(document.title().contains("Additive Oscillator"));
        assertTrue(document.plain().contains("±∞"));assertTrue(document.plain().contains("Σ"));
        var tables=document.blocks().stream().filter(b->b instanceof DocumentationPage.Table).map(b->(DocumentationPage.Table)b).toList();
        assertEquals(8,tables.get(0).rows.get(0).size());assertEquals(5,tables.get(1).rows.get(0).size());
        assertTrue(tables.get(0).rows.stream().flatMap(List::stream).flatMap(c->c.spans.stream()).anyMatch(s->s.link()!=null&&s.link().contains("#control")));
        assertTrue(document.blocks().stream().anyMatch(b->b instanceof DocumentationPage.Text t&&t.pre&&t.plain().contains("\n")));
    }

    @Test void everyBundledPageHasANativePresentationAndStableAnchors()throws Exception{
        for(var entry:NodeDocumentation.documents().entrySet()){
            var doc=DocumentationPage.parse(entry.getValue(),entry.getKey().contains("/de/")?"de":"en");
            assertFalse(doc.blocks().isEmpty(),entry.getKey());assertFalse(doc.title().isBlank(),entry.getKey());
            var font=com.vingame.glswing.ui.GLFontSpec.DEFAULT.measurementFont();
            doc.plain().codePoints().filter(cp->cp>255).forEach(cp->assertTrue(font.canRender(cp),entry.getKey()+" missing U+"+Integer.toHexString(cp)));
        }
        SwingUtilities.invokeAndWait(()->{try{
            var page=new DocumentationPage(link->{});
            page.show(DocumentationPage.parse(NodeDocumentation.read("user/en/rates.md"),"en"));
            assertTrue(page.anchor("auto")>0);assertFalse(page.runs().isEmpty());
            var first=page.runs().stream().filter(r->r.link()!=null).findFirst().orElseThrow();
            assertEquals(first.link(),page.linkAt(first.x()+first.width()/2,first.y()+first.height()/2));
        }catch(Exception failure){throw new AssertionError(failure);}});
    }

    @Test void wrappedTableTextStaysWithinCellsEvenWithLongUnbrokenNames()throws Exception{
        SwingUtilities.invokeAndWait(()->{try{
            String word="abcdefghijklmnopqrstuvwxyz".repeat(5);
            var page=new DocumentationPage(link->{});page.show(DocumentationPage.parse("# Test\n\n| Name | Value |\n| --- | --- |\n| "+word+" | ±∞ and multiple wrapped words |\n","en"));page.reflow(300);
            assertEquals(4,page.boxes().size());
            for(var run:page.runs()){
                var cell=page.boxes().stream().filter(b->run.y()>=b.y()&&run.y()<b.y()+b.height()&&run.x()>=b.x()&&run.x()<b.x()+b.width()).findFirst();
                if(cell.isPresent())assertTrue(run.x()+run.width()<=cell.get().x()+cell.get().width()+.5,run.text());
            }
        }catch(Exception failure){throw new AssertionError(failure);}});
    }

    @Test void navigationFollowsLocalAnchorsAndRestoresScrollWithoutLeavingTheApp()throws Exception{
        String override=System.getProperty("easyknack.documentation.language");System.setProperty("easyknack.documentation.language","en");
        try{
        var holder=new AtomicReference<DocumentationViewer>();var closed=new AtomicBoolean();
        SwingUtilities.invokeAndWait(()->{var panel=new DocumentationViewer(()->closed.set(true));panel.setBounds(0,0,800,320);holder.set(panel);});
        var panel=holder.get();await(()->panel.openPage("nodes/en/oscillator.md"));
        var old=new AtomicReference<DocumentationViewer.Position>();
        SwingUtilities.invokeAndWait(()->{panel.scroll.setScrollOffset(0,160);old.set(panel.position());assertTrue(old.get().y()>0);});
        await(()->panel.follow("../../user/en/rates.md#auto"));
        SwingUtilities.invokeAndWait(()->{assertEquals("user/en/rates.md",panel.position().page());assertTrue(panel.position().y()>0);assertTrue(panel.back.isEnabled());});
        await(panel::back);
        SwingUtilities.invokeAndWait(()->{assertEquals(old.get().page(),panel.position().page());assertEquals(old.get().y(),panel.position().y(),1);});
        await(panel::forward);
        await(()->panel.follow("file:///private.md"));
        SwingUtilities.invokeAndWait(()->{assertEquals("user/en/rates.md",panel.position().page());var key=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),java.awt.event.KeyEvent.KEY_PRESSED,0,0,java.awt.event.KeyEvent.VK_ESCAPE,'\u001b'));assertTrue(panel.capturesKeyPress(key));assertTrue(closed.get());});
        }finally{if(override==null)System.clearProperty("easyknack.documentation.language");else System.setProperty("easyknack.documentation.language",override);}
    }
    private static void await(java.util.function.Supplier<CompletableFuture<Void>> action)throws Exception{
        var pending=new AtomicReference<CompletableFuture<Void>>();SwingUtilities.invokeAndWait(()->pending.set(action.get()));pending.get().get(30,TimeUnit.SECONDS);
    }
}
