package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Persist public port IDs in the asset; execute through the Core's existing actions. */
final class GroupRouting {
    static final String INSERT_IN="easyknack.insert.input", INSERT_OUT="easyknack.insert.output",
            MUTE_IN="easyknack.mute.input", MUTE_OUT="easyknack.mute.output";
    static GLNodeInsertionPorts pair(GLNodeGraphAsset asset,GLNodeTypeRegistry types,String inKey,String outKey){
        var state=new NodeState(asset.metadata());var descriptor=asset.compositeDescriptor();
        if(descriptor==null)return null;
        if(!asset.metadata().containsKey(inKey)&&!asset.metadata().containsKey(outKey)){
            var pairs=new ArrayList<GLNodeInsertionPorts>();
            for(var a:descriptor.portMappings())if(a.publicPort().direction()==GLPortDirection.INPUT&&a.presentation().socketVisible())
                for(var b:descriptor.portMappings())if(b.publicPort().direction()==GLPortDirection.OUTPUT&&a.publicPort().valueTypeId().equals(b.publicPort().valueTypeId()))pairs.add(new GLNodeInsertionPorts(a.publicPort().id(),b.publicPort().id()));
            if(pairs.size()==1)return pairs.get(0);
            var audio=pairs.stream().filter(p->descriptor.nodeType().findPort(p.inputPortId()).orElseThrow().valueTypeId().equals(com.vin.easyknack.nodes.SignalCategory.AUDIO.valueTypeId())).toList();
            return audio.size()==1?audio.get(0):null;
        }
        var in=descriptor.nodeType().findPort(state.text(inKey,"")).orElse(null);
        var out=descriptor.nodeType().findPort(state.text(outKey,"")).orElse(null);
        if(in==null||out==null||in.direction()!=GLPortDirection.INPUT||out.direction()!=GLPortDirection.OUTPUT
                ||!types.compatibleValueTypes(in.valueTypeId(),out.valueTypeId()))return null;
        return new GLNodeInsertionPorts(in.id(),out.id());
    }
    static void synchronize(GLCompositeGraphRegistry groups,GLNodeTypeRegistry types){
        for(var asset:groups.assets()){
            var descriptor=asset.compositeDescriptor();if(descriptor==null||types.find(descriptor.nodeTypeId()).isEmpty())continue;
            var insert=pair(asset,types,INSERT_IN,INSERT_OUT);var mute=pair(asset,types,MUTE_IN,MUTE_OUT);
            types.setInsertionPorts(descriptor.nodeTypeId(),insert);
            types.setExclusionInput(descriptor.nodeTypeId(),insert==null?null:insert.inputPortId());
            types.setMuteBypasses(descriptor.nodeTypeId(),mute==null?List.of():List.of(new GLNodeMuteBypass(mute.inputPortId(),mute.outputPortId())));
        }
    }
}
