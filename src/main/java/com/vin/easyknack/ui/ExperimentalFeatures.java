package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.NodeRegistry;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Discovery policy only: loading existing experimental graphs never removes their nodes. */
final class ExperimentalFeatures {
    private ExperimentalFeatures(){}
    static boolean requiresOptIn(String type,Collection<GLNodeGraphAsset> assets){
        return requiresOptIn(List.of(type),assets);
    }
    static boolean requiresOptIn(GLNodeGraphDocument document){
        return requiresOptIn(document.root().graph().nodes().stream().map(GLGraphNode::typeId).toList(),document.embeddedAssets().values());
    }
    private static boolean requiresOptIn(Collection<String> roots,Collection<GLNodeGraphAsset> assets){
        var definitions=new HashMap<String,GLNodeGraphAsset>();
        for(var asset:assets)if(asset.compositeDescriptor()!=null)definitions.put(asset.compositeDescriptor().nodeTypeId(),asset);
        var pending=new ArrayDeque<>(roots);var seen=new HashSet<String>();
        while(!pending.isEmpty()){
            var type=pending.removeFirst();if(!seen.add(type))continue;
            if(NodeRegistry.find(type).map(d->d.isExperimental()).orElse(false))return true;
            var asset=definitions.get(type);if(asset!=null)asset.graph().nodes().forEach(n->pending.addLast(n.typeId()));
        }
        return false;
    }
    static String label(String title){return title+" ["+AppText.text("experimental")+"]";}
}
