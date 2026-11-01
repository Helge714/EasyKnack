package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class DocumentationViewerTest {
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
    }
    private static void await(java.util.function.Supplier<CompletableFuture<Void>> action)throws Exception{
        var pending=new AtomicReference<CompletableFuture<Void>>();SwingUtilities.invokeAndWait(()->pending.set(action.get()));pending.get().get(30,TimeUnit.SECONDS);
    }
}
