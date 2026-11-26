package com.vin.easyknack;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MacDesktopTest {
    @Test void commandShortcutsTranslateOnlyForTheGraphCanvas(){
        int commandShift=InputEvent.META_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK;
        assertEquals(InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK,MacDesktop.shortcutModifiers(KeyEvent.VK_Z,commandShift,true));
        assertEquals(commandShift,MacDesktop.shortcutModifiers(KeyEvent.VK_Z,commandShift,false));
        assertEquals(commandShift,MacDesktop.shortcutModifiers(KeyEvent.VK_Q,commandShift,true));
        int option=InputEvent.META_DOWN_MASK|InputEvent.ALT_DOWN_MASK;
        assertEquals(option,MacDesktop.shortcutModifiers(KeyEvent.VK_S,option,true));
        assertEquals(0,MacDesktop.shortcutModifiers(KeyEvent.VK_N,0,true));
    }
}
