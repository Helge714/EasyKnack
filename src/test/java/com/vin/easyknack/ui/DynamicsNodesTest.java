package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class DynamicsNodesTest {
    private static List<BasicNode> definitions(){return List.of(new EnvelopeFollowerNode(),new DynamicsCurveNode(),new CompressorNode(),new LimiterNode(),new AllpassNode(),new DelayAllpassNode());}
    @Test void registeredDefinitionsExposeConsistentContractsAndDocumentation()throws Exception {
        new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());
        for(var node:definitions()){
            assertNotNull(NodeRegistry.require(node.registration().id()));
            var state=new NodeState(node.defaults());node.layout(state).validate(state);
            assertTrue(MultipleGroups.supports(node),node.registration().id());
            assertFalse(node.encode(state,p->-1).isBlank());
            assertFalse(NativeContractGenerator.fingerprint(node).isBlank());
        }
        assertTrue(new AllpassNode().supportsSampleFeedback());assertTrue(new DelayAllpassNode().supportsSampleFeedback());
        assertTrue(new EnvelopeFollowerNode().supportsSampleFeedback());assertTrue(new DynamicsCurveNode().supportsSampleFeedback());
        assertFalse(new CompressorNode().supportsSampleFeedback());assertFalse(new LimiterNode().supportsSampleFeedback());
        var curve=new DynamicsCurveNode();assertEquals(ProcessingRate.AUDIO,curve.resolveAutomaticRate(new NodeState(curve.defaults()),Map.of(DynamicsCurveNode.IN,ProcessingRate.AUDIO)));
        assertEquals(ProcessingRate.CONTROL,curve.resolveAutomaticRate(new NodeState(curve.defaults()),Map.of()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void nodeViewsExposeAllModelPortsIncludingHiddenAllpassControls()throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(()->{
            try(var c=new WorkspaceController()){
                int previous=c.workspace().editor().getNodes().size();
                c.workspace().currentGraph().edit("Dynamics",edit->{for(var node:definitions())edit.addNode(new GLGraphNode(GLNodeId.random(),node.registration().id(),node.defaults()));});
                assertEquals(previous+definitions().size(),c.workspace().editor().getNodes().size());
            }
        });
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.dynamics",matches="true")
    void nativeFiltersDynamicsStereoLatencyAndRealtimeChecks()throws Exception {
        var dir=Files.createTempDirectory(Path.of("target"),"dynamics-test-").toAbsolutePath();var exe=dir.resolve("test.exe");
        run(List.of(System.getProperty("easyknack.test.cxx","clang++"),"-std=c++17","-O2","-static","-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),Path.of("native/audio-host/tests/DynamicsTest.cpp").toAbsolutePath().toString(),"-o",exe.toString()),dir.resolve("build.log"));
        run(List.of(exe.toString()),dir.resolve("run.log"));System.out.print(Files.readString(dir.resolve("run.log")));
    }
    private static void run(List<String> command,Path log)throws Exception {
        var p=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{assertTrue(p.waitFor(120,TimeUnit.SECONDS),"Native test timed out: "+log);assertEquals(0,p.exitValue(),Files.readString(log));}
        finally{if(p.isAlive())p.destroyForcibly();}
    }
}
