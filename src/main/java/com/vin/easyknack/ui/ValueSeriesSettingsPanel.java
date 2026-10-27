package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.rendering.GLIcon;
import com.vingame.glswing.rendering.GLPath2D;
import com.vingame.glswing.layout.GLConstraints;
import com.vingame.glswing.ui.*;
import java.util.*;
import java.util.function.Consumer;

/** Shared name/unit rows and stable-slot reordering for numeric observers. */
public final class ValueSeriesSettingsPanel extends GLContainer {
    private final Consumer<GLNodeValue> changed;
    private final int count;
    private NodeState state;
    private List<ValueSeries.Entry> entries;
    private final List<Controls> controls=new ArrayList<>();
    private final GLLabel nameLabel=new GLLabel(NodeText.text("viewer.name","Name")),unitLabel=new GLLabel(NodeText.text("viewer.unit","Unit"));
    private boolean updating;
    private static final GLIcon UP=GLIcon.fromPath(new GLPath2D().moveTo(0,8).lineTo(5,2).lineTo(10,8).lineTo(8,10).lineTo(5,6).lineTo(2,10).closePath());
    private static final GLIcon DOWN=GLIcon.fromPath(new GLPath2D().moveTo(0,2).lineTo(5,8).lineTo(10,2).lineTo(8,0).lineTo(5,4).lineTo(2,0).closePath());
    private record Unit(String id){public String toString(){return id.equals("none")?NodeText.text("viewer.unit.none","None"):UnitFormat.fromId(id).toString();}}
    private record Controls(int input,GLLayoutGroup group,GLTextEditor name,GLComboBox<Unit> unit,GLButton up,GLButton down){}
    public ValueSeriesSettingsPanel(NodeState state,int count,Consumer<GLNodeValue> changed){
        this.changed=changed;this.count=count;this.state=state;entries=ValueSeries.all(state);
        addGL(nameLabel);addGL(unitLabel);
        for(var e:ValueSeries.visible(state,count)){
            var row=new Controls(e.input(),new GLLayoutGroup(),new GLTextEditor(),unitChoices(),new GLButton(""),new GLButton(""));
            controls.add(row);addGL(row.group);row.group.setName("series-row-"+row.input);
            row.name.setPreferredGLSize(0,28);row.unit.setPreferredGLSize(0,28);
            row.up.setPreferredGLSize(24,28);row.down.setPreferredGLSize(24,28);
            row.up.setIcon(UP);row.down.setIcon(DOWN);row.up.setName("series-up-"+row.input);row.down.setName("series-down-"+row.input);
            row.group.addGL(row.name,GLConstraints.at(0,0).weight(3,1).fillBoth().connectAll());
            row.group.addGL(row.unit,GLConstraints.at(1,0).weight(2,1).fillBoth().connectAll());
            row.group.addGL(row.up,GLConstraints.at(2,0).fillBoth().connectAll());
            row.group.addGL(row.down,GLConstraints.at(3,0).fillBoth().connectAll());
            row.name.addActionListener(a->replace(row.input,row.name.getText(),selected(row.input).unit()));
            row.unit.addActionListener(a->{if(!updating&&row.unit.getSelectedItem()!=null)replace(row.input,selected(row.input).name(),row.unit.getSelectedItem().id());});
            row.up.setToolTipText(NodeText.text("viewer.series.up","Move up"));row.down.setToolTipText(NodeText.text("viewer.series.down","Move down"));
            row.unit.setToolTipText(NodeText.text("viewer.series.unitsHelp","Label only; numeric values are not converted."));
            row.up.addActionListener(a->move(row.input,-1));row.down.addActionListener(a->move(row.input,1));
        }
        setPreferredGLSize(240,26+count*32);refresh();
    }
    private static GLComboBox<Unit> unitChoices(){
        var combo=new GLComboBox<Unit>();combo.addItem(new Unit("none"));
        var groups=new TreeMap<String,List<UnitFormat>>(Comparator.comparing(d->AppText.text("unit.group."+d),String.CASE_INSENSITIVE_ORDER));
        for(var unit:UnitFormat.values())groups.computeIfAbsent(unit.dimension().toString(),key->new ArrayList<>()).add(unit);
        groups.forEach((dimension,units)->combo.addSubMenu(AppText.text("unit.group."+dimension),units.stream().sorted(Comparator.comparing(UnitFormat::displayName,String.CASE_INSENSITIVE_ORDER)).map(u->new Unit(u.id())).toList()));
        return combo;
    }
    private ValueSeries.Entry selected(int input){return entries.stream().filter(e->e.input()==input).findFirst().orElseThrow();}
    private void replace(int input,String name,String unit){if(updating)return;entries=entries.stream().map(e->e.input()==input?new ValueSeries.Entry(input,name,unit):e).toList();commit();}
    private void move(int input,int direction){entries=ValueSeries.move(entries,count,input,direction);commit();}
    private void commit(){changed.accept(ValueSeries.encode(entries));refresh();}
    public void configure(NodeState state){this.state=state;entries=ValueSeries.all(state);refresh();}
    public void update(GLNodeValue value){var values=new HashMap<>(state.values());values.put(ValueSeries.SETTINGS.value(),value);configure(new NodeState(values));}
    private void refresh(){
        updating=true;try{
            var visible=entries.stream().filter(e->e.input()<count).toList();
            for(var row:controls){var e=selected(row.input);if(!row.name.getText().equals(e.name()))row.name.setText(e.name());row.unit.setSelectedItem(new Unit(e.unit()));int at=visible.indexOf(e);row.up.setEnabled(at>0);row.down.setEnabled(at+1<visible.size());}
            resized(getWidth(),getHeight());invalidateRecording();repaintGL();
        }finally{updating=false;}
    }
    @Override protected void resized(float w,float h){
        float available=Math.max(0,w-48),nameWidth=available*.6f,unitWidth=available-nameWidth;
        nameLabel.setBounds(0,0,nameWidth,24);unitLabel.setBounds(nameWidth,0,unitWidth,24);
        var visible=entries.stream().filter(e->e.input()<count).toList();
        for(int i=0;i<visible.size();i++){int input=visible.get(i).input();var row=controls.stream().filter(r->r.input==input).findFirst().orElse(null);if(row==null)continue;
            row.group.setBounds(0,26+i*32,w,28);row.group.refreshConnections();
        }
    }
    @Override protected void record(GLContext2D c){}
}
