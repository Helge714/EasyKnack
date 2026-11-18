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
    @Test void buffersShareAudioColorsInEveryBundledTheme() {
        for (var choice : BundledThemes.available()) {
            var theme = ApplicationTheme.loadSelection(choice.id());
            assertEquals(theme.signal(SignalCategory.AUDIO), theme.signal(SignalCategory.HISTORY), choice.name());
            assertEquals(theme.title(NodeKind.AUDIO), theme.title(NodeKind.HISTORY), choice.name());
            for (String suffix : java.util.List.of("light", "dark"))
                assertEquals(theme.color("node.AUDIO.title." + suffix), theme.color("node.HISTORY.title." + suffix), choice.name());
        }
    }
    @Test void variableRolesAndQualifiersKeepOneForegroundInEveryTheme() {
        for (var choice : BundledThemes.available()) {
            var values = ApplicationTheme.loadSelection(choice.id()).theme().values();
            for (String role : java.util.List.of("variable", "variable.declaration", "variable.readonly", "variable.static", "parameter", "parameter.declaration", "parameter.readonly"))
                assertEquals(values.getColor("codeEditor.text"), values.getColor("codeEditor.semantic." + role), choice.name() + ": " + role);
        }
        assertEquals(NodeKind.AUDIO, new DelayNode().registration().kind());
    }
    @Test void selectedColorsDoNotRequireTheSameKeyInTheFallback()throws Exception{
        var file=directory.resolve("selected-only.gltheme");
        Files.writeString(file,"theme.extends=default\n@COLOR easyknack.selected.only=#123456\n");
        assertEquals(new Color(0x123456),ApplicationTheme.load(file).color("selected.only"));
    }
    @Test void bakeAccentResolvesForEveryBundledThemeAndLegacyCustomThemes()throws Exception{
        for(var choice:BundledThemes.available()){
            var theme=ApplicationTheme.loadSelection(choice.id());
            assertEquals(theme.theme().values().getColor("easyknack.accent.focus"),theme.color("accent.focus"));
        }
        var legacy=directory.resolve("legacy.gltheme");Files.writeString(legacy,"theme.extends=default\n");
        assertEquals(new Color(0x83B9AB),ApplicationTheme.load(legacy).color("accent.focus"));
    }
    @Test void onlyFourSupportedThemesAreListedAndOldChoicesMigrate(){
        var choices=BundledThemes.available();
        assertEquals(java.util.List.of("EasyKnack Mint","EasyKnack Amber","EasyKnack Light Mint","EasyKnack Light Amber"),choices.stream().map(Object::toString).toList());
        for(var choice:choices)assertNotNull(ApplicationTheme.loadSelection(choice.id()));
        for(String old:java.util.List.of("easyknack","easyknack-blue","easyknack-darkblue"))
            assertEquals("classpath:"+ApplicationTheme.DEFAULT_RESOURCE,ApplicationTheme.normalizeSelection("classpath:/"+old+".gltheme"));
        assertEquals("classpath:/easyknack-light-mint.gltheme",ApplicationTheme.normalizeSelection("classpath:/easyknack-light.gltheme"));
    }
    @Test void borderlessThemesRetainVisibleConnectionStateOutlines(){
        for(var choice:BundledThemes.available()){
            var theme=ApplicationTheme.loadSelection(choice.id()).theme();var values=theme.values();
            assertEquals(0,values.getColor("nodeEditor.connectionBorder").getAlpha());
            assertTrue(values.getFloat("nodeEditor.connectionBorderWidth",0)>0);
            for(String key:java.util.List.of("nodeEditor.selectedConnectionBorder","nodeEditor.warningConnectionBorder","nodeEditor.errorConnectionBorder"))assertEquals(255,values.getColor(key).getAlpha());
            assertTrue(contrast(values.getColor("nodeEditor.selectedConnectionBorder"),values.getColor("nodeEditor.background"))>=3,choice.toString());
        }
    }
    @Test void lightWorkspaceAndCurveHaveReadableContrast(){
        var theme=ApplicationTheme.loadSelection("classpath:/easyknack-light.gltheme").theme();
        assertTrue(contrast(theme.labelStyle().textColor(),theme.values().getColor("node.background"))>=7);
        assertTrue(contrast(theme.values().getColor("curve.line"),theme.values().getColor("curve.background"))>=3);
        assertNotEquals(0,theme.values().getColor("nodeEditor.gridMajor").getAlpha());
    }
    @Test void lightAccentThemesHaveLightHeadersAndReadableDarkText(){
        var dark=ApplicationTheme.load(null);
        for(String variant:java.util.List.of("mint","amber")){
            var light=ApplicationTheme.loadSelection("classpath:/easyknack-light-"+variant+".gltheme");
            for(var kind:NodeKind.values()){
                assertTrue(luminance(light.title(kind).light())>luminance(dark.title(kind).light()),kind.name());
                assertEquals(light.title(kind).light(),light.title(kind).dark());
                assertTrue(contrast(light.color("node.title.text"),light.title(kind).light())>=4.5,kind.name());
                assertTrue(contrast(light.signal(kind.signal()),light.theme().values().getColor("node.background"))>=3,kind.name());
            }
            assertTrue(contrast(light.theme().labelStyle().textColor(),light.theme().values().getColor("slider.progressLight"))>=4.5);
            assertTrue(contrast(light.theme().values().getColor("curve.line"),light.theme().values().getColor("curve.background"))>=3);
        }
    }
    @Test void pastelThemesKeepNeutralBodiesAndReadableTypeTitles(){
        var defaults=ApplicationTheme.load(null);
        for(String variant:java.util.List.of("mint","amber")){
            var selected=ApplicationTheme.loadSelection("classpath:/easyknack-slider-"+variant+".gltheme");
            var values=selected.theme().values();
            assertEquals(defaults.theme().values().getFloat("node.portRadius",-1),values.getFloat("node.portRadius",-2));
            assertEquals(.85f,values.getFloat("node.portDiamondScale",1));
            assertEquals(0,values.getFloat("node.titleInset",-1));
            assertEquals(values.getColor("palette.text"),values.getColor("checkBox.check"));
            assertEquals(defaults.theme().values().getColor("node.background"),values.getColor("node.background"));
            assertEquals(values.getColor("palette.accent"),values.getColor("tabbedPane.tabSelected"));
            for(var kind:NodeKind.values()){
                assertTrue(contrast(selected.color("node.title.text"),selected.title(kind).light())>=4.5,kind.name());
                assertTrue(contrast(selected.signal(kind.signal()),values.getColor("node.background"))>=3,kind.name());
            }
        }
        assertEquals(ApplicationTheme.loadSelection("classpath:/easyknack-slider-mint.gltheme").theme().values().asMap(),
                ApplicationTheme.loadSelection("classpath:/easyknack-slider-mint-nodes.gltheme").theme().values().asMap());
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
                    // Themes may change intrinsic editor sizes, but never graph content or placement.
                    var after=NodeDocuments.comparable(controller.snapshotDocument());
                    assertEquals(document.root().graph(),after.root().graph());
                    assertEquals(document.root().metadata(),after.root().metadata());
                    assertEquals(document.embeddedAssets(),after.embeddedAssets());
                    assertEquals(document.externalAssets(),after.externalAssets());
                    document.root().viewState().nodes().forEach((id,before)->{
                        var bounds=after.root().viewState().nodes().get(id);
                        assertEquals(before.x(),bounds.x());assertEquals(before.y(),bounds.y());
                    });
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
        var resource = GLThemeLoader.loadResource(ApplicationTheme.DEFAULT_RESOURCE);
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

