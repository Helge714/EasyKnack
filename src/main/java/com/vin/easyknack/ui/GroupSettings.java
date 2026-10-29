package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.component.GLContainer;

/** Application-owned general settings and input presentation, without parameter binding UI. */
final class GroupSettings extends GLContainer {
    private final GLCompositeInterfaceEditor editor;
    private final GLCompositeGraphRegistry groups;
    private final GLNodeCatalog catalog;
    private final GLNodeTypeRegistry types;
    private final GLHistoryManager history;
    private final Runnable changed;
    private final EditorTemplatesPanel templatesPanel;
    private final GroupInputSettings inputSettings;
    private final GLLabel kindLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Node type (title color)")),categoryLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Add menu category"));
    private final GLComboBox<NodeKind> kind=new GLComboBox<>(List.of(NodeKind.values()));
    private final GLComboBox<NodeCategory> category=new GLComboBox<>(List.of(NodeCategory.choices()));
    private final List<GLLabel> routingLabels=List.of(new GLLabel(com.vin.easyknack.ui.AppText.literal("Insert: input")),new GLLabel(com.vin.easyknack.ui.AppText.literal("Insert: output")),new GLLabel(com.vin.easyknack.ui.AppText.literal("Mute bypass: input")),new GLLabel(com.vin.easyknack.ui.AppText.literal("Mute bypass: output")));
    private final List<GLComboBox<String>> routing=List.of(new GLComboBox<>(),new GLComboBox<>(),new GLComboBox<>(),new GLComboBox<>());
    private static final List<String> ROUTING_KEYS=List.of(GroupRouting.INSERT_IN,GroupRouting.INSERT_OUT,GroupRouting.MUTE_IN,GroupRouting.MUTE_OUT);
    private final GLContainer general=new GLContainer(){
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        @Override protected void resized(float w,float h){float y=0;if(formulaRate.isVisible()){formulaRateLabel.setBounds(0,y,w,20);formulaRate.setBounds(0,y+22,w,28);y+=54;}kindLabel.setBounds(0,y,w,20);y+=22;kind.setBounds(0,y,w,28);y+=32;categoryLabel.setBounds(0,y,w,20);y+=22;category.setBounds(0,y,w,28);y+=36;for(int i=0;i<routing.size();i++){routingLabels.get(i).setBounds(0,y,w,20);y+=22;routing.get(i).setBounds(0,y,w,28);y+=32;}if(skipEmpty.isVisible()){skipEmpty.setBounds(0,y,w,28);y+=36;parallelBins.setBounds(0,y,w,28);y+=36;}if(maximumLabel.isVisible()){maximumLabel.setBounds(0,y,w,20);maximumInstances.setBounds(0,y+22,w,28);mergeLabel.setBounds(0,y+54,w,20);mergeMode.setBounds(0,y+76,w,28);}}
    };
    private final GLLabel maximumLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Maximum instances (1-256)")),mergeLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Instance mix"));
    private final GLTextEditor maximumInstances=new GLTextEditor();
    private final GLComboBox<String> mergeMode=new GLComboBox<>(List.of("sum","mean"));
    private final GLCheckBox skipEmpty=new GLCheckBox(com.vin.easyknack.ui.AppText.literal("Skip empty input bins"));
    private final GLCheckBox parallelBins=new GLCheckBox(com.vin.easyknack.ui.AppText.literal("Parallel bins (automatic)"));
    private final GLLabel formulaRateLabel=new GLLabel(AppText.text("formula.rate"));
    private final GLComboBox<ProcessingRate> formulaRate=new GLComboBox<>(ProcessingRate.NUMERIC);
    private boolean updating;
    GroupSettings(GLCompositeInterfaceEditor editor,GLCompositeGraphRegistry groups,GLNodeCatalog catalog,GLNodeTypeRegistry types,GLHistoryManager history,Runnable changed,EditorTemplates templates){
        this.editor=editor;this.groups=groups;this.catalog=catalog;this.types=types;this.history=history;this.changed=changed;
        editor.setPortPresentationTransform(p->{var a=editor.getAssetId()==null?null:groups.findAsset(editor.getAssetId()).orElse(null);return FormulaGroups.isFormula(a)?FormulaGroups.normalize(p):GroupInputs.normalize(p,a,groups);});
        editor.setApplicationOwnsInputEditors(true);
        templatesPanel=new EditorTemplatesPanel(templates,history,()->{editor.refresh();changed.run();});
        inputSettings=new GroupInputSettings(editor,groups,templates,history,changed);addGL(inputSettings);
        general.setPreferredGLSize(280,112+4*54);editor.setGeneralSettings(general);
        general.addGL(kindLabel);general.addGL(kind);general.addGL(categoryLabel);general.addGL(category);category.setItemTextProvider(NodeCategory::path);
        for(var control:List.of(maximumLabel,maximumInstances,mergeLabel,mergeMode))general.addGL(control);
        general.addGL(skipEmpty);general.addGL(formulaRateLabel);general.addGL(formulaRate);
        formulaRate.addActionListener(e->{if(formulaRate.getSelectedItem()!=null)setMetadata(FormulaGroups.RATE,formulaRate.getSelectedItem().id());});
        general.addGL(parallelBins);
        category.setItemTextProvider(NodeCategory::path);mergeMode.setItemTextProvider(AppText::literal);
        parallelBins.setToolTipText(com.vin.easyknack.ui.AppText.literal("Use one shared worker for sufficiently large independent FFT-bin programs. Small programs and per-bin audio voices stay serial; busy workers fall back to serial execution."));
        parallelBins.addActionListener(e->setMetadata(SpectralGroups.PARALLEL,Boolean.toString(parallelBins.isSelected())));
        skipEmpty.setToolTipText(com.vin.easyknack.ui.AppText.literal("Skip bins whose input coefficient is exactly zero, before Discard and bin processing. Disable when creating bins or reading other bins. Applies after Spectrum Prune too."));
        skipEmpty.addActionListener(e->setMetadata(SpectralGroups.SKIP_EMPTY,Boolean.toString(skipEmpty.isSelected())));
        maximumInstances.setToolTipText(com.vin.easyknack.ui.AppText.literal("Reserved instance capacity. Active Count is controlled on the group node. Storage is prepared outside the audio thread."));
        maximumInstances.addActionListener(e->{if(updating)return;try{int n=Integer.parseInt(maximumInstances.getText().strip());if(n<1||n>256)throw new IllegalArgumentException();setMetadata(MultipleGroups.MAXIMUM,Integer.toString(n));}catch(IllegalArgumentException ex){maximumInstances.setToolTipText(com.vin.easyknack.ui.AppText.literal("Enter an integer from 1 to 256."));}});
        mergeMode.addActionListener(e->{if(mergeMode.getSelectedItem()!=null)setMetadata(MultipleGroups.MERGE,mergeMode.getSelectedItem());});
        kind.addActionListener(e->{if(kind.getSelectedItem()!=null)setMetadata("easyknack.kind",kind.getSelectedItem().name());});
        category.addActionListener(e->{if(category.getSelectedItem()!=null)setMetadata("easyknack.category",category.getSelectedItem().path());});
        for(int i=0;i<routing.size();i++){int index=i;var box=routing.get(i);general.addGL(routingLabels.get(i));general.addGL(box);box.setItemTextProvider(id->id.isEmpty()?"None":id);box.addActionListener(e->{if(box.getSelectedItem()!=null)setMetadata(ROUTING_KEYS.get(index),box.getSelectedItem());});}
    }
    GLContainer templatesPanel(){return templatesPanel;}
    private void setMetadata(String key,String value){
        if(updating||editor.getAssetId()==null)return;
        var before=groups.findAsset(editor.getAssetId()).orElseThrow();
        if(key.equals(FormulaGroups.RATE)){try{com.vin.easyknack.formula.FormulaRatePlan.resolve(FormulaGroups.program(before),ProcessingRate.fromId(value),Map.of());}catch(IllegalArgumentException e){formulaRate.setToolTipText(e.getMessage());refresh();return;}}
        var metadata=new HashMap<>(before.metadata());metadata.put(key,GLNodeValue.of(value));
        var after=new GLNodeGraphAsset(before.id(),before.version(),before.kind(),before.graph(),before.viewState(),metadata,before.compositeDescriptor());
        history.perform("Change group presentation",()->apply(after),()->apply(before));
    }
    private void apply(GLNodeGraphAsset asset){
        groups.replaceAsset(asset);var d=asset.compositeDescriptor();
        catalog.find(d.nodeTypeId()).ifPresent(entry->catalog.replace(new GLNodeCatalogEntry(entry.type(),entry.displayName(),NodeCategory.catalogPath(new NodeState(asset.metadata()).text("easyknack.category","Groups")),entry.keywords(),entry.defaultProperties())));
        refresh();changed.run();
    }
    void refresh(){
        if(updating)return;updating=true;
        try{
            templatesPanel.refresh();var asset=editor.getAssetId()==null?null:groups.findAsset(editor.getAssetId()).orElse(null);
            boolean formula=FormulaGroups.isFormula(asset);formulaRate.setVisible(formula);formulaRateLabel.setVisible(formula);if(formula)formulaRate.setSelectedItem(ProcessingRate.fromId(new NodeState(asset.metadata()).text(FormulaGroups.RATE,"auto")));
            boolean multiple=asset!=null&&MultipleGroups.multiple(asset);
            for(var control:List.of(maximumLabel,maximumInstances,mergeLabel,mergeMode))control.setVisible(multiple);
            boolean legacy=multiple&&new NodeState(asset.metadata()).number(MultipleGroups.EXECUTION,1)<2&&asset.graph().nodes().stream().noneMatch(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof InstanceCombineNode);
            mergeLabel.setVisible(legacy);mergeMode.setVisible(legacy);
            boolean spectral=asset!=null&&SpectralGroups.spectral(asset);skipEmpty.setVisible(spectral);
            parallelBins.setVisible(spectral);parallelBins.setSelected(spectral&&new NodeState(asset.metadata()).text(SpectralGroups.PARALLEL,"true").equals("true"));
            skipEmpty.setSelected(spectral&&new NodeState(asset.metadata()).text(SpectralGroups.SKIP_EMPTY,"false").equals("true"));
            general.setPreferredGLSize(280,112+4*54+(multiple?(legacy?112:56):0)+(spectral?72:0)+(formula?54:0));
            if(multiple){var state=new NodeState(asset.metadata());maximumInstances.setText(state.text(MultipleGroups.MAXIMUM,"16"));mergeMode.setSelectedItem(state.text(MultipleGroups.MERGE,"mean"));}
            kind.setEnabled(asset!=null);category.setEnabled(asset!=null);
            if(asset!=null){var state=new NodeState(asset.metadata());kind.setSelectedItem(NodeKind.valueOf(state.text("easyknack.kind","AUDIO")));category.setSelectedItem(Arrays.stream(NodeCategory.choices()).filter(c->c.matches(state.text("easyknack.category",""))).findFirst().orElse(NodeCategory.ROUTING));}
            for(int i=0;i<routing.size();i++){
                var choices=new ArrayList<String>();choices.add("");
                if(asset!=null){var d=asset.compositeDescriptor();var input=d.nodeType().findPort(new NodeState(asset.metadata()).text(ROUTING_KEYS.get(i-i%2),"")).orElse(null);
                    for(var mapping:d.portMappings()){if(MultipleGroups.countPort(mapping))continue;var p=mapping.publicPort();if(p.direction()==(i%2==0?GLPortDirection.INPUT:GLPortDirection.OUTPUT)&&(i%2==0||input==null||types.compatibleValueTypes(input.valueTypeId(),p.valueTypeId())))choices.add(p.id());}}
                var box=routing.get(i);if(!box.getItems().equals(choices)){box.removeAllItems();choices.forEach(box::addItem);}String selected=asset==null?"":new NodeState(asset.metadata()).text(ROUTING_KEYS.get(i),"");if(asset!=null&&!asset.metadata().containsKey(ROUTING_KEYS.get(i))){var inferred=GroupRouting.pair(asset,types,ROUTING_KEYS.get(i-i%2),ROUTING_KEYS.get(i-i%2+1));if(inferred!=null)selected=i%2==0?inferred.inputPortId():inferred.outputPortId();}box.setSelectedItem(choices.contains(selected)?selected:"");box.setEnabled(asset!=null);
            }
            inputSettings.refresh();setPreferredGLSize(280,inputSettings.getPreferredGLSize().height());inputSettings.setBounds(0,0,getWidth(),getPreferredGLSize().height());invalidateGLLayout();
        }finally{updating=false;}
    }
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
    @Override protected void resized(float w,float h){inputSettings.setBounds(0,0,w,h);}
}
