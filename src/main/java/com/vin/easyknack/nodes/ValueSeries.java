package com.vin.easyknack.nodes;

import com.vingame.glswing.node.model.GLNodeValue;
import java.util.*;

/** Display metadata indexed by stable native input slots, independent of list order. */
public final class ValueSeries {
    public static final PropertyId SETTINGS=new PropertyId("series-settings");
    public record Entry(int input,String name,String unit){
        public String suffix(){return unit.equals("none")?"":UnitFormat.fromId(unit).suffix();}
        public String heading(){return name+(suffix().isBlank()?"":" ["+suffix()+"]");}
    }
    private ValueSeries(){}
    public static List<Entry> all(NodeState state){
        var entries=new ArrayList<Entry>();var seen=new HashSet<Integer>();
        var saved=state.values().get(SETTINGS.value());
        if(saved instanceof GLNodeValue.ListValue list)for(var item:list.values()){
            if(!(item instanceof GLNodeValue.MapValue m))continue;
            var v=m.values();Object slot=v.getOrDefault("input",GLNodeValue.of(-1)).toJavaValue();
            if(!(slot instanceof Number n)||n.doubleValue()!=n.intValue())continue;
            int index=n.intValue();if(index<0||index>=8||!seen.add(index))continue;
            String name=v.get("name") instanceof GLNodeValue.TextValue t?t.value():fallback(state,index);
            String unit=v.get("unit") instanceof GLNodeValue.TextValue t?t.value():"none";
            entries.add(new Entry(index,name,validUnit(unit)));
        }
        for(int i=0;i<8;i++)if(seen.add(i))entries.add(new Entry(i,fallback(state,i),validUnit(state.text("unit-"+i,"none"))));
        return List.copyOf(entries);
    }
    private static String fallback(NodeState state,int i){return state.text("name-"+i,NodeText.text("viewer.series.value","Value")+" "+(i+1));}
    private static String validUnit(String id){if(id.equals("none"))return id;try{UnitFormat.fromId(id);return id;}catch(IllegalArgumentException ex){return "none";}}
    public static List<Entry> visible(NodeState state,int count){return all(state).stream().filter(e->e.input()<count).toList();}
    public static GLNodeValue encode(List<Entry> entries){return new GLNodeValue.ListValue(entries.stream().map(e->GLNodeValue.of(Map.of("input",e.input(),"name",e.name(),"unit",e.unit()))).toList());}
    public static List<Entry> move(List<Entry> all,int count,int input,int direction){
        var visible=all.stream().filter(e->e.input()<count).toList();int from=-1;
        for(int i=0;i<visible.size();i++)if(visible.get(i).input()==input)from=i;
        int to=from+direction;if(from<0||to<0||to>=visible.size())return all;
        var result=new ArrayList<>(all);Collections.swap(result,result.indexOf(visible.get(from)),result.indexOf(visible.get(to)));return List.copyOf(result);
    }
    public static com.vin.easyknack.ui.ValueHistory.Snapshot reorder(com.vin.easyknack.ui.ValueHistory.Snapshot s,List<Entry> entries){
        var shown=entries.stream().filter(e->e.input()<s.channels()).toList();
        float[][] lo=new float[shown.size()][],hi=new float[shown.size()][],value=new float[shown.size()][];
        String[] names=new String[shown.size()],units=new String[shown.size()];
        for(int i=0;i<shown.size();i++){var e=shown.get(i);lo[i]=s.minimum()[e.input()];hi[i]=s.maximum()[e.input()];value[i]=s.value()[e.input()];names[i]=e.name();units[i]=e.suffix();}
        return new com.vin.easyknack.ui.ValueHistory.Snapshot(s.time(),lo,hi,value,s.interval(),names,units,s.frozen(),s.capturedAt());
    }
}
