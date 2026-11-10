package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import java.util.*;

/** Bounded UI-owned history. Native packets carry overlapping peak envelopes and
 * an exact sample clock, so slow painting does not lose intermediate audio blocks. */
public final class ValueHistory {
    public static final int MAX_RECORDS=1_000_000;
    public record Snapshot(double[] time,float[][] minimum,float[][] maximum,float[][] value,double interval,
            String[] names,String[] units,boolean frozen,String capturedAt) {
        public int channels(){return value.length;}
        public int size(){return time.length;}
        public double end(){return size()==0?0:time[size()-1];}
        public Snapshot range(double start,double end){
            int a=0,b=size();while(a<b&&time[a]<start)a++;while(b>a&&time[b-1]>end)b--;
            var lo=new float[channels()][];var hi=new float[channels()][];var last=new float[channels()][];
            for(int p=0;p<channels();p++){lo[p]=Arrays.copyOfRange(minimum[p],a,b);hi[p]=Arrays.copyOfRange(maximum[p],a,b);last[p]=Arrays.copyOfRange(value[p],a,b);}
            return new Snapshot(Arrays.copyOfRange(time,a,b),lo,hi,last,interval,names.clone(),units.clone(),frozen,capturedAt);
        }
    }
    private int channels=1,capacity=1100,size,cursor;
    private double seconds=10,interval=.01,rate;
    private long lastSample=-1;
    private float[][] low=new float[1][capacity],high=new float[1][capacity],last=new float[1][capacity];
    private double[] time=new double[capacity];
    private AudioAnalysis.Frame lastPacket;
    private long revision;
    public long revision(){return revision;}
    public void configure(int count,double duration){
        if(count<1||count>8||!Double.isFinite(duration)||duration<=0)throw new IllegalArgumentException("Invalid value history settings");
        if(channels==count&&seconds==duration)return;
        channels=count;seconds=duration;resize();revision++;
    }
    private void resize(){
        int desired=(int)Math.min(MAX_RECORDS/channels,Math.ceil(seconds/Math.max(1e-9,interval))+2);
        desired=Math.max(2,desired);
        if(low.length==channels&&desired==capacity)return;
        var before=low.length==channels?snapshot(null,null,false):null;
        capacity=desired;low=new float[channels][capacity];high=new float[channels][capacity];last=new float[channels][capacity];time=new double[capacity];size=cursor=0;
        if(before!=null)for(int i=Math.max(0,before.size()-capacity);i<before.size();i++)append(before.time[i],before.minimum,before.maximum,before.value,i);
    }
    private void append(double at,float[][] lo,float[][] hi,float[][] value,int i){
        time[cursor]=at;for(int p=0;p<channels;p++){low[p][cursor]=lo[p][i];high[p][cursor]=hi[p][i];last[p][cursor]=value[p][i];}
        cursor=(cursor+1)%capacity;size=Math.min(capacity,size+1);
    }
    public void accept(AudioAnalysis.Frame packet){
        if(packet==null||packet==lastPacket)return;
        var v=packet.values();int count=(int)packet.frequency();
        if(v.length<8||v[0]!=1||count!=channels||packet.confidence()<=0)return;
        int stride=(int)v[1],buckets=(int)v[2];double nextRate=packet.confidence();
        if(stride<1||buckets<0||8L+(long)buckets*count*3!=v.length)return;
        long end=0;for(int j=0;j<4;j++){int word=(int)v[4+j];if(word<0||word>65535)return;end|=(long)word<<(16*j);}
        if(end<0)return;
        long previousSample=lastSample;int previousSize=size;double previousRate=rate,previousInterval=interval;
        double nextInterval=stride/nextRate;
        if(rate!=nextRate||interval!=nextInterval||end<lastSample){size=cursor=0;lastSample=-1;rate=nextRate;interval=nextInterval;resize();}
        lastPacket=packet;
        for(int b=0;b<buckets;b++){
            long sample=end-(long)(buckets-1-b)*stride;if(sample<=lastSample)continue;
            time[cursor]=sample/rate;
            for(int p=0;p<count;p++){
                int index=8+(b*count+p)*3;low[p][cursor]=v[index];high[p][cursor]=v[index+1];last[p][cursor]=v[index+2];
            }
            cursor=(cursor+1)%capacity;size=Math.min(capacity,size+1);lastSample=sample;
        }
        while(size>0&&time[(cursor+capacity-size)%capacity]<end/rate-seconds)size--;
        if(previousSample!=lastSample||previousSize!=size||previousRate!=rate||previousInterval!=interval)revision++;
    }
    public Snapshot snapshot(String[] names,String[] units,boolean frozen){return snapshot(names,units,frozen,Integer.MAX_VALUE);}
    /** Screen snapshots are reduced before allocation; extrema survive reduction. */
    public Snapshot snapshot(String[] names,String[] units,boolean frozen,int maxPoints){
        int step=Math.max(1,(int)Math.ceil(size/(double)Math.max(1,maxPoints))),n=(size+step-1)/step;
        double[] t=new double[n];float[][] lo=new float[channels][n],hi=new float[channels][n],values=new float[channels][n];
        for(int a=0;a<n;a++){
            int first=a*step,end=Math.min(size,first+step),index=(cursor+capacity-size+end-1)%capacity;t[a]=time[index];
            for(int p=0;p<channels;p++){
                float min=Float.POSITIVE_INFINITY,max=Float.NEGATIVE_INFINITY;
                for(int i=first;i<end;i++){int k=(cursor+capacity-size+i)%capacity;min=Math.min(min,low[p][k]);max=Math.max(max,high[p][k]);}
                lo[p][a]=min;hi[p][a]=max;values[p][a]=last[p][index];
            }
        }
        var labels=names==null?new String[channels]:names.clone();var suffixes=units==null?new String[channels]:units.clone();
        for(int p=0;p<channels;p++){if(labels[p]==null)labels[p]="Value "+(p+1);if(suffixes[p]==null)suffixes[p]="";}
        return new Snapshot(t,lo,hi,values,interval*step,labels,suffixes,frozen,java.time.Instant.now().toString());
    }
    public void resetTransport(){if(lastPacket!=null||lastSample!=-1||size!=0)revision++;lastPacket=null;lastSample=-1;size=cursor=0;}
}
