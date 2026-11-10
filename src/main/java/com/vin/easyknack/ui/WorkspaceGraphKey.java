package com.vin.easyknack.ui;

import java.util.*;
import java.util.function.Function;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.GLNodeGraphAsset;

/** Reuses semantic graph keys across scalar edits; immutable snapshots make unchanged assets O(1). */
final class WorkspaceGraphKey {
    private final Function<GLGraphNode,Object> signature;
    private final Structure visible=new Structure(),root=new Structure();
    private final Map<String,Structure> assets=new HashMap<>();
    private Object key;
    WorkspaceGraphKey(Function<GLGraphNode,Object> signature){this.signature=signature;}
    Object update(GLNodeGraphSnapshot current,GLNodeGraphSnapshot project,Collection<GLNodeGraphAsset> embedded){
        Object currentKey=visible.update(current),rootKey=current==project?currentKey:root.update(project);
        var assetKeys=new ArrayList<Object>();var ids=new HashSet<String>();
        for(var asset:embedded){ids.add(asset.id());assetKeys.add(List.of(asset.id(),assets.computeIfAbsent(asset.id(),id->new Structure()).update(asset.graph()),
            asset.compositeDescriptor()==null?"":asset.compositeDescriptor(),asset.metadata()));}
        assets.keySet().retainAll(ids);
        var next=List.of(currentKey,rootKey,assetKeys);
        if(!next.equals(key))key=next;
        return key;
    }
    private record Entry(GLGraphNode model,Object signature){}
    private final class Structure {
        private GLNodeGraphSnapshot previous;
        private Map<GLNodeId,Entry> entries=new HashMap<>();
        private Object token=new Object();
        Object update(GLNodeGraphSnapshot snapshot){
            if(snapshot==previous)return token;
            boolean changed=previous==null||!snapshot.graphId().equals(previous.graphId())||
                !snapshot.connections().equals(previous.connections())||snapshot.nodes().size()!=entries.size();
            int index=0;
            for(var node:snapshot.nodes()){
                var old=entries.get(node.id());
                var entry=old!=null&&old.model().equals(node)?old:new Entry(node,signature.apply(node));
                if(old==null||!old.signature().equals(entry.signature()))changed=true;
                if(previous!=null&&index<previous.nodes().size()&&!previous.nodes().get(index).id().equals(node.id()))changed=true;
                if(entry!=old)entries.put(node.id(),entry);index++;
            }
            if(entries.size()!=snapshot.nodes().size()){
                var ids=new HashSet<GLNodeId>();for(var node:snapshot.nodes())ids.add(node.id());
                entries.keySet().retainAll(ids);
            }
            previous=snapshot;
            if(changed)token=new Object();
            return token;
        }
    }
}
