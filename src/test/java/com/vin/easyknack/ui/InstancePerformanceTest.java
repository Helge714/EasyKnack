package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;

/** Reproducible, opt-in fixture generator for the standalone instance benchmark. */
class InstancePerformanceTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.benchmark",matches="true")
    void generate()throws Exception {
        var base=MultipleGroupsTest.fixture(false,"weighted");var asset=base.document().embeddedAssets().values().iterator().next();
        var graph=new GLNodeGraph(asset.graph());var harmonic=graph.snapshot().nodes().stream().filter(n->n.typeId().equals(new MathNode().registration().id())&&new NodeState(n.properties()).text("operation","").equals("add")).findFirst().orElseThrow();
        var combine=graph.snapshot().nodes().stream().filter(n->n.typeId().equals(new InstanceCombineNode().registration().id())).findFirst().orElseThrow();
        var power=add(graph,new MathNode());
        graph.edit("Falloff",e->{e.setProperty(power,"operation",GLNodeValue.of("power"));e.setProperty(power,"b",GLNodeValue.of(-1.));e.setProperty(power,"clamp",GLNodeValue.of(false));e.setProperty(combine.id(),"mode",GLNodeValue.of("weighted"));e.addConnection(SpectralGroupsTest.wire(harmonic.id(),"audio-out",power,"a"));e.addConnection(SpectralGroupsTest.wire(power,"audio-out",combine.id(),"weight"));});
        var metadata=new HashMap<>(asset.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("40"));
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),metadata,asset.compositeDescriptor());
        var instance=base.instance().withProperty(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(40.)).withProperty(GroupUnits.key("Count"),GLNodeValue.of(40.));
        var root=base.document().root();root=new GLNodeGraphAsset(root.id(),1,root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,List.of(instance),List.of()),root.viewState());
        var document=new GLNodeGraphDocument(root,Map.of(asset.id(),asset),List.of());
        String input=System.getProperty("easyknack.benchmark.document","");
        if(!input.isEmpty()){
            try(var documents=new NodeDocuments()){document=documents.read(Path.of(input)).toCompletableFuture().join();}
            var assets=document.embeddedAssets();
            instance=document.root().graph().nodes().stream().filter(n->assets.values().stream().anyMatch(a->MultipleGroups.multiple(a)&&a.compositeDescriptor().nodeTypeId().equals(n.typeId()))).findFirst().orElseThrow();
            var selected=instance;
            asset=assets.values().stream().filter(a->a.compositeDescriptor()!=null&&a.compositeDescriptor().nodeTypeId().equals(selected.typeId())).findFirst().orElseThrow();
            Files.writeString(Path.of("target/instance-user-graph.txt"),"Instance: "+instance+"\nMetadata: "+asset.metadata()+"\nPorts: "+asset.compositeDescriptor()+"\nNodes: "+asset.graph().nodes()+"\nWires: "+asset.graph().connections());
            Files.writeString(Path.of("target/instance-user-root.txt"),"Nodes: "+document.root().graph().nodes()+"\nWires: "+document.root().graph().connections());
            root=document.root();root=new GLNodeGraphAsset(root.id(),1,root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,List.of(instance),List.of()),root.viewState());
            document=new GLNodeGraphDocument(root,assets,List.of());
        }
        var compiled=MultipleGroupsTest.compiled(new MultipleGroupsTest.Fixture(document,instance));
        var export=GroupSourceExporter.create(document,instance);var def=NodeRegistry.require(compiled.typeId());
        var directory=Path.of("target/instance-performance");Files.createDirectories(directory);
        Files.writeString(directory.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(export.definition());Files.writeString(directory.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String live="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(compiled.properties()),p->-1);
        String baked="!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->-1);
        Files.writeString(directory.resolve("Fixture.hpp"),"#include \""+export.className()+".hpp\"\nusing BakedInstance=easyknack::"+export.className()+";\ninline const char* liveParameters="+quote(live)+";\ninline const char* bakedParameters="+quote(baked)+";\n");
        try(var out=Files.newOutputStream(directory.resolve("reference.knack"))){new KnackDocumentCodec().write(document,out);}
    }
}
