package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLSlider;
import java.util.function.Consumer;

/** Runtime control; the model stores only the editor placeholder, never an OS volume. */
public record DeviceVolumeEditor(String endpoint) implements Editor {
    @Override public GLNodeValue defaultValue() { return GLNodeValue.of(false); }
    @Override public String viewKey() { return "device-volume:"+endpoint; }
    @Override public Control create(String label, Consumer<GLNodeValue> ignored) {
        class Volume extends GLSlider {
            private boolean updating;
            private final Runnable listener=this::refresh;
            Volume() {
                super(label,0,100,0); setPresentation(Presentation.BAR); setStep(1); setIntegerValuesOnly(true);
                setUnit("%");
                setGLHistoryManager(new com.vingame.glswing.history.GLHistoryManager());
                addChangeListener(event->{ if(!updating) AudioDeviceVolumes.request(endpoint,getValue()/100); });
                AudioDeviceVolumes.observe(listener); refresh();
            }
            void refresh() {
                updating=true;
                try {
                    var value=AudioDeviceVolumes.get(endpoint);
                    setEnabled(value!=null);
                    setText(value==null?NodeText.text("volume.unavailable","Windows · unavailable"):value.muted()?NodeText.text("volume.muted","Windows · muted"):label);
                    if(value!=null) setValue(Math.round(value.scalar()*100));
                } finally { updating=false; }
            }
        }
        var control=new Volume(); return new Control(control,value->control.refresh());
    }
}



