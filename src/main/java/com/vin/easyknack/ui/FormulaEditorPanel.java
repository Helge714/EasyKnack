package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.formula.*;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;
import com.vingame.glswing.node.document.GLNodeGraphAsset;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

final class FormulaEditorPanel extends GLContainer implements AutoCloseable {
    private final String assetId;private final GLCompositeGraphRegistry groups;private final GLHistoryManager history;private final FormulaRuntime runtime;private final Runnable changed;
    final GLCodeEditor code;
    private final GLButton apply=new GLButton(AppText.text("formula.apply")),format=new GLButton(AppText.text("formula.format"));
    private final GLCheckBox live=new GLCheckBox(AppText.text("formula.live"));
    private final NodeTooltipLabel status=new NodeTooltipLabel(""),ports=new NodeTooltipLabel("");
    private final javax.swing.Timer debounce=new javax.swing.Timer(350,e->apply());
    private boolean updating;private Object schema;private String error="";
    FormulaEditorPanel(String id,GLCompositeGraphRegistry groups,GLHistoryManager history,FormulaRuntime runtime,Runnable changed){
        assetId=id;this.groups=groups;this.history=history;this.runtime=runtime;this.changed=changed;
        code=FormulaEditorPreparation.acquire();
        for(var c:List.of(code,apply,format,live,status,ports))addGL(c);debounce.setRepeats(false);
        var initial=asset();
        code.configureDocument(java.net.URI.create("easyknack-formula:/"+id),new FormulaLanguage(FormulaGroups.ports(initial),FormulaGroups.version(initial)));
        schema=List.copyOf(initial.compositeDescriptor().portMappings());
        apply.addActionListener(e->apply());format.addActionListener(e->code.formatCode());
        live.addActionListener(e->{if(updating)return;var a=asset();var meta=new HashMap<>(a.metadata());meta.put(FormulaGroups.LIVE,GLNodeValue.of(live.isSelected()));groups.replaceAsset(FormulaGroups.metadata(a,meta));if(live.isSelected())debounce.restart();else debounce.stop();changed.run();});
        code.getDocument().addDocumentListener((d,c)->{if(updating)return;var a=asset();var meta=new HashMap<>(a.metadata());meta.put(FormulaGroups.DRAFT,GLNodeValue.of(code.getText()));groups.replaceAsset(FormulaGroups.metadata(a,meta));if(live.isSelected())debounce.restart();});
        refresh();
    }
    private GLNodeGraphAsset asset(){return groups.findAsset(assetId).orElseThrow();}
    void refresh(){if(updating)return;updating=true;try{
        var a=asset();var current=a.compositeDescriptor().portMappings();
        if(!Objects.equals(schema,current)){schema=List.copyOf(current);try{code.setLanguage(new FormulaLanguage(FormulaGroups.ports(a),FormulaGroups.version(a)));}catch(RuntimeException e){error=e.getMessage();}}
        String draft=FormulaGroups.draft(a);if(!code.getText().equals(draft))code.setText(draft);
        live.setSelected(new NodeState(a.metadata()).flag(FormulaGroups.LIVE,true));
        String names=current.stream().map(p->FormulaGroups.codeName(p)+" ("+p.publicPort().id()+")").collect(java.util.stream.Collectors.joining("  ·  "));
        ports.setText(names);ports.setToolTipText(names);status.setText(error.isEmpty()?runtime.status(a):error);status.setToolTipText(status.getText());
    }finally{updating=false;}}
    void apply(){
        debounce.stop();try{var before=asset();var after=FormulaGroups.accept(before,code.getText());error="";
            if(!after.metadata().equals(before.metadata()))history.perform(AppText.text("formula.apply"),()->replace(after),()->replace(before));
            else changed.run();
        }catch(RuntimeException e){error=e.getMessage();}refresh();
    }
    private void replace(GLNodeGraphAsset a){groups.replaceAsset(a);refresh();changed.run();}
    @Override protected void resized(float w,float h){apply.setBounds(12,10,110,28);format.setBounds(130,10,110,28);live.setBounds(252,10,140,28);ports.setBounds(12,44,Math.max(0,w-24),24);code.setBounds(12,74,Math.max(0,w-24),Math.max(0,h-118));status.setBounds(12,Math.max(0,h-34),Math.max(0,w-24),24);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
    public void close(){debounce.stop();code.clearDiagnosticProvider();}
}
