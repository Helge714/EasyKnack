package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.ui.GLThemeLoader;

class ApplicationThemeTest {
    @TempDir Path directory;
    @Test void variantsInheritCorePresetsAndShareApplicationGeometry(){
        var choices=BundledThemes.available();
        assertEquals("EasyKnack",choices.get(0).toString());
        assertTrue(choices.stream().map(Object::toString).toList().containsAll(java.util.List.of("EasyKnack","EasyKnack Blue","EasyKnack Dark Blue","EasyKnack Light")));
        assertTrue(choices.stream().noneMatch(c->c.resource().startsWith("/easyknack/themes/")));
        var defaults=ApplicationTheme.load(null);
        for(var entry:java.util.Map.of("blue","blue","darkblue","darkblue","light","light").entrySet()){
            var variant=ApplicationTheme.loadSelection("classpath:/easyknack-"+entry.getKey()+".gltheme");
            var base=GLThemeLoader.loadResource("/com/vingame/glswing/themes/"+entry.getValue()+".gltheme");
            assertEquals(base.values().getColor("palette.surface"),variant.theme().values().getColor("palette.surface"));
            assertEquals(base.values().getColor("palette.text"),variant.theme().labelStyle().textColor());
            for(String key:java.util.List.of("node.minimumWidth","node.cornerRadius","node.rowGap","curve.height","metrics.controlHeight"))
                assertEquals(defaults.theme().values().getFloat(key,-1),variant.theme().values().getFloat(key,-2),key);
            for(var signal:SignalCategory.values())assertEquals(defaults.signal(signal),variant.signal(signal));
            for(var kind:NodeKind.values())assertNotEquals(Color.MAGENTA,variant.title(kind).dark());
        }
    }
    @Test void lightWorkspaceAndCurveHaveReadableContrast(){
        var theme=ApplicationTheme.loadSelection("classpath:/easyknack-light.gltheme").theme();
        assertTrue(contrast(theme.labelStyle().textColor(),theme.values().getColor("node.background"))>=7);
        assertTrue(contrast(theme.values().getColor("curve.line"),theme.values().getColor("curve.background"))>=3);
        assertNotEquals(0,theme.values().getColor("nodeEditor.gridMajor").getAlpha());
    }
    @Test void variantsPreviewOnExistingNodesWithoutEditingTheDocument()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var previous=ApplicationTheme.current();
            try(var controller=new WorkspaceController()){
                // Match startup: apply the application theme before measuring document views.
                controller.workspace().setGLTheme(previous.theme());
                controller.openDocument("Themes",StarterDocument.create());
                var document=NodeDocuments.comparable(controller.snapshotDocument());
                var node=controller.workspace().editor().getNodes().get(0);
                for(var choice:BundledThemes.available()){
                    var selected=ApplicationTheme.loadSelection(choice.id());ApplicationTheme.activate(selected);
                    controller.workspace().setGLTheme(selected.theme());controller.refreshAppearance();
                    assertSame(node,controller.workspace().editor().getNodes().get(0));
                    assertEquals(selected.theme().values().getColor("palette.surface"),node.getGLTheme().values().getColor("palette.surface"));
                    assertEquals(document,NodeDocuments.comparable(controller.snapshotDocument()));
                }
            }finally{ApplicationTheme.activate(previous);}
        });
    }
    private static double contrast(Color a,Color b){double x=luminance(a),y=luminance(b);return (Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
    private static double luminance(Color c){
        double[] channels={c.getRed()/255.,c.getGreen()/255.,c.getBlue()/255.};
        for(int i=0;i<channels.length;i++)channels[i]=channels[i]<=.04045?channels[i]/12.92:Math.pow((channels[i]+.055)/1.055,2.4);
        return channels[0]*.2126+channels[1]*.7152+channels[2]*.0722;
    }
    @Test void bundledThemeLoadsTheApplicationResourceIncludingItsOverrides() {
        var theme = ApplicationTheme.load(null);
        for (var kind : NodeKind.values()) {
            assertNotEquals(Color.MAGENTA, theme.title(kind).light());
            assertNotEquals(Color.MAGENTA, theme.title(kind).dark());
        }
        var resource = GLThemeLoader.loadResource("/easyknack.gltheme");
        assertEquals(resource.sliderStyle(), theme.theme().sliderStyle());
        assertEquals(resource.values().asMap(), theme.theme().values().asMap());
        assertEquals(resource.values().getColor("easyknack.signal.AUDIO", Color.MAGENTA), theme.signal(SignalCategory.AUDIO));
    }
    @Test void externalThemeReloadsColorsAndGeometryIncludingSiblingDefault() throws Exception {
        var base = directory.resolve("default.gltheme");
        Files.writeString(base, "slider.height=23\n");
        var file = directory.resolve("custom.gltheme");
        Files.writeString(file, "theme.extends=default\n@COLOR easyknack.signal.VALUE=#654321\n");
        var first = ApplicationTheme.load(file);
        assertEquals(23f, first.theme().sliderStyle().height());
        assertEquals(new Color(0x654321), first.signal(SignalCategory.VALUE));
        Files.writeString(base, "slider.height=31\n");
        Files.writeString(file, "theme.extends=default\n@COLOR easyknack.signal.VALUE=#123456\n");
        var second = ApplicationTheme.load(file);
        assertEquals(31f, second.theme().sliderStyle().height());
        assertEquals(new Color(0x123456), second.signal(SignalCategory.VALUE));
        assertEquals(first.signal(SignalCategory.AUDIO), second.signal(SignalCategory.AUDIO));
    }
}

