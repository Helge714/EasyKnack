package com.vin.easyknack.ui;
import java.util.*;
import java.util.function.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.history.GLHistoryManager;

/** EasyKnack group controls and unit semantics; all graph edits still use the workspace. */
final class GroupViewFactory implements GLNodeViewFactory {
    private final GLCompositeGraphRegistry groups;private final GLNodeCatalog catalog;
    private final NodeViewContext.PropertyWriter writer;private final GLHistoryManager history;private final Runnable observe;
    GroupViewFactory(GLCompositeGraphRegistry groups,GLNodeCatalog catalog,NodeViewContext.PropertyWriter writer,GLHistoryManager history,Runnable observe){this.groups=groups;this.catalog=catalog;this.writer=writer;this.history=history;this.observe=observe;}
    private String structure(GLGraphNode node){
        var d=groups.findComposite(node.typeId()).orElseThrow();
        return d.toString()+GroupChoices.choices(groups.findAsset(d.assetId()).orElseThrow(),groups).stream().map(c->c.id()+c.label()+c.editor().items()+c.editor().defaultValue()).toList()+d.portMappings().stream().map(p->p.publicPort().id()+":"+GroupUnits.selected(node,p,GroupUnits.base(groups,d,p))).toList();
    }
    public GLNode createNode(GLGraphNode node,GLNodeTypeDescriptor type){observe.run();return new GroupView(node);}
    public boolean isViewCurrent(GLNode view,GLGraphNode node,GLNodeTypeDescriptor type){return view instanceof GroupView group&&group.structure.equals(structure(node));}
    public void updateNode(GLNode view,GLGraphNode node,GLNodeTypeDescriptor type){((GroupView)view).update(node);}
    final class GroupView extends GLNode {
        private final GLNodeId nodeId;
        private final String structure;private final GLCompositeNodeDescriptor descriptor;
        private final Map<String,com.vin.easyknack.nodes.Editor.Control> controls=new LinkedHashMap<>();
        private final Map<String,com.vin.easyknack.nodes.Editor.Control> choiceControls=new LinkedHashMap<>();
        private final Map<String,com.vingame.glswing.component.GLContainer> publicControls=new LinkedHashMap<>();
        private final Map<GLControl,String> controlledTooltips=new IdentityHashMap<>();
        private com.vin.easyknack.nodes.Editor.Control countControl;
        private String formulaWarning="";
        private boolean updating;private NodeKind kind;private GLTheme cachedTheme;private GLNodeStyle cachedStyle;
        GroupView(GLGraphNode node){
            super(catalog.find(node.typeId()).map(GLNodeCatalogEntry::displayName).orElse("Group"));
            nodeId=node.id();
            var source=GroupInputs.materialize(groups.findAsset(groups.findComposite(node.typeId()).orElseThrow().assetId()).orElseThrow());
            NodeDocumentation.attach(this,PartialGroups.partial(source)?"group:partial":FormulaGroups.isFormula(source)?"group:formula":SpectralGroups.spectral(source)?"group:spectral":MultipleGroups.multiple(source)?"group:instance":"group:ordinary");
            descriptor=source.compositeDescriptor();structure=structure(node);
            setGroupIndicatorVisible(true);
            kind=groupKind();setTitleGradientEnabled(true);setPortOrder(GLNodePortOrder.OUTPUTS_FIRST);setResizeMode(GLNodeResizeMode.HORIZONTAL);setGLHistoryManager(history);
            if(MultipleGroups.multiple(source)){
                var countPort=descriptor.portMappings().stream().filter(MultipleGroups::countPort).findFirst().orElse(null);
                String countKey=countPort==null?MultipleGroups.COUNT_PROPERTY:GroupUnits.key(countPort.publicPort().id());
                countControl=com.vin.easyknack.nodes.Editor.Slider.button(8,0.0,256.0).integers().create("Count",v->{if(!updating)writer.set(node.id(),new PropertyId(countKey),v);});
                ((GLSlider)countControl.component()).setToolTipText(com.vin.easyknack.i18n.AppText.literal("Active instances, limited by Maximum instances in Group-General. Fixed Control input; no internal wiring is required."));
                if(countControl.component() instanceof GLSlider slider)UnitSlider.bindHistory(slider,()->history);
                publicControls.put(countKey,countControl.component());
                if(countPort==null)addContent(countControl.component());else {var port=addInput(countPort.publicPort().id(),countControl.component());port.setContentHiddenWhenConnected(true);}
            }
            for(var p:descriptor.portMappings()){
                if(MultipleGroups.countPort(p))continue;
                var port=p.publicPort();boolean output=port.direction()==GLPortDirection.OUTPUT;
                com.vingame.glswing.component.GLContainer control;
                if(!output&&GroupVectorEditors.isVector(p)){
                    var binding=GroupVectorEditors.editor(p).create(port.id(),v->{if(!updating)writer.set(node.id(),new PropertyId(GroupUnits.key(port.id())),v);});
                    control=binding.component();controls.put(port.id(),binding);
                    for(var slider:((GLSliderGroup)control).getSliders())UnitSlider.bindHistory(slider,()->history);
                }else if(!output&&p.presentation().inputEditorId().equals(GroupInputSettings.BUTTON)){
                    var button=new GLButton(port.id());button.addActionListener(e->GroupInputTriggers.fire(groups,node.id().toString(),descriptor,p));control=button;
                }else if(!output&&GroupInputs.choice(p)){
                    var choice=GroupChoices.choices(source,groups).stream().filter(c->c.id().equals(GroupInputs.property(p))).findFirst().orElse(null);
                    if(choice==null)control=new NodeTooltipLabel(port.id(),false);
                    else {var binding=choice.editor().create(port.id(),v->{if(!updating)writer.set(node.id(),new PropertyId(choice.id()),v);});choiceControls.put(choice.id(),binding);control=binding.component();}
                }else if(!output&&GroupInputSettings.checkbox(p)){
                    var binding=new com.vin.easyknack.nodes.Editor.Toggle(new NodeState(p.presentation().editorOptions()).number("default",0)!=0).create(port.id(),v->{if(!updating)writer.set(node.id(),new PropertyId(GroupUnits.key(port.id())),v);});
                    control=binding.component();controls.put(port.id(),binding);
                }else if(!output&&p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)){
                    var options=new NodeState(p.presentation().editorOptions());var base=GroupUnits.base(groups,descriptor,p);var selected=GroupUnits.selected(node,p,base);
                    Double min=options.flag("minimumEnabled",true)?options.number("minimum",0):null,max=options.flag("maximumEnabled",true)?options.number("maximum",1):null;
                    var spec=new com.vin.easyknack.nodes.Editor.Slider(options.number("default",0),min,max,options.number("step",.01),options.text("numericType","float").equals("integer"),options.text("presentation","bar").equals("bar"));if(!options.flag("continuous",true))spec=spec.discrete();
                    var format=new UnitSlider(spec,new UnitFormats(base,GroupUnits.initial(p,base),GroupUnits.allowed(p,base)),selected);
                    var binding=format.create(port.id(),v->{if(!updating)writer.set(node.id(),new PropertyId(GroupUnits.key(port.id())),v);});
                    control=binding.component();controls.put(port.id(),binding);
                    UnitSlider.bindHistory((GLSlider)control,()->history);
                    format.bindUnitMenu((GLSlider)control,u->writer.set(node.id(),new PropertyId(GroupUnits.unitKey(port.id())),GLNodeValue.of(u.id())));
                }else control=new NodeTooltipLabel(port.id(),output);
                if(!output){control.setName(port.id());publicControls.put(GroupEditorCopies.groupProperty(p),control);}
                if(GroupUnits.numeric(p) && (output || p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER) || p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE))) {
                    var base=GroupUnits.base(groups,descriptor,p);var selected=GroupUnits.selected(node,p,base);
                    String label=port.id()+" ["+selected.suffix()+"]";control.setName(label);
                    if(control instanceof GLLabel text)text.setText(label);
                }
                if(!output)MidiDrivers.attach(history,node.id(),GroupInputs.choice(p)?GroupInputs.property(p):GroupUnits.key(port.id()),control);
                if(output)addOutput(port.id(),control);
                else {var input=addInput(port.id(),control);input.setSocketVisible(p.presentation().socketVisible());input.setContentHiddenWhenConnected(controls.containsKey(port.id()) || GroupInputs.choice(p) || p.presentation().inputEditorId().equals(GroupInputSettings.BUTTON));}
                if(control instanceof GLSliderGroup vectorGroup)setContentPortAnchor(control,vectorGroup.getSliders().get(0));
                if(GroupUnits.numeric(p) && (output || p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER) || p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE))) {
                    var base=GroupUnits.base(groups,descriptor,p);var selected=GroupUnits.selected(node,p,base);
                    findPort(port.id()).setContextMenu(PortUnitMenus.create(GroupUnits.allowed(p,base),selected,
                        u->writer.set(node.id(),new PropertyId(GroupUnits.unitKey(port.id())),GLNodeValue.of(u.id()))));
                }


            }
            for(var choice:GroupChoices.choices(groups.findAsset(descriptor.assetId()).orElseThrow(),groups)){
                if(choiceControls.containsKey(choice.id()))continue;
                addContent(new GLLabel(choice.label()));
                var binding=choice.editor().create(choice.id(),v->{if(!updating)writer.set(node.id(),new PropertyId(choice.id()),v);});
                choiceControls.put(choice.id(),binding);MidiDrivers.attach(history,node.id(),choice.id(),binding.component());addContent(binding.component());
            }
            CompositePortColors.apply(this,descriptor);update(node);
            String monitor=new NodeState(groups.findAsset(descriptor.assetId()).orElseThrow().metadata()).text(PitchReadoutEditor.GROUP_MONITOR,"");
            if(!monitor.isEmpty()){var readout=new PitchReadoutEditor.Readout(monitor);readout.bind(node.id().toString());addContent(readout);}
        }
        private NodeKind groupKind(){return NodeKind.valueOf(new NodeState(groups.findAsset(descriptor.assetId()).orElseThrow().metadata()).text("easyknack.kind","AUDIO"));}
        GLNodeId nodeId(){return nodeId;}
        void controlledByGroup(Set<PropertyId> properties){
            publicControls.forEach((key,component)->setControlled(component,!properties.contains(new PropertyId(key))));
            choiceControls.forEach((key,binding)->setControlled(binding.component(),!properties.contains(new PropertyId(key))));
        }
        private void setControlled(com.vingame.glswing.component.GLContainer component,boolean enabled){
            if(component instanceof GLSliderGroup vector)for(var slider:vector.getSliders())setControlled(slider,enabled);
            else if(component instanceof GLControl control){
                if(enabled&&!controlledTooltips.containsKey(control))return;
                if(!enabled&&!controlledTooltips.containsKey(control))controlledTooltips.put(control,control.getToolTipText());
                control.setEnabled(enabled);control.setToolTipText(enabled?controlledTooltips.remove(control):AppText.text("group.editor.controlled"));
            }
        }
        void update(GLGraphNode node){
            updating=true;try{if(countControl!=null)countControl.update().accept(node.properties().getOrDefault(descriptor.portMappings().stream().filter(MultipleGroups::countPort).map(p->GroupUnits.key(p.publicPort().id())).findFirst().orElse(MultipleGroups.COUNT_PROPERTY),node.properties().getOrDefault(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(8.))));for(var entry:controls.entrySet()){
                var p=descriptor.portMappings().stream().filter(m->m.publicPort().id().equals(entry.getKey())).findFirst().orElseThrow();
                entry.getValue().update().accept(node.properties().getOrDefault(GroupUnits.key(entry.getKey()),p.presentation().editorOptions().getOrDefault("default",GLNodeValue.of(0.0))));
            }
            var asset=GroupInputs.materialize(groups.findAsset(descriptor.assetId()).orElseThrow());
            for(var choice:GroupChoices.choices(asset,groups))choiceControls.get(choice.id()).update().accept(GroupChoices.selection(asset,choice,node.properties().getOrDefault(choice.id(),choice.editor().defaultValue())));
            }finally{updating=false;}
            var next=groupKind();if(kind!=next){kind=next;cachedTheme=null;invalidateRecording();}
            setTitle(catalog.find(node.typeId()).map(GLNodeCatalogEntry::displayName).orElse("Group"));
            refreshFormulaState();
        }
        void refreshFormulaState(){
            var formula=groups.findAsset(descriptor.assetId()).orElseThrow();
            if(!FormulaGroups.isFormula(formula))return;
            var edit=FormulaGroups.editState(formula);String warning=edit.valid()?"":AppText.text("formula.invalidDraft")+" — "+edit.error();
            if(Objects.equals(warning,formulaWarning))return;formulaWarning=warning;
            setTitleSuffix(warning.isEmpty()?"":"  !");GLToolTipManager.leave(this);
        }

        @Override public void mouseEntered(com.vingame.glswing.input.GLMouseEvent event){super.mouseEntered(event);if(!formulaWarning.isEmpty())GLToolTipManager.hover(this,event,formulaWarning,null);}
        @Override public void mouseMoved(com.vingame.glswing.input.GLMouseEvent event){super.mouseMoved(event);if(!formulaWarning.isEmpty())GLToolTipManager.hover(this,event,formulaWarning,null);}
        @Override public void setContentHidden(boolean hidden){
            if(isContentHidden()==hidden)return;
            clearPreferredGLSize();super.setContentHidden(hidden);
        }
        @Override public GLNodeStyle getGLStyle(){
            if(kind==null)return super.getGLStyle();var theme=getGLTheme();
            if(cachedTheme!=theme||cachedStyle==null){cachedTheme=theme;cachedStyle=ApplicationTheme.current().nodeStyle(theme,kind);}return cachedStyle;
        }
    }
}

