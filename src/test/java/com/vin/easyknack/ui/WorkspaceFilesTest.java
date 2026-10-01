package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
class WorkspaceFilesTest {
    @TempDir Path temp;
    @Test void imageScaleAndViewportUseGraphCoordinates(){
        assertArrayEquals(new float[]{-500,-400,2000,1600},GraphImageExport.visibleBounds(1000,800,.5f,0,0));
        assertArrayEquals(new float[]{200,225,500,400},GraphImageExport.visibleBounds(1000,800,2,100,-50));
        assertTrue(GraphImageExport.validDimensions(1000,800,.01));
        assertTrue(GraphImageExport.validDimensions(10,10,50));
        assertFalse(GraphImageExport.validDimensions(1000,800,Double.MAX_VALUE));
        assertFalse(GraphImageExport.validDimensions(1000,800,Double.NaN));
        assertFalse(GraphImageExport.validDimensions(1000,800,.009));
    }
    @Test void seededExamplesRoundTripAndExistingFilesArePreserved()throws Exception{
        String previous=System.getProperty("easyknack.examples");System.setProperty("easyknack.examples",temp.toString());
        try{
            ExampleLibrary.initialize();var path=temp.resolve("Harmonizer.knack");
            try(var files=new NodeDocuments()){assertDoesNotThrow(()->files.read(path).toCompletableFuture().join());}
            Files.writeString(path,"user-edited");ExampleLibrary.initialize();assertEquals("user-edited",Files.readString(path));
        }finally{if(previous==null)System.clearProperty("easyknack.examples");else System.setProperty("easyknack.examples",previous);}
    }
    @Test void portableEditorsAndEndpointLookupTablesGetFreshIds(){
        var doc=GroupUnitsTest.document(false);var registry=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();registry.registerDocument(doc);
        var asset=doc.embeddedAssets().get("test.asset");var port=asset.compositeDescriptor().portMappings().get(0);
        var template=new EditorTemplates.Template("portable","My editor",List.of(new EditorTemplates.Entry("first","First",GLNodeValue.of(0.))),"first");
        asset=GroupInputs.applyTemplate(asset,port,template,registry);
        var source=new GLNodeGraphDocument(doc.root(),Map.of(asset.id(),asset),List.of());
        var imported=GroupImport.copy(source,asset.id(),"Imported").selected();var copied=imported.compositeDescriptor().portMappings().get(0);
        assertNotEquals(template.id(),GroupInputs.template(copied).id());assertEquals(template.entries(),GroupInputs.template(copied).entries());
        assertEquals(EditorTemplates.PREFIX+GroupInputs.template(copied).id(),copied.presentation().inputEditorId());
        assertNotEquals(port.internalEndpoint().nodeId(),copied.internalEndpoint().nodeId());
        var targets=(Map<?,?>)copied.presentation().editorOptions().get("targetProperties").toJavaValue();
        assertTrue(targets.containsKey(copied.internalEndpoint().nodeId()+"/"+copied.internalEndpoint().portId()));
    }
    @Test void examplesAreSortedNestedAndDoNotFollowLinks()throws Exception{
        Files.createDirectories(temp.resolve("Synths"));Files.writeString(temp.resolve("B.knack"),"");Files.writeString(temp.resolve("a.knack"),"");Files.writeString(temp.resolve("Synths/Voice.knack"),"");Files.writeString(temp.resolve("ignore.txt"),"");
        var entries=ExampleLibrary.scan(temp);assertEquals(List.of("a","B","Synths"),entries.stream().map(ExampleLibrary.Entry::name).toList());assertEquals("Voice",entries.get(2).children().get(0).name());
    }
    @Test void newDocumentIsConnectedAndImportCopiesAreIndependent()throws Exception{
        var document=NodeDocuments.prepare(StarterDocument.create());assertEquals(2,document.root().graph().nodes().size());assertEquals(1,document.root().graph().connections().size());
        var a=GroupImport.copy(document,document.root().id(),"My synth");var b=GroupImport.copy(document,document.root().id(),"My synth");
        assertNotEquals(a.selected().id(),b.selected().id());assertNotEquals(a.selected().compositeDescriptor().nodeTypeId(),b.selected().compositeDescriptor().nodeTypeId());assertEquals(document.root().graph().connections().size(),a.selected().graph().connections().size());
        assertTrue(java.util.Collections.disjoint(a.selected().graph().nodes().stream().map(GLGraphNode::id).toList(),b.selected().graph().nodes().stream().map(GLGraphNode::id).toList()));
        assertEquals("My synth",a.selected().metadata().get("easyknack.title").toJavaValue());
        var imported=new GLNodeGraphDocument(document.root(),Map.of(a.selected().id(),a.selected(),b.selected().id(),b.selected()),List.of());assertDoesNotThrow(()->NodeDocuments.prepare(imported));
        var codec=new KnackDocumentCodec();var out=new java.io.ByteArrayOutputStream();codec.write(imported,out);assertEquals(imported,codec.read(new java.io.ByteArrayInputStream(out.toByteArray()),codec.currentSchemaVersion()));
    }
    @Test void nestedGroupsAndTemplatesSurviveImport(){
        var doc=HarmonizerExample.create();var template=new EditorTemplates.Template("choice-id","My choices",List.of(new EditorTemplates.Entry("first","My label",GLNodeValue.of(2.5))),"first");
        var metadata=new LinkedHashMap<>(doc.root().metadata());metadata.put(EditorTemplates.KEY,GLNodeValue.of(List.of(template.value().toJavaValue())));
        var root=doc.root();doc=new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),root.graph(),root.viewState(),metadata,root.compositeDescriptor()),doc.embeddedAssets(),List.of());
        var result=GroupImport.copy(doc,root.id(),"Imported");assertTrue(result.assets().size()>1);assertEquals(1,result.templates().size());assertNotEquals(template.id(),result.templates().get(0).id());assertEquals(template.entries(),result.templates().get(0).entries());
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();result.assets().forEach(a->assets.put(a.id(),a));var selected=assets.remove(result.selected().id());assertDoesNotThrow(()->NodeDocuments.prepare(new GLNodeGraphDocument(selected,assets,List.of())));
    }
    @Test void languageBundlesHaveMatchingKeysAndTranslatedMenus(){
        var control=ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        var en=ResourceBundle.getBundle("application",Locale.ENGLISH,control);var de=ResourceBundle.getBundle("application",Locale.GERMAN,control);
        assertEquals(en.keySet(),de.keySet());assertEquals("New",en.getString("new"));assertEquals("Neu",de.getString("new"));
        for(String key:en.keySet())assertFalse(de.getString(key).isBlank(),key);
    }
    @Test void auditBuiltinPresentationText()throws Exception{
        var root=ResourceBundle.getBundle("application",Locale.ROOT);var missing=new TreeSet<String>();
        java.util.function.Consumer<String> check=text->{if(text==null||text.isBlank())return;try{String key="text."+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8))).substring(0,16);if(!root.containsKey(key))missing.add(text);}catch(Exception e){throw new RuntimeException(e);}};
        for(var node:com.vin.easyknack.nodes.NodeRegistry.builtinDefinitions()){
            check.accept(node.title());for(var row:node.layout(new com.vin.easyknack.nodes.NodeState(node.defaults())).rows()){
                check.accept(row.label());check.accept(row.tooltip());
                if(row.editor() instanceof com.vin.easyknack.nodes.Editor.ComboBox<?> combo)for(var item:combo.items())check.accept(item.toString());
            }
        }
        Files.write(Path.of("target/missing-node-texts.txt"),missing);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void realRendererExportsPngAndJpegWithoutChangingDocument()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController()){
                controller.openDocument("Test",StarterDocument.create());controller.workspace().setBounds(0,0,900,600);
                var before=NodeDocuments.comparable(controller.snapshotDocument());
                assertFalse(GraphImageExport.availableAreas(controller).contains(GraphImageExport.Area.SELECTED));
                var padded=GraphImageExport.capturePixels(controller,GraphImageExport.Area.ALL,900,600,48,true);
                assertEquals(900,padded.getWidth());assertEquals(600,padded.getHeight());
                assertEquals(0,padded.getRGB(10,10)>>>24);
                javax.imageio.ImageIO.write(padded,"png",Path.of("target/export-padding.png").toFile());
                int minY=padded.getHeight(),maxY=0;
                for(int y=0;y<padded.getHeight();y++)for(int x=0;x<padded.getWidth();x++)if((padded.getRGB(x,y)>>>24)>100){minY=Math.min(minY,y);maxY=Math.max(maxY,y);}
                assertTrue(Math.abs(minY-(padded.getHeight()-1-maxY))<5,"Graph must be centered vertically");
                try(var dialogs=new RememberingFileDialogs()){
                    var panel=GraphImageExport.createPanel(controller,null,new com.vingame.glswing.component.GLDialog[1],dialogs);
                    try{
                        panel.content().setGLTheme(ApplicationTheme.current().theme());panel.content().setGLLocalizer(com.vingame.glswing.ui.GLLocalizer.ENGLISH);panel.content().setBounds(0,0,1120,535);panel.preview().run();
                        var picture=GraphImageExport.renderPixels(panel.content(),1120,535,1120,535,false);
                        javax.imageio.ImageIO.write(picture,"png",Path.of("target/export-dialog.png").toFile());
                        var children=panel.content().getGLChildren();
                        var group=(com.vingame.glswing.ui.GLNumberSpinnerGroup)children.stream().filter(c->c instanceof com.vingame.glswing.ui.GLNumberSpinnerGroup).findFirst().orElseThrow();
                        var dimensionEditor=group.getSpinners().get(0).getEditor();
                        dimensionEditor.mousePressed(groupMouse(dimensionEditor,java.awt.event.MouseEvent.MOUSE_PRESSED,10));
                        dimensionEditor.mouseDragged(groupMouse(dimensionEditor,java.awt.event.MouseEvent.MOUSE_DRAGGED,42));
                        dimensionEditor.mouseReleased(groupMouse(dimensionEditor,java.awt.event.MouseEvent.MOUSE_RELEASED,42));
                        dimensionEditor.setText("800");assertTrue(dimensionEditor.commit());
                        for(var field:group.getSpinners())assertEquals(800,field.getValue(),"Joint edit must override the old aspect ratio");
                        dimensionEditor.setText("400");assertTrue(dimensionEditor.commit());
                        for(var field:group.getSpinners())assertEquals(400,field.getValue(),"Proportional edit must use the new aspect ratio");
                        var transparency=children.stream().filter(c->c instanceof com.vingame.glswing.ui.GLCheckBox check&&check.getText().equals(AppText.text("image.transparent"))).findFirst().orElseThrow();
                        assertTrue(transparency.isVisible());
                        @SuppressWarnings("unchecked") var format=(com.vingame.glswing.ui.GLComboBox<String>)children.stream().filter(c->c instanceof com.vingame.glswing.ui.GLComboBox<?> combo&&combo.getItems().contains("JPG")).findFirst().orElseThrow();
                        format.setSelectedItem("JPG");assertFalse(transparency.isVisible());
                        format.setSelectedItem("PNG");assertTrue(transparency.isVisible());
                    }finally{panel.close().run();}
                }
                var spinner=new com.vingame.glswing.ui.GLNumberSpinner(1,1e20,123456789123456789.0);
                spinner.setUnit("px");spinner.setBounds(0,0,180,28);
                assertFalse(spinner.getEditor().getText().contains("px"));
                var spinnerImage=GraphImageExport.renderPixels(spinner,180,28,360,56,false);
                javax.imageio.ImageIO.write(spinnerImage,"png",Path.of("target/spinner-unit.png").toFile());
                controller.workspace().editor().setZoom(.5f);
                var halfZoom=GraphImageExport.capture(controller,GraphImageExport.Area.ALL,.5,false);
                controller.workspace().editor().setZoom(2);
                var fullScale=GraphImageExport.capture(controller,GraphImageExport.Area.ALL,1,false);
                assertEquals((int)Math.ceil(fullScale.getWidth()*.5),halfZoom.getWidth());
                assertEquals((int)Math.ceil(fullScale.getHeight()*.5),halfZoom.getHeight());
                var visible=GraphImageExport.capture(controller,GraphImageExport.Area.VISIBLE,1,false);
                assertEquals((int)Math.ceil(controller.workspace().editor().getWidth()/2),visible.getWidth());
                var tiny=GraphImageExport.capture(controller,GraphImageExport.Area.ALL,.01,false);
                assertEquals((int)Math.ceil(fullScale.getWidth()*.01),tiny.getWidth());
                for(var area:GraphImageExport.Area.values()){
                    if(area==GraphImageExport.Area.SELECTED)controller.workspace().editor().setSelectedNode(controller.workspace().editor().getNodes().get(0));
                    if(area==GraphImageExport.Area.SELECTED)assertTrue(GraphImageExport.availableAreas(controller).contains(area));
                    var image=GraphImageExport.capture(controller,area,1,true);assertTrue(image.getWidth()>0);assertTrue(image.getColorModel().hasAlpha());
                    javax.imageio.ImageIO.write(image,"png",Path.of("target/export-"+area+".png").toFile());
                }
                var jpg=GraphImageExport.capture(controller,GraphImageExport.Area.ALL,2,false);assertFalse(jpg.getColorModel().hasAlpha());assertTrue(javax.imageio.ImageIO.write(jpg,"jpg",Path.of("target/export-ALL.jpg").toFile()));
                assertEquals(before,NodeDocuments.comparable(controller.snapshotDocument()));
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
    }
    private static com.vingame.glswing.input.GLMouseEvent groupMouse(com.vingame.glswing.ui.GLTextEditor editor,int id,float y){
        var original=new java.awt.event.MouseEvent(new javax.swing.JPanel(),id,System.currentTimeMillis(),0,10,(int)y,1,false,java.awt.event.MouseEvent.BUTTON1);
        var pick=new com.vingame.glswing.input.GLPickResult(0,editor,10,y,10,y,1,0,0,1,0,0);
        return new com.vingame.glswing.input.GLMouseEvent(original,pick,0,32);
    }
}
