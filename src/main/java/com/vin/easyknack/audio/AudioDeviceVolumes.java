package com.vin.easyknack.audio;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Live Windows state, deliberately excluded from document persistence and undo. */
public final class AudioDeviceVolumes {
    public record Value(double scalar, boolean muted, double peak) { }
    private static Map<String,Value> current=Map.of();
    private static final List<WeakReference<Runnable>> listeners=new ArrayList<>();
    private static final ConcurrentHashMap<String,Double> pending=new ConcurrentHashMap<>();
    private AudioDeviceVolumes() { }
    public static Value get(String id) { return current.get(id); }
    public static void observe(Runnable listener) { listeners.add(new WeakReference<>(listener)); }
    public static void request(String id,double scalar) {
        if(!Double.isFinite(scalar) || scalar<0 || scalar>1) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid device volume"));
        pending.put(id,scalar);
    }
    static Map<String,Double> drain() {
        var result=new HashMap<String,Double>();
        pending.forEach((id,value)-> { if(pending.remove(id,value)) result.put(id,value); });
        return result;
    }
    public static Map<String,Value> parse(String response) {
        String[] tokens=response.split(" ");
        if(tokens.length<2 || !tokens[0].equals("OK") || !tokens[1].matches("[01]") || (tokens.length-2)%4!=0)
            throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid volume snapshot"));
        var result=new HashMap<String,Value>();
        for(int i=2;i<tokens.length;i+=4) {
            double scalar=Double.parseDouble(tokens[i+1]);
            double peak=Double.parseDouble(tokens[i+3]);
            if(!Double.isFinite(peak) || peak<0 || peak>1) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid peak"));
            if(!Double.isFinite(scalar) || scalar<0 || scalar>1 || !tokens[i+2].matches("[01]")) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid volume"));
            if(result.put(AudioEndpoint.unhex(tokens[i]),new Value(scalar,tokens[i+2].equals("1"),peak))!=null) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Duplicate device"));
        }
        return Map.copyOf(result);
    }
    public static void publish(Map<String,Value> values) {
        if(current.equals(values)) return;
        current=Map.copyOf(values);
        listeners.removeIf(ref->ref.get()==null);
        for(var ref:List.copyOf(listeners)) { var listener=ref.get(); if(listener!=null) listener.run(); }
    }
}
