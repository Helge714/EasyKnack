package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.*;
import java.io.*;

/** Immutable document data. Packet counters and clock words survive the float transport exactly. */
public final class MeasurementTable {
    public static final int MAX_ROWS=4096,HEADER=12,MAGIC=-9137;
    private MeasurementTable(){}
    public static GLNodeValue empty(){return data("0","",0,List.of());}
    public static String generation(GLNodeValue value){return text(value,"generation","0");}
    public static GLNodeValue clear(){return data(Long.toUnsignedString(Long.parseUnsignedLong(UUID.randomUUID().toString().replace("-","").substring(0,16),16),16),"",0,List.of());}
    private static GLNodeValue data(String generation,String session,long received,List<GLNodeValue> rows){
        return GLNodeValue.of(Map.of("generation",generation,"session",session,"received",received,"rows",new GLNodeValue.ListValue(rows)));
    }
    private static GLNodeValue field(GLNodeValue value,String key){return value instanceof GLNodeValue.MapValue m?m.values().get(key):null;}
    private static String text(GLNodeValue value,String key,String fallback){return field(value,key) instanceof GLNodeValue.TextValue t?t.value():fallback;}
    public static List<GLNodeValue> rows(GLNodeValue value){return field(value,"rows") instanceof GLNodeValue.ListValue list?list.values():List.of();}
    static String words(float[] values,int offset){
        long bits=0;for(int i=0;i<4;i++){float v=values[offset+i];if(v<0||v>65535||v!=Math.rint(v))throw new IllegalArgumentException("Invalid recorder clock word");bits|=(long)v<<(16*i);}return Long.toUnsignedString(bits,16);
    }
    public static GLNodeValue merge(GLNodeValue previous,AudioAnalysis.Frame frame){
        if(previous==null)previous=empty();if(frame==null)return previous;
        var v=frame.values();if(v.length<HEADER||v[0]!=MAGIC||v[1]!=1)return previous;
        int count=(int)v[2],stored=(int)v[3];
        if(count<1||count>8||count!=v[2]||stored<0||stored>MAX_ROWS||stored!=v[3]||v.length!=HEADER+stored*(5+count)||!Double.isFinite(frame.confidence())||frame.confidence()<=0)return previous;
        if(!words(v,4).equals(generation(previous)))return previous;
        String session=words(v,8);
        long received=text(previous,"session","").equals(session)&&field(previous,"received") instanceof GLNodeValue.IntegerValue n?n.value():0;
        if(stored<=received||rows(previous).size()>=MAX_ROWS)return previous;
        var result=new ArrayList<>(rows(previous));
        for(int i=(int)received;i<stored&&result.size()<MAX_ROWS;i++){
            int p=HEADER+i*(5+count);long samples=Long.parseUnsignedLong(words(v,p),16);
            // The host's session clock is bounded far below signed overflow.
            var row=new ArrayList<GLNodeValue>();row.add(GLNodeValue.of(session));row.add(GLNodeValue.of(samples/frame.confidence()));
            int valid=(int)v[p+4];for(int j=0;j<count;j++)row.add((valid&(1<<j))!=0&&Float.isFinite(v[p+5+j])?GLNodeValue.of(v[p+5+j]):GLNodeValue.NullValue.INSTANCE);
            result.add(new GLNodeValue.ListValue(row));
        }
        return data(generation(previous),session,stored,result);
    }
    public static int columns(GLNodeValue table,int configured){return Math.max(configured,rows(table).stream().mapToInt(r->((GLNodeValue.ListValue)r).values().size()-2).max().orElse(0));}
    public static void csv(GLNodeValue table,String[] names,Writer out,boolean excel)throws IOException{
        String separator=excel?";":",";if(excel)out.write('\uFEFF');
        out.write("measurement"+separator+"session"+separator+"time_s");
        for(String name:names)out.write(separator+quote(name));out.write("\r\n");int number=0;
        for(var value:rows(table)){
            var row=((GLNodeValue.ListValue)value).values();out.write(++number+separator+quote((String)row.get(0).toJavaValue()));
            for(int i=1;i<names.length+2;i++)out.write(separator+(i<row.size()?number(row.get(i),excel):""));out.write("\r\n");
        }
    }
    private static String number(GLNodeValue value,boolean excel){if(value==GLNodeValue.NullValue.INSTANCE)return "";String s=value.toJavaValue().toString();return excel?s.replace('.',','):s;}
    private static String quote(String text){return '"'+text.replace("\"","\"\"")+'"';}
}
