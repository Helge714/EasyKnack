package com.vin.easyknack.nodes;

import java.awt.Color;
import java.util.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.i18n.AppText;

/** Shared axes and peak-preserving geometry for on-screen and raster-export rendering. */
public final class AnalysisChart {
    private AnalysisChart(){}
    public record Snapshot(AudioAnalysis.Frame frame,boolean spectrum,boolean logarithmic,double range,
        boolean triggers,double low,double high,String capturedAt,boolean paused,View view,double cursorA,double cursorB) {
        public Snapshot(AudioAnalysis.Frame frame,boolean spectrum,boolean logarithmic,double range,boolean triggers,double low,double high,String capturedAt,boolean paused){this(frame,spectrum,logarithmic,range,triggers,low,high,capturedAt,paused,View.FULL,Double.NaN,Double.NaN);}
        public Snapshot withView(View v,double a,double b){return new Snapshot(frame,spectrum,logarithmic,range,triggers,low,high,capturedAt,paused,v,a,b);}
        public Snapshot { if(frame!=null)frame=new AudioAnalysis.Frame(frame.frequency(),frame.confidence(),frame.span(),frame.symmetric(),frame.values().clone()); }
        public boolean log(){return spectrum&&logarithmic&&frame!=null&&frame.values().length>2&&frame.span()>0;}
        public double minimumX(){return log()?frame.span()/(frame.values().length-1):frame!=null&&frame.symmetric()?-frame.span()*1000:0;}
        public double maximumX(){return frame==null?1:spectrum?frame.span():frame.span()*1000;}
        public double sampleX(int index){return frame==null||frame.values().length<2?0:index*maximumX()/(frame.values().length-1);}
        public int nearestIndex(double x){return frame==null||frame.values().length==0?0:Math.max(0,Math.min(frame.values().length-1,(int)Math.round(Math.abs(x)/Math.max(1e-30,maximumX())*(frame.values().length-1))));}
        public double basePosition(double x){double min=minimumX(),max=maximumX();return log()?Math.log(x/min)/Math.log(max/min):(x-min)/Math.max(1e-30,max-min);}
        public double position(double x){return (basePosition(x)-view.x0())/(view.x1()-view.x0());}
        public double xAt(double fraction){double t=view.x0()+fraction*(view.x1()-view.x0());return log()?minimumX()*Math.pow(maximumX()/minimumX(),t):minimumX()+t*(maximumX()-minimumX());}
        public double yPosition(double value){return (view.y1()-value/range)/(view.y1()-view.y0());}
        public String measurements(){
            if(frame==null||frame.values().length==0)return "";
            if(!Double.isFinite(cursorA)||!Double.isFinite(cursorB)){
                double value=Double.isFinite(cursorA)?cursorA:cursorB;
                return Double.isFinite(value)?(Double.isFinite(cursorA)?"A: ":"B: ")+number(value)+(spectrum?" Hz":" ms")+" / "+number(frame.values()[nearestIndex(value)]):"";
            }
            double delta=Math.abs(cursorB-cursorA);String unit=spectrum?" Hz":" ms";
            String result="A: "+number(cursorA)+unit+" / "+number(frame.values()[nearestIndex(cursorA)])+"   B: "+number(cursorB)+unit+" / "+number(frame.values()[nearestIndex(cursorB)])+"   Δ"+(spectrum?"f: ":"t: ")+number(delta)+unit;
            result+="   Δy: "+number(frame.values()[nearestIndex(cursorB)]-frame.values()[nearestIndex(cursorA)]);
            return !spectrum&&delta>0?result+"   1/Δt: "+number(1000/delta)+" Hz":result;
        }
        public String xLabel(){return AppText.text(spectrum?"analysis.frequency":frame!=null&&frame.symmetric()?"analysis.lag":"analysis.time");}
        public String yLabel(){return AppText.text(spectrum?"analysis.coefficient":frame!=null&&frame.symmetric()?"analysis.correlation":"analysis.amplitude");}
    }
    public record View(double x0,double x1,double y0,double y1){
        public static final View FULL=new View(0,1,-1,1);
        public View zoom(double anchor,double factor,boolean vertical){
            double lo=vertical?y0:x0,hi=vertical?y1:x1,limit=vertical?2:1;
            double span=Math.max(limit/100000,Math.min(limit,(hi-lo)*factor));
            double start=lo+anchor*(hi-lo-span);start=Math.max(vertical?-1:0,Math.min(1-span,start));
            return vertical?new View(x0,x1,start,start+span):new View(start,start+span,y0,y1);
        }
        public View pan(double dx,double dy){double x=Math.max(0,Math.min(1-(x1-x0),x0+dx)),y=Math.max(-1,Math.min(1-(y1-y0),y0+dy));return new View(x,x+x1-x0,y,y+y1-y0);}
    }
    public record Segment(double x1,double y1,double x2,double y2){}
    public record Tick(double value,boolean major){}
    public interface Painter {
        void line(double x1,double y1,double x2,double y2,Color color,float width);
        void text(String text,double x,double baseline,Color color);
        double textWidth(String text);
        void clip(double x,double y,double width,double height);
        void unclip();
    }
    public record Palette(Color grid,Color text,Color curve,Color low,Color high){}
    public static String number(double value){
        String text=String.format(Locale.ROOT,"%.5g",value==0?0:value);
        int exponent=text.indexOf('e');String suffix=exponent<0?"":text.substring(exponent);
        String base=exponent<0?text:text.substring(0,exponent);
        if(base.contains(".")){while(base.endsWith("0"))base=base.substring(0,base.length()-1);if(base.endsWith("."))base=base.substring(0,base.length()-1);}
        return base+suffix;
    }
    public static java.util.List<Tick> ticks(Snapshot s){
        var result=new ArrayList<Tick>();double min=s.xAt(0),max=s.xAt(1);
        if(!Double.isFinite(min)||!Double.isFinite(max)||max<=min)return result;
        if(s.log()){
            for(int exp=(int)Math.floor(Math.log10(min));exp<=Math.ceil(Math.log10(max));exp++)
                for(int m=1;m<=9;m++){double v=m*Math.pow(10,exp);if(v>=min&&v<=max)result.add(new Tick(v,m==1||m==2||m==5));}
        }
        if(!s.log()||result.stream().filter(Tick::major).count()<2){
            result.clear();
            double raw=(max-min)/6,base=Math.pow(10,Math.floor(Math.log10(raw))),ratio=raw/base;
            double step=(ratio<=1?1:ratio<=2?2:ratio<=5?5:10)*base;
            for(double v=Math.ceil(min/step)*step;v<=max+step*1e-8&&result.size()<32;v+=step)result.add(new Tick(v,true));
        }
        return result;
    }
    /** Keeps first/last and min/max per pixel column, in original order. Flat buckets stay connected. */
    public static java.util.List<Segment> geometry(Snapshot s,int columns){
        var lines=new ArrayList<Segment>();if(s.frame()==null||columns<1)return lines;
        float[] data=s.frame().values();int count=data.length;if(count==0)return lines;
        int total=s.frame().symmetric()?2*count-1:count;
        var reducer=new Reducer(lines);
        for(int i=0;i<total;i++){
            int index=s.frame().symmetric()?Math.abs(i-(count-1)):i;
            if(s.log()&&i==0)continue;
            double valueX=s.frame().symmetric()?Math.copySign(s.sampleX(index),i-(count-1)):s.sampleX(index);
            double x=count==1?.5:s.position(valueX);
            int column=x<0?-1:x>1?columns:Math.min(columns-1,(int)(x*columns));
            if(!Float.isFinite(data[index])){reducer.flush();reducer.previousIndex=-1;continue;}
            reducer.accept(i,x,data[index],column);
        }
        reducer.flush();
        if(total==1&&Float.isFinite(data[0])){lines.clear();lines.add(new Segment(.5-.5/columns,data[0],.5+.5/columns,data[0]));}
        return lines;
    }
    /** Constant working storage, independent of FFT size. */
    private static final class Reducer {
        final java.util.List<Segment> lines;
        int column=-1,first=-1,last,min,max,previousIndex=-1;
        double firstX,firstY,lastX,lastY,minX,minY,maxX,maxY,previousX,previousY;
        Reducer(java.util.List<Segment> lines){this.lines=lines;}
        void accept(int index,double x,double y,int bucket){
            if(first>=0&&bucket!=column)flush();
            column=bucket;
            if(first<0){first=min=max=index;firstX=minX=maxX=x;firstY=minY=maxY=y;}
            if(y<minY){min=index;minX=x;minY=y;}if(y>maxY){max=index;maxX=x;maxY=y;}
            last=index;lastX=x;lastY=y;
        }
        void point(int index,double x,double y){
            if(index==previousIndex)return;
            if(previousIndex>=0)lines.add(new Segment(previousX,previousY,x,y));
            previousIndex=index;previousX=x;previousY=y;
        }
        void flush(){
            if(first<0)return;
            point(first,firstX,firstY);
            if(min<max){point(min,minX,minY);point(max,maxX,maxY);}else{point(max,maxX,maxY);point(min,minX,minY);}
            point(last,lastX,lastY);first=-1;
        }
    }
    public static void draw(Painter p,Snapshot s,int width,int height,boolean labels,double pixelScale,Palette colors){
        double left=labels?78:0,top=labels?36:0,right=labels?24:0,bottom=labels?62:0;
        double w=Math.max(1,width-left-right),h=Math.max(1,height-top-bottom);
        double lastLabel=-1000;
        for(var tick:ticks(s)){
            double x=left+s.position(tick.value())*w;
            p.line(x,top,x,top+h,colors.grid(),tick.major()?1:.5f);
            if(labels&&tick.major()){String text=number(tick.value());double tw=p.textWidth(text);if(x-tw/2>lastLabel+8){p.text(text,x-tw/2,top+h+19,colors.text());lastLabel=x+tw/2;}}
        }
        for(int i=0;i<=4;i++){
            double y=top+i*h/4,value=s.range()*(s.view().y1()-i*(s.view().y1()-s.view().y0())/4);
            p.line(left,y,left+w,y,colors.grid(),i==2?1.2f:.7f);
            if(labels){String text=number(value);p.text(text,left-9-p.textWidth(text),y+4,colors.text());}
        }
        p.clip(left,top,w,h);
        for(var segment:geometry(s,Math.max(1,(int)Math.ceil(w*pixelScale))))
            p.line(left+segment.x1()*w,top+h*s.yPosition(segment.y1()),left+segment.x2()*w,top+h*s.yPosition(segment.y2()),colors.curve(),2f);
        if(s.triggers())for(int i=0;i<2;i++){
            double v=i==0?s.low():s.high(),y=top+h*s.yPosition(v);
            if(y<top||y>top+h)continue;Color color=i==0?colors.low():colors.high();
            for(double x=left;x<left+w;x+=9)p.line(x,y,Math.min(x+5,left+w),y,color,1);
            if(labels)p.text((i==0?"L ":"H ")+number(v),left+8,Math.max(top+14,y-4),color);
        }
        for(int i=0;i<2;i++){
            double value=i==0?s.cursorA():s.cursorB(),x=left+s.position(value)*w;
            if(!Double.isFinite(value)||!Double.isFinite(x)||x<left||x>left+w)continue;
            Color color=i==0?colors.low():colors.high();p.line(x,top,x,top+h,color,1.4f);p.text(i==0?"A":"B",x+4,top+14,color);
        }
        p.unclip();
        if(labels){
            p.text(s.yLabel(),left,19,colors.text());
            String axis=s.xLabel()+(s.log()?" · log10":"");p.text(axis,left+(w-p.textWidth(axis))/2,top+h+41,colors.text());
        }
    }
}
