package com.vin.easyknack.audio;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Runtime MIDI discovery. Saved references retain names when a device is unplugged. */
public final class MidiDevices {
    public record Device(String id,String name,boolean available){
        public Device {
            if(id==null||!id.matches("[0-9]{1,20}")||name==null||name.length()>4096)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid MIDI device"));
            Long.parseUnsignedLong(id);
        }
        public String reference(){return id+":"+AudioEndpoint.hex(name);}
        public static Device reference(String value){String[] fields=value.split(":",-1);if(fields.length!=2)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid MIDI reference"));return new Device(fields[0],AudioEndpoint.unhex(fields[1]),fields[0].equals("0"));}
        @Override public String toString(){return name+(available?"":" (unavailable)");}
    }
    public static final Device NONE=new Device("0","No device",true);
    public static final Device KEYBOARD=new Device("1","Computer keyboard",true);
    private static List<Device> current=List.of(KEYBOARD,NONE);
    private static final ArrayDeque<Integer> keyboardEvents=new ArrayDeque<>();
    private static volatile Runnable keyboardWakeup=()->{};
    static void setKeyboardWakeup(Runnable callback){keyboardWakeup=callback;}
    public static void keyboardEvent(int message){synchronized(keyboardEvents){if(keyboardEvents.size()>=256){keyboardEvents.clear();keyboardEvents.add(255);}else keyboardEvents.add(message);}keyboardWakeup.run();}
    public static List<Integer> drainKeyboard(){synchronized(keyboardEvents){var events=List.copyOf(keyboardEvents);keyboardEvents.clear();return events;}}
    private static final List<WeakReference<Runnable>> listeners=new ArrayList<>();
    private static final AtomicBoolean panic=new AtomicBoolean();
    private MidiDevices(){}
    public static List<Device> current(){return current;}
    public static void observe(Runnable callback){listeners.add(new WeakReference<>(callback));}
    public static void requestPanic(){panic.set(true);}
    static boolean takePanic(){return panic.getAndSet(false);}
    public static void publish(List<Device> devices){
        devices=List.copyOf(devices);if(current.equals(devices))return;current=devices;
        listeners.removeIf(r->r.get()==null);for(var ref:List.copyOf(listeners)){var callback=ref.get();if(callback!=null)callback.run();}
    }
    public static List<Device> parse(String response){
        String[] tokens=response.split(" ");if(tokens.length<2||!tokens[0].equals("OK"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid MIDI response"));
        int count=Integer.parseInt(tokens[1]);if(count<0||count>4096||tokens.length!=2+2*count)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid MIDI device count"));
        var devices=new ArrayList<Device>();devices.add(KEYBOARD);devices.add(NONE);var ids=new HashSet<String>();ids.add("0");ids.add("1");
        for(int i=0;i<count;i++){var device=new Device(tokens[2+2*i],AudioEndpoint.unhex(tokens[3+2*i]),true);if(!ids.add(device.id()))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Duplicate MIDI device"));devices.add(device);}
        return List.copyOf(devices);
    }
    static void unavailable(){drainKeyboard();publish(current.stream().map(d->new Device(d.id(),d.name(),d.id().equals("0")||d.id().equals("1"))).toList());}
}
