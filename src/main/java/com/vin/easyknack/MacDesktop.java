package com.vin.easyknack;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.desktop.AppReopenedListener;
import java.util.List;

/** Finder document events and native Quit use the same document/close flows as the UI. */
final class MacDesktop {
    private MacDesktop() { }
    static void install(DocumentLaunch launches) {
        if(NativePlatform.current().os()!=NativePlatform.OperatingSystem.MACOS||!Desktop.isDesktopSupported())return;
        var desktop=Desktop.getDesktop();
        var keyboard=KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyboard.addKeyEventDispatcher(event -> {
            int modifiers=shortcutModifiers(event.getKeyCode(),event.getModifiersEx(),event.getComponent() instanceof com.jogamp.opengl.awt.GLCanvas);
            if(modifiers==event.getModifiersEx())return false;
            var translated=new KeyEvent(event.getComponent(),event.getID(),event.getWhen(),modifiers,event.getKeyCode(),event.getKeyChar(),event.getKeyLocation());
            keyboard.redispatchEvent(event.getComponent(),translated);event.consume();return true;
        });
        if(desktop.isSupported(Desktop.Action.APP_OPEN_FILE))desktop.setOpenFileHandler(event -> submit(launches,event.getFiles().stream().map(java.io.File::toPath).toList()));
        if(desktop.isSupported(Desktop.Action.APP_EVENT_REOPENED))desktop.addAppEventListener((AppReopenedListener)event -> submit(launches,List.of()));
        if(desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER))desktop.setQuitHandler((event,response) -> {
            response.cancelQuit();
            java.awt.EventQueue.invokeLater(() -> {
                for(var frame:Frame.getFrames())if(frame.isVisible()&&frame.getTitle().contains("EasyKnack")){frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));return;}
            });
        });
    }
    static int shortcutModifiers(int key,int modifiers,boolean canvas){
        boolean familiar=switch(key){case KeyEvent.VK_UNDEFINED,KeyEvent.VK_A,KeyEvent.VK_C,KeyEvent.VK_V,KeyEvent.VK_X,KeyEvent.VK_Z,KeyEvent.VK_Y,KeyEvent.VK_S,KeyEvent.VK_O,KeyEvent.VK_N,KeyEvent.VK_F -> true;default -> false;};
        return canvas&&familiar&&(modifiers&InputEvent.META_DOWN_MASK)!=0&&(modifiers&InputEvent.ALT_DOWN_MASK)==0
                ? (modifiers&~InputEvent.META_DOWN_MASK)|InputEvent.CTRL_DOWN_MASK : modifiers;
    }
    private static void submit(DocumentLaunch launches,List<java.nio.file.Path> paths){try{launches.submit(paths);}catch(java.io.IOException error){error.printStackTrace();}}
}
