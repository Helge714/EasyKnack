package com.vin.easyknack.compiler;
import com.vin.easyknack.i18n.AppText;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** First conservative, closed stateful chain. All other shapes retain the scheduler. */
public final class StatefulChainSpecialization {
    public static boolean supports(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,int inputs,int outputs){
        if(inputs!=0||outputs!=1||nodes.size()!=3)return false;
        if(!(NodeRegistry.require(nodes.get(0).typeId()) instanceof OscillatorNode)
                ||!(NodeRegistry.require(nodes.get(1).typeId()) instanceof MathNode)
                ||!(NodeRegistry.require(nodes.get(2).typeId()) instanceof FilterNode))return false;
        var gain=new NodeState(nodes.get(1).properties());
        if(!gain.text("operation","multiply").equals("multiply")||gain.flag("clamp",false))return false;
        for(var entry:sources.entrySet()){
            var target=entry.getKey();var source=entry.getValue();
            if(target.nodeId().equals(nodes.get(0).id()))return false;
            if(target.nodeId().equals(nodes.get(1).id())&&!(target.portId().equals("a")&&source.equals(new GLNodeEndpoint(nodes.get(0).id(),"audio-out"))))return false;
            if(target.nodeId().equals(nodes.get(2).id())&&!(target.portId().equals("audio-in")&&source.equals(new GLNodeEndpoint(nodes.get(1).id(),"audio-out"))))return false;
        }
        return new GLNodeEndpoint(nodes.get(0).id(),"audio-out").equals(sources.get(new GLNodeEndpoint(nodes.get(1).id(),"a")))
            &&new GLNodeEndpoint(nodes.get(1).id(),"audio-out").equals(sources.get(new GLNodeEndpoint(nodes.get(2).id(),"audio-in")));
    }
    public static String code(){return """
        bool renderSpecializedChain(int frames)noexcept{
            auto& g=runtime->graph;
            for(int i=0;i<4;i++){auto& n=g.nodes[i];if(!n.active||n.stereo||n.voiceCount()!=1||n.controlRate||n.eventRate)return false;}
            auto& o=static_cast<OscillatorNode&>(*g.nodes[0].processor);
            auto& m=static_cast<MathNode&>(*g.nodes[1].processor);
            auto& f=static_cast<FilterNode&>(*g.nodes[2].processor);
            if(m.operation!=MathOperation::multiply||m.clamp)return false;
            // The output boundary is the only consumer; write there directly.
            renderOscGainFilter<true>(o,m,f,g.nodes[0].channelCache[0].outputs[0].data(),
                g.nodes[1].channelCache[0].outputs[0].data(),g.nodes[3].channelCache[0].outputs[0].data(),frames,sampleRate);
            return true;
        }
        """;}
}
