package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.ui.GLFontSpec;
import com.vingame.glswing.rendering.GLFont;

class DocumentationPreparationTest {
    @Test void cacheReusesUnchangedTextAndInvalidatesEditedPagesWithoutSharingLayout()throws Exception{
        var cache=new DocumentationPreparation.Cache();
        var first=cache.document("nodes/en/test.md","# First\n\nText");
        assertSame(first,cache.document("nodes/en/test.md","# First\n\nText"));
        var edited=cache.document("nodes/en/test.md","# Edited\n\nNew text");
        assertNotSame(first,edited);assertEquals("Edited",edited.title());
        assertEquals("First",first.title());
        var one=new DocumentationPage(link->{});var two=new DocumentationPage(link->{});
        one.show(edited);two.show(edited);one.reflow(300);two.reflow(1000);
        assertSame(one.document(),two.document());assertNotEquals(one.getPreferredGLSize().width(),two.getPreferredGLSize().width());
    }
    @Test void fontWarmupUsesActualHeadingAndCodeSpecifications()throws Exception{
        var cache=new DocumentationPreparation.Cache();var base=new GLFontSpec(GLFont.SANS_SERIF,GLFont.PLAIN,16);
        cache.document("nodes/en/test.md","# Title\n\n## Section\n\n**Bold** and `code`\n\n| Header |\n| --- |\n| Value |\n");
        var fonts=cache.fonts(base);
        assertTrue(fonts.contains(base));assertTrue(fonts.contains(new GLFontSpec(base.family(),GLFont.BOLD,base.size()*1.6f)));
        assertTrue(fonts.contains(new GLFontSpec(base.family(),GLFont.BOLD,base.size()*1.3f)));
        assertTrue(fonts.contains(new GLFontSpec(GLFont.MONOSPACED,GLFont.PLAIN,base.size())));
    }
    @Test void startupPreparationFillsTheCacheForBothLanguagesAndReportsProgress()throws Exception{
        var completed=new AtomicInteger();var total=new AtomicInteger();var titles=new HashSet<String>();
        DocumentationPreparation.prepare(titles::add,(done,count)->{assertTrue(done>completed.get());completed.set(done);total.set(count);});
        assertTrue(total.get()>200);assertEquals(total.get(),completed.get());assertFalse(titles.isEmpty());
        for(String page:List.of("nodes/en/oscillator.md","nodes/de/oscillator.md"))assertSame(DocumentationPreparation.document(page),DocumentationPreparation.document(page));
    }
}
