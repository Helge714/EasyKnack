package com.vin.easyknack.formula;

import java.util.*;
import com.vin.easyknack.nodes.ProcessingRate;

/** Scheduling is independent from the numeric/trigger type of individual ports. */
public record FormulaRatePlan(ProcessingRate execution,Map<String,ProcessingRate> inputs) {
    public FormulaRatePlan{inputs=Map.copyOf(inputs);}
    public static FormulaRatePlan resolve(FormulaCompiler.Program program,ProcessingRate requested,Map<String,ProcessingRate> connections){
        if(requested==ProcessingRate.EVENT)throw new IllegalArgumentException("Trigger is a port type, not a formula execution rate");
        boolean sample=false;var inputs=new LinkedHashMap<String,ProcessingRate>();
        for(var p:program.ports()){
            if(p.output()){if(p.type()==FormulaCompiler.Type.BOOLEAN)sample=true;continue;}
            ProcessingRate rate=connections.getOrDefault(p.id(),p.type()==FormulaCompiler.Type.BOOLEAN?ProcessingRate.EVENT:ProcessingRate.CONTROL);
            if(rate==ProcessingRate.AUTO)throw new IllegalArgumentException("Resolve the connected input rate first: "+p.name());
            if((p.type()==FormulaCompiler.Type.BOOLEAN)!=(rate==ProcessingRate.EVENT))throw new IllegalArgumentException("Incompatible signal type: "+p.name());
            inputs.put(p.id(),rate);
            if(program.usedInputs().contains(p.id())&&(rate==ProcessingRate.EVENT||rate==ProcessingRate.AUDIO))sample=true;
        }
        if(requested==ProcessingRate.CONTROL&&sample)throw new IllegalArgumentException("Control execution would lose samples or triggers; use an explicit rate converter");
        return new FormulaRatePlan(requested==ProcessingRate.AUTO?(sample||program.sampleContext()?ProcessingRate.AUDIO:ProcessingRate.CONTROL):requested,inputs);
    }
}
