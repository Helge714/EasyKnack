package com.vin.easyknack.nodes;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import com.vin.easyknack.audio.MidiDevices;
import com.vingame.glswing.component.GLParent;
import com.vingame.glswing.input.GLKeyEvent;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.rendering.GLStroke;
import com.vingame.glswing.ui.GLButton;

/** Runtime performance focus is intentionally neither saved nor recorded in undo history. */
public final class KeyboardMidiEditor implements Editor {
    @Override public GLNodeValue defaultValue(){return GLNodeValue.of(false);}
    @Override public Control create(String label,java.util.function.Consumer<GLNodeValue> ignored){return new Control(new Keyboard(),v->{});}
    public static final class Keyboard extends GLButton {
        private final Map<Integer,Integer> held=new LinkedHashMap<>();
        private int transpose;
        public Keyboard(){super(com.vin.easyknack.i18n.AppText.literal("Click to play"));setToolTipText(com.vin.easyknack.i18n.AppText.literal("Click to capture keyboard input. A W S E D F T G Y/Z H U J K O L P play C4 upward. Escape or click elsewhere to leave. Fixed velocity: 100/127."));}
        public void setTranspose(int value){if(value!=transpose){releaseNotes();transpose=value;}}
        @Override public boolean isKeyboardInputExclusive(){return true;}
        @Override protected void controlFocusChanged(boolean focused){super.controlFocusChanged(focused);if(!focused)releaseNotes();setText(focused?com.vin.easyknack.i18n.AppText.literal("Playing · Esc to leave"):com.vin.easyknack.i18n.AppText.literal("Click to play"));invalidateRecording();}
        @Override protected void controlEnabledChanged(boolean enabled){if(!enabled)releaseNotes();}
        @Override protected void detached(GLParent parent){releaseNotes();super.detached(parent);}
        private void releaseNotes(){for(int note:held.values())MidiDevices.keyboardEvent(0x80|(note<<8));held.clear();}
        private static int offset(int key){return switch(key){
            case KeyEvent.VK_A->0;case KeyEvent.VK_W->1;case KeyEvent.VK_S->2;case KeyEvent.VK_E->3;
            case KeyEvent.VK_D->4;case KeyEvent.VK_F->5;case KeyEvent.VK_T->6;case KeyEvent.VK_G->7;
            case KeyEvent.VK_Y,KeyEvent.VK_Z->8;case KeyEvent.VK_H->9;case KeyEvent.VK_U->10;case KeyEvent.VK_J->11;
            case KeyEvent.VK_K->12;case KeyEvent.VK_O->13;case KeyEvent.VK_L->14;case KeyEvent.VK_P->15;default->-1;};}
        @Override protected void controlKeyPressed(GLKeyEvent e){
            e.consume();if(e.getKeyCode()==KeyEvent.VK_ESCAPE){clearGLFocus(this);return;}
            int key=e.getKeyCode(),offset=offset(key);if(offset<0||held.containsKey(key)||e.getModifiersEx()!=0)return;
            int note=60+transpose+offset;if(note<0||note>127||held.containsValue(note))return;
            held.put(key,note);MidiDevices.keyboardEvent(0x90|(note<<8)|(100<<16));
        }
        @Override protected void controlKeyReleased(GLKeyEvent e){e.consume();Integer note=held.remove(e.getKeyCode());if(note!=null)MidiDevices.keyboardEvent(0x80|(note<<8));}
        @Override protected void controlKeyTyped(GLKeyEvent e){e.consume();}
        @Override protected void record(GLContext2D c){super.record(c);if(isFocused())c.drawUIRoundRect(1,1,Math.max(0,getWidth()-2),Math.max(0,getHeight()-2),4,2,
                getGLTheme().values().getColor("easyknack.midi.keyboard.focus",com.vin.easyknack.ui.ApplicationTheme.current().color("midi.keyboard.focus")),GLStroke.WidthSpace.SCREEN);}
    }
}
