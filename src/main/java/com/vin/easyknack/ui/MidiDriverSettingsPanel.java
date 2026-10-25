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
    public MidiDriverSettingsPanel(MidiDriverBinding binding,GLDriverTarget target,Consumer<GLNodeValue> changed){
        this.binding=binding;this.target=target;this.changed=changed;
        rows.add(source);boolean numeric=target!=null&&target.kind()==GLDriverTarget.Kind.NUMBER;
        if(numeric){rows.addAll(List.of(minLabel,minimum,maxLabel,maximum,reverse,log,pickup));}
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
            log.setEnabled(value.mapping().minimum()>0&&value.mapping().maximum()>0);
        }finally{updating=false;}
    }
    private void commit(boolean fromMin){
        if(updating||binding==null)return;
        double min=minimum.getValue(),max=maximum.getValue();
        if(target!=null&&target.kind()==GLDriverTarget.Kind.NUMBER){min=Math.max(target.minimum(),Math.min(target.maximum(),min));max=Math.max(target.minimum(),Math.min(target.maximum(),max));}
        if(min>max){if(fromMin)max=min;else min=max;}
        var value=new MidiDriverBinding(binding.device(),binding.name(),binding.channel(),binding.number(),binding.cc(),
            new GLDriverMapping(min,max,reverse.isSelected(),log.isSelected()&&min>0&&max>0,pickup.isSelected()),hold.isSelected(),(int)threshold.getValue());
        update(value);changed.accept(value.value());
    }
    @Override protected void resized(float w,float h){float y=6;for(var row:rows){row.setBounds(6,y,Math.max(0,w-12),26);y+=30;}}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
}
