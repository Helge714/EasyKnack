package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
/** Choose a sample; Divide B is its root frequency (440 Hz by default). */
final class SamplerExample {
 static GLNodeGraphDocument create(){
  var graph=new GLNodeGraph();var views=new LinkedHashMap<GLNodeId,GLNodeViewState>();var nodes=new ArrayList<GLGraphNode>();
  var defs=List.of(new MidiInputNode(),new MidiNoteNode(),new MathNode(),new FileBufferNode(),new HistoryReaderNode(),new EnvelopeNode(),new MathNode(),new GainNode(),new OutputNode());
  float[][] positions={{40,40},{320,40},{600,40},{600,400},{880,400},{880,40},{1160,300},{1440,300},{1720,300}};
  for(int i=0;i<defs.size();i++){var def=defs.get(i);var values=new HashMap<>(def.defaults());if(i==2){values.put("operation",GLNodeValue.of("divide"));values.put("b",GLNodeValue.of(440.0));}if(i==5){values.put("duration",GLNodeValue.of(10.0));values.put("release",GLNodeValue.of(40.0));}nodes.add(new GLGraphNode(GLNodeId.random(),def.registration().id(),values));}
  graph.edit("Sampler example",e->{for(int i=0;i<nodes.size();i++){var node=nodes.get(i);e.addNode(node);views.put(node.id(),new GLNodeViewState(positions[i][0],positions[i][1]));}
   Object[][] wires={{0,"midi-out",1,"midi-in"},{1,"frequency",2,"a"},{2,"audio-out",4,"speed"},{3,"history",4,"history"},{1,"trigger",4,"reset"},{1,"gate",5,"gate"},{1,"trigger",5,"trigger"},{4,"audio-out",6,"a"},{5,"audio-out",6,"b"},{6,"audio-out",7,"audio-in"},{7,"audio-out",8,"audio-in"}};
   for(var wire:wires)e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(nodes.get((int)wire[0]).id(),(String)wire[1]),new GLNodeEndpoint(nodes.get((int)wire[2]).id(),(String)wire[3])));
  });
  return new GLNodeGraphDocument(new GLNodeGraphAsset("sampler-example",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),new GLNodeEditorViewState(views)),Map.of(),List.of());
 }
}
