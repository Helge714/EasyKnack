package com.vin.easyknack.ui;

import com.vingame.glswing.ui.GLControl;
import com.vingame.glswing.ui.GLLabel;
import com.vingame.glswing.rendering.GLContext2D;

/** A label-sized hover target without a button appearance or keyboard focus. */
final class NodeTooltipLabel extends GLControl {
    private String text;
    String getText(){return text;}
    void setText(String value){if(!java.util.Objects.equals(text,value)){text=value;measure();repaintGL();}}
    void refreshLanguage(){text=AppText.translateExisting(text);measure();}
    private final boolean right;
    private final boolean framed;
    NodeTooltipLabel(String text){this(text,false);}
    NodeTooltipLabel(String text,boolean right){this(text,right,false);}
    NodeTooltipLabel(String text,boolean right,boolean framed){super(false);this.text=text;this.right=right;this.framed=framed;measure();}
    private void measure(){var label=new GLLabel(text);label.setGLTheme(getGLTheme());setIntrinsicPreferredGLSize(label.getPreferredGLSize());}
    @Override protected void themeChanged(){super.themeChanged();if(text!=null)measure();}
    @Override protected void record(GLContext2D context){
        var style=getGLTheme().labelStyle();var font=getGLFont(style.font());
        float top=style.padding().top()+Math.max(0,getHeight()-style.padding().vertical()-font.getLineHeight())*.5f;
        float left=framed?Math.max(style.padding().left(),top):style.padding().left();
        String visible=text;float available=Math.max(0,getWidth()-left-style.padding().right());
        if(font.measureText(visible)>available){int end=visible.length();while(end>0&&font.measureText(visible.substring(0,end)+"…")>available)end--;visible=end==0?"":visible.substring(0,end)+"…";}
        float baseline=top+font.getAscent();
        context.drawUIText(font,visible,(right?getWidth()-style.padding().right()-font.measureText(visible):left),baseline,style.textColor());
    }
}
