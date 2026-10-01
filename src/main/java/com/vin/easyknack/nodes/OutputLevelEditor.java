package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.AudioDeviceVolumes;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLLabel;
import java.util.function.Consumer;

public record OutputLevelEditor(String endpoint) implements Editor {
    @Override public GLNodeValue defaultValue() { return GLNodeValue.of(false); }
    @Override public String viewKey() { return "output-level:"+endpoint; }
    @Override public Control create(String label,Consumer<GLNodeValue> ignored) {
        class Level extends GLLabel {
            private final Runnable listener=this::refresh;
            Level() { super(label); AudioDeviceVolumes.observe(listener); refresh(); }
            void refresh() {
                var value=AudioDeviceVolumes.get(endpoint);
                setText(value==null?"Signal: --":value.peak()<0.000001?"Signal: silent":
                        String.format(java.util.Locale.ROOT,"Signal: %.1f dBFS",20*Math.log10(value.peak())));
            }
        }
        var control=new Level(); return new Control(control,value->control.refresh());
    }
}

