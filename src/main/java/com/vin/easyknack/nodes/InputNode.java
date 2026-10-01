package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.AudioEndpoint;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id = "easyknack.audio.input", title = "Audio Input", category=NodeCategory.IO, kind=NodeKind.AUDIO)
public final class InputNode extends BasicNode {
    public enum Mode { COPY; @Override public String toString() { return "Copy"; } }
    public enum Channels { MONO, STEREO; @Override public String toString() { return this == MONO ? "Mono" : "Stereo"; } }
    public static final PropertyId OUT = new PropertyId("audio-out"), ENDPOINT = new PropertyId("endpoint"),
            MODE = new PropertyId("capture-mode"), CHANNELS = new PropertyId("channels");
    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(OUT, "Audio Out");
        node.addEditor(ENDPOINT, "Source", new EndpointEditor(AudioEndpoint.SYSTEM)).keepVisibleOnHide();
        // Device input is not a claim that the original application was automatically rerouted.
        var endpoint = AudioEndpoint.reference(state.text(ENDPOINT, AudioEndpoint.SYSTEM.reference()));
        node.addEditor(MODE, "Mode", Editor.comboBox(List.of(Mode.COPY), Mode.COPY, m -> "copy"))
                .visible(endpoint.type() == AudioEndpoint.Type.SYSTEM_MIX || endpoint.type() == AudioEndpoint.Type.APPLICATION);
        node.addEditor(CHANNELS, "Channels", Editor.comboBox(List.of(Channels.values()), Channels.STEREO, c -> c.name().toLowerCase(java.util.Locale.ROOT)));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("input", "nodes/InputNode.hpp",
            ParameterContract.builder().mapped(ENDPOINT,s -> AudioEndpoint.hex(AudioEndpoint.reference(s.text(ENDPOINT,AudioEndpoint.SYSTEM.reference())).id())).value(MODE).value(CHANNELS).build()); }
}

