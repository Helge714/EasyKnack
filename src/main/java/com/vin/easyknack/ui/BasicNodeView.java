package com.vin.easyknack.ui;

import java.util.*;
import java.util.function.Consumer;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.input.GLKeyEvent;
import com.vingame.glswing.node.GLNode;
import com.vingame.glswing.node.GLNodePort;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.component.GLContainer;

/** Default node projection. Subclasses can replace controls and rendering, with typed model writes. */
public class BasicNodeView extends GLNode {
        void executionArea(String area){setTitle(definition.title()+(area.isEmpty()?"":" ["+area+"]"));}
        private final BasicNode definition;
        private final NodeViewContext.PropertyWriter writer;
        private final GLHistoryManager history;
        private final GLNodeId id;
        private final String structure;
        private final java.util.Locale presentationLocale=ApplicationSettings.locale();
        private final List<Consumer<NodeState>> updates = new ArrayList<>();
        private final Map<PropertyId,GLContainer> controls = new LinkedHashMap<>();
        private final Map<PropertyId,String> tooltips = new LinkedHashMap<>();
        protected final void onStateChanged(Consumer<NodeState> update) { updates.add(update); }
        private boolean synchronizing;
        private GLGraphNode lastModel;
        private Set<PropertyId> lastConnected=Set.of();
        private final Map<PropertyId,Boolean> rowVisibility=new HashMap<>();
        private boolean themeRefreshPending;
        private NodeKind category;
        private GLTheme categoryTheme;
        private com.vingame.glswing.node.GLNodeStyle categoryStyle;

