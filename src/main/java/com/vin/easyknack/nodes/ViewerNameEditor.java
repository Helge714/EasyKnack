package com.vin.easyknack.nodes;

import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.*;

/** Committed text edits avoid rebuilding a node while its name is being typed. */
record ViewerNameEditor(String initial) implements Editor {
    public GLNodeValue defaultValue(){return GLNodeValue.of(initial);}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var field=new GLTextEditor();field.setText(initial);field.addActionListener(e->changed.accept(GLNodeValue.of(field.getText())));
        var panel=new GLContainer(){
            final GLLabel title=new GLLabel(label);
            {addGL(title);addGL(field);setPreferredGLSize(180,54);}
            @Override protected void resized(float w,float h){title.setBounds(0,0,w,24);field.setBounds(0,26,w,Math.max(24,h-26));}
            @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        return new Control(panel,v->{String text=String.valueOf(v.toJavaValue());if(!field.getText().equals(text))field.setText(text);});
    }
}
