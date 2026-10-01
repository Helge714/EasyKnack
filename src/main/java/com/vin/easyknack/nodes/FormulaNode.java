package com.vin.easyknack.nodes;

import java.util.*;
import com.vin.easyknack.formula.*;

/** Private execution node materialized from a Formula composite's interface. */
@RegisterNode(id="easyknack.formula.kernel",title="Formula",category=NodeCategory.MATH,kind=NodeKind.VALUE)
public final class FormulaNode extends BasicNode {
    public static final int INPUTS=16,OUTPUTS=8;
    public static PropertyId input(int i){return new PropertyId("in"+i);}
    public static PropertyId output(int i){return new PropertyId("out"+i);}
    public static String hex(String text){return java.util.HexFormat.of().formatHex(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public boolean isListedInCatalog(){return false;}
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public static int count(NodeState s,String key,int fallback,int max){int n=(int)s.number(key,fallback);if(n<0||n>max)throw new IllegalArgumentException("Formula port limit exceeded");return n;}
    public void define(NodeBuilder n,NodeState s){
        int outputs=count(s,"outputs",1,OUTPUTS),inputs=count(s,"inputs",1,INPUTS),inMask=(int)s.number("triggerInputs",0),outMask=(int)s.number("triggerOutputs",0),buffers=(int)s.number("bufferInputs",0);
        for(int i=0;i<outputs;i++){var row=n.addOutput(output(i),"Output "+(i+1));if((outMask&(1<<i))!=0)row.signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT);else row.signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);}
        for(int i=0;i<INPUTS;i++){
            if((buffers&(1<<i))!=0){n.addInput(input(i),"Buffer "+(i+1)).dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL).visible(i<inputs);continue;}
            var row=(inMask&(1<<i))!=0?n.addInput(input(i),"Input "+(i+1),new Editor.Trigger()).signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT):n.addInput(input(i),"Input "+(i+1),Editor.Slider.button(0,null,null)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
            row.visible(i<inputs);
            if((inMask&(1<<i))==0){var unit=UnitFormat.fromId(s.text("unit.in"+i,"factor"));row.formats(new UnitFormats(unit,unit,List.of(unit)));}
        }
        ProcessingRate.addEditor(n,new PropertyId("rate"));
    }
    @Override public ProcessingRate resolveAutomaticRate(NodeState s,Map<PropertyId,ProcessingRate> inputs){
        if(s.flag("sampleContext",false))return ProcessingRate.AUDIO;
        if(s.number("triggerOutputs",0)!=0||((int)s.number("triggerInputs",0)&(int)s.number("usedInputs",65535))!=0)return ProcessingRate.AUDIO;
        int used=(int)s.number("usedInputs",65535);return inputs.entrySet().stream().filter(e->{int i=Integer.parseInt(e.getKey().value().substring(2));return (used&(1<<i))!=0;}).map(e->e.getValue()==ProcessingRate.EVENT?ProcessingRate.AUDIO:e.getValue()).reduce(ProcessingRate.CONTROL,ProcessingRate::maximum);
    }
    public static String payload(NodeState s){
        int ins=count(s,"inputs",1,INPUTS),outs=count(s,"outputs",1,OUTPUTS);
        String bytecode=s.text("bytecode",FormulaCompiler.compile("result=signal;",List.of(new FormulaCompiler.Port("in0","signal",FormulaCompiler.Type.NUMBER,false),new FormulaCompiler.Port("out0","result",FormulaCompiler.Type.NUMBER,true))).bytecode());
        var text=new StringBuilder("FN2 ").append(ins).append(' ').append(outs).append(' ').append((int)s.number("triggerInputs",0)).append(' ').append((int)s.number("triggerOutputs",0));
        for(int i=0;i<INPUTS;i++)text.append(' ').append(s.number(input(i),0));
        text.append(' ').append(hex(bytecode)).append(' ').append(s.text("library","-")).append(' ').append((int)s.number("usedInputs",65535)).append(' ').append((int)s.number("bufferInputs",0)).append(' ').append((long)s.number("seed",0));return hex(text.toString());
    }
    public NativeProcessor processor(){var b=ParameterContract.builder();for(int i=0;i<INPUTS;i++){final int index=i;b.source(input(i),s->((int)s.number("usedInputs",65535)&(1<<index))!=0);}return new NativeProcessor("formula","nodes/FormulaNode.hpp",b.mapped(new PropertyId("program"),FormulaNode::payload).build());}
}
