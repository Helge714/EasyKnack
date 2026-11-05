package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class BufferWritePositionTest {
    @Test void appendedNumericOutputRetainsTheResourcePortAndExports(){
        var definition=new HistoryBufferNode();var outputs=definition.layout(new NodeState(definition.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();
        assertEquals(List.of("history","write-position"),outputs.stream().map(NodeBuilder.Row::id).toList());
        assertEquals(PortDataType.HISTORY,outputs.get(0).dataType());assertEquals(ProcessingRate.AUDIO,outputs.get(1).rate());
        var graph=new GLNodeGraph();var buffer=add(graph,definition);var output=add(graph,new OutputNode());
        connect(graph,buffer,"write-position",output,"audio-in");
        assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()));
        var fixture=InstanceBufferTest.fixture(false,false,true);assertNotNull(GroupSourceExporter.create(fixture.document(),fixture.instance()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativePositionWrapsAndMatchesTheReadersSampleBySample()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"buffer-write-position-").toAbsolutePath();var source=dir.resolve("test.cpp");
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        Files.writeString(source,"""
            #include "nodes/HistoryBufferNode.hpp"
            #include "BufferRead.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                HistoryBufferNode node;node.seconds=1;node.prepare(8,4);
                assert(node.outputCount()==2&&node.outputResourceKind(0)==ResourceKind::history);
                assert(node.outputResourceKind(1)==ResourceKind::none&&node.resourceOutput(1)==nullptr);
                std::array<float,4> resource{},position{};float* outputs[]={resource.data(),position.data()};
                ProcessContext c{};c.bufferSize=4;c.output=resource.data();c.outputs=outputs;c.outputCount=2;
                for(unsigned block=0;block<3;block++){
                    node.process(c);
                    for(unsigned i=0;i<4;i++){
                        double expected=((block*4+i+1)%8)/8.;assert(position[i]==expected);
                        auto read=readBufferPosition(node.history,position[i]*8,i);assert(read.distance==0&&read.position==position[i]);
                    }
                }
                HistoryBufferNode replacement;replacement.seconds=1;replacement.prepare(8,4);replacement.adoptState(node);replacement.process(c);
                assert(position[0]==5.f/8&&position[3]==0);
                replacement.seconds=2;replacement.prepare(8,4);replacement.process(c);
                assert(position[0]==1.f/16&&position[3]==4.f/16);
            }
            """);
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
