package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.MidiDevices;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLComboBox;
import java.util.*;
import java.util.function.Consumer;

public final class MidiDeviceEditor implements Editor {
    @Override public GLNodeValue defaultValue(){return GLNodeValue.of(MidiDevices.KEYBOARD.reference());}
    @Override public void validate(GLNodeValue value){if(!(value instanceof GLNodeValue.TextValue t))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("MIDI device reference required"));MidiDevices.Device.reference(t.value());}
    @Override public Control create(String label,Consumer<GLNodeValue> changed){
        class Chooser extends GLComboBox<MidiDevices.Device>{
            private MidiDevices.Device selected=MidiDevices.KEYBOARD;
            private boolean refreshing;
            private final Runnable observer=this::refresh;
            Chooser(){super(List.of(MidiDevices.KEYBOARD));setPopupTitle(label);setPopupMatchesComboWidth(false);MidiDevices.observe(observer);
                addActionListener(e->{if(!refreshing&&getSelectedItem()!=null){selected=getSelectedItem();changed.accept(GLNodeValue.of(selected.reference()));}});refresh();}
            void refresh(){refreshing=true;try{
                var choices=new ArrayList<>(MidiDevices.current());var choice=choices.stream().filter(d->d.id().equals(selected.id())).findFirst().orElse(null);
                if(choice==null){choice=MidiDevices.Device.reference(selected.reference());choices.add(choice);}
                removeAllItems();choices.forEach(this::addItem);setSelectedItem(choice);
            }finally{refreshing=false;}}
            void update(GLNodeValue value){selected=MidiDevices.Device.reference(((GLNodeValue.TextValue)value).value());refresh();}
        }
        var chooser=new Chooser();return new Control(chooser,chooser::update);
    }
}

