package com.vin.easyknack.audio;

import java.lang.ref.WeakReference;
import java.util.*;

/** Runtime discovery snapshot. Changes update controls, never document properties or history. */
public final class AudioEndpoints {
    public record Capabilities(boolean systemCapture, boolean applicationCapture, boolean applicationRouting,
            boolean virtualInputs, boolean virtualOutputs) { }
    public record Snapshot(Capabilities capabilities, List<AudioEndpoint> endpoints) {
        public Snapshot { endpoints = List.copyOf(endpoints); }
    }
    private static Snapshot current = new Snapshot(new Capabilities(false, false, false, false, false), List.of(AudioEndpoint.DEFAULT));
    private static final List<WeakReference<Runnable>> listeners = new ArrayList<>();
    private AudioEndpoints() { }
    public static Snapshot current() { return current; }
    public static void observe(Runnable listener) { listeners.add(new WeakReference<>(listener)); }
    public static void publish(Snapshot snapshot) {
        if (snapshot.equals(current)) return;
        current = snapshot;
        listeners.removeIf(ref -> ref.get() == null);
        for (var ref : List.copyOf(listeners)) { var listener = ref.get(); if (listener != null) listener.run(); }
    }
    public static Snapshot parse(String response) {
        String[] tokens = response.split(" ");
        if (tokens.length < 3 || !tokens[0].equals("OK") || !tokens[1].matches("[01]{5}")) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid endpoint response"));
        int count = Integer.parseInt(tokens[2]);
        if (count < 0 || count > 4096 || tokens.length != 3 + count * 6) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid endpoint count"));
        var endpoints = new ArrayList<AudioEndpoint>(); var identities = new HashSet<String>();
        for (int i=3; i<tokens.length; i+=6) {
            if (!tokens[i].matches("[io]") || !tokens[i+4].matches("[01]") || !tokens[i+5].matches("[01]")) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid endpoint flags"));
            int type = Integer.parseInt(tokens[i+1]);
            if (type < 0 || type >= AudioEndpoint.Type.values().length) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unknown endpoint type"));
            var endpoint = new AudioEndpoint(AudioEndpoint.unhex(tokens[i+2]), AudioEndpoint.unhex(tokens[i+3]),
                    AudioEndpoint.Type.values()[type], tokens[i].equals("i"), tokens[i+4].equals("1"), tokens[i+5].equals("1"));
            if (!identities.add(tokens[i] + endpoint.id())) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Duplicate endpoint"));
            endpoints.add(endpoint);
        }
        String flags = tokens[1];
        return new Snapshot(new Capabilities(flags.charAt(0)=='1', flags.charAt(1)=='1', flags.charAt(2)=='1', flags.charAt(3)=='1', flags.charAt(4)=='1'), endpoints);
    }
}
