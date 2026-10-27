package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.GLContext2D;
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
    private record Unit(String id){public String toString(){return id.equals("none")?NodeText.text("viewer.unit.none","None"):UnitFormat.fromId(id).toString();}}
    private record Controls(int input,GLLabel slot,GLTextEditor name,GLComboBox<Unit> unit,GLButton up,GLButton down){}
    public ValueSeriesSettingsPanel(NodeState state,int count,Consumer<GLNodeValue> changed){
        this.changed=changed;this.count=count;this.state=state;entries=ValueSeries.all(state);
        addGL(nameLabel);addGL(unitLabel);
        var units=new ArrayList<Unit>();units.add(new Unit("none"));for(var u:UnitFormat.values())units.add(new Unit(u.id()));
        for(var e:ValueSeries.visible(state,count)){
            var row=new Controls(e.input(),new GLLabel(Integer.toString(e.input()+1)),new GLTextEditor(),new GLComboBox<>(units),new GLButton("↑"),new GLButton("↓"));
            controls.add(row);for(var c:List.of(row.slot,row.name,row.unit,row.up,row.down))addGL(c);
            row.name.addActionListener(a->replace(row.input,row.name.getText(),selected(row.input).unit()));
            row.unit.addActionListener(a->{if(!updating&&row.unit.getSelectedItem()!=null)replace(row.input,selected(row.input).name(),row.unit.getSelectedItem().id());});
            row.up.setToolTipText(NodeText.text("viewer.series.up","Move up"));row.down.setToolTipText(NodeText.text("viewer.series.down","Move down"));
            row.unit.setToolTipText(NodeText.text("viewer.series.unitsHelp","Label only; numeric values are not converted."));
            row.up.addActionListener(a->move(row.input,-1));row.down.addActionListener(a->move(row.input,1));
        }
        setPreferredGLSize(240,26+count*32);refresh();
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
        float available=Math.max(0,w-68),nameWidth=available*.56f,unitWidth=available-nameWidth;
        nameLabel.setBounds(20,0,nameWidth,24);unitLabel.setBounds(24+nameWidth,0,unitWidth,24);
        var visible=entries.stream().filter(e->e.input()<count).toList();
        for(int i=0;i<visible.size();i++){int input=visible.get(i).input();var row=controls.stream().filter(r->r.input==input).findFirst().orElse(null);if(row==null)continue;
            float y=26+i*32;row.slot.setBounds(0,y,18,28);row.name.setBounds(20,y,nameWidth,28);row.unit.setBounds(24+nameWidth,y,unitWidth,28);row.up.setBounds(w-40,y,19,28);row.down.setBounds(w-19,y,19,28);
        }
    }
    @Override protected void record(GLContext2D c){}
}
