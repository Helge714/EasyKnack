package com.vin.easyknack.ui;

import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;
import java.util.*;
import java.util.function.Consumer;

/** Same mapping controls are used by the node inspector and driver popup. */
public final class MidiDriverSettingsPanel extends GLContainer {
    private MidiDriverBinding binding;
    private boolean updating;
    private final GLDriverTarget target;
    private final Consumer<GLNodeValue> changed;
    private final List<GLContainer> rows=new ArrayList<>();
    private final GLLabel source=new GLLabel("");
    private final GLNumberSpinner minimum=new GLNumberSpinner(0),maximum=new GLNumberSpinner(1),threshold=new GLNumberSpinner(1,127,64);
    private final GLLabel minLabel=new GLLabel(AppText.text("driver.minimum")),maxLabel=new GLLabel(AppText.text("driver.maximum")),thresholdLabel=new GLLabel(AppText.text("driver.threshold"));
    private final GLCheckBox reverse=new GLCheckBox(AppText.text("driver.reverse")),log=new GLCheckBox(AppText.text("driver.log")),pickup=new GLCheckBox(AppText.text("driver.pickup")),hold=new GLCheckBox(AppText.text("driver.hold"));
    private record Mode(String id){public String toString(){return AppText.text("control.mode."+id);}}
    private final GLComboBox<Mode> interpolation=new GLComboBox<>(List.of(new Mode("inherit"),new Mode("linear"),new Mode("off")));
    private final GLNumberSpinner interpolationMin=new GLNumberSpinner(0,60000,5),interpolationMax=new GLNumberSpinner(0,60000,50);
    private final GLCheckBox buffered=new GLCheckBox(AppText.text("control.buffered")),smoothing=new GLCheckBox(AppText.text("control.smoothing"));
    private final GLNumberSpinner bufferMs=new GLNumberSpinner(0,1000,50),smoothMs=new GLNumberSpinner(0,100,5);
    public MidiDriverSettingsPanel(MidiDriverBinding binding,GLDriverTarget target,Consumer<GLNodeValue> changed){this(binding,target,changed,true);}
    public MidiDriverSettingsPanel(MidiDriverBinding binding,GLDriverTarget target,Consumer<GLNodeValue> changed,boolean continuous){
        this.binding=binding;this.target=target;this.changed=changed;
        rows.add(source);boolean numeric=target!=null&&target.kind()==GLDriverTarget.Kind.NUMBER;
        if(numeric){rows.addAll(List.of(minLabel,minimum,maxLabel,maximum,reverse,log,pickup));}
        if(numeric&&continuous){rows.addAll(List.of(new GLLabel(AppText.text("control.interpolation")),interpolation,new GLLabel(AppText.text("control.minimum")),interpolationMin,new GLLabel(AppText.text("control.maximum")),interpolationMax,buffered,bufferMs,smoothing,smoothMs));}
        bufferMs.setUnit("ms");smoothMs.setUnit("ms");
        buffered.setToolTipText(AppText.text("control.buffered.hint"));smoothing.setToolTipText(AppText.text("control.smoothing.hint"));
        buffered.addActionListener(e->commit(false));smoothing.addActionListener(e->commit(false));bufferMs.addChangeListener(e->commit(false));smoothMs.addChangeListener(e->commit(false));
        interpolationMin.setUnit("ms");interpolationMax.setUnit("ms");
        interpolation.addActionListener(e->commit(false));interpolationMin.addChangeListener(e->{if(!updating&&interpolationMin.getValue()>interpolationMax.getValue()){updating=true;interpolationMax.setValue(interpolationMin.getValue());updating=false;}commit(false);});
        interpolationMax.addChangeListener(e->{if(!updating&&interpolationMax.getValue()<interpolationMin.getValue()){updating=true;interpolationMin.setValue(interpolationMax.getValue());updating=false;}commit(false);});
        if(target!=null&&target.kind()==GLDriverTarget.Kind.TOGGLE&&binding!=null&&!binding.cc())rows.add(hold);
        if(binding!=null&&binding.cc()&&target!=null&&(target.kind()==GLDriverTarget.Kind.TOGGLE||target.kind()==GLDriverTarget.Kind.ACTION))rows.addAll(List.of(thresholdLabel,threshold));
        rows.forEach(this::addGL);minimum.setRange(-Double.MAX_VALUE,Double.MAX_VALUE);maximum.setRange(-Double.MAX_VALUE,Double.MAX_VALUE);threshold.setIntegerValuesOnly(true);
        minimum.addChangeListener(e->commit(true));maximum.addChangeListener(e->commit(false));threshold.addChangeListener(e->commit(false));
        for(var box:List.of(reverse,log,pickup,hold))box.addActionListener(e->commit(false));
        setPreferredGLSize(260,rows.size()*30+12);update(binding);
    }
    public void update(MidiDriverBinding value){
        updating=true;try{binding=value;if(value==null)return;source.setText(value.description());source.setToolTipText(value.description());
            minimum.setValue(value.mapping().minimum());maximum.setValue(value.mapping().maximum());reverse.setSelected(value.mapping().reversed());
            log.setSelected(value.mapping().logarithmic());pickup.setSelected(value.mapping().pickup());hold.setSelected(value.hold());threshold.setValue(value.threshold());
            interpolation.setSelectedItem(new Mode(value.interpolation()==null?"inherit":value.interpolation().enabled()?"linear":"off"));
            interpolationMin.setValue(value.interpolation()==null?5:value.interpolation().minimumMs());interpolationMax.setValue(value.interpolation()==null?50:value.interpolation().maximumMs());
            interpolationMin.setEnabled(value.interpolation()!=null&&value.interpolation().enabled());interpolationMax.setEnabled(value.interpolation()!=null&&value.interpolation().enabled());
            buffered.setSelected(value.interpolation()==null||value.interpolation().buffered());smoothing.setSelected(value.interpolation()==null||value.interpolation().smoothing());
            bufferMs.setValue(value.interpolation()==null?50:value.interpolation().bufferMs());smoothMs.setValue(value.interpolation()==null?5:value.interpolation().smoothingMs());
            boolean custom=value.interpolation()!=null&&value.interpolation().enabled();buffered.setEnabled(custom);smoothing.setEnabled(custom);bufferMs.setEnabled(custom&&buffered.isSelected());smoothMs.setEnabled(custom&&smoothing.isSelected());
            log.setEnabled(value.mapping().minimum()>0&&value.mapping().maximum()>0);
        }finally{updating=false;}
    }
    private void commit(boolean fromMin){
        if(updating||binding==null)return;
        double min=minimum.getValue(),max=maximum.getValue();
        if(target!=null&&target.kind()==GLDriverTarget.Kind.NUMBER){min=Math.max(target.minimum(),Math.min(target.maximum(),min));max=Math.max(target.minimum(),Math.min(target.maximum(),max));}
        if(min>max){if(fromMin)max=min;else min=max;}
        var value=new MidiDriverBinding(binding.device(),binding.name(),binding.channel(),binding.number(),binding.cc(),
            new GLDriverMapping(min,max,reverse.isSelected(),log.isSelected()&&min>0&&max>0,pickup.isSelected()),hold.isSelected(),(int)threshold.getValue(),interpolation.getSelectedItem().id().equals("inherit")?null:new ControlInterpolationSettings(interpolation.getSelectedItem().id().equals("linear"),interpolationMin.getValue(),interpolationMax.getValue(),buffered.isSelected(),bufferMs.getValue(),smoothing.isSelected(),smoothMs.getValue()));
        update(value);changed.accept(value.value());
    }
    @Override protected void resized(float w,float h){float y=6;for(var row:rows){row.setBounds(6,y,Math.max(0,w-12),26);y+=30;}}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
}
