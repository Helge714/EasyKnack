package com.vin.easyknack.nodes;

import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

/** Generic inspector host for every application editor, including runtime device controls. */
public final class InspectorEditor extends GLContainer {
    private final Editor.Control editor;
    private final NodeBuilder.Row row;
    private final BasicNode definition;
    private final GLLabel label;
    private boolean synchronizing;
    private final GLNodeId nodeId;
    private InspectorEditor(NodeBuilder.Row row,BasicNode definition,GLGraphNode model,java.util.function.Consumer<GLNodeValue> changed){
        this.row=row;this.definition=definition;this.nodeId=model.id();
        row=definition.layout(new NodeState(model.properties())).rows().stream().filter(r->r.id().equals(this.row.id())).findFirst().orElse(row);
        editor=row.editor().create(row.label(),value->{if(!synchronizing)changed.accept(value);});
        if(editor.component() instanceof GLSlider slider)slider.getContextMenu().addItem(com.vin.easyknack.ui.AppText.literal("Reset to automatic"),e->{
            for(var parent=getGLParent();parent!=null;parent=parent instanceof GLContainer c?c.getGLParent():null)
                if(parent instanceof com.vingame.glswing.node.GLNodePropertyInspector inspector){inspector.setSelectedProperty(GLGraphNode.AUTOMATIC_PREFIX+this.row.id(),GLNodeValue.of(true));break;}
        });
        // Combo boxes and custom visualizations need a name; buttons/sliders/labels label themselves.
        label=(editor.component() instanceof GLComboBox<?> || row.editor() instanceof Editor.Text)?new GLLabel(row.label()):null;
        if(label!=null)addGL(label);addGL(editor.component());setName(row.id());
        if(editor.component() instanceof GLControl control && row.tooltip()!=null && !row.tooltip().isBlank())control.setToolTipText(row.tooltip());
        if(editor.component() instanceof WavetableViewerEditor.Plot plot)plot.bind(model.id().toString());
        if(editor.component() instanceof AnalysisEditor.Plot plot)plot.bind(model.id().toString());
        if(editor.component() instanceof com.vin.easyknack.ui.ValueViewerPanel viewer)viewer.bind(model.id().toString());
        if(editor.component() instanceof PitchReadoutEditor.Readout readout)readout.bind(model.id().toString());
        if(editor.component() instanceof Editor.TriggerButton button)button.bind(model.id().toString(),row.id());
        if(row.isDriverAssignable())com.vin.easyknack.ui.MidiDrivers.attachInspector(model.id(),row.id(),editor.component());
        update(model);
        setPreferredGLSize(180,(label==null?0:24)+Math.max(30,editor.component().getPreferredGLSize().height()));
    }
    @Override protected void attached(com.vingame.glswing.component.GLParent parent){
        super.attached(parent);
        if(row.isDriverAssignable())com.vin.easyknack.ui.MidiDrivers.attachInspectorNow(nodeId,row.id(),editor.component());
    }
    public GLContainer control(){return editor.component();}
    private void update(GLGraphNode model){
        synchronizing=true;
        try {
        var current=definition.layout(new NodeState(model.properties())).rows().stream().filter(r->r.id().equals(row.id())).findFirst().orElse(row);
        EditorPresentation.update(current,editor.component());if(label!=null)label.setText(current.label());
        editor.update().accept(model.properties().getOrDefault(row.id(),current.editor().defaultValue()));
        definition.updateEditor(row.key(),editor.component(),new NodeState(model.properties()));
        if(editor.component() instanceof GLControl control)control.setEnabled(current.isEnabled());
        } finally {synchronizing=false;}
    }
    @Override protected void resized(float width,float height){float top=label==null?0:24;if(label!=null)label.setBounds(0,0,width,24);editor.component().setBounds(0,top,width,Math.max(0,height-top));}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
    public static GLCustomNodeProperty property(NodeBuilder.Row row,BasicNode definition){
        return new GLCustomNodeProperty(row.id(),row.label(),(model,changed)->new InspectorEditor(row,definition,model,changed),
                (control,model)->((InspectorEditor)control).update(model));
    }
}
