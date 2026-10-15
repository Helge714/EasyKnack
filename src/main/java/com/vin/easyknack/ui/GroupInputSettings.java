package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.GLNodeGraphAsset;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

/** One editor per public input. Its internal wires are the only editable routing. */
final class GroupInputSettings extends GLContainer {
    static final String MODE="easyknack.editorMode", RATE="easyknack.portRate", BUTTON="easyknack.button";
    enum Kind { SLIDER("Slider"), CHECKBOX("Checkbox"), BUTTON("Button"), COMBO("Combobox"), COPY("Copy Editor");
        final String title;Kind(String title){this.title=title;}public String toString(){return AppText.literal(title);}}
    record Source(GLNodeEndpoint endpoint,String label,Editor value,GLCompositePortPresentation presentation){public String toString(){return label;}}
    private final GLCompositeInterfaceEditor editor;
    private final GLCompositeGraphRegistry groups;
    private final EditorTemplates templates;
    private final List<GLContainer> rows=new ArrayList<>(),unitRows=new ArrayList<>();
    final GLComboBox<ProcessingRate> rate=new GLComboBox<>();
    final GLComboBox<Kind> kind=new GLComboBox<>();
    final GLComboBox<EditorTemplates.Template> model=new GLComboBox<>();
    final GLComboBox<Source> source=new GLComboBox<>();
    private final GLLabel rateLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Rate")),kindLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Editor")),modelLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Model")),sourceLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Connected editor"));
    private final GLTextEditor minimum=new GLTextEditor(),maximum=new GLTextEditor(),value=new GLTextEditor();
    private final GLLabel minLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Minimum (empty = unbounded)")),maxLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Maximum (empty = unbounded)")),valueLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Value")),unitsLabel=new GLLabel(com.vin.easyknack.ui.AppText.literal("Allowed units"));
    final GLCheckBox integer=new GLCheckBox(AppText.text("group.slider.integer"));
    private final GLCheckBox checked=new GLCheckBox(com.vin.easyknack.ui.AppText.literal("Default: on"));
    private final GLLabel status=new GLLabel("");
    private final NodeTooltipLabel formulaName=new NodeTooltipLabel(AppText.text("formula.identifier"));
    private final GLTextEditor formulaIdentifier=new GLTextEditor();
    private final GLComboBox<UnitFormat> formulaUnitBase=new GLComboBox<>(List.of(UnitFormat.values()));
    private boolean updating;
    GroupInputSettings(GLCompositeInterfaceEditor editor,GLCompositeGraphRegistry groups,EditorTemplates templates,com.vingame.glswing.history.GLHistoryManager history,Runnable changed){
        this.editor=editor;this.groups=groups;this.templates=templates;
        for(var c:List.of(formulaName,formulaIdentifier,rateLabel,rate,kindLabel,kind,modelLabel,model,sourceLabel,source,integer,minLabel,minimum,maxLabel,maximum,valueLabel,value,checked,unitsLabel,formulaUnitBase,status)){rows.add(c);addGL(c);}
        formulaIdentifier.addActionListener(e->edit(()->{var before=asset();if(!FormulaGroups.isFormula(before)||editor.selectedPort()==null)return;var after=FormulaGroups.rename(before,editor.selectedPort(),formulaIdentifier.getText().strip());history.perform(AppText.text("formula.identifier"),()->{groups.replaceAsset(after);editor.refresh();changed.run();},()->{groups.replaceAsset(before);editor.refresh();changed.run();});}));
        formulaUnitBase.addActionListener(e->edit(()->{if(formulaUnitBase.getSelectedItem()==null||editor.selectedPort()==null)return;var options=new HashMap<>(editor.selectedPort().presentation().editorOptions());String unit=formulaUnitBase.getSelectedItem().id();options.put(GroupUnits.BASE,GLNodeValue.of(unit));options.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of(unit)));save(GLNodeInputEditorRegistry.SLIDER,options,Kind.SLIDER);}));
        rate.setToolTipText(ProcessingRate.editorHint());
        rate.addActionListener(e->edit(()->chooseRate()));kind.addActionListener(e->edit(()->chooseKind()));
        model.addActionListener(e->edit(()->{if(model.getSelectedItem()!=null)applyModel(model.getSelectedItem(),Kind.COMBO,null);}));
        checked.addActionListener(e->edit(()->{var values=new HashMap<>(editor.selectedPort().presentation().editorOptions());values.put("default",GLNodeValue.of(checked.isSelected()?1.0:0.0));save(GLNodeInputEditorRegistry.SLIDER,values,Kind.CHECKBOX);}));
        source.addActionListener(e->edit(this::copySource));
        integer.addActionListener(e->edit(()->{var p=editor.selectedPort();save(p.presentation().inputEditorId(),integerOptions(p.presentation().editorOptions(),integer.isSelected()),selectedKind(p));}));
        for(var field:List.of(minimum,maximum,value))field.addActionListener(e->edit(this::sliderValues));
    }
    private void edit(Runnable action){if(updating)return;updating=true;try{action.run();status.setText("");}catch(RuntimeException ex){status.setText(ex.getMessage());}finally{updating=false;refresh();editor.refresh();}}
    private GLNodeGraphAsset asset(){return editor.getAssetId()==null?null:groups.findAsset(editor.getAssetId()).orElse(null);}
    static ProcessingRate selectedRate(GLCompositePortMapping p){return SignalCategory.TRIGGER.valueTypeId().equals(p.publicPort().valueTypeId())?ProcessingRate.EVENT:ProcessingRate.fromId(new NodeState(p.presentation().editorOptions()).text(RATE,"auto"));}
    static Kind selectedKind(GLCompositePortMapping p){var s=new NodeState(p.presentation().editorOptions());String mode=s.text(MODE,"");if(!mode.isEmpty())return Kind.valueOf(mode);return checkbox(p)?Kind.CHECKBOX:p.presentation().inputEditorId().equals(BUTTON)?Kind.BUTTON:GroupInputs.choice(p)?Kind.COMBO:Kind.SLIDER;}
    static boolean checkbox(GLCompositePortMapping p){return p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)&&new NodeState(p.presentation().editorOptions()).text("presentation","").equals("checkbox");}
    private static Map<String,GLNodeValue> checkboxOptions(boolean checked){var values=new HashMap<>(defaults());values.put("presentation",GLNodeValue.of("checkbox"));values.put("default",GLNodeValue.of(checked?1.0:0.0));values.put(GroupUnits.BASE,GLNodeValue.of("factor"));values.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of("factor")));return values;}
    private boolean numeric(GLCompositePortMapping p){return List.of(SignalCategory.AUDIO,SignalCategory.VALUE,SignalCategory.WAVETABLE,SignalCategory.TRIGGER).stream().anyMatch(c->c.valueTypeId().equals(p.publicPort().valueTypeId()));}
    private void save(String id,Map<String,GLNodeValue> options,Kind mode){
        var p=editor.selectedPort();if(p==null)return;var values=new HashMap<>(options);values.put(MODE,GLNodeValue.of(mode.name()));
        for(String key:List.of(FormulaGroups.ID,FormulaGroups.NAME))if(p.presentation().editorOptions().containsKey(key))values.put(key,p.presentation().editorOptions().get(key));
        var requested=rate.getSelectedItem()==null?selectedRate(p):rate.getSelectedItem();values.put(RATE,GLNodeValue.of(requested.id()));
        String type=requested==ProcessingRate.EVENT?SignalCategory.TRIGGER.valueTypeId():p.publicPort().valueTypeId();
        if(requested!=ProcessingRate.EVENT&&type.equals(SignalCategory.TRIGGER.valueTypeId()))type=SignalCategory.VALUE.valueTypeId();
        var publicPort=p.publicPort();var replacement=new GLCompositePortMapping(new GLNodePortDescriptor(publicPort.id(),publicPort.direction(),type,publicPort.timing(),false,publicPort.maximumConnections()),p.internalEndpoint(),new GLCompositePortPresentation(id,p.presentation().socketVisible(),values),p.additionalEndpoints());
        editor.updateSelectedPort(GroupInputs.normalize(replacement,asset(),groups));
    }
    private void chooseRate(){
        var p=editor.selectedPort();if(p==null)return;
        if(rate.getSelectedItem()==ProcessingRate.EVENT)save(BUTTON,Map.of(),Kind.BUTTON);
        else if(selectedRate(p)==ProcessingRate.EVENT)save(GLNodeInputEditorRegistry.SLIDER,defaults(),Kind.SLIDER);
        else save(p.presentation().inputEditorId(),p.presentation().editorOptions(),selectedKind(p));
    }
    private static Map<String,GLNodeValue> defaults(){return Map.of("default",GLNodeValue.of(0.),"minimum",GLNodeValue.of(0.),"maximum",GLNodeValue.of(1.),"minimumEnabled",GLNodeValue.of(true),"maximumEnabled",GLNodeValue.of(true),"presentation",GLNodeValue.of("bar"));}
    private void chooseKind(){
        var k=kind.getSelectedItem();if(k==null)return;
        switch(k){
            case SLIDER->save(GLNodeInputEditorRegistry.SLIDER,defaults(),k);
            case CHECKBOX->save(GLNodeInputEditorRegistry.SLIDER,checkboxOptions(false),k);
            case BUTTON->save(BUTTON,Map.of(),k);
            case COMBO->{if(!templates.all().isEmpty())applyModel(templates.all().get(0),k,null);else save("easyknack.choice",Map.of(),k);}
            case COPY->{var candidates=sources();if(candidates.isEmpty()){var p=editor.selectedPort();save(p.presentation().inputEditorId(),p.presentation().editorOptions(),k);}else {copy(candidates.get(0));}}
        }
    }
    private void applyModel(EditorTemplates.Template t,Kind k,Source copied){
        var p=editor.selectedPort();var options=new HashMap<String,GLNodeValue>();options.put("template",t.value());options.put("autoBind",GLNodeValue.of(true));options.put(GroupInputs.CHOICE,GLNodeValue.of(GroupInputs.property(p)));
        if(copied!=null)options.put("copySource",GLNodeValue.of(copied.endpoint().toString()));
        save("easyknack.choice",options,k);
    }
    private void copySource(){if(source.getSelectedItem()!=null)copy(source.getSelectedItem());}
    private void copy(Source s){
        if(s.presentation()!=null&&GroupInputs.choice(new GLCompositePortMapping(editor.selectedPort().publicPort(),null,s.presentation()))){
            var node=asset().graph().findNode(s.endpoint().nodeId()).orElseThrow();var nested=groups.findComposite(node.typeId()).orElseThrow();
            var child=nested.portMappings().stream().filter(p->p.publicPort().id().equals(s.endpoint().portId())).findFirst().orElseThrow();
            var choice=GroupChoices.choices(groups.findAsset(nested.assetId()).orElseThrow(),groups).stream().filter(c->c.id().equals(GroupInputs.property(child))).findFirst().orElseThrow();
            applyModel(EditorTemplates.inherit(s.label(),choice.editor()),Kind.COPY,s);return;
        }
        if(s.presentation()!=null){var options=new HashMap<>(s.presentation().editorOptions());options.put("copySource",GLNodeValue.of(s.endpoint().toString()));if(GroupInputs.template(new GLCompositePortMapping(editor.selectedPort().publicPort(),null,s.presentation()))!=null)options.put(GroupInputs.CHOICE,GLNodeValue.of(GroupInputs.property(editor.selectedPort())));save(s.presentation().inputEditorId(),options,Kind.COPY);return;}
        if(s.value() instanceof Editor.ComboBox<?> combo){applyModel(EditorTemplates.inherit(s.label(),combo),Kind.COPY,s);return;}
        if(s.value() instanceof Editor.Trigger){save(BUTTON,Map.of("copySource",GLNodeValue.of(s.endpoint().toString())),Kind.COPY);return;}
        if(s.value() instanceof Editor.CheckBox c){save(GLNodeInputEditorRegistry.SLIDER,checkboxOptions(c.value()),Kind.COPY);return;}
        if(s.value() instanceof Editor.Toggle c){save(GLNodeInputEditorRegistry.SLIDER,checkboxOptions(c.value()),Kind.COPY);return;}
        Editor.Slider slider=s.value() instanceof UnitSlider u?u.spec():(Editor.Slider)s.value();
        var options=new HashMap<String,GLNodeValue>(defaults());options.put("default",slider.defaultValue());options.put("minimumEnabled",GLNodeValue.of(slider.min()!=null));options.put("maximumEnabled",GLNodeValue.of(slider.max()!=null));options.put("minimum",GLNodeValue.of(slider.min()==null?0:slider.min()));options.put("maximum",GLNodeValue.of(slider.max()==null?1:slider.max()));options.put("presentation",GLNodeValue.of(slider.bar()?"bar":"buttons"));options.put("numericType",GLNodeValue.of(slider.integral()?"integer":"float"));options.put("copySource",GLNodeValue.of(s.endpoint().toString()));
        if(s.value() instanceof UnitSlider u){options.put(GroupUnits.BASE,GLNodeValue.of(u.formats().stored().id()));options.put(GroupUnits.ALLOWED,GLNodeValue.of(u.formats().stored().compatible().stream().map(UnitFormat::id).toList()));}
        save(GLNodeInputEditorRegistry.SLIDER,options,Kind.COPY);
    }
    private List<Source> sources(){
        var a=asset();var p=editor.selectedPort();if(a==null||p==null)return List.of();var result=new ArrayList<Source>();
        for(var endpoint:p.internalEndpoints()){
            var n=a.graph().findNode(endpoint.nodeId()).orElse(null);if(n==null)continue;var nested=groups.findComposite(n.typeId()).orElse(null);
            if(nested!=null){var child=nested.portMappings().stream().filter(v->v.publicPort().id().equals(endpoint.portId())).findFirst().orElse(null);if(child!=null&&!child.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE))result.add(new Source(endpoint,endpoint.portId(),null,child.presentation()));continue;}
            var def=NodeRegistry.find(n.typeId()).orElse(null);if(def==null)continue;
            for(var row:def.layout(new NodeState(n.properties())).rows())if(row.id().equals(endpoint.portId())&&(row.editor() instanceof Editor.Slider||row.editor() instanceof UnitSlider||row.editor() instanceof Editor.ComboBox<?>||row.editor() instanceof Editor.Trigger||row.editor() instanceof Editor.CheckBox||row.editor() instanceof Editor.Toggle))result.add(new Source(endpoint,def.title()+" / "+row.label(),row.editor(),null));
        }
        return result.stream().filter(s->selectedRate(p)==ProcessingRate.EVENT?(s.value() instanceof Editor.Trigger||s.presentation()!=null&&s.presentation().inputEditorId().equals(BUTTON)):!(s.value() instanceof Editor.Trigger)&&!(s.presentation()!=null&&s.presentation().inputEditorId().equals(BUTTON))).toList();
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
    private void sliderValues(){
        var p=editor.selectedPort();var options=new HashMap<>(p.presentation().editorOptions());Double min=minimum.getText().isBlank()?null:Double.valueOf(minimum.getText()),max=maximum.getText().isBlank()?null:Double.valueOf(maximum.getText());double v=Double.parseDouble(value.getText());new Editor.Slider(v,min,max,.01);
        options.put("default",GLNodeValue.of(v));options.put("minimumEnabled",GLNodeValue.of(min!=null));options.put("maximumEnabled",GLNodeValue.of(max!=null));options.put("minimum",GLNodeValue.of(min==null?0:min));options.put("maximum",GLNodeValue.of(max==null?1:max));save(GLNodeInputEditorRegistry.SLIDER,options,Kind.SLIDER);
    }
    void refresh(){
        if(updating)return;updating=true;try{
            var p=editor.selectedPort();boolean input=p!=null&&p.publicPort().direction()==GLPortDirection.INPUT;
            for(var r:rows)r.setVisible(false);for(var r:unitRows)removeGL(r);unitRows.clear();
            if(p!=null&&FormulaGroups.isFormula(asset())){formulaIdentifier.setText(FormulaGroups.codeName(p));formulaIdentifier.setVisible(true);formulaName.setToolTipText(AppText.text("formula.identifierHint"));formulaName.setVisible(true);}
            status.setVisible(!status.getText().isEmpty());if(!input){resized(getWidth(),getHeight());return;}
            rateLabel.setVisible(true);rate.setVisible(true);kindLabel.setVisible(true);kind.setVisible(true);status.setVisible(!status.getText().isEmpty());
            rate.removeAllItems();(numeric(p)?List.of(ProcessingRate.AUTO,ProcessingRate.AUDIO,ProcessingRate.CONTROL,ProcessingRate.EVENT):List.of(ProcessingRate.AUTO)).forEach(rate::addItem);rate.setSelectedItem(selectedRate(p));rate.setEnabled(numeric(p));
            var choices=numeric(p)?selectedRate(p)==ProcessingRate.EVENT?List.of(Kind.BUTTON,Kind.COPY):List.of(Kind.SLIDER,Kind.CHECKBOX,Kind.COMBO,Kind.COPY):List.of(Kind.COPY);
            kind.removeAllItems();choices.forEach(kind::addItem);var mode=choices.contains(selectedKind(p))?selectedKind(p):choices.get(0);kind.setSelectedItem(p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE)&&!p.presentation().editorOptions().containsKey(MODE)?null:mode);
            var state=new NodeState(p.presentation().editorOptions());
            if(mode==Kind.COMBO){modelLabel.setVisible(true);model.setVisible(true);model.removeAllItems();templates.all().forEach(model::addItem);var applied=GroupInputs.template(p);if(applied!=null){var found=model.getItems().stream().filter(t->t.id().equals(applied.id())).findFirst().orElse(null);if(found==null){model.addItem(applied);found=applied;}model.setSelectedItem(found);}else model.setSelectedItem(null);model.setToolTipText(com.vin.easyknack.ui.AppText.literal("Choose a model defined in Editors. Its values follow the internal connections."));}
            if(mode==Kind.COPY){sourceLabel.setVisible(true);source.setVisible(true);source.removeAllItems();sources().forEach(source::addItem);source.getItems().stream().filter(s->s.endpoint().toString().equals(state.text("copySource",""))).findFirst().ifPresent(source::setSelectedItem);source.setEnabled(!source.getItems().isEmpty());}
            if(mode==Kind.CHECKBOX){checked.setVisible(true);checked.setSelected(state.number("default",0)!=0);}
            if(mode==Kind.SLIDER){integer.setVisible(true);integer.setSelected(state.text("numericType","float").equals("integer"));for(var r:List.of(minLabel,minimum,maxLabel,maximum,valueLabel,value,unitsLabel))r.setVisible(true);minimum.setText(state.flag("minimumEnabled",true)?Double.toString(state.number("minimum",0)):"");maximum.setText(state.flag("maximumEnabled",true)?Double.toString(state.number("maximum",1)):"");value.setText(Double.toString(state.number("default",0)));
                var base=GroupUnits.base(groups,asset().compositeDescriptor(),p);formulaUnitBase.setVisible(FormulaGroups.isFormula(asset()));formulaUnitBase.setSelectedItem(base);var allowed=GroupUnits.allowed(p,base);unitsLabel.setText(com.vin.easyknack.ui.AppText.literal("Units (stored: ")+base+")");
                for(var unit:base.compatible()){var box=new GLCheckBox(unit.toString());box.setSelected(allowed.contains(unit)||unit==base);box.setEnabled(unit!=base);box.addActionListener(e->edit(()->{var options=new HashMap<>(editor.selectedPort().presentation().editorOptions());var next=new ArrayList<>(allowed);if(box.isSelected()){if(!next.contains(unit))next.add(unit);}else next.remove(unit);options.put(GroupUnits.BASE,GLNodeValue.of(base.id()));options.put(GroupUnits.ALLOWED,GLNodeValue.of(next.stream().map(UnitFormat::id).toList()));save(GLNodeInputEditorRegistry.SLIDER,options,Kind.SLIDER);}));unitRows.add(box);addGL(box);}
            }
            resized(getWidth(),getHeight());
        }finally{updating=false;}
    }
    @Override protected void resized(float w,float h){float y=0;for(var row:rows)if(row.isVisible()){row.setBounds(0,y,w,28);y+=32;}for(var row:unitRows){row.setBounds(0,y,w,24);y+=26;}setPreferredGLSize(260,y);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
}
