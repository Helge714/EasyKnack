package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vingame.glswing.application.GLApplication;
import com.vingame.glswing.application.GLBackend;
import com.vingame.glswing.node.GLNodeWorkspace;
import com.vingame.glswing.ui.GLThemes;

/** Explicit desktop integration check; requires a display and OpenGL driver. */
@EnabledIfSystemProperty(named = "easyknack.test.gl", matches = "true")
class CanvasSmokeTest {
    @Test void rendersCanvasAndClosesCleanly() throws Exception {
        var handle = GLApplication.builder().title("EasyKnack — Startprüfung")
                .backend(GLBackend.CANVAS).size(1280, 800).theme(ApplicationTheme.current().theme())
                .surface(context -> new WorkspaceView(context.window()))
                .onClose(context -> context.parent().getGLChildren().stream()
                        .filter(WorkspaceView.class::isInstance)
                        .map(WorkspaceView.class::cast).forEach(WorkspaceView::close)).launch();
        try {
            var context = handle.readyFuture().get(30, TimeUnit.SECONDS);
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(context.window().isShowing());
                assertTrue(context.parent() instanceof com.jogamp.opengl.awt.GLCanvas);
                var root = context.parent().getGLChildren().stream()
                        .filter(WorkspaceView.class::isInstance).findFirst().orElseThrow();
                assertTrue(root.getWidth() >= 900);
                var workspace = (GLNodeWorkspace) root.getGLChildren().stream()
                        .filter(GLNodeWorkspace.class::isInstance).findFirst().orElseThrow();
                var editor = workspace.editor();
                assertFalse(workspace.isInspectorVisible());
                for (boolean expected : new boolean[] {true, false}) {
                    var key = new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(
                            new java.awt.Canvas(), java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                            0, java.awt.event.KeyEvent.VK_N, 'n'));
                    ((WorkspaceView) root).keyPressed(key);
                    assertTrue(key.isConsumed()); assertEquals(expected, workspace.isInspectorVisible());
                }
                assertNotNull(editor.getEditingActions());
                assertEquals(3, workspace.currentGraph().snapshot().nodes().size());
                assertTrue(editor.getHeight() > 400);
                assertEquals(2, editor.getConnections().size());
                var keyboard=new com.vin.easyknack.nodes.KeyboardMidiEditor.Keyboard();
                context.parent().addGL(keyboard);keyboard.setBounds(0,0,160,30);
                var canvas=(com.vingame.glswing.component.GLSwingContainerCanvas)context.parent();
                canvas.requestGLFocus(editor);
                var arrangeKey=new java.awt.event.KeyEvent(canvas,java.awt.event.KeyEvent.KEY_PRESSED,System.currentTimeMillis(),
                    java.awt.event.InputEvent.CTRL_DOWN_MASK|java.awt.event.InputEvent.SHIFT_DOWN_MASK,java.awt.event.KeyEvent.VK_L,'L');
                var positions=editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
                for(var listener:canvas.getKeyListeners())listener.keyPressed(arrangeKey);
                assertTrue(arrangeKey.isConsumed(),"Canvas must dispatch the arrange shortcut");
                assertNotEquals(positions,editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
                canvas.requestGLFocus(keyboard);
                com.vin.easyknack.audio.MidiDevices.drainKeyboard();
                for(int code:new int[]{java.awt.event.KeyEvent.VK_A,java.awt.event.KeyEvent.VK_N}){
                    var event=new java.awt.event.KeyEvent(canvas,java.awt.event.KeyEvent.KEY_PRESSED,1,0,code,java.awt.event.KeyEvent.CHAR_UNDEFINED);
                    for(var listener:canvas.getKeyListeners())listener.keyPressed(event);
                    assertTrue(event.isConsumed());
                }
                assertFalse(workspace.isInspectorVisible(),"Performance focus must suppress N shortcut");
                assertTrue(keyboard.isFocused());
                for(var listener:canvas.getFocusListeners())listener.focusLost(new java.awt.event.FocusEvent(canvas,java.awt.event.FocusEvent.FOCUS_LOST));
                assertFalse(keyboard.isFocused());
                context.parent().removeGL(keyboard);
                assertTrue(editor.getNodes().stream().allMatch(node -> node.getHeight() > 60));
                var math = new com.vin.easyknack.nodes.MathNode();
                var properties = new java.util.HashMap<>(math.defaults());
                properties.put("rate", com.vingame.glswing.node.model.GLNodeValue.of("control"));
                workspace.currentGraph().edit("ControlRate rendering", e -> e.addNode(
                        new com.vingame.glswing.node.model.GLGraphNode(
                                com.vingame.glswing.node.model.GLNodeId.random(), math.registration().id(), properties)));
                context.window().setSize(960, 640);
                var audioInput = new com.vin.easyknack.nodes.InputNode();
                workspace.currentGraph().edit("Input rendering", e -> e.addNode(
                        new com.vingame.glswing.node.model.GLGraphNode(
                                com.vingame.glswing.node.model.GLNodeId.random(), audioInput.registration().id(), audioInput.defaults())));
                var additive = new com.vin.easyknack.nodes.AdditiveOscillatorNode();
                var additiveProperties = new java.util.HashMap<>(additive.defaults());
                additiveProperties.put("harmonics", com.vingame.glswing.node.model.GLNodeValue.of(144));
                workspace.currentGraph().edit("Additive rendering", e -> e.addNode(
                        new com.vingame.glswing.node.model.GLGraphNode(
                                com.vingame.glswing.node.model.GLNodeId.random(), additive.registration().id(), additiveProperties)));
            });
            // Force a real frame containing the diamond ports before closing.
            ((com.jogamp.opengl.awt.GLCanvas) context.parent()).display();
        } finally {
            handle.close();
            handle.closedFuture().get(15, TimeUnit.SECONDS);
        }
    }
}

