package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.AnalysisChart;
import java.awt.Color;

/** One renderer for node previews, expanded inspection and image export. */
public final class ValueHistoryChart {
    private ValueHistoryChart(){}
    public static void draw(AnalysisChart.Painter g,ValueHistory.Snapshot s,double width,double height,
            boolean numbers,boolean overlay,Color grid,Color text,Color[] colors,double cursorA,double cursorB){
        if(s.channels()==0)return;
        double left=Math.min(76,width*.24),right=Math.max(left+1,width-12),top=22,bottom=Math.max(top+1,height-26);
        if(s.size()==0){g.text(AppText.literal("Waiting for audio"),8,20,text);return;}
        if(numbers){
            double row=Math.max(1,Math.min(40,(height-10)/s.channels()));
            for(int p=0;p<s.channels();p++){
                String value=AnalysisChart.number(s.value()[p][s.size()-1])+" "+s.units()[p];
                g.text(s.names()[p],8,20+p*row,colors[p%colors.length]);
                g.text(value,Math.max(8,width-g.textWidth(value)-10),20+p*row,text);
            }return;
        }
        double start=s.time()[0],end=s.end(),span=Math.max(s.interval(),end-start);
        for(int tick=0;tick<=4;tick++){
            double x=left+(right-left)*tick/4.;g.line(x,top,x,bottom,grid,1);
            String label=AnalysisChart.number(-span*(1-tick/4.))+" s";g.text(label,Math.max(0,x-g.textWidth(label)/2),height-7,text);
        }
        for(int p=0;p<s.channels();p++){
            double rowHeight=(bottom-top)/(overlay?1:s.channels()),y0=top+(overlay?0:p*rowHeight),y1=y0+rowHeight-5;
            double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY;
            for(int q=overlay?0:p;q<(overlay?s.channels():p+1);q++)for(int i=0;i<s.size();i++){lo=Math.min(lo,s.minimum()[q][i]);hi=Math.max(hi,s.maximum()[q][i]);}
            if(!Double.isFinite(lo)||!Double.isFinite(hi)){lo=-1;hi=1;}
            if(hi-lo<1e-9){double padding=Math.max(1e-3,Math.abs(lo)*.1);lo-=padding;hi+=padding;}
            double pad=(hi-lo)*.05;lo-=pad;hi+=pad;
            Color color=colors[p%colors.length];
            g.text(s.names()[p]+(s.units()[p].isBlank()?"":" ["+s.units()[p]+"]"),left+4,y0+13+(overlay?p*15:0),color);
            if(!overlay||p==0){g.text(AnalysisChart.number(hi),2,y0+12,text);g.text(AnalysisChart.number(lo),2,y1,text);g.line(left,y1,right,y1,grid,1);}
            g.clip(left,y0,right-left,Math.max(1,y1-y0));
            // Reduce to screen columns and preserve min/max, rather than drawing
            // thousands of overlapping segments that disappear under minification.
            double previousX=0,previousY=0;int i=0;boolean connected=false;
            while(i<s.size()){
                int first=i;double x=left+(s.time()[i]-start)/span*(right-left);int column=(int)x;
                float min=s.minimum()[p][i],max=s.maximum()[p][i];
                while(i+1<s.size()&&(int)(left+(s.time()[i+1]-start)/span*(right-left))==column&&s.time()[i+1]-s.time()[i]<=s.interval()*1.5){++i;min=Math.min(min,s.minimum()[p][i]);max=Math.max(max,s.maximum()[p][i]);}
                double y=y1-(s.value()[p][i]-lo)/(hi-lo)*(y1-y0);
                if(connected&&s.time()[first]-s.time()[first-1]<=s.interval()*1.5)g.line(previousX,previousY,x,y,color,1.3f);
                double a=y1-(max-lo)/(hi-lo)*(y1-y0),b=y1-(min-lo)/(hi-lo)*(y1-y0);
                g.line(x,a,x,Math.max(a+.8,b),color,1.3f);previousX=x;previousY=y;connected=true;i++;
            }
            for(double cursor:new double[]{cursorA,cursorB})if(Double.isFinite(cursor)&&cursor>=start&&cursor<=end){double x=left+(cursor-start)/span*(right-left);g.line(x,y0,x,y1,text,1);}
            g.unclip();
        }
    }
}
