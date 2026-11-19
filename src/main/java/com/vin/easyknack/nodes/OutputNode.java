package com.vin.easyknack.nodes;

import java.util.function.ToIntFunction;

@RegisterNode(id = "easyknack.audio.output", title = "Audio Output", category=NodeCategory.OUTPUT, kind=NodeKind.AUDIO)
public final class OutputNode extends BasicNode {
    public static final PropertyId IN = new PropertyId("audio-in"), ENDPOINT = new PropertyId("endpoint");
    public static final PropertyId DEVICE_VOLUME = new PropertyId("device-volume-control"), VOLUME_KEYS = new PropertyId("volume-keys");
    public static final PropertyId LEVEL = new PropertyId("output-level-display");

    @Override public void define(NodeBuilder node, NodeState state) {
        node.addInput(IN, "Audio In");
        node.addEditor(ENDPOINT, "Device", new EndpointEditor(com.vin.easyknack.audio.AudioEndpoint.DEFAULT)).keepVisibleOnHide();
        var endpoint=com.vin.easyknack.audio.AudioEndpoint.reference(state.text(ENDPOINT,com.vin.easyknack.audio.AudioEndpoint.DEFAULT.reference()));
        var platform=com.vin.easyknack.NativePlatform.current();
        boolean volumeControls=(platform.windows() || platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.LINUX) && (endpoint.id().equals("default") || endpoint.id().startsWith("out:"));
        node.addEditor(DEVICE_VOLUME,"Device volume",new DeviceVolumeEditor(endpoint.id())).visible(volumeControls).keepVisibleOnHide();
        node.addEditor(VOLUME_KEYS,"Link volume keys",Editor.checkBox(false)).visible(volumeControls);
        node.addEditor(LEVEL,"Signal",new OutputLevelEditor(endpoint.id())).visible(volumeControls);
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("deviceout", "nodes/DeviceOutputNode.hpp",
            ParameterContract.builder().source(IN).mapped(ENDPOINT,s -> com.vin.easyknack.audio.AudioEndpoint.hex(com.vin.easyknack.audio.AudioEndpoint.reference(s.text(ENDPOINT,com.vin.easyknack.audio.AudioEndpoint.DEFAULT.reference())).id())).build()); }
}


