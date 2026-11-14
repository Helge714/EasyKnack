package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.ui.*;

class LiveSettingsTest {
    @Test void themesAreDiscoverableAndLoadable(){
        var choices=BundledThemes.available();assertTrue(choices.stream().anyMatch(c->c.resource().equals(ApplicationTheme.DEFAULT_RESOURCE)));
        for(var choice:choices)assertNotNull(ApplicationTheme.loadSelection(choice.id()).theme());
    }
    @Test void liveLanguagePreservesGraphAndUserText()throws Exception{
        String previous=System.getProperty("easyknack.language");
        try{
            System.setProperty("easyknack.language","en");AppText.reload();com.vin.easyknack.nodes.NodeText.reload();
            javax.swing.SwingUtilities.invokeAndWait(()->{
                try(var controller=new WorkspaceController()){
                    controller.openDocument("User title",StarterDocument.create());
                    var before=NodeDocuments.comparable(controller.snapshotDocument());
                    var label=new GLLabel(AppText.text("new"));var user=new GLLabel(new String("New"));
                    var container=new com.vingame.glswing.component.GLContainer(){protected void record(com.vingame.glswing.rendering.GLContext2D c){}};
                    var bar=new GLMenuBar();var menu=new GLPopupMenu();var item=menu.addItem(AppText.text("new"),e->{});var heading=bar.addMenu(AppText.literal("Datei"),menu);container.addGL(bar);
                    container.addGL(label);container.addGL(user);
                    var portLabel=new NodeTooltipLabel(com.vin.easyknack.nodes.NodeText.text("easyknack.audio.oscillator.audio-out","Audio Out"));
                    var customPortLabel=new NodeTooltipLabel(new String("Audio Out"));
                    container.addGL(portLabel);container.addGL(customPortLabel);
                    var selected=controller.workspace().editor().getNodes().get(0);controller.workspace().editor().setSelectedNode(selected);
                    System.setProperty("easyknack.language","de");AppText.reload();com.vin.easyknack.nodes.NodeText.reload();
                    controller.workspace().setGLLocalizer(GLLocalizer.GERMAN);controller.refreshAppearance();LiveLanguage.refresh(container);
                    assertEquals("Neu",label.getText());assertEquals("New",user.getText());
                    assertEquals("Audioausgang",portLabel.getText());assertEquals("Audio Out",customPortLabel.getText());
                    assertTrue(labels(controller.workspace()).contains("Audioausgang"));
                    assertEquals("Datei",heading.getText());assertEquals("Neu",item.getText());
                    assertTrue(controller.workspace().editor().getNodes().stream().anyMatch(n->n.getTitle().equals("Oszillator")));
                    assertFalse(controller.selectedNodeIds().isEmpty());
                    assertEquals(before,NodeDocuments.comparable(controller.snapshotDocument()));
                    System.setProperty("easyknack.language","en");AppText.reload();com.vin.easyknack.nodes.NodeText.reload();controller.refreshAppearance();LiveLanguage.refresh(container);
                    assertEquals("New",label.getText());assertTrue(controller.workspace().editor().getNodes().stream().anyMatch(n->n.getTitle().equals("Oscillator")));
                    assertEquals("File",heading.getText());assertEquals("New",item.getText());
                    LiveLanguage.refresh(controller.workspace());
                    assertEquals("Audio Output",portLabel.getText());assertEquals("Audio Out",customPortLabel.getText());
                    assertTrue(labels(controller.workspace()).contains("Audio Output"));
                    assertFalse(labels(controller.workspace()).contains("Audioausgang"));
                }
            });
        }finally{if(previous==null)System.clearProperty("easyknack.language");else System.setProperty("easyknack.language",previous);AppText.reload();com.vin.easyknack.nodes.NodeText.reload();}
    }
    private static java.util.List<String> labels(com.vingame.glswing.component.GLContainer parent){
        var labels=new java.util.ArrayList<String>();
        if(parent instanceof NodeTooltipLabel label)labels.add(label.getText());
        for(var child:parent.getGLChildren())labels.addAll(labels(child));
        return labels;
    }
}
