package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import org.junit.jupiter.api.Test;
import java.util.*;

class PartialAnalysisSettingsTest {
    @Test void originalLorisControlsReplaceExperimentalClassifier(){
        var definition=new PartialAnalysisNode();var layout=definition.layout(new NodeState(definition.defaults()));
        var resolution=layout.rows().stream().filter(r->r.id().equals("resolution")).findFirst().orElseThrow();
        var slider=((UnitSlider)resolution.editor()).spec();assertEquals(0.,slider.min());
        assertEquals(0.,((Number)slider.defaultValue().toJavaValue()).doubleValue());assertTrue(resolution.isVisible());
        assertFalse(layout.rows().stream().anyMatch(r->PartialAnalysisMigration.RETIRED.contains(r.id())));
        var none=definition.layout(new NodeState(Map.of("bandwidth",GLNodeValue.of("none"))));
        assertFalse(none.rows().stream().filter(r->r.id().equals("convergence")).findFirst().orElseThrow().isVisible());
        assertFalse(none.rows().stream().filter(r->r.id().equals("region")).findFirst().orElseThrow().isVisible());
        String encoded=definition.encode(new NodeState(definition.defaults()),p->-1);assertTrue(encoded.contains("convergence"));
    }
    @Test void oldDocumentsRetireScoresAndPreserveRawAnalysis(){
        for(boolean raw:List.of(false,true)){
            var doc=PartialsTest.document(false);var root=doc.root();var graph=root.graph();
            var nodes=graph.nodes().stream().map(n->{
                if(!n.typeId().startsWith(new PartialAnalysisNode().registration().id()))return n;
                return n.withoutProperty("classify").withoutProperty("peak-snr").withoutProperty("bandwidth")
                    .withProperty("sinusoidality",GLNodeValue.of(raw?0.:.8))
                    .withProperty("confidence",GLNodeValue.of(raw?0.:.7))
                    .withProperty("assessment",GLNodeValue.of(30.)).withProperty("prominence",GLNodeValue.of(3.))
                    .withProperty("maximum",GLNodeValue.of(24000.));
            }).toList();
            root=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,graph.connections()),root.viewState(),root.metadata(),root.compositeDescriptor());
            var migrated=NodeDocuments.prepare(new GLNodeGraphDocument(root,doc.embeddedAssets(),doc.externalAssets()));
            var analysis=migrated.root().graph().nodes().stream().filter(n->n.typeId().startsWith(new PartialAnalysisNode().registration().id())).findFirst().orElseThrow();
            assertEquals(GLNodeValue.of(raw?"none":"convergence"),analysis.properties().get("bandwidth"));
            assertEquals(GLNodeValue.of(24000.),analysis.properties().get("maximum"));
            assertTrue(PartialAnalysisMigration.RETIRED.stream().noneMatch(analysis.properties()::containsKey));
            assertEquals(graph.connections(),migrated.root().graph().connections());
            assertEquals(migrated,PartialAnalysisMigration.migrate(migrated));
            PartialsTest.compiled(migrated);
        }
    }
    @Test void copiedRetiredEditorsDetachWithoutInvalidatingTheGroup(){
        var asset=GroupAuthoringTest.asset(new PartialAnalysisNode(),"resolution");var d=asset.compositeDescriptor();
        var port=d.portMappings().get(0);var options=new HashMap<>(port.presentation().editorOptions());
        var oldTargets=GroupEditorCopies.targets(port).stream().map(e->new GLNodeEndpoint(e.nodeId(),"sinusoidality")).toList();
        options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(oldTargets));
        options.put("copySource",GLNodeValue.of(oldTargets.get(0).toString()));options.put(GroupEditorCopies.SOURCE_PROPERTY,GLNodeValue.of("sinusoidality"));
        port=new GLCompositePortMapping(port.publicPort(),null,new GLCompositePortPresentation(port.presentation().inputEditorId(),false,options));
        var nodes=asset.graph().nodes().stream().map(n->n.withProperty("sinusoidality",GLNodeValue.of(.8)).withProperty("confidence",GLNodeValue.of(.7))).toList();
        var graph=new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),nodes,asset.graph().connections());
        var mappings=d.propertyMappings().stream().map(m->m.internalPropertyId().equals("resolution")?new GLCompositePropertyMapping(m.publicPropertyId(),m.internalNodeId(),"sinusoidality"):m).toList();
        d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),List.of(port),d.properties(),mappings);
        asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph,asset.viewState(),asset.metadata(),d);
        var doc=NodeDocuments.prepare(GroupAuthoringTest.document(asset,GLNodeValue.of(.8)));
        var migrated=doc.embeddedAssets().get(asset.id());
        assertTrue(GroupEditorCopies.targets(migrated.compositeDescriptor().portMappings().get(0)).isEmpty());
        assertTrue(migrated.compositeDescriptor().propertyMappings().stream().noneMatch(m->m.internalPropertyId().equals("sinusoidality")));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var prepared=GroupCompiler.prepare(doc.root(),registry);
        var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());
    }
}
