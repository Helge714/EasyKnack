package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.binding.*;

class BufferWritePositionTest {
    static GLNodeGraphDocument legacy(boolean group){
        var graph=new GLNodeGraph();var writer=add(graph,new HistoryBufferNode());var out=add(graph,new OutputNode());
        connect(graph,writer,"write-position",out,"audio-in");
        var descriptor=group?new GLCompositeNodeDescriptor("test.buffer.context","test.buffer.context.asset",1,List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("Head",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(writer,"write-position")))):null;
        var asset=new GLNodeGraphAsset(group?descriptor.assetId():"root",1,group?GLNodeGraphAssetKind.COMPOSITE:GLNodeGraphAssetKind.PROJECT,graph.snapshot(),new GLNodeEditorViewState(Map.of(writer,new GLNodeViewState(20,30,180,100))),Map.of(),descriptor);
        if(!group)return new GLNodeGraphDocument(asset,Map.of(),List.of());
        var root=new GLNodeGraph();root.edit("Group",e->e.addNode(new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of())));
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
    }
    @Test void contextOwnsMetadataAndLegacyWiresAndGroupOutputsArePreserved(){
        var writer=new HistoryBufferNode();assertEquals(List.of("history"),writer.layout(new NodeState(writer.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::id).toList());
        for(boolean group:List.of(false,true)){
            var old=legacy(group);assertEquals(1,BufferContextMigration.count(old));var migrated=NodeDocuments.prepare(old);
            assertEquals(0,BufferContextMigration.count(migrated));assertEquals(migrated,BufferContextMigration.migrate(migrated));
            var asset=group?migrated.embeddedAssets().values().iterator().next():migrated.root();
            var context=asset.graph().nodes().stream().filter(n->n.typeId().equals(new BufferContextNode().registration().id())).findFirst().orElseThrow();
            assertEquals(2,asset.graph().connections().size());assertTrue(asset.viewState().nodes().containsKey(context.id()));
            assertTrue(asset.graph().connections().stream().anyMatch(w->w.output().equals(new GLNodeEndpoint(context.id(),"write-position"))));
            if(group)assertEquals(new GLNodeEndpoint(context.id(),"write-position"),asset.compositeDescriptor().portMappings().get(0).internalEndpoint());
            assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(asset.graph()));
        }
    }
    @Test void fileLoadReportsMigrationOnceAndRoundTripsWithoutLoss()throws Exception{
        var file=Files.createTempFile(Path.of("target"),"legacy-buffer-",".knack");var codec=new KnackDocumentCodec();
        try(var out=Files.newOutputStream(file)){codec.write(legacy(false),out);}
        var notices=new java.util.concurrent.atomic.AtomicInteger();
        try(var files=new NodeDocuments(notices::addAndGet)){
            var migrated=files.read(file).toCompletableFuture().get();assertEquals(1,notices.get());
            files.write(file,migrated).toCompletableFuture().get();files.read(file).toCompletableFuture().get();assertEquals(1,notices.get());
        }finally{Files.deleteIfExists(file);}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeContextMatchesReadersAndHandlesFileAndEmptyResources()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"buffer-context-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/BufferContextTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
