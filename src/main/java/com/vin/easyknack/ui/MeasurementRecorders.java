package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.MeasurementRecorderNode;
import com.vin.easyknack.nodes.NodeRegistry;

/** Document-scoped capture, independent of whether a node or its group is visible. */
final class MeasurementRecorders {
    private final WorkspaceController controller;
    private final Runnable listener=this::receive;
    private record Address(String asset,com.vingame.glswing.node.model.GLNodeId node){}
    private final java.util.Map<Address,com.vingame.glswing.node.model.GLNodeValue> latest=new java.util.HashMap<>();
    private final com.vingame.glswing.history.GLHistoryManagerListener historyListener=h->retain();
    private boolean receiving,closed;
    MeasurementRecorders(WorkspaceController controller){this.controller=controller;AudioAnalysis.listen(listener);controller.history().addListener(historyListener);}
    private void receive(){
        if(closed||receiving)return;receiving=true;
        try{
            var seen=new java.util.HashSet<com.vingame.glswing.node.model.GLNodeId>();
            for(var asset:controller.driverGraphs().entrySet())for(var node:asset.getValue().nodes())if(NodeRegistry.find(node.typeId()).orElse(null) instanceof MeasurementRecorderNode&&seen.add(node.id())){
                var old=node.properties().getOrDefault(MeasurementRecorderNode.TABLE.value(),MeasurementTable.empty());
                var next=MeasurementTable.merge(old,AudioAnalysis.frame(node.id().toString()));
                latest.put(new Address(asset.getKey(),node.id()),next);
                if(!next.equals(old))controller.writeExternalProperty(asset.getKey(),node.id(),MeasurementRecorderNode.TABLE.value(),next,"Record measurement");
            }
        }finally{receiving=false;}
    }
    private void retain(){
        if(closed||receiving)return;receiving=true;
        try{
            var seen=new java.util.HashSet<com.vingame.glswing.node.model.GLNodeId>();
            for(var asset:controller.driverGraphs().entrySet())for(var node:asset.getValue().nodes())if(NodeRegistry.find(node.typeId()).orElse(null) instanceof MeasurementRecorderNode&&seen.add(node.id())){
                var address=new Address(asset.getKey(),node.id());var current=node.properties().getOrDefault(MeasurementRecorderNode.TABLE.value(),MeasurementTable.empty());var captured=latest.get(address);
                if(captured!=null&&MeasurementTable.generation(captured).equals(MeasurementTable.generation(current))&&MeasurementTable.rows(captured).size()>MeasurementTable.rows(current).size())
                {
                    var retained=MeasurementTable.retainCaptured(captured,current);latest.put(address,retained);
                    controller.writeExternalProperty(asset.getKey(),node.id(),MeasurementRecorderNode.TABLE.value(),retained,"Retain measurements");
                }
                else latest.put(address,current);
            }
        }finally{receiving=false;}
    }
    void documentChanged(){latest.clear();}
    void close(){closed=true;controller.history().removeListener(historyListener);latest.clear();}
}
