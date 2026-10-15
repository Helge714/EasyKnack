package com.vin.easyknack.nodes;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLButton;
public final class CaptureReadout implements Editor {
    public GLNodeValue defaultValue(){return GLNodeValue.of("");}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new GLButton(label),v->{});}
}
