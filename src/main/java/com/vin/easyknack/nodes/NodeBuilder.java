package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** With OUTPUTS_FIRST, outputs lead; inputs and standalone editors keep their joint declaration order. */
public final class NodeBuilder {
    public enum RowKind { OUTPUT, EDITOR, INPUT }
    public static final class Row {
        private final RowKind kind;
        private final PropertyId key;
        private String label;
        private String tooltip;
        public Row tooltip(String text) {tooltip=text;return this;}
        public String tooltip() {return tooltip;}
        private Editor editor;
        private UnitFormats formats;
        private UnitFormat unit;
        private PropertyId unitProperty;
        public UnitFormats formats(){return formats;}
        public UnitFormat unit(){return unit;}
        public PropertyId unitProperty(){return unitProperty==null?new PropertyId(id()+".unit"):unitProperty;}
        public Row unitProperty(PropertyId key){unitProperty=key;return this;}
        public String portLabel(){return formats==null?label:label.replaceFirst("\\s*\\[[^\\]]*\\]$", "")+" ["+unit.suffix()+"]";}

        public Row formats(UnitFormats value) { formats=value;return this; }
        private boolean alwaysVisible;
        private boolean editorCopyable=true,driverAssignable=true;
        public Row driverAssignable(boolean value){driverAssignable=value;return this;}
        public boolean isDriverAssignable(){return driverAssignable&&(editor instanceof Editor.Slider||editor instanceof UnitSlider||editor instanceof Editor.CheckBox||editor instanceof Editor.Toggle||editor instanceof Editor.ComboBox<?>||editor instanceof Editor.Trigger);}
        /** Disable for editors whose behavior depends on a custom node view. */
        public Row editorCopyable(boolean value){editorCopyable=value;return this;}
        public boolean isEditorCopyable(){return editorCopyable;}
        private boolean socketVisible=true;
        public boolean socketVisible(){return socketVisible;}
        public Row socketVisible(boolean value){requirePort();socketVisible=value;return this;}
        private boolean inspectorOnly;
        private boolean menuOnly;
        public boolean isMenuOnly() { return menuOnly; }
        private boolean visible = true, enabled = true;
        private float verticalGrow;
        public float verticalGrow() { return verticalGrow; }
        public Row growVertically() { verticalGrow = 1; return this; }
        private PortDataType dataType = PortDataType.SIGNAL;
        private ProcessingRate rate = ProcessingRate.AUDIO;
        private PortRates rates;
        private boolean adaptive;
        private SignalCategory signal = SignalCategory.AUDIO;
        private GLPortTiming timing = GLPortTiming.CURRENT;
        /** Marks an output whose value was produced in the preceding DSP step. */
        public Row previousStep() { if(kind!=RowKind.OUTPUT)throw new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Previous-step timing requires an output"));timing=GLPortTiming.PREVIOUS_STEP;return this; }
        private Row(RowKind kind, PropertyId key, String label, Editor editor) {
            this.kind = kind; this.key = Objects.requireNonNull(key); this.label = label; this.editor = editor;
        }
        public RowKind kind() { return kind; }
        public PropertyId key() { return key; }
        public String id() { return key.value(); }
        public String label() { return label; }
        public Editor editor() { return editor; }
        public boolean alwaysVisible() { return alwaysVisible; }
        public boolean isInspectorOnly() { return inspectorOnly; }
        public boolean isVisible() { return visible; }
        public boolean isEnabled() { return enabled; }
        /** Conditional standalone editors retain their model values while absent from the UI. */
        public Row visible(boolean value) {
            visible = value; return this;
        }
        public Row enabled(boolean value) { enabled = value; return this; }
        public Row dataType(PortDataType value) { requirePort(); dataType = Objects.requireNonNull(value); return this; }
        public PortDataType dataType() { return dataType; }
        /** A model property edited in the sidebar, with no row or socket in the node. */
        public Row inspectorOnly() {
            if (kind != RowKind.EDITOR) throw new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Only standalone editors can be inspector-only"));
            inspectorOnly = true; return this;
        }
        public ProcessingRate rate() { return rate; }
        public SignalCategory signal() { return signal; }
        public Row rate(ProcessingRate value) { requirePort(); rate = Objects.requireNonNull(value); rates = null; return this; }
        /** Declare numeric capabilities; the shared resolver handles connections and manual overrides. */
        public Row rates(PortRates value) { requirePort(); rates = Objects.requireNonNull(value); return this; }
        public PortRates rates() { return rates; }
        public boolean adaptive() { return adaptive; }
        /** In a baked graph, adaptive inputs must retain full precision if its external source changes. */
        public char hostInputRate() { return adaptive ? 'a' : rate.code(); }
        public Row signal(SignalCategory value) { requirePort(); signal = Objects.requireNonNull(value); return this; }
        private void requirePort() {
            if (kind == RowKind.EDITOR) throw new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Standalone editors have no signal port"));
        }
        /** The entire row stays visible when the workspace Hide action collapses the node. */
        public Row keepVisibleOnHide() { return keepVisibleOnHide(true); }
        public Row keepVisibleOnHide(boolean value) { alwaysVisible = value; return this; }
    }
    private final List<Row> rows = new ArrayList<>();
    public Row addOutput(PropertyId id, String label) { return add(new Row(RowKind.OUTPUT, id, label, null)); }
    public Row addInput(PropertyId id, String label) { return add(new Row(RowKind.INPUT, id, label, null)); }
    public Row addInput(PropertyId id, String label, Editor editor) { return add(new Row(RowKind.INPUT, id, label, Objects.requireNonNull(editor))); }
    public Row addEditor(PropertyId id, String label, Editor editor) { return add(new Row(RowKind.EDITOR, id, label, Objects.requireNonNull(editor))); }
    private Row add(Row row) {
        if (row.id() == null || row.id().isBlank() || row.label() == null || rows.stream().anyMatch(r -> r.id().equals(row.id())))
            throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A unique stable row ID and label are required: ") + row.id());
        rows.add(row);
        return row;
    }
    void applyRates(NodeState state){
        String requested=state.text("rate","auto");
        for(var row:rows){
            if(row.rates!=null){
                if(row.dataType!=PortDataType.SIGNAL&&row.dataType!=PortDataType.VECTOR)
                    throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Numeric rates cannot be assigned to ")+row.dataType);
                row.adaptive=row.rates==PortRates.AUDIO_CONTROL&&requested.equals("auto");
                var selected=row.adaptive?ProcessingRate.fromId(state.text(row.kind==RowKind.INPUT
                        ?ProcessingRate.EFFECTIVE+".input."+row.id():ProcessingRate.EFFECTIVE,"control"))
                        :requested.equals("control")?ProcessingRate.CONTROL:ProcessingRate.AUDIO;
                row.rate=row.rates.resolve(selected);
            }else if(state.text("rate","").equals("auto")&&row.kind==RowKind.INPUT
                    &&(row.dataType==PortDataType.SIGNAL)){
                // Source-plugin compatibility for definitions predating PortRates.
                row.adaptive=true;
                row.rate=ProcessingRate.fromId(state.text(ProcessingRate.EFFECTIVE+".input."+row.id(),"control"));
            }
        }
    }
    void applyInteractionPolicy(Map<PropertyId,Integer> nativePorts){
        for(var row:rows)if(row.editor instanceof Editor.Slider slider&&!nativePorts.containsKey(row.key))row.editor=slider.discrete();
    }
    void applyFormats(NodeState state) {
        for(var row:List.copyOf(rows)) {
            var slider=row.editor instanceof Editor.Slider value?value:null;
            var formats=row.formats!=null?row.formats:slider==null?null:slider.formats();
            if(formats==null)continue;
            if(row.kind!=RowKind.EDITOR && row.dataType!=PortDataType.SIGNAL)
                throw new IllegalArgumentException("Scalar units require a numeric signal port");
            var key=row.unitProperty();
            var selected=UnitFormat.fromId(state.text(key,formats.stored().id()));
            if(!formats.choices().contains(selected))throw new IllegalArgumentException("Unsupported unit "+selected.id()+" for port "+row.id()+"; expected "+formats.choices());
            row.formats=formats;row.unit=selected;
            if(slider!=null)row.editor=new UnitSlider(slider,formats,selected);
            if(rows.stream().noneMatch(r->r.key.equals(key))) {
                var choice=addEditor(key,row.label+" unit",Editor.comboBox(formats.choices(),formats.initial(),UnitFormat::id)).inspectorOnly();
                choice.menuOnly=true;
            }
        }
    }
    public List<Row> rows() { return List.copyOf(rows); }
    void translate(String nodeId) {
        rows.forEach(row -> {row.label = NodeText.text(nodeId + "." + row.id(), row.label);row.tooltip=NodeText.text(nodeId+"."+row.id()+".tooltip",row.tooltip==null?"":row.tooltip);var slider=row.editor instanceof UnitSlider u?u.spec():row.editor instanceof Editor.Slider v?v:null;if(slider!=null||row.editor instanceof VectorEditor){String hint=com.vin.easyknack.ui.AppText.text(slider==null||slider.continuousChanges()?"control.tooltip.continuous":"control.tooltip.discrete");row.tooltip=row.tooltip==null||row.tooltip.isBlank()?hint:row.tooltip+"\n"+hint;}});
    }
    public List<GLNodePortDescriptor> ports() {
        return rows.stream().filter(r -> r.kind() != RowKind.EDITOR).map(r -> r.kind() == RowKind.OUTPUT
                ? GLNodePortDescriptor.output(r.id(), r.dataType.id(),r.timing,Integer.MAX_VALUE)
                : GLNodePortDescriptor.input(r.id(), r.dataType.id(), r.editor() == null, 1)).toList();
    }
    public Map<String, GLNodeValue> defaults() {
        Map<String, GLNodeValue> result = new LinkedHashMap<>();
        rows.stream().filter(r -> r.editor() != null).forEach(r -> {result.put(r.id(), r.editor().defaultValue()); result.put(com.vingame.glswing.node.model.GLGraphNode.AUTOMATIC_PREFIX+r.id(),GLNodeValue.of(true)); result.put(com.vingame.glswing.node.model.GLGraphNode.AUTOMATIC_DEFAULT_PREFIX+r.id(),r.editor().defaultValue());});
        return Map.copyOf(result);
    }
    public void validate(NodeState state) {
        rows.stream().filter(r -> r.editor() != null).forEach(r ->
                r.editor().validate(state.values().getOrDefault(r.id(), r.editor().defaultValue())));
    }
}

