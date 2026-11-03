package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vin.easyknack.nodes.Editor;

class RetiredGroupChoiceTest {
    private static final String RETIRED="input-choice-f295cfb3-4ef6-4778-b97c-ff51f2f6af7d";
    private static final String CURRENT="input-choice-5e922828-5385-40c4-a134-e71157b6a0c4";
    private static final String ENTRY="2254f423-9f97-48ea-8090-f86fa00e14d3",OTHER="7bb812a7-8d35-4c0e-aedd-0ddf95768133";
    private static GLNodeGraphDocument changed(GroupChoicesTest.Fixture fixture,GLGraphNode node){
        var root=fixture.doc().root();var graph=new GLNodeGraph(root.graph());
        graph.edit("Selection",e->{for(var entry:node.properties().entrySet())e.setProperty(node.id(),entry.getKey(),entry.getValue());});
        return new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor()),fixture.doc().embeddedAssets(),List.of());
    }
    private static GroupCompiler.Prepared prepare(GLNodeGraphDocument document){
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(document);
        return GroupCompiler.prepare(document.root(),registry);
    }
    private static GroupChoicesTest.Fixture replacedChoice(boolean ambiguous){
        var fixture=GroupChoicesTest.fixture(false);var assets=new HashMap<>(fixture.doc().embeddedAssets());
        var asset=assets.values().iterator().next();var target=asset.graph().nodes().get(0).id();
        asset=GroupChoices.remove(asset,"mode");
        var options=List.of(new GroupChoices.Option(OTHER,"Fixed"),new GroupChoices.Option(ENTRY,"Scale"));
        for(String property:ambiguous?List.of(CURRENT,"second-choice"):List.of(CURRENT))
            asset=GroupChoices.store(asset,new GroupChoices.Choice(property,Editor.comboBox(options,options.get(0),GroupChoices.Option::id)),
                property.equals(CURRENT)?List.of(new GLCompositePropertyMapping(property,target,"mode",Map.of(OTHER,GLNodeValue.of("fixed"),ENTRY,GLNodeValue.of("scale")))):List.of());
        assets.put(asset.id(),asset);
        return new GroupChoicesTest.Fixture(new GLNodeGraphDocument(fixture.doc().root(),assets,List.of()),fixture.group());
    }
    @Test void uniquelyIdentifiedCustomEntriesSurviveAReplacedEditor(){
        var fixture=replacedChoice(false);
        var document=changed(fixture,fixture.group().withExplicitProperty(RETIRED,GLNodeValue.of(ENTRY)));
        var restored=NodeDocuments.prepare(document);
        assertEquals(GLNodeValue.of(ENTRY),restored.root().graph().findNode(fixture.group().id()).orElseThrow().properties().get(CURRENT));
        var flat=SpectralGroups.flatten(prepare(restored),new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());
        assertTrue(flat.graph().orElseThrow().nodes().stream().anyMatch(n->GLNodeValue.of("scale").equals(n.properties().get("mode"))));
    }
    @Test void recoveryDoesNotOverwriteCurrentExplicitValuesOrGuessAmbiguousEditors(){
        var fixture=replacedChoice(false);
        var explicit=changed(fixture,fixture.group().withExplicitProperty(RETIRED,GLNodeValue.of(ENTRY)).withExplicitProperty(CURRENT,GLNodeValue.of(OTHER)));
        assertEquals(GLNodeValue.of(OTHER),prepare(explicit).root().graph().findNode(fixture.group().id()).orElseThrow().properties().get(CURRENT));
        var ambiguous=replacedChoice(true);
        var prepared=prepare(changed(ambiguous,ambiguous.group().withExplicitProperty(RETIRED,GLNodeValue.of(ENTRY))));
        assertFalse(prepared.root().graph().findNode(ambiguous.group().id()).orElseThrow().properties().containsKey(CURRENT));
    }
    @Test void retiredChoicesDoNotBreakDirectOrNestedGroupsAndKeepCurrentSelections(){
        for(boolean nested:List.of(false,true)){
            var fixture=GroupChoicesTest.fixture(nested);
            var node=fixture.group().withProperty("mode",GLNodeValue.of("scale"))
                .withProperty(RETIRED,GLNodeValue.of("old-entry"))
                .withProperty(GLGraphNode.AUTOMATIC_PREFIX+RETIRED,GLNodeValue.of(false))
                .withProperty(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX+RETIRED,GLNodeValue.of("old-default"));
            var document=changed(fixture,node);var prepared=prepare(document);
            var properties=prepared.root().graph().findNode(node.id()).orElseThrow().properties();
            for(String key:List.of(RETIRED,GLGraphNode.AUTOMATIC_PREFIX+RETIRED,GLGraphNode.AUTOMATIC_DEFAULT_PREFIX+RETIRED))assertFalse(properties.containsKey(key),key);
            assertEquals(GLNodeValue.of("scale"),properties.get("mode"));
            var flat=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000));
            assertTrue(flat.isValid(),flat.diagnostics().toString());
            assertTrue(flat.graph().orElseThrow().nodes().stream().anyMatch(n->GLNodeValue.of("scale").equals(n.properties().get("mode"))));
            assertEquals(GLNodeValue.of("old-entry"),document.root().graph().findNode(node.id()).orElseThrow().properties().get(RETIRED),"Execution must preserve the document and undo values");
        }
    }
    @Test void genuineUnknownPropertiesStillProduceDiagnostics(){
        var fixture=GroupChoicesTest.fixture(false);
        var prepared=prepare(changed(fixture,fixture.group().withProperty("custom-typo",GLNodeValue.of("x"))));
        var flat=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000));
        assertFalse(flat.isValid());
        assertTrue(flat.diagnostics().stream().anyMatch(d->d.code().equals("composite.property-unknown")));
    }
    @Test void declaredRetiredDescriptorsRemainValidForUndoCompatibility(){
        var fixture=GroupChoicesTest.fixture(false);var document=changed(fixture,fixture.group().withProperty(RETIRED,GLNodeValue.of("legacy")));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(document);
        var descriptor=registry.findComposite(fixture.group().typeId()).orElseThrow();
        var asset=registry.findAsset(descriptor.assetId()).orElseThrow();var properties=new ArrayList<>(descriptor.properties());
        properties.add(new GLCompositePropertyDescriptor(RETIRED,"text",GLCompositeValueKind.TEXT,GLNodeValue.of("legacy")));
        registry.unregisterAsset(asset.id());
        registry.registerAsset(GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),descriptor.portMappings(),properties,descriptor.propertyMappings())));
        var prepared=GroupCompiler.prepare(document.root(),registry);
        assertEquals(GLNodeValue.of("legacy"),prepared.root().graph().findNode(fixture.group().id()).orElseThrow().properties().get(RETIRED));
        assertTrue(SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000)).isValid());
    }
}
