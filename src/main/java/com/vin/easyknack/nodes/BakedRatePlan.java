package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;

/** Generated topological rate plan, evaluated only when graph topology/rate settings change. */
public record BakedRatePlan(int inputCount,List<Step> steps,List<Integer> outputs) {
    public record Step(BasicNode definition,Map<String,GLNodeValue> properties,List<Integer> sources) {
        public Step { properties=Map.copyOf(properties);sources=List.copyOf(sources); }
    }
    public BakedRatePlan {steps=List.copyOf(steps);outputs=List.copyOf(outputs);}
    public ProcessingRate resolve(Map<PropertyId,ProcessingRate> incoming) {
        return resolveOutputs(incoming).stream().reduce(ProcessingRate.CONTROL,ProcessingRate::maximum);
    }
    public List<ProcessingRate> resolveOutputs(Map<PropertyId,ProcessingRate> incoming) {
        var rates=new ArrayList<List<ProcessingRate>>();
        for(int i=0;i<inputCount;i++)rates.add(List.of(incoming.getOrDefault(new PropertyId("input_"+i),ProcessingRate.CONTROL)));
        for(var step:steps)rates.add(step.definition.layout(new NodeState(step.properties)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::rate).toList());
        for(int pass=0;pass<=steps.size();pass++){
        boolean changed=false;int index=inputCount;
        for(var step:steps){
            var values=new HashMap<>(step.definition.defaults());values.putAll(step.properties);values.keySet().removeIf(key->key.equals(ProcessingRate.EFFECTIVE)||key.startsWith(ProcessingRate.EFFECTIVE+"."));
            var inputs=new HashMap<PropertyId,ProcessingRate>();var ports=step.definition.processor().inputs(new NodeState(values));
            for(int i=0;i<step.sources.size();i++)if(step.sources.get(i)>=0)inputs.put(ports.get(i),source(rates,step.sources.get(i)));
            values=new HashMap<>(AutomaticRates.resolveProperties(step.definition,values,inputs));
            var resolved=step.definition.layout(new NodeState(values)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::rate).toList();
            changed|=!resolved.equals(rates.get(index));rates.set(index++,resolved);
        }
        if(!changed)break;
        }
        return outputs.stream().map(ref->source(rates,ref)).toList();
    }
    private static ProcessingRate source(List<List<ProcessingRate>> rates,int ref){return ref<0?ProcessingRate.CONTROL:rates.get(ref%256).get(ref/256);}
}
