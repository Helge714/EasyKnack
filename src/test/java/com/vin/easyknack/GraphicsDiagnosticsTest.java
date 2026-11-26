package com.vin.easyknack;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named="easyknack.test.gl", matches="true")
class GraphicsDiagnosticsTest {
    @Test void distinguishesContextRecreationFromAWindowOrProcessRestart() throws Exception {
        var bytes = new ByteArrayOutputStream();
        var trace = GraphicsDiagnostics.start(new PrintStream(bytes, true, java.nio.charset.StandardCharsets.UTF_8));
        JFrame[] window = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                var canvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL3)));
                window[0] = new JFrame("Graphics diagnostic regression");
                window[0].add(canvas); window[0].setSize(240, 160); window[0].setVisible(true);
                canvas.display();
                window[0].remove(canvas); window[0].add(canvas); window[0].validate();
                canvas.display();
                window[0].setLocation(40, 40);
                window[0].dispose();
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> { if (window[0] != null) window[0].dispose(); });
            trace.close();
        }
        String log = bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(log.contains("pid=" + ProcessHandle.current().pid()));
        assertEquals(1, log.lines().filter(line -> line.contains("window.observed JFrame@")).count(), log);
        assertTrue(log.lines().filter(line -> line.contains("] gl.init ")).count() >= 2, log);
        assertTrue(log.contains("] gl.dispose "), log);
        assertTrue(log.contains("] gl.first-frame "), log);
        assertTrue(log.contains("scale="), log);
        assertTrue(log.contains("renderer="), log);
    }
}
