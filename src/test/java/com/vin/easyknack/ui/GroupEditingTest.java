package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;

class GroupEditingTest {
    static java.util.stream.Stream<com.vingame.glswing.component.GLContainer> descendants(com.vingame.glswing.component.GLContainer parent){
        return parent.getGLChildren().stream().flatMap(child->java.util.stream.Stream.concat(java.util.stream.Stream.of(child),descendants(child)));
    }
    @Test void authoringButtonsAllowUnboundChoicesAndUndoRemoval()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var f=GroupChoicesTest.fixture(false);c.openDocument("Editors",f.doc());c.workspace().openComposite("test.choices.asset");
            c.workspace().inspectorTabs().setSelectedIndex(1);
            var panel=descendants(c.workspace()).filter(EditorTemplatesPanel.class::isInstance).findFirst().orElseThrow();
            java.util.function.Consumer<String> click=label->descendants(panel).filter(v->v instanceof com.vingame.glswing.ui.GLButton b&&b.getText().equals(label)).map(com.vingame.glswing.ui.GLButton.class::cast).findFirst().orElseThrow().doClick();
            click.accept("Add combobox");assertEquals(1,c.editorTemplates().all().size());
            click.accept("Add entry");assertEquals(2,c.editorTemplates().all().get(0).entries().size());
            var saved=c.snapshotDocument();assertNotNull(saved.root().metadata().get(EditorTemplates.KEY));
            click.accept("Remove template");assertTrue(c.editorTemplates().all().isEmpty());
            c.history().undo();assertEquals(1,c.editorTemplates().all().size());
            var expected=c.editorTemplates().snapshot();c.openDocument("Reload",saved);assertEquals(expected,c.editorTemplates().snapshot());

        }});
    }
    @Test void entriesKeepTheirMeaningAfterRenameReorderAndRemoval(){
        var f=mappedFixture();var asset=f.doc().embeddedAssets().get("test.choices.asset");var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.doc());
        var old=GroupChoices.choices(asset,registry).stream().filter(c->c.id().equals("preset")).findFirst().orElseThrow();
        var high=new GroupChoices.Option("high","Renamed high");var low=new GroupChoices.Option("low","Renamed low");
        var bindings=asset.compositeDescriptor().propertyMappings().stream().filter(m->m.publicPropertyId().equals("preset")).toList();
        var changed=GroupChoices.store(asset,new GroupChoices.Choice("preset","Preset",Editor.comboBox(List.of(high,low),high,GroupChoices.Option::id)),bindings);
        assertEquals(GLNodeValue.of(880.),bindings.get(0).mappedValue(GLNodeValue.of("high")));
        registry.replaceAsset(changed);var choice=GroupChoices.choices(changed,registry).stream().filter(c->c.id().equals("preset")).findFirst().orElseThrow();
        assertEquals("high",choice.editor().items().get(0).id());assertEquals("Preset",choice.label());
        assertEquals(GLNodeValue.of("high"),GroupChoices.selection(changed,choice,GLNodeValue.of("deleted-entry")));
        assertEquals(GLNodeValue.of("low"),GroupChoices.selection(changed,choice,GLNodeValue.of("low")));
        assertTrue(GroupChoices.choices(GroupChoices.remove(changed,"preset"),registry).stream().noneMatch(c->c.id().equals("preset")));
    }
    @Test void interfacePositionsSurviveSavingWhileInsideAndAfterNavigation()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var f=GroupChoicesTest.fixture(false);c.openDocument("Positions",f.doc());c.workspace().openComposite("test.choices.asset");
            var boundaries=c.workspace().editor().getNodes().stream().filter(n->!(n instanceof BasicNodeView)&&n.getPorts().size()>0).toList();assertEquals(2,boundaries.size());
            boundaries.get(0).setLocation(-530,237);boundaries.get(1).setLocation(1447,-172);
            var saved=c.snapshotDocument();var expected=saved.embeddedAssets().get("test.choices.asset").viewState().auxiliaryNodes();assertEquals(2,expected.size());
            assertTrue(c.workspace().navigateBack());
            saved=c.snapshotDocument();assertEquals(expected,saved.embeddedAssets().get("test.choices.asset").viewState().auxiliaryNodes());
            try{var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(saved,bytes);saved=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());}catch(Exception e){throw new RuntimeException(e);}
            c.openDocument("Reloaded",saved);c.workspace().openComposite("test.choices.asset");
            assertEquals(expected,c.snapshotDocument().embeddedAssets().get("test.choices.asset").viewState().auxiliaryNodes());
        }});
    }
    static GroupChoicesTest.Fixture mappedFixture(){
        var f=GroupChoicesTest.fixture(false);var old=f.doc().embeddedAssets().get("test.choices.asset");var inner=old.graph().nodes().get(0);
        var a=new GroupChoices.Option("low","Lower");var b=new GroupChoices.Option("high","Higher");
        var choice=new GroupChoices.Choice("preset","Destination",Editor.comboBox(List.of(a,b),a,GroupChoices.Option::id));
        var maps=List.of(new GLCompositePropertyMapping("preset",inner.id(),"target",Map.of("low",GLNodeValue.of(220.),"high",GLNodeValue.of(880.))),new GLCompositePropertyMapping("preset",inner.id(),"tuning",Map.of("low",GLNodeValue.of(440.),"high",GLNodeValue.of(442.))));
        var asset=GroupChoices.store(old,choice,maps);
        return new GroupChoicesTest.Fixture(new GLNodeGraphDocument(f.doc().root(),Map.of(asset.id(),asset),List.of()),f.group());
    }
    @Test void customChoiceMapsMultipleParametersAndRoundTrips()throws Exception{
        var f=mappedFixture();var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(f.doc(),bytes);var doc=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        assertEquals(NodeDocuments.comparable(f.doc()),NodeDocuments.comparable(doc));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);
        var root=doc.root();var nodes=root.graph().nodes().stream().map(n->n.withProperty("preset",GLNodeValue.of("high"))).toList();
        root=new GLNodeGraphAsset(root.id(),1,root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,nodes,root.graph().connections()),root.viewState());
        var flat=GLCompositeGraphFlattener.flatten(root,registry,new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid(),flat.diagnostics().toString());
        var node=flat.graph().orElseThrow().nodes().get(0);assertEquals(880.,node.properties().get("target").toJavaValue());assertEquals(442.,node.properties().get("tuning").toJavaValue());
        var export=GroupSourceExporter.create(doc,f.group());assertNotNull(GroupBakeTest.definition(export));
        assertTrue(export.files().get("native/"+export.className()+".hpp").contains("880.0"));
    }
}
