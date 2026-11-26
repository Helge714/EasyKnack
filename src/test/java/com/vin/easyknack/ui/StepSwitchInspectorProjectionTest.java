package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.GLNodeEditorBinding;
import com.vingame.glswing.node.model.*;

class StepSwitchInspectorProjectionTest {
    @Test void inspectorContractChangesAfterNavigationKeepViewsAndAllowedWires()throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var failures=new ArrayList<String>();var logger=Logger.getLogger(GLNodeEditorBinding.class.getName());
            var handler=new Handler(){
                public void publish(LogRecord record){if(record.getMessage().startsWith("Cannot project"))failures.add(record.getMessage());}
                public void flush(){}public void close(){}
            };
            logger.addHandler(handler);
            try(var c=new WorkspaceController()) {
                c.openDocument("Inspector contracts",GroupUnitsTest.document(false));
                var definition=new StepSwitchNode();var id=GLNodeId.random();
                c.workspace().currentGraph().edit("Add switch",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
                var editor=c.workspace().editor();
                editor.connect(GroupUnitsTest.group(c).findPort("Out"),view(c,id).findPort("step-0"));
                var cables=c.workspace().currentGraph().snapshot().connections();
                c.workspace().openComposite("test.asset");c.workspace().navigateBack();
                editor.setSelectedNode(view(c,id));
                for(int i=0;i<3;i++) {
                    c.workspace().propertyInspector().setSelectedProperty("mode",GLNodeValue.of("index"));check(c,id,false,failures);
                    assertTrue(c.history().undo());check(c,id,true,failures);assertTrue(c.history().redo());check(c,id,false,failures);
                    c.workspace().propertyInspector().setSelectedProperty("mode",GLNodeValue.of("trigger"));check(c,id,true,failures);
                    assertEquals(cables,c.workspace().currentGraph().snapshot().connections());
                }
                for(var type:StepSwitchNode.Type.values()) {
                    c.workspace().propertyInspector().setSelectedProperty("type",GLNodeValue.of(type.id()));check(c,id,true,failures);
                    c.workspace().propertyInspector().setSelectedProperty("mode",GLNodeValue.of("index"));check(c,id,false,failures);
                    c.workspace().propertyInspector().setSelectedProperty("mode",GLNodeValue.of("trigger"));check(c,id,true,failures);
                }
                c.workspace().openComposite("test.asset");c.workspace().navigateBack();check(c,id,true,failures);
            } finally {logger.removeHandler(handler);}
        });
    }
    private static BasicNodeView view(WorkspaceController c,GLNodeId id) {
        return c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).map(BasicNodeView.class::cast).findFirst().orElseThrow();
    }
    private static void check(WorkspaceController c,GLNodeId id,boolean reset,List<String> failures) {
        assertTrue(failures.isEmpty(),failures.toString());
        assertEquals(c.workspace().currentGraph().snapshot().nodes().size(),c.workspace().editor().getNodes().size());
        assertEquals(reset,view(c,id).findPort("reset")!=null);assertNotNull(view(c,id).findPort("next"));
    }
}
