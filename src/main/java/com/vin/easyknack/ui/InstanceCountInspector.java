package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.GLContainer;

/** Execution properties are separate from the user-authored group interface. */
final class InstanceCountInspector {
    private static final class Control extends GLContainer {
        private boolean updating;private final String key;
        private final Editor.Control binding;
        Control(String key,GLGraphNode node,java.util.function.Consumer<GLNodeValue> changed){
            this.key=key;
            binding=Editor.Slider.button(8,0.0,256.0).integers().create("Count",v->{if(!updating)changed.accept(v);});
            addGL(binding.component());setPreferredGLSize(180,30);sync(node);
        }
        void sync(GLGraphNode node){updating=true;try{binding.update().accept(node.properties().getOrDefault(key,node.properties().getOrDefault(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(8.))));}finally{updating=false;}}
        @Override protected void resized(float w,float h){binding.component().setBounds(0,0,w,h);}
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
    }
    static void register(GLNodePropertySchemaRegistry schemas,GLCompositeGraphRegistry registry){
        for(var asset:registry.assets())if(MultipleGroups.multiple(asset)&&asset.compositeDescriptor()!=null){
            String type=asset.compositeDescriptor().nodeTypeId();var properties=new ArrayList<>(schemas.properties(type));
            String key=asset.compositeDescriptor().portMappings().stream().filter(MultipleGroups::countPort).map(p->GroupUnits.key(p.publicPort().id())).findFirst().orElse(MultipleGroups.COUNT_PROPERTY);
            if(properties.stream().anyMatch(p->p.id().equals(key)&&p instanceof GLCustomNodeProperty))continue;
            properties.removeIf(p->p.id().equals(key));
            properties.add(new GLCustomNodeProperty(key,"Count",(node,changed)->new Control(key,node,changed),(control,node)->((Control)control).sync(node)));
            schemas.register(type,properties);
        }
    }
}
