package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.GLNodeGraphAsset;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

/** Public input editors, with explicit property coupling for editors without sockets. */
final class GroupInputSettings extends GLContainer {
    static final String MODE="easyknack.editorMode", RATE="easyknack.portRate", BUTTON="easyknack.button";
    enum Kind { NONE("No editor"), SLIDER("Slider"), CHECKBOX("Checkbox"), BUTTON("Button"), COMBO("Combobox"), COPY("Copy Editor");
        final String title;Kind(String title){this.title=title;}public String toString(){return AppText.literal(title);}}
    record Source(String type,String property,String label,Editor value,List<GLNodeEndpoint> endpoints){GLNodeEndpoint endpoint(){return endpoints.get(0);}public String toString(){return label;}}
    private final GLCompositeInterfaceEditor editor;
    private final GLCompositeGraphRegistry groups;
    private final EditorTemplates templates;
    private final List<GLContainer> rows=new ArrayList<>(),unitRows=new ArrayList<>();
    final GLComboBox<ProcessingRate> rate=new GLComboBox<>();
    final GLComboBox<Kind> kind=new GLComboBox<>();
    final GLComboBox<EditorTemplates.Template> model=new GLComboBox<>();
    final GLComboBox<Source> source=new GLComboBox<>();
    private final GLLabel rateLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Rate")),kindLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Editor")),modelLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Model")),sourceLabel=new GLLabel(com.vin.easyknack.ui.AppText.text("group.editor.source"));
    private final GLTextEditor minimum=new GLTextEditor(),maximum=new GLTextEditor(),value=new GLTextEditor();
    private final GLLabel minLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Minimum (empty = unbounded)")),maxLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Maximum (empty = unbounded)")),valueLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Value")),unitsLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Allowed units"));
    final GLCheckBox integer=new GLCheckBox(AppText.text("group.slider.integer"));
    private final GLCheckBox checked=new GLCheckBox(com.vin.easyknack.ui.AppText.literal("Default: on"));
    private final GLLabel status=new GLLabel("");
    private final NodeTooltipLabel formulaName=new NodeTooltipLabel(AppText.text("formula.identifier"));
    private final GLTextEditor formulaIdentifier=new GLTextEditor();
    final GLComboBox<EditorTemplates.Entry> defaultChoice=new GLComboBox<>();
    private final GLLabel styleLabel=new GLLabel(AppText.literal("Slider style")),initialLabel=new GLLabel(AppText.literal("Default unit"));
    enum SliderStyle { BAR, BUTTONS;public String toString(){return AppText.literal(this==BAR?"Bar":"Buttons");}}
    final GLComboBox<SliderStyle> sliderStyle=new GLComboBox<>(List.of(SliderStyle.values()));
    final GLComboBox<UnitFormat> initialUnit=new GLComboBox<>();
    private boolean updating;
    GroupInputSettings(GLCompositeInterfaceEditor editor,GLCompositeGraphRegistry groups,EditorTemplates templates,com.vingame.glswing.history.GLHistoryManager history,Runnable changed){
        this.editor=editor;this.groups=groups;this.templates=templates;
        for(var c:List.of(formulaName,formulaIdentifier,rateLabel,rate,kindLabel,kind,modelLabel,model,sourceLabel,source,styleLabel,sliderStyle,integer,minLabel,minimum,maxLabel,maximum,valueLabel,value,checked,unitsLabel,defaultChoice,initialLabel,initialUnit,status)){rows.add(c);addGL(c);}
        formulaIdentifier.addActionListener(e->edit(()->{var before=asset();if(!FormulaGroups.isFormula(before)||editor.selectedPort()==null)return;var after=FormulaGroups.rename(before,editor.selectedPort(),formulaIdentifier.getText().strip());history.perform(AppText.text("formula.identifier"),()->{groups.replaceAsset(after);editor.refresh();changed.run();},()->{groups.replaceAsset(before);editor.refresh();changed.run();});}));
        defaultChoice.addActionListener(e->edit(()->{var p=editor.selectedPort();var t=GroupInputs.template(p);var selected=defaultChoice.getSelectedItem();if(t==null||selected==null)return;var options=new HashMap<>(p.presentation().editorOptions());options.put("template",new EditorTemplates.Template(t.id(),t.label(),t.entries(),selected.id()).value());save(p.presentation().inputEditorId(),options,selectedKind(p));}));
        rate.setToolTipText(ProcessingRate.editorHint());
        rate.addActionListener(e->edit(()->chooseRate()));kind.addActionListener(e->edit(()->chooseKind()));
        model.addActionListener(e->edit(()->{if(model.getSelectedItem()!=null)applyModel(model.getSelectedItem(),Kind.COMBO,null);}));
        checked.addActionListener(e->edit(()->{var values=new HashMap<>(editor.selectedPort().presentation().editorOptions());values.put("default",GLNodeValue.of(checked.isSelected()?1.0:0.0));save(GLNodeInputEditorRegistry.SLIDER,values,selectedKind(editor.selectedPort()));}));
        source.addActionListener(e->edit(this::copySource));
        integer.addActionListener(e->edit(()->{var p=editor.selectedPort();save(p.presentation().inputEditorId(),integerOptions(p.presentation().editorOptions(),integer.isSelected()),selectedKind(p));}));
        sliderStyle.addActionListener(e->edit(()->{var p=editor.selectedPort();if(p==null||sliderStyle.getSelectedItem()==null)return;var options=new HashMap<>(p.presentation().editorOptions());options.put("presentation",GLNodeValue.of(sliderStyle.getSelectedItem()==SliderStyle.BAR?"bar":"buttons"));save(p.presentation().inputEditorId(),options,selectedKind(p));}));
        initialUnit.addActionListener(e->edit(()->{var p=editor.selectedPort();var selected=initialUnit.getSelectedItem();if(p==null||selected==null)return;var options=new HashMap<>(p.presentation().editorOptions());var base=GroupUnits.base(groups,asset().compositeDescriptor(),p);var allowed=new ArrayList<>(GroupUnits.allowed(p,base));if(!base.compatible(selected)){if(selectedKind(p)==Kind.COPY)return;base=selected;allowed=new ArrayList<>(selected.compatible());}if(!allowed.contains(selected))allowed.add(selected);options.put(GroupUnits.BASE,GLNodeValue.of(base.id()));options.put(GroupUnits.ALLOWED,GLNodeValue.of(allowed.stream().map(UnitFormat::id).toList()));options.put(GroupUnits.INITIAL,GLNodeValue.of(selected.id()));save(p.presentation().inputEditorId(),options,selectedKind(p));}));
        for(var field:List.of(minimum,maximum,value))field.addActionListener(e->edit(()->sliderValues(field)));
    }
    private void edit(Runnable action){if(updating)return;updating=true;try{action.run();status.setText("");}catch(RuntimeException ex){status.setText(ex.getMessage());}finally{updating=false;refresh();editor.refresh();}}
    private GLNodeGraphAsset asset(){return editor.getAssetId()==null?null:groups.findAsset(editor.getAssetId()).orElse(null);}
    static ProcessingRate selectedRate(GLCompositePortMapping p){return SignalCategory.TRIGGER.valueTypeId().equals(p.publicPort().valueTypeId())?ProcessingRate.EVENT:!p.presentation().socketVisible()?ProcessingRate.CONTROL:ProcessingRate.fromId(new NodeState(p.presentation().editorOptions()).text(RATE,"auto"));}
    static Kind selectedKind(GLCompositePortMapping p){var s=new NodeState(p.presentation().editorOptions());String mode=s.text(MODE,"");if(!mode.isEmpty())return Kind.valueOf(mode);return p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE)?Kind.NONE:checkbox(p)?Kind.CHECKBOX:p.presentation().inputEditorId().equals(BUTTON)?Kind.BUTTON:GroupInputs.choice(p)?Kind.COMBO:Kind.SLIDER;}
    static boolean checkbox(GLCompositePortMapping p){return p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)&&new NodeState(p.presentation().editorOptions()).text("presentation","").equals("checkbox");}
    private static Map<String,GLNodeValue> checkboxOptions(boolean checked){var values=new HashMap<>(defaults());values.put("presentation",GLNodeValue.of("checkbox"));values.put("default",GLNodeValue.of(checked?1.0:0.0));values.put(GroupUnits.BASE,GLNodeValue.of("factor"));values.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of("factor")));return values;}
    private boolean numeric(GLCompositePortMapping p){return List.of(SignalCategory.AUDIO,SignalCategory.VALUE,SignalCategory.WAVETABLE,SignalCategory.TRIGGER).stream().anyMatch(c->c.valueTypeId().equals(p.publicPort().valueTypeId()));}
    private void save(String id,Map<String,GLNodeValue> options,Kind mode){
        save(editor.selectedPort(),id,options,mode);
    }
    private void save(GLCompositePortMapping p,String id,Map<String,GLNodeValue> options,Kind mode){
        if(p==null)return;var values=new HashMap<>(options);values.put(MODE,GLNodeValue.of(mode.name()));
        for(String key:List.of(FormulaGroups.ID,FormulaGroups.NAME))if(p.presentation().editorOptions().containsKey(key))values.put(key,p.presentation().editorOptions().get(key));
        var requested=rate.getSelectedItem()==null?selectedRate(p):rate.getSelectedItem();values.put(RATE,GLNodeValue.of(requested.id()));
        String type=id.equals(GroupVectorEditors.ID)?SignalCategory.VECTOR.valueTypeId():requested==ProcessingRate.EVENT?SignalCategory.TRIGGER.valueTypeId():p.publicPort().valueTypeId();
        if(requested!=ProcessingRate.EVENT&&type.equals(SignalCategory.TRIGGER.valueTypeId()))type=SignalCategory.VALUE.valueTypeId();
        var publicPort=p.publicPort();var replacement=new GLCompositePortMapping(new GLNodePortDescriptor(publicPort.id(),publicPort.direction(),type,publicPort.timing(),false,publicPort.maximumConnections()),values.containsKey(GroupEditorCopies.TARGETS)?null:p.internalEndpoint(),new GLCompositePortPresentation(id,!values.containsKey(GroupEditorCopies.TARGETS)&&(mode==Kind.NONE||p.presentation().socketVisible()),values),values.containsKey(GroupEditorCopies.TARGETS)?List.of():p.additionalEndpoints());
        var linked=controlledTargets(replacement);
        if(asset().compositeDescriptor().portMappings().stream().filter(other->!other.publicPort().id().equals(p.publicPort().id())).flatMap(other->controlledTargets(other).stream()).anyMatch(linked::contains))throw new IllegalArgumentException(AppText.text("group.editor.duplicate"));
        editor.updateSelectedPort(GroupInputs.normalize(replacement,asset(),groups));
    }
    private void chooseRate(){
        var p=editor.selectedPort();if(p==null)return;
        if(selectedKind(p)==Kind.NONE)save(GLNodeInputEditorRegistry.NONE,Map.of(),Kind.NONE);
        else if(rate.getSelectedItem()==ProcessingRate.EVENT)save(BUTTON,Map.of(),Kind.BUTTON);
        else if(selectedRate(p)==ProcessingRate.EVENT)save(GLNodeInputEditorRegistry.SLIDER,defaults(),Kind.SLIDER);
        else save(p.presentation().inputEditorId(),p.presentation().editorOptions(),selectedKind(p));
    }
    private static Map<String,GLNodeValue> defaults(){return Map.of("default",GLNodeValue.of(0.),"minimum",GLNodeValue.of(0.),"maximum",GLNodeValue.of(1.),"minimumEnabled",GLNodeValue.of(true),"maximumEnabled",GLNodeValue.of(true),"presentation",GLNodeValue.of("bar"));}
    private void chooseKind(){
        var k=kind.getSelectedItem();if(k==null)return;
        switch(k){
            case NONE->save(GLNodeInputEditorRegistry.NONE,Map.of(),k);
            case SLIDER->save(GLNodeInputEditorRegistry.SLIDER,defaults(),k);
            case CHECKBOX->save(GLNodeInputEditorRegistry.SLIDER,checkboxOptions(false),k);
            case BUTTON->save(BUTTON,Map.of(),k);
            case COMBO->{if(!templates.all().isEmpty())applyModel(templates.all().get(0),k,null);else save("easyknack.choice",Map.of(),k);}
            case COPY->{var p=editor.selectedPort();if(p.isBound()){var copied=GroupEditorCopies.copy(p,p.internalEndpoint(),asset(),groups);save(copied.presentation().inputEditorId(),copied.presentation().editorOptions(),k);break;}var candidates=sources();if(candidates.isEmpty()){save(p.presentation().inputEditorId(),p.presentation().editorOptions(),k);}else {copy(candidates.get(0));}}
        }
    }
    private void applyModel(EditorTemplates.Template t,Kind k,Source copied){
        var p=editor.selectedPort();var options=new HashMap<String,GLNodeValue>();options.put("template",t.value());options.put("autoBind",GLNodeValue.of(true));options.put(GroupInputs.CHOICE,GLNodeValue.of(GroupInputs.allocateProperty(p)));
        if(copied!=null)options.put("copySource",GLNodeValue.of(copied.endpoint().toString()));
        save("easyknack.choice",options,k);
    }
    private void copySource(){if(source.getSelectedItem()!=null)copy(source.getSelectedItem());}
    private void copy(Source s){
        var used=asset().compositeDescriptor().portMappings().stream().filter(p->!p.publicPort().id().equals(editor.selectedPort().publicPort().id())).flatMap(p->controlledTargets(p).stream()).collect(java.util.stream.Collectors.toSet());
        var endpoint=s.endpoints().stream().filter(e->!used.contains(e)).findFirst().orElseThrow(()->new IllegalArgumentException(AppText.text("group.editor.duplicate")));
        var copied=GroupEditorCopies.copy(editor.selectedPort(),endpoint,asset(),groups);
        save(copied,copied.presentation().inputEditorId(),copied.presentation().editorOptions(),Kind.COPY);
    }
    private static List<GLNodeEndpoint> controlledTargets(GLCompositePortMapping port){return java.util.stream.Stream.concat(GroupEditorCopies.targets(port).stream(),port.internalEndpoints().stream()).distinct().toList();}
    private boolean linkableSource(GLGraphNode node,String property){
        return NodeRegistry.find(node.typeId()).map(d->d.layout(new NodeState(node.properties())).rows().stream().anyMatch(r->r.id().equals(property)&&GroupEditorCopies.linkable(r)))
            .orElseGet(()->groups.findComposite(node.typeId()).map(d->GroupEditorCopies.groupPort(d,property)).filter(Objects::nonNull).filter(GroupInputs::editorOnly).isPresent());
    }
    private String sourceLabel(GLGraphNode node,String property){
        var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition!=null)return definition.title()+" / "+property;
        var descriptor=groups.findComposite(node.typeId()).orElseThrow();var port=GroupEditorCopies.groupPort(descriptor,property);
        return new NodeState(groups.findAsset(descriptor.assetId()).orElseThrow().metadata()).text("easyknack.title","Group")+" / "+port.publicPort().id();
    }
    private List<Source> sources(){return asset()==null||editor.selectedPort()==null?List.of():availableSources(asset(),selectedRate(editor.selectedPort()),editor.selectedPort().publicPort().id(),groups);}
    static List<Source> availableSources(GLNodeGraphAsset asset,ProcessingRate rate,String selectedPort){
        return availableSources(asset,rate,selectedPort,null);
    }
    static List<Source> availableSources(GLNodeGraphAsset asset,ProcessingRate rate,String selectedPort,GLCompositeGraphRegistry groups){
        var used=asset.compositeDescriptor().portMappings().stream().filter(p->!p.publicPort().id().equals(selectedPort))
            .flatMap(p->controlledTargets(p).stream()).collect(java.util.stream.Collectors.toSet());
        return availableSources(asset,rate,groups).stream().map(s->new Source(s.type(),s.property(),s.label(),s.value(),s.endpoints().stream().filter(e->!used.contains(e)).toList()))
            .filter(s->!s.endpoints().isEmpty()).toList();
    }
    static List<Source> availableSources(GLNodeGraphAsset asset,ProcessingRate rate){
        return availableSources(asset,rate,(GLCompositeGraphRegistry)null);
    }
    static Editor sourceEditor(GLCompositePortMapping port,GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        if(port.publicPort().direction()!=GLPortDirection.INPUT)return null;
        var template=GroupInputs.template(port);if(template!=null)return template.editor();
        if(GroupInputs.choice(port))return GroupChoices.choices(asset,groups).stream().filter(c->c.id().equals(GroupInputs.property(port))).map(c->(Editor)c.editor()).findFirst().orElse(null);
        if(GroupVectorEditors.isVector(port))return GroupVectorEditors.editor(port);
        if(port.presentation().inputEditorId().equals(BUTTON))return new Editor.Trigger();
        if(!port.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER))return null;
        var state=new NodeState(port.presentation().editorOptions());
        if(checkbox(port))return new Editor.CheckBox(state.number("default",0)!=0);
        return Editor.Slider.button(state.number("default",0),state.flag("minimumEnabled",true)?state.number("minimum",0):null,state.flag("maximumEnabled",true)?state.number("maximum",1):null);
    }
    private static List<Source> availableSources(GLNodeGraphAsset asset,ProcessingRate rate,GLCompositeGraphRegistry groups){
        return GLNodeEditorSourceCatalog.<Editor>collect(asset.graph(), n->{
            var def=NodeRegistry.find(n.typeId()).orElse(null);
            if(def==null){
                var descriptor=groups==null?null:groups.findComposite(n.typeId()).orElse(null);if(descriptor==null)return List.of();
                var child=groups.findAsset(descriptor.assetId()).orElseThrow();
                var result=new ArrayList<GLNodeEditorSourceCatalog.Candidate<Editor>>();
                for(var stored:descriptor.portMappings()){
                    var port=MultipleGroups.exposedPort(stored);var value=sourceEditor(port,child,groups);if(value==null)continue;
                    var kind=value instanceof Editor.ComboBox<?>?GLNodeEditorCapabilities.Kind.CHOICE:value instanceof Editor.Trigger?GLNodeEditorCapabilities.Kind.TRIGGER:value instanceof Editor.CheckBox?GLNodeEditorCapabilities.Kind.CHECKBOX:value instanceof VectorEditor?GLNodeEditorCapabilities.Kind.CUSTOM:GLNodeEditorCapabilities.Kind.SLIDER;
                    var property=GroupInputs.editorOnly(port)?GroupEditorCopies.groupProperty(port):port.publicPort().id();
                    // Public group controls can be copied even when their input socket remains visible.
                    result.add(new GLNodeEditorSourceCatalog.Candidate<>(property,new NodeState(child.metadata()).text("easyknack.title","Group")+" / "+port.publicPort().id(),value,new GLNodeEditorCapabilities(kind,true,true,value.defaultValue(),Map.of())));
                }
                return result;
            }
            return def.layout(new NodeState(n.properties())).rows().stream().map(row->
                new GLNodeEditorSourceCatalog.Candidate<Editor>(row.id(),def.title()+" / "+row.label(),row.editor(),GroupEditorCapabilities.of(row))).toList();
        },capability->(rate==ProcessingRate.EVENT)==(capability.kind()==GLNodeEditorCapabilities.Kind.TRIGGER))
        .stream().map(s->new Source(s.type(),s.property(),s.label(),s.editor(),s.endpoints())).toList();
    }

    static Map<String,GLNodeValue> integerOptions(Map<String,GLNodeValue> source,boolean enabled){
        var options=new HashMap<>(source);var state=new NodeState(source);
        double min=state.flag("minimumEnabled",true)?state.number("minimum",0):Double.NEGATIVE_INFINITY;
        double max=state.flag("maximumEnabled",true)?state.number("maximum",1):Double.POSITIVE_INFINITY;
        if(enabled&&Math.ceil(min)>Math.floor(max))throw new IllegalArgumentException(AppText.text("group.slider.noInteger"));
        options.put("numericType",GLNodeValue.of(enabled?"integer":"float"));
        options.put("step",GLNodeValue.of(enabled?Math.max(1,state.number("step",.01)):.01));
        if(enabled)options.put("default",GLNodeValue.of(Math.max(Math.ceil(min),Math.min(Math.floor(max),Math.rint(state.number("default",0))))));
        return options;
    }
    private void sliderValues(GLTextEditor field){
        var p=editor.selectedPort();var options=new HashMap<>(p.presentation().editorOptions());
        String key=field==minimum?"minimum":field==maximum?"maximum":"default";
        Double entered=field.getText().isBlank()&&!key.equals("default")?null:Double.valueOf(field.getText());
        if(selectedKind(p)==Kind.COPY){
            var base=GroupUnits.base(groups,asset().compositeDescriptor(),p);
            entered=GroupUnits.initial(p,base).convert(entered,base);
            var state=new NodeState(options);
            if(state.flag("minimumEnabled",true))entered=Math.max(state.number("minimum",0),entered);
            if(state.flag("maximumEnabled",true))entered=Math.min(state.number("maximum",1),entered);
        }
        save(GLNodeInputEditorRegistry.SLIDER,GroupSliderBounds.adjust(options,key,entered),selectedKind(p));
    }
    private ProcessingRate inheritedRate(GLCompositePortMapping p){
        var endpoints=p.isBound()?p.internalEndpoints():GroupEditorCopies.targets(p);
        ProcessingRate result=ProcessingRate.CONTROL;
        for(var endpoint:endpoints){var n=asset().graph().findNode(endpoint.nodeId()).orElse(null);if(n==null)continue;var def=NodeRegistry.find(n.typeId()).orElse(null);if(def==null)continue;
            var row=def.layout(new NodeState(n.properties())).rows().stream().filter(r->r.id().equals(endpoint.portId())).findFirst().orElse(null);if(row==null)continue;
            var current=row.editor() instanceof Editor.Trigger?ProcessingRate.EVENT:row.kind()==NodeBuilder.RowKind.EDITOR?ProcessingRate.CONTROL:row.adaptive()?ProcessingRate.AUTO:row.rate();
            if(current==ProcessingRate.AUDIO||current==ProcessingRate.EVENT)return current;if(current==ProcessingRate.AUTO)result=current;
        }return result;
    }
    private void unitChoices(GLCompositePortMapping p,boolean copied){
        var base=GroupUnits.base(groups,asset().compositeDescriptor(),p);var allowed=GroupUnits.allowed(p,base);var initial=GroupUnits.initial(p,base);
        initialLabel.setVisible(true);initialUnit.setVisible(true);initialUnit.removeAllItems();
        var units=copied?allowed:List.of(UnitFormat.values());
        if(copied)units.forEach(initialUnit::addItem);
        else for(var dimension:units.stream().map(u->u.dimension().toString()).distinct().toList()){
            var menu=initialUnit.addSubMenu(AppText.text("unit.group."+dimension));units.stream().filter(u->u.dimension().equals(dimension)).forEach(menu::addItem);
        }initialUnit.setSelectedItem(initial);
        if(copied)return;unitsLabel.setVisible(true);
        for(var unit:base.compatible()){var box=new GLCheckBox(unit.toString());box.setSelected(allowed.contains(unit));box.setEnabled(unit!=initial);box.addActionListener(e->edit(()->{var options=new HashMap<>(editor.selectedPort().presentation().editorOptions());var next=new ArrayList<>(allowed);if(box.isSelected()){if(!next.contains(unit))next.add(unit);}else next.remove(unit);options.put(GroupUnits.BASE,GLNodeValue.of(base.id()));options.put(GroupUnits.ALLOWED,GLNodeValue.of(next.stream().map(UnitFormat::id).toList()));save(GLNodeInputEditorRegistry.SLIDER,options,selectedKind(editor.selectedPort()));}));unitRows.add(box);addGL(box);}
    }
    private void showReadOnlyRate(ProcessingRate value){
        rate.removeAllItems();rate.addItem(value);rate.setSelectedItem(value);rate.setEnabled(false);
    }
    void refresh(){
        if(updating)return;updating=true;try{
            var p=editor.selectedPort();boolean input=p!=null&&p.publicPort().direction()==GLPortDirection.INPUT;
            for(var r:rows)r.setVisible(false);for(var r:unitRows)removeGL(r);unitRows.clear();
            if(p!=null&&FormulaGroups.isFormula(asset())){formulaIdentifier.setText(FormulaGroups.codeName(p));formulaIdentifier.setVisible(true);formulaName.setToolTipText(AppText.text("formula.identifierHint"));formulaName.setVisible(true);}
            status.setVisible(!status.getText().isEmpty());if(!input){resized(getWidth(),getHeight());return;}
            rateLabel.setVisible(true);rate.setVisible(true);kindLabel.setVisible(true);kind.setVisible(true);status.setVisible(!status.getText().isEmpty());
            rate.removeAllItems();(numeric(p)?List.of(ProcessingRate.AUTO,ProcessingRate.AUDIO,ProcessingRate.CONTROL,ProcessingRate.EVENT):List.of(selectedRate(p))).forEach(rate::addItem);rate.setSelectedItem(selectedRate(p));rate.setEnabled(numeric(p));
            var choices=numeric(p)?selectedRate(p)==ProcessingRate.EVENT?List.of(Kind.NONE,Kind.BUTTON,Kind.COPY):List.of(Kind.NONE,Kind.SLIDER,Kind.CHECKBOX,Kind.COMBO,Kind.COPY):List.of(Kind.NONE,Kind.COPY);
            kind.removeAllItems();choices.forEach(kind::addItem);var mode=choices.contains(selectedKind(p))?selectedKind(p):choices.get(0);kind.setSelectedItem(mode);
            if(mode==Kind.COPY){showReadOnlyRate(inheritedRate(p));}
            if(!p.presentation().socketVisible()){showReadOnlyRate(selectedRate(p)==ProcessingRate.EVENT?ProcessingRate.EVENT:ProcessingRate.CONTROL);}
            var state=new NodeState(p.presentation().editorOptions());
            if(mode==Kind.COMBO){modelLabel.setVisible(true);model.setVisible(true);model.removeAllItems();templates.all().forEach(model::addItem);var applied=GroupInputs.template(p);if(applied!=null){var found=model.getItems().stream().filter(t->t.id().equals(applied.id())).findFirst().orElse(null);if(found==null){model.addItem(applied);found=applied;}model.setSelectedItem(found);}else model.setSelectedItem(null);model.setToolTipText(com.vin.easyknack.ui.AppText.literal("Choose a model defined in Editors. Its values follow the internal connections."));}
            if(mode==Kind.COPY){sourceLabel.setVisible(true);source.setVisible(true);source.removeAllItems();sources().forEach(source::addItem);source.getItems().stream().filter(s->s.type().equals(state.text(GroupEditorCopies.SOURCE_TYPE,""))&&s.property().equals(state.text(GroupEditorCopies.SOURCE_PROPERTY,""))||s.endpoints().stream().anyMatch(e->e.toString().equals(state.text("copySource","")))).findFirst().ifPresent(source::setSelectedItem);source.setEnabled(!source.getItems().isEmpty());source.setVisible(!p.isBound()&&!source.getItems().isEmpty());sourceLabel.setVisible(source.isVisible());}
            if(mode==Kind.CHECKBOX||mode==Kind.COPY&&checkbox(p)){checked.setVisible(true);checked.setSelected(state.number("default",0)!=0);}
            if(mode==Kind.SLIDER&&p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)&&!checkbox(p)){
                integer.setVisible(true);integer.setSelected(state.text("numericType","float").equals("integer"));styleLabel.setVisible(true);sliderStyle.setVisible(true);sliderStyle.setSelectedItem(state.text("presentation","bar").equals("bar")?SliderStyle.BAR:SliderStyle.BUTTONS);

            }
            var targets=GroupEditorCopies.targets(p);
            if(mode==Kind.COPY&&!targets.isEmpty()){
                var heading=new GLLabel(AppText.text("group.editor.targets"));unitRows.add(heading);addGL(heading);
                var first=targets.stream().filter(t->asset().graph().findNode(t.nodeId()).isPresent()).findFirst().orElse(targets.get(0));var original=asset().graph().findNode(first.nodeId()).orElse(null);
                if(original!=null)for(var n:asset().graph().nodes())if(n.typeId().equals(original.typeId())&&linkableSource(n,first.portId())){
                    var endpoint=new GLNodeEndpoint(n.id(),first.portId());var box=new GLCheckBox(sourceLabel(n,first.portId())+" · "+n.id().toString().substring(0,8));box.setSelected(targets.contains(endpoint));
                    box.addActionListener(e->edit(()->{var next=new ArrayList<>(targets);if(box.isSelected()){if(!next.contains(endpoint))next.add(endpoint);}else{if(next.size()==1)return;next.remove(endpoint);}var opts=new HashMap<>(editor.selectedPort().presentation().editorOptions());opts.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(next));save(p.presentation().inputEditorId(),opts,Kind.COPY);}));unitRows.add(box);addGL(box);
                }
            }
            if(mode==Kind.SLIDER&&p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)&&!checkbox(p)){for(var r:List.of(minLabel,minimum,maxLabel,maximum,valueLabel,value,unitsLabel))r.setVisible(true);minimum.setText(state.flag("minimumEnabled",true)?Double.toString(state.number("minimum",0)):"");maximum.setText(state.flag("maximumEnabled",true)?Double.toString(state.number("maximum",1)):"");value.setText(Double.toString(state.number("default",0)));
                unitChoices(p,false);
            }
            if(mode==Kind.COPY && GroupVectorEditors.isVector(p)){
                var vector=GroupVectorEditors.editor(p).create(AppText.literal("Default"),v->edit(()->{var options=new HashMap<>(editor.selectedPort().presentation().editorOptions());options.put("default",v);save(GroupVectorEditors.ID,options,Kind.COPY);}));
                unitRows.add(vector.component());addGL(vector.component());
            }
            if(mode==Kind.COPY){
                var t=GroupInputs.template(p);if(t!=null){valueLabel.setVisible(true);defaultChoice.setVisible(true);defaultChoice.removeAllItems();t.entries().forEach(defaultChoice::addItem);defaultChoice.setSelectedItem(t.entries().stream().filter(e->e.id().equals(t.initial())).findFirst().orElseThrow());}
                else if(p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)&&!checkbox(p)){
                    valueLabel.setVisible(true);value.setVisible(true);var base=GroupUnits.base(groups,asset().compositeDescriptor(),p);value.setText(Double.toString(base.convert(state.number("default",0),GroupUnits.initial(p,base))));unitChoices(p,true);
                }
            }

            resized(getWidth(),getHeight());
        }finally{updating=false;}
    }
    @Override protected void resized(float w,float h){float y=0;for(var row:rows)if(row.isVisible()){row.setBounds(0,y,w,28);y+=32;}for(var row:unitRows){float height=row instanceof GLSliderGroup?row.getPreferredGLSize().height():24;row.setBounds(0,y,w,height);y+=height+2;}setPreferredGLSize(260,y);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
}
