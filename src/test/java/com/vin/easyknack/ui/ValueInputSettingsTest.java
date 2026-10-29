package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.GLSlider;

class ValueInputSettingsTest {
    @Test void boundsClampValuesAndPersistThroughRegistry() {
        var definition=new ValueNode();var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());
        var node=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()).withProperty("value",GLNodeValue.of(20.));
        node=registry.normalize(node.withProperty("slider-style",GLNodeValue.of("bar")).withProperty("minimum",GLNodeValue.of(2.)).withProperty("maximum",GLNodeValue.of(10.)));
        assertEquals(10.,node.properties().get("value").toJavaValue());
        var slider=(Editor.Slider)definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.key().equals(ValueNode.VALUE)).findFirst().orElseThrow().editor();
        assertTrue(slider.bar());assertEquals(2.,slider.min());assertEquals(10.,slider.max());
        assertTrue(definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.EDITOR&&!r.key().equals(ValueNode.VALUE)).allMatch(NodeBuilder.Row::isInspectorOnly));
        node=registry.normalize(node.withProperty("minimum",GLNodeValue.of(12.)));
        assertTrue((double)node.properties().get("maximum").toJavaValue()>12);
        assertEquals(12.,node.properties().get("value").toJavaValue());
    }
    @Test void nestedStepLabelsUpdateWhenOnlyLanguageChanges() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            String previous=System.getProperty("easyknack.language");
            try {
                System.setProperty("easyknack.language","de");NodeText.reload();AppText.reload();
                var definition=new StepSwitchNode();var model=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults());
                var view=new StepSwitchView(definition,new NodeViewContext((id,key,value)->{},null),model);
                assertTrue(sliders(view).stream().anyMatch(s->s.getText().equals("Eingang 1")));
                System.setProperty("easyknack.language","en");NodeText.reload();AppText.reload();view.update(model);
                assertTrue(sliders(view).stream().anyMatch(s->s.getText().equals("Input 1")));
                assertFalse(sliders(view).stream().anyMatch(s->s.getText().startsWith("Eingang")));
            } finally {
                if(previous==null)System.clearProperty("easyknack.language");else System.setProperty("easyknack.language",previous);
                NodeText.reload();AppText.reload();
            }
        });
    }
    private static List<GLSlider> sliders(GLContainer c){var result=new ArrayList<GLSlider>();if(c instanceof GLSlider s)result.add(s);for(var child:c.getGLChildren())result.addAll(sliders(child));return result;}
    @Test void languageResourcesAreUtf8() throws Exception {
        try(var paths=java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources"))){
            for(var path:paths.filter(p->p.toString().endsWith(".properties")).toList())assertDoesNotThrow(()->java.nio.file.Files.readString(path),path.toString());
        }
    }
}
