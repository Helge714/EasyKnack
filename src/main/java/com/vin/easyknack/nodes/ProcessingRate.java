package com.vin.easyknack.nodes;

/** Independent of GLSwing's CURRENT/PREVIOUS_STEP dependency timing. */
public enum ProcessingRate {
    AUTO("auto", "Auto", '?'), AUDIO("audio", "AudioRate", 'a'), CONTROL("control", "ControlRate", 'c'), EVENT("event", "Trigger", 'e');
    private final String id, label;
    private final char code;
    ProcessingRate(String id, String label, char code) { this.id = id; this.label = label; this.code = code; }
    public String id() { return id; }
    public char code() { if(this==AUTO)throw new IllegalStateException(com.vin.easyknack.i18n.AppText.literal("Resolve Auto before encoding")); return code; }
    public com.vingame.glswing.node.GLNodePort.Shape shape() {
        return switch(this) {case AUDIO -> com.vingame.glswing.node.GLNodePort.Shape.CIRCLE;
            case AUTO, CONTROL -> com.vingame.glswing.node.GLNodePort.Shape.DIAMOND;
            case EVENT -> com.vingame.glswing.node.GLNodePort.Shape.DOUBLE_DIAMOND;};
    }
    public String inputHint() {
        return NodeText.text("rate."+id+".inputHint",switch(this){
            case AUTO -> "Auto: Audio when a numeric input is Audio; otherwise Control. Each numeric input retains its own rate.";
            case AUDIO -> "Audio: one numeric value per sample. Control inputs retain their one value for the whole block.";
            case CONTROL -> "ControlRate: reads inputs at the start of each DSP block. Audio changes and events inside the block are not sampled.";
            case EVENT -> "Trigger: sample-timed occurrences, with no held numeric value. MIDI ports carry MIDI messages.";
        });
    }
    public String outputHint() {
        return NodeText.text("rate."+id+".outputHint",switch(this){
            case AUTO -> "Auto: Audio when a numeric input is Audio; otherwise Control. Each numeric input retains its own rate.";
            case AUDIO -> "AudioRate: one numeric value per sample.";
            case CONTROL -> "ControlRate: one numeric value per DSP block, held for the block.";
            case EVENT -> com.vin.easyknack.i18n.AppText.text("rate.trigger.tooltip");
        });
    }
    @Override public String toString() { return com.vin.easyknack.i18n.AppText.literal(label); }
    public static final java.util.List<ProcessingRate> NUMERIC = java.util.List.of(AUTO,AUDIO,CONTROL);
    public static NodeBuilder.Row addEditor(NodeBuilder node,PropertyId id){
        return addEditor(node,id,NUMERIC,AUTO);
    }
    public static NodeBuilder.Row addEditor(NodeBuilder node,PropertyId id,java.util.List<ProcessingRate> choices,ProcessingRate initial){
        return node.addEditor(id,NodeText.text("rate.label","Rate"),Editor.comboBox(choices,initial,ProcessingRate::id)).inspectorOnly().tooltip(editorHint());
    }
    public static String editorHint(){return NodeText.text("rate.editor.tooltip","Auto: Audio if any numeric input is Audio, otherwise Control. Inputs keep their individual rates. Audio: one value per sample. Control: one value per block (first sample). Trigger and MIDI are separate data types. Only manual slider changes are smoothed.");}
    public static final String EFFECTIVE = "easyknack.effectiveRate";
    public static ProcessingRate maximum(ProcessingRate a,ProcessingRate b) {
        if(a==AUDIO||b==AUDIO)return AUDIO;
        if(a==EVENT||b==EVENT)return EVENT;
        return CONTROL;
    }
    public static ProcessingRate selected(NodeState state, PropertyId key, String legacyDefault) {
        var requested = fromId(state.text(key, legacyDefault));
        if(requested!=AUTO)return requested==EVENT?AUDIO:requested;
        var effective=fromId(state.text(EFFECTIVE,"control"));
        return effective==AUTO?CONTROL:effective==EVENT?AUDIO:effective;
    }
    public static ProcessingRate fromId(String id) {
        return java.util.Arrays.stream(values()).filter(rate -> rate.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unknown processing rate: ") + id));
    }
}

