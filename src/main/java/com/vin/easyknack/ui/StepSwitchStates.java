package com.vin.easyknack.ui;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.*;
/** Persist runtime selection without adding trigger activity to Undo history. */
final class StepSwitchStates implements AutoCloseable {
    private final WorkspaceController controller;
    private final Runnable listener=this::receive;
    private boolean receiving,closed;
    StepSwitchStates(WorkspaceController controller){this.controller=controller;AudioAnalysis.listen(listener);}
    static long reportedRevision(AudioAnalysis.Frame frame){
        // Four 16-bit words survive both float storage and the text transport exactly.
        if(frame.values().length<5)return (long)frame.confidence();
        long revision=0;for(int i=0;i<4;i++){
            float word=frame.values()[i+1];if(!Float.isFinite(word)||word!=Math.rint(word)||word<0||word>65535)return -1;
            revision|=(long)word<<(16*i);
        }
        return revision;
    }
    private void receive(){
        if(closed||receiving)return;receiving=true;
        try{
            for(var asset:controller.driverGraphs().entrySet())for(var node:asset.getValue().nodes()){
                if(!(NodeRegistry.find(node.typeId()).orElse(null) instanceof StepSwitchNode))continue;
                var frame=AudioAnalysis.frame(node.id().toString());var state=new NodeState(node.properties());
                if(frame==null||!Double.isFinite(frame.frequency())||reportedRevision(frame)!=StepSwitchNode.revision(state))continue;
                int next=Math.max(0,Math.min(StepSwitchNode.count(state)-1,(int)frame.frequency()));
                if(next!=StepSwitchNode.selected(state))controller.writeExternalProperty(asset.getKey(),node.id(),StepSwitchNode.SELECTION.value(),StepSwitchNode.selection(next,StepSwitchNode.revision(state)),"Remember selected step");
            }
        }finally{receiving=false;}
    }
    @Override public void close(){closed=true;}
}
