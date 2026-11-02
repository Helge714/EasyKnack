package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import com.vin.easyknack.audio.AudioHostService.VolumeKeyEvent;

class VolumeKeyOverlayTest {
    @Test void positionsAboveTaskbarOnNegativeCoordinateMonitor() {
        assertEquals(new Rectangle(-1120, 912, 320, 96), VolumeKeyOverlay.placement(
                new Rectangle(-1920, 0, 1920, 1080), new Insets(0, 0, 40, 0), 320, 96));
    }

    @Test void labelsDistinguishSingleOutputFromHighestLinkedLevel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var content = new VolumeKeyOverlay.Content();
            content.update(new VolumeKeyEvent(42, 1));
            assertEquals("42 %", content.percent.getText());
            assertEquals(AppText.text("volume.overlay.single"), content.caption.getText());
            content.update(new VolumeKeyEvent(75, 3));
            assertEquals(AppText.format("volume.overlay.multiple", 3), content.caption.getText());
            content.update(new VolumeKeyEvent(120, 3));
            assertEquals(100, content.level);
        });
    }

    @Test void desktopWindowDoesNotTakeFocusAndClosesWithoutResurrection() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        var overlay = new AtomicReference<VolumeKeyOverlay>();
        var popup = new AtomicReference<JWindow>();
        var visibilityCheck = new AtomicReference<javax.swing.Timer>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                var previous = Arrays.asList(Window.getWindows());
                overlay.set(new VolumeKeyOverlay(null));
                overlay.get().show(new VolumeKeyEvent(0, 0));
                assertEquals(previous, Arrays.asList(Window.getWindows()));
                overlay.get().show(new VolumeKeyEvent(42, 1));
                popup.set((JWindow) Arrays.stream(Window.getWindows())
                        .filter(window -> !previous.contains(window) && window instanceof JWindow)
                        .findFirst().orElseThrow());
                assertTrue(popup.get().isVisible());
                assertTrue(popup.get().isAlwaysOnTop());
                assertFalse(popup.get().getFocusableWindowState());
                assertFalse(popup.get().isAutoRequestFocus());
                assertFalse(popup.get().isFocused());
                overlay.get().show(new VolumeKeyEvent(52, 2));
                assertEquals(1, Arrays.stream(Window.getWindows())
                        .filter(window -> !previous.contains(window) && window.isVisible()).count());
            });
            // Timer keeps the UI responsive and hides the reusable window after inactivity.
            var hidden = new java.util.concurrent.CountDownLatch(1);
            SwingUtilities.invokeAndWait(() -> {
                var check = new javax.swing.Timer(50, event -> {
                    if (!popup.get().isVisible()) {
                        ((javax.swing.Timer) event.getSource()).stop();
                        hidden.countDown();
                    }
                });
                visibilityCheck.set(check);
                check.start();
            });
            assertTrue(hidden.await(10, java.util.concurrent.TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                overlay.get().close();
                assertFalse(popup.get().isDisplayable());
                overlay.get().show(new VolumeKeyEvent(80, 1));
                assertFalse(popup.get().isVisible());
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (visibilityCheck.get() != null) visibilityCheck.get().stop();
                if (overlay.get() != null) overlay.get().close();
            });
        }
    }
}
