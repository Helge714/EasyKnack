package com.vin.easyknack.nodes;
import com.vingame.glswing.ui.GLControl;
import com.vingame.glswing.rendering.*;
/** Pixel envelopes preserve narrow peaks when many resource samples share a pixel. */
final class SampleGraph {
    private SampleGraph(){}
    static void draw(GLControl control,GLContext2D c,float[] samples){
        var style=control.getGLTheme().sliderStyle();float w=control.getWidth(),h=control.getHeight(),pad=9;
        c.setMouseTarget(control);c.fillUIRoundRect(0,0,w,h,style.cornerRadius(),style.background());
        c.beginClip(0,0,w,h);var old=c.getStroke();
        try{
            c.setStroke(new GLStroke(1).withWidthSpace(GLStroke.WidthSpace.SCREEN));
            float width=Math.max(1,w-2*pad),height=Math.max(1,h-2*pad);
            c.drawLine(pad,h/2,w-pad,h/2,style.borderColor());
            for(int k=1;k<4;k++)c.drawLine(pad+width*k/4,pad,pad+width*k/4,h-pad,style.borderColor());
            if(samples==null||samples.length==0)return;
            double range=1;for(float sample:samples)range=Math.max(range,Math.abs((double)sample));
            int columns=Math.max(1,Math.min(samples.length,(int)Math.ceil(width*Math.max(1,c.getTransform().getMaximumScale()))));
            float previousX=pad,previousY=(float)(pad+height*(1-samples[0]/range)/2);
            for(int col=0;col<columns;col++){
                int start=col*samples.length/columns,end=Math.max(start+1,(col+1)*samples.length/columns);
                float min=samples[start],max=min;for(int i=start+1;i<end;i++){min=Math.min(min,samples[i]);max=Math.max(max,samples[i]);}
                float x=pad+width*start/(samples.length-1),y=(float)(pad+height*(1-samples[start]/range)/2);
                c.drawLine(previousX,previousY,x,y,style.progressLightColor());
                c.drawLine(x,(float)(pad+height*(1-min/range)/2),x,(float)(pad+height*(1-max/range)/2),style.progressLightColor());
                previousX=x;previousY=(float)(pad+height*(1-samples[end-1]/range)/2);
            }
        }finally{c.setStroke(old);c.endClip();}
    }
}
