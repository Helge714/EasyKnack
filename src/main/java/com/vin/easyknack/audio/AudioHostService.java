package com.vin.easyknack.audio;

import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/** Coalesces live graph updates; owns the child process and its control connection. */
public final class AudioHostService implements AutoCloseable {
    public enum State { STOPPED, STARTING, RUNNING, PAUSED, STOPPING, CLOSED }
    public record VolumeKeyEvent(int percent,int targets) { }
    private volatile State state=State.STOPPED;
    private volatile Consumer<State> stateListener=value->{ };
    private volatile Consumer<VolumeKeyEvent> volumeKeyListener=value->{ };
    private long lastVolumeKey;
    public State state() { return state; }
    public void setStateListener(Consumer<State> listener) { stateListener=listener; SwingUtilities.invokeLater(()->listener.accept(state)); }
    public void setVolumeKeyListener(Consumer<VolumeKeyEvent> listener) { volumeKeyListener=listener; }
    private void state(State value) {
        if(closed && value!=State.CLOSED) return;
        if(state==value) return;
        state=value;
        SwingUtilities.invokeLater(()->{
            if(state!=State.RUNNING)AudioAnalysis.clear();
            stateListener.accept(state);
        });
    }
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "EasyKnack audio control"); thread.setDaemon(true); return thread;
    });
    private volatile boolean wanted;
    private volatile boolean closed;
    private record GraphUpdate(String command, String error, java.nio.file.Path executable) { }
    private java.nio.file.Path openedExecutable;
    private volatile GraphUpdate graph = new GraphUpdate("GRAPH 0", null, null);
    private GraphUpdate rejectedGraph;
    private final boolean nullDevice;
    private volatile Consumer<String> listener = text -> { };
    private volatile Consumer<String> loadListener = text -> { };
    private volatile Consumer<AudioDiagnostics> diagnosticsListener=value->{};
    private boolean diagnosticsUnavailable;
    public void setDiagnosticsListener(Consumer<AudioDiagnostics> listener){diagnosticsListener=listener;}
    public void resetDiagnostics(){worker.execute(()->{if(client!=null)try{client.command("DIAGNOSTICS_RESET");}catch(java.io.IOException e){status(com.vin.easyknack.ui.AppText.literal("Diagnostics reset failed: ")+e.getMessage());}});}
    private AudioHostClient client;
    private boolean playing;
    private boolean retainedGraph;
    public void trigger(String nodeId){
        if(closed||!wanted)return;
        if(!nodeId.matches("[A-Za-z0-9_.:/-]+"))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid trigger node ID"));
        worker.execute(()->{
            if(!closed&&wanted&&graph.error()==null&&!java.util.Objects.equals(graph.command(),sent))tick();
            if(!closed&&wanted&&playing&&client!=null)try{client.command("TRIGGER "+nodeId);}
            catch(java.io.IOException failure){status(com.vin.easyknack.ui.AppText.literal("Trigger failed: ")+failure.getMessage());}
        });
    }
    private String sent;
    private volatile boolean discoveryRequested = true;
    private long nextDiscovery, nextMeters, nextVolumes, nextAnalysis;
    private String routingStatus = "";
    private volatile String volumeLinks="VOLUME_LINKS";
    private String sentVolumeLinks;
    private boolean volumeKeysUnavailable;
    public void setVolumeLinks(java.util.Set<String> ids) {
        volumeLinks="VOLUME_LINKS"+ids.stream().sorted().map(id->" "+AudioEndpoint.hex(id)).collect(java.util.stream.Collectors.joining());
    }

    public AudioHostService() { this(true); }
    AudioHostService(boolean poll) { this(poll, false); }
    AudioHostService(boolean poll, boolean nullDevice) {
        this.nullDevice = nullDevice;
        MidiDevices.setKeyboardWakeup(()->{if(!closed)try{worker.execute(()->{try{sendKeyboard();}catch(Exception failure){status(com.vin.easyknack.ui.AppText.literal("MIDI keyboard: ")+failure.getMessage());MidiDevices.requestPanic();}});}catch(RejectedExecutionException ignored){}});
        if(poll) worker.scheduleWithFixedDelay(this::tick, 0, 20, TimeUnit.MILLISECONDS);
    }
    private void sendKeyboard() throws java.io.IOException {
        var events=MidiDevices.drainKeyboard();
        if(client!=null&&playing&&!events.isEmpty())client.command("MIDI_KEYBOARD "+events.size()+events.stream().map(e->" "+e).collect(java.util.stream.Collectors.joining()));
    }
    public void setStatusListener(Consumer<String> listener) { this.listener = listener; }
    public void setLoadListener(Consumer<String> listener) { loadListener = listener; }
    private void loadStatus(String text) { SwingUtilities.invokeLater(() -> { if (!closed) loadListener.accept(text); }); }
    public void update(String graph) { update(graph,null); }
    public void update(String graph,java.nio.file.Path executable) { this.graph=new GraphUpdate(graph,null,executable); }
    public synchronized void start() {
        if(closed || state!=State.STOPPED) return;
        wanted=true; state(State.STARTING); status(graph.error()==null?com.vin.easyknack.ui.AppText.literal("Audio startet ..."):"Audio pausiert: "+graph.error());
    }
    public synchronized void stop() {
        if(closed || !wanted) return;
        wanted=false; state(State.STOPPING);
    }
    public void refreshEndpoints() { discoveryRequested = true; }
    /** Reopen the matched plugin host on the control worker, preserving the requested play state. */
    public void reloadHost() {
        worker.execute(()->{
            if(closed)return;
            if(client!=null)client.close();client=null;playing=false;sent=null;sentVolumeLinks=null;
            rejectedGraph=null;discoveryRequested=true;lastVolumeKey=0;
        });
    }
    public void invalidGraph(String message) { graph = new GraphUpdate(null, message,graph.executable()); status(com.vin.easyknack.ui.AppText.literal("Audio pausiert: ") + message); }
    private void status(String text) { SwingUtilities.invokeLater(() -> { if (!closed) listener.accept(text); }); }

    void tick() {
        if (closed) return;
        try {
            GraphUpdate latest=graph;
            var executable=latest.executable()==null?AudioHostClient.defaultExecutable():latest.executable();
            if(client!=null&&!java.util.Objects.equals(openedExecutable,executable)){
                client.close();client=null;playing=false;sent=null;sentVolumeLinks=null;
                rejectedGraph=null;discoveryRequested=true;lastVolumeKey=0;
            }
            if (client == null && (wanted || discoveryRequested)) {
                discoveryRequested = false;
                client = new AudioHostClient();
                client.start(executable, nullDevice);openedExecutable=executable;
                diagnosticsUnavailable=false;
                SwingUtilities.invokeLater(()->diagnosticsListener.accept(null));
                nextDiscovery = 0;
            }
            if (client == null) return;
            if(MidiDevices.takePanic())client.command("MIDI_PANIC");
            sendKeyboard();
            if (latest.error() == null && latest != rejectedGraph && !latest.command().equals(sent)) {
                try { client.command(latest.command()); sent = latest.command(); rejectedGraph = null; retainedGraph=false; }
                catch (AudioHostClient.CommandRejectedException failure) {
                    // File-buffer load failures retain the previous graph and playback.
                    retainedGraph=failure.getMessage().contains("FILE_BUFFER_LOAD:") && sent!=null;
                    if(!retainedGraph){playing = false; sent = null;} rejectedGraph = latest;
                    status((retainedGraph?com.vin.easyknack.ui.AppText.text("buffer.previousGraph"):com.vin.easyknack.ui.AppText.literal("Audio pausiert: ")) + failure.getMessage());
                }
            }
            boolean valid = latest.error() == null && (latest != rejectedGraph || retainedGraph);
            boolean next = wanted && valid;
            if (playing != next) {
                client.command(next ? "PLAY" : "STOP"); playing = next;
                status(next ? com.vin.easyknack.ui.AppText.literal("Audio aktiv · 48 kHz · Stereo") : valid ? com.vin.easyknack.ui.AppText.literal("Audio gestoppt") : "Audio pausiert: " + latest.error());
            }
            state(wanted && !valid ? State.PAUSED : playing ? State.RUNNING : State.STOPPED);

            if(!volumeLinks.equals(sentVolumeLinks)) { client.command(volumeLinks); sentVolumeLinks=volumeLinks; }
            for(var request:AudioDeviceVolumes.drain().entrySet()) {
                try { client.command("VOLUME "+AudioEndpoint.hex(request.getKey())+" "+request.getValue()); }
                catch(java.io.IOException failure) { status(com.vin.easyknack.ui.AppText.literal("Gerätelautstärke: ")+failure.getMessage()); }
            }
            if (System.nanoTime() >= nextVolumes) {
            nextVolumes = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(100);
            String volumeResponse=client.command("VOLUMES");
            var volumes=AudioDeviceVolumes.parse(volumeResponse);
            String[] key=client.command("VOLUME_KEY_EVENT").split(" ");
            if(key.length!=4 || !key[0].equals("OK")) throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid volume key event"));
            long sequence=Long.parseLong(key[1]);
            if(sequence!=lastVolumeKey) {
                lastVolumeKey=sequence;
                var event=new VolumeKeyEvent((int)Math.round(Double.parseDouble(key[2])*100),Integer.parseInt(key[3]));
                SwingUtilities.invokeLater(()->{ if(!closed) volumeKeyListener.accept(event); });
            }
            boolean unavailable=!volumeLinks.equals("VOLUME_LINKS") && volumeResponse.startsWith("OK 0");
            if(unavailable && !volumeKeysUnavailable) status(com.vin.easyknack.ui.AppText.literal("Lautstärketasten: Kopplung oder ausgewähltes Windows-Gerät nicht verfügbar"));
            volumeKeysUnavailable=unavailable;
            SwingUtilities.invokeLater(()->{ if(!closed) AudioDeviceVolumes.publish(volumes); });
            }
            if (discoveryRequested || System.nanoTime() >= nextDiscovery) {
                discoveryRequested = false;
                var endpoints = AudioEndpoints.parse(client.command("ENDPOINTS"));
                var midiDevices = MidiDevices.parse(client.command("MIDI_INPUTS"));
                SwingUtilities.invokeLater(() -> {if(!closed)MidiDevices.publish(midiDevices);});
                SwingUtilities.invokeLater(() -> { if (!closed) AudioEndpoints.publish(endpoints); });
                nextDiscovery = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            }
            if(playing && System.nanoTime()>=nextAnalysis){
                nextAnalysis=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(50);
                var analysis=AudioAnalysis.parse(client.command("ANALYSIS"));
                if(!analysis.isEmpty())SwingUtilities.invokeLater(()->{if(!closed&&state==State.RUNNING)AudioAnalysis.publish(analysis);});
            }
            if (System.nanoTime() < nextMeters) return;
            nextMeters = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(250);
            String meter=playing?AudioLoad.parse(client.command("STATS")).label():"Render — "+state;
            if(!diagnosticsUnavailable)try{
                var diagnostic=AudioDiagnostics.parse(client.command("DIAGNOSTICS"));
                SwingUtilities.invokeLater(()->{if(!closed)diagnosticsListener.accept(diagnostic);});
                meter=playing&&diagnostic.frames()>0?diagnostic.label():"Render — "+state;
            }catch(java.io.IOException unsupported){diagnosticsUnavailable=true;SwingUtilities.invokeLater(()->diagnosticsListener.accept(null));}
            loadStatus(meter);
            String routing = AudioEndpoint.unhex(client.command("ROUTING_STATUS").substring(3));
            if (state != State.PAUSED && !routing.equals(routingStatus)) {
                routingStatus = routing;
                status(routing.isEmpty() ? (playing ? com.vin.easyknack.ui.AppText.literal("Audio aktiv · 48 kHz · Stereo") : com.vin.easyknack.ui.AppText.literal("Audio gestoppt")) : routing);
            }
        } catch (Exception failure) {
            wanted = false; playing = false; sent = null; sentVolumeLinks=null;
            lastVolumeKey=0; state(State.STOPPED);
            SwingUtilities.invokeLater(()->AudioDeviceVolumes.publish(java.util.Map.of()));
            SwingUtilities.invokeLater(MidiDevices::unavailable);
            if (client != null) client.close(); client = null;
            status(com.vin.easyknack.ui.AppText.literal("Audiohost: ") + failure.getMessage());
            loadStatus("DSP —");
            SwingUtilities.invokeLater(() -> {
                if (!closed) AudioEndpoints.publish(new AudioEndpoints.Snapshot(
                        new AudioEndpoints.Capabilities(false, false, false, false, false),
                        AudioEndpoints.current().endpoints().stream().map(e -> new AudioEndpoint(e.id(), e.name(), e.type(), e.input(), false, false)).toList()));
            });
        }
    }

    @Override public synchronized void close() {
        if(closed) return;
        closed = true; wanted = false;
        MidiDevices.setKeyboardWakeup(()->{});MidiDevices.drainKeyboard();
        state(State.CLOSED);
        AudioDeviceVolumes.drain();
        SwingUtilities.invokeLater(()->AudioDeviceVolumes.publish(java.util.Map.of()));
        SwingUtilities.invokeLater(MidiDevices::unavailable);
        worker.execute(() -> { if (client != null) client.close(); });
        worker.shutdown();
    }
}