        public BasicNodeView(BasicNode definition, NodeViewContext context, GLGraphNode model) {
            super(definition.title());
            this.definition = definition; this.writer = context.writer(); this.history = context.history();
            id = model.id();
            category = definition.registration().kind();
            setTitleGradientEnabled(true);
            var layout = definition.layout(new NodeState(model.properties()));
            structure = structure(layout);
            setPortOrder(definition.portOrder());
            setResizeMode(definition.resizeMode());
            for (var row : layout.rows()) {
                if (row.isInspectorOnly()) continue;
                GLContainer content = row.editor() == null ? new NodeTooltipLabel(row.label(), row.kind()==NodeBuilder.RowKind.OUTPUT) : control(row);
                controls.put(row.key(),content);
                if(content instanceof GLSlider slider)slider.getContextMenu().addItem(com.vin.easyknack.ui.AppText.literal("Reset to automatic"),e->write(new PropertyId(GLGraphNode.AUTOMATIC_PREFIX+row.id()),GLNodeValue.of(true)));
                if(content instanceof GLControl control) {
                    String tip=row.editor() instanceof UnitSlider?(row.tooltip()==null||row.tooltip().isBlank()?"":row.tooltip()+"\n")+control.getToolTipText():row.tooltip();
                    if(row.editor() instanceof Editor.Slider slider)tip=(tip==null||tip.isBlank()?row.label():tip)+"\nRange: "+(slider.min()==null?"unbounded":slider.min())+" to "+(slider.max()==null?"unbounded":slider.max())+(slider.unitSuffix().isBlank()?"":" "+slider.unitSuffix());
                    tooltips.put(row.key(),tip);
                    if(row.kind()!=NodeBuilder.RowKind.EDITOR && row.dataType()!=PortDataType.MIDI)tip=(tip==null||tip.isBlank()?row.label():tip)+"\n"+(row.kind()==NodeBuilder.RowKind.INPUT?row.rate().inputHint():row.rate().outputHint());
                    else if(row.dataType()==PortDataType.MIDI)tip=(tip==null||tip.isBlank()?row.label():tip)+"\nMIDI events with sample positions; not an audio or control-rate value.";
                    control.setToolTipText(tip);
                }
                if (content instanceof GLControl control) control.setEnabled(row.isEnabled());
                GLNodePort port = null;
                switch (row.kind()) {
                    case OUTPUT -> port = addOutput(row.id(), content);
                    case INPUT -> port = addInput(row.id(), content);
                    case EDITOR -> addContent(content);
                }
                setAlwaysVisible(content, row.alwaysVisible());
                setContentVerticalGrow(content,row.verticalGrow());
                if (port != null) {
                    port.setToolTipText(content instanceof GLControl control && control.getToolTipText()!=null ? control.getToolTipText() : row.label());
                    port.setColor(ApplicationTheme.current().signal(row.signal()));
                    port.setShape(row.rate().shape());
                    port.setConnectionDashed(row.dataType()==PortDataType.HISTORY || row.dataType()==PortDataType.ANALYSIS || row.dataType()==PortDataType.SPECTRUM);
                    if (row.kind() == NodeBuilder.RowKind.INPUT) {
                        port.setSocketVisible(row.socketVisible());
                        port.setMaximumConnections(1);
                        port.setContentHiddenWhenConnected(row.editor() != null);
                    }
                }
            }
            updateBindings(model);
            applyThemeWidth();
        }
        void refreshPortColors(){
            categoryTheme=null;categoryStyle=null;
            if(lastModel!=null)for(var row:definition.layout(new NodeState(lastModel.properties())).rows()){
                var port=findPort(row.id());if(port!=null)port.setColor(ApplicationTheme.current().signal(row.signal()));
            }
        }
        private void applyThemeWidth() {
            float width=getGLStyle().minimumWidth();
            float contentWidth=Math.max(0,width-getGLStyle().contentPadding().horizontal());
            for(var content:getContents()) content.setMaximumGLSize(contentWidth,Float.MAX_VALUE);
            clearPreferredGLSize();
            setPreferredGLSize(width,getPreferredGLSize().height());
        }
        @Override public void setContentHidden(boolean hidden) {
            if(isContentHidden()==hidden)return;
            clearPreferredGLSize();
            for(var content:controls.values()) { if(content instanceof GLCurveEditor curve)curve.setPreview(hidden); if(content instanceof AnalysisEditor.Plot plot)plot.setPreview(hidden); }
            setResizeMode(hidden?com.vingame.glswing.node.GLNodeResizeMode.HORIZONTAL:definition.resizeMode());
            super.setContentHidden(hidden);
            applyThemeWidth();
        }
        @Override public com.vingame.glswing.node.GLNodeStyle getGLStyle() {
            if (category == null) return super.getGLStyle();
            var theme = getGLTheme();
            if (categoryTheme != theme || categoryStyle == null) {
                categoryTheme = theme; categoryStyle = ApplicationTheme.current().nodeStyle(theme, category);
            }
            return categoryStyle;
        }
        @Override protected void themeChanged() {
            var before = getPreferredGLSize();
            float widthBefore = getWidth(), heightBefore = getHeight();
            boolean autoWidth = getWidth() <= 0 || Math.abs(getWidth() - before.width()) < .5f;
            boolean autoHeight = getHeight() <= 0 || Math.abs(getHeight() - before.height()) < .5f;
            super.themeChanged();
            if (themeRefreshPending) return;
            themeRefreshPending = true;
            // Core propagates the theme parent-first. Measure again after the editors
            // have received it; otherwise the node still uses their old preferred sizes.
            java.awt.EventQueue.invokeLater(() -> {
                themeRefreshPending = false;
                super.themeChanged();
                applyThemeWidth();
                var preferred = getPreferredGLSize();
                setSize(autoWidth && Math.abs(getWidth() - widthBefore) < .5f ? preferred.width() : Math.max(getWidth(), preferred.width()),
                        autoHeight && Math.abs(getHeight() - heightBefore) < .5f || isContentHidden()
                                ? preferred.height() : Math.max(getHeight(), preferred.height()));
            });
        }
        protected GLContainer control(NodeBuilder.Row row) {
            if (row.editor() instanceof Editor.Slider spec) {
                GLSlider slider;
                if (spec.min() != null && spec.max() != null)
                    slider = new BoundSlider(row.label(), spec.min(), spec.max(), spec.value());
                else {
                    slider = new BoundSlider(row.label(), spec.value());
                    slider.setValueBounds(spec.min(), spec.max());
                    // Only model edits belong to the workspace history, never detached view state.
                    slider.setGLHistoryManager(new GLHistoryManager());
                }
                slider.setPresentation(spec.bar() ? GLSlider.Presentation.BAR : GLSlider.Presentation.BUTTONS);
                slider.setUnit(spec.unitSuffix());
                if(!spec.unitSuffix().isBlank())slider.setName(row.label()+" ["+spec.unitSuffix()+"]");
                slider.setIntegerValuesOnly(spec.integral());
                slider.setStep(spec.step()); slider.setDragSensitivity(spec.step());
                slider.addChangeListener(event -> write(row.key(), GLNodeValue.of(slider.getValue())));
                updates.add(state -> slider.setValue(state.number(row.key(), spec.value())));
                return slider;
            }
            if (row.editor() instanceof Editor.CheckBox spec) {
                GLCheckBox checkbox = new GLCheckBox(row.label());
                checkbox.addActionListener(event -> write(row.key(), GLNodeValue.of(checkbox.isSelected())));
                updates.add(state -> checkbox.setSelected(state.flag(row.key(), spec.value())));
                return checkbox;
            }
            if (row.editor() instanceof Editor.ComboBox<?> spec) return combo(row, spec);
            var custom = row.editor().create(row.label(), value -> write(row.key(), value));
            if(row.editor() instanceof UnitSlider spec && custom.component() instanceof GLSlider slider)
                spec.bindUnitMenu(slider,unit->write(new PropertyId(row.id()+".unit"),GLNodeValue.of(unit.id())));
            if(custom.component() instanceof GLSlider slider) {UnitSlider.bindHistory(slider,()->history);slider.setName(row.label()+ (slider.getUnit().isBlank()?"":" ["+slider.getUnit()+"]"));}
            if(custom.component() instanceof AnalysisEditor.Plot plot)plot.bind(id.toString());
            if(custom.component() instanceof PitchReadoutEditor.Readout readout)readout.bind(id.toString());
            if(custom.component() instanceof Editor.TriggerButton button)button.bind(id.toString(),row.id());
            if(custom.component() instanceof GLCurveEditor curve)CurveEditor.bindHistory(curve,()->history);
            updates.add(state -> custom.update().accept(state.values().getOrDefault(row.id(), row.editor().defaultValue())));
            return custom.component();
        }
        private <T> GLComboBox<T> combo(NodeBuilder.Row row, Editor.ComboBox<T> spec) {
            GLComboBox<T> combo = spec.createCombo();
            combo.addActionListener(event -> write(row.key(), GLNodeValue.of(spec.id().apply(combo.getSelectedItem()))));
            updates.add(state -> combo.setSelectedItem(spec.items().stream().filter(item ->
                    spec.id().apply(item).equals(state.text(row.key(), spec.id().apply(spec.value())))).findFirst().orElseThrow()));
            return combo;
        }
        protected final void write(PropertyId key, GLNodeValue value) { if (!synchronizing) writer.set(id, key, value); }
        public void update(GLGraphNode model) { updateBindings(model); }
        private void updateBindings(GLGraphNode model) {
            synchronizing = true;
            try {
                var connected = definition.layout(new NodeState(model.properties())).rows().stream()
                        .filter(row -> row.kind() == NodeBuilder.RowKind.INPUT)
                        .filter(row -> findPort(row.id()) != null && findPort(row.id()).getActiveConnectionCount() > 0)
                        .map(NodeBuilder.Row::key).collect(java.util.stream.Collectors.toSet());
                if(model.equals(lastModel) && connected.equals(lastConnected)) return;
                lastModel=model;lastConnected=connected;
                var state = new NodeState(model.properties(), connected);
                var layout=definition.layout(state);
                boolean geometryChanged=layout.rows().stream().anyMatch(row -> {
                    var content=controls.get(row.key());
                    return content!=null && !Objects.equals(rowVisibility.get(row.key()),row.isVisible());
                });
                float previousPreferredHeight=geometryChanged?getPreferredGLSize().height():0;
                boolean automaticHeight=geometryChanged&&(getHeight()<=0||Math.abs(getHeight()-previousPreferredHeight)<.5f);
                updates.forEach(update -> update.accept(state));

                for (var row : layout.rows()) {
                    var content=controls.get(row.key());
                    if(content!=null) {
                        setContentVerticalGrow(content,isContentHidden()?0:row.verticalGrow());
                        EditorPresentation.update(row,content);
                        definition.updateEditor(row.key(),content,state);
                        setContentVisible(content,row.isVisible());rowVisibility.put(row.key(),row.isVisible());
                        if(content instanceof GLControl control && !(row.editor() instanceof DeviceVolumeEditor)) control.setEnabled(row.isEnabled());
                    }
                    if (row.kind() == NodeBuilder.RowKind.EDITOR) continue;
                    var port = findPort(row.id());
                    if (port == null) continue;
                    port.setColor(ApplicationTheme.current().signal(row.signal()));
                    port.setShape(row.rate().shape());
                    port.setConnectionDashed(row.dataType()==PortDataType.HISTORY || row.dataType()==PortDataType.ANALYSIS || row.dataType()==PortDataType.SPECTRUM);

                    String tip=tooltips.get(row.key());
                    tip=(tip==null||tip.isBlank()?row.label():tip)+"\n"+(row.dataType()==PortDataType.MIDI
                            ? "MIDI events with sample positions; not an audio or control-rate value."
                            : row.kind()==NodeBuilder.RowKind.INPUT?row.rate().inputHint():row.rate().outputHint());
                    if(row.kind()==NodeBuilder.RowKind.INPUT && row.rate()!=ProcessingRate.AUDIO && row.dataType()!=PortDataType.MIDI
                            && getGLParent() instanceof com.vingame.glswing.node.GLNodeEditor editor
                            && editor.getConnections().stream().anyMatch(c->c.getInput()==port && c.getOutput().getNode() instanceof BasicNodeView
                                && c.getOutput().getShape()==GLNodePort.Shape.CIRCLE))
                        tip=(tip==null?row.label():tip)+"\nAudioRate source connected: intermediate samples are not continuously processed. Select AudioRate for sample-by-sample processing.";
                    port.setToolTipText(tip==null?row.label():tip);
                    if(content instanceof GLControl control)control.setToolTipText(tip);

                }
                invalidateRecording();
                if(geometryChanged) {
                    applyThemeWidth();
                    float preferredHeight = getPreferredGLSize().height();
                    if (getWidth() > 0 && Math.abs(preferredHeight - previousPreferredHeight) > .5f)
                        setSize(getWidth(), automaticHeight ? preferredHeight : Math.max(getHeight(), preferredHeight));
                }
            }
            finally { synchronizing = false; }
        }
    private static String structure(NodeBuilder layout) {
        return layout.rows().stream().filter(row -> !row.isInspectorOnly()).map(row -> row.kind() + ":" + row.id() + ":" + row.alwaysVisible() + ":" + row.socketVisible() + ":" + row.signal() + ":"
                + (row.editor() == null ? "label" : row.editor().viewKey())).collect(java.util.stream.Collectors.joining("|"));
    }
    public final GLNodeId nodeId() { return id; }
    public boolean isCurrent(GLGraphNode model, GLNodeTypeDescriptor type) {
        if(!presentationLocale.equals(ApplicationSettings.locale()))return false;
        var layout=definition.layout(new NodeState(model.properties()));
        return !layout.ports().equals(type.ports()) || structure.equals(structure(layout));
    }
    /** Group a drag and the Core control's history entry into one model-edit transaction. */
    private final class BoundSlider extends GLSlider {
        private GLHistoryManager.Transaction gesture;
        BoundSlider(String label, double value) { super(label,value); }
        BoundSlider(String label, double min, double max, double value) { super(label, min, max, value); }
        @Override protected void controlMousePressed(GLMouseEvent event) {
            if (gesture == null) gesture = history.beginTransaction("Change " + getText());
            super.controlMousePressed(event);
        }
        @Override protected void controlMouseReleased(GLMouseEvent event) {
            try { super.controlMouseReleased(event); } finally { finishGesture(); }
        }
        @Override protected void controlMouseCaptureLost(GLMouseEvent event) {
            try { super.controlMouseCaptureLost(event); } finally { finishGesture(); }
        }
        @Override protected void controlKeyPressed(GLKeyEvent event) {
            try (var transaction = history.beginTransaction("Change " + getText())) { super.controlKeyPressed(event); }
        }
        @Override protected void detached(com.vingame.glswing.component.GLParent parent) {
            try { super.detached(parent); } finally { finishGesture(); }
        }
        private void finishGesture() {
            if (gesture != null) { gesture.close(); gesture = null; }
        }
    }

    private GLControl row(String text) {
        return new NodeTooltipLabel(text);
    }
}




