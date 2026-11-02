package com.vin.easyknack.ui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.Timer;
import com.vin.easyknack.audio.AudioHostService.VolumeKeyEvent;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.component.GLSwingPanel;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.GLControl;
import com.vingame.glswing.ui.GLLabel;

/** Brief desktop feedback for volume keys consumed by the audio host. EDT-only. */
final class VolumeKeyOverlay implements AutoCloseable {
    private final JFrame owner;
    private final Timer dismissal;
    private JWindow window;
    private Surface surface;
    private final Content content = new Content();
    private boolean closed;

    VolumeKeyOverlay(JFrame owner) {
        this.owner = owner;
        dismissal = new Timer(1500, event -> hide());
        dismissal.setRepeats(false);
    }

    void show(VolumeKeyEvent event) {
        if (closed || event.targets() < 1 || GraphicsEnvironment.isHeadless()) return;
        content.update(event);
        var configuration = screen();
        if (window == null) {
            // Deliberately unowned: feedback also works while the app is minimized.
            window = new JWindow((Window) null, configuration);
            window.setType(Window.Type.POPUP);
            window.setFocusableWindowState(false);
            window.setAutoRequestFocus(false);
            window.setAlwaysOnTop(true);
            surface = new Surface(content, () -> {
                if (!closed && window != null && window.isVisible()) dismissal.restart();
            });
            surface.setFocusable(false);
            window.setContentPane(surface);
            window.setSize(320, 96);
        }
        surface.setGLTheme(ApplicationTheme.current().theme());
        Rectangle bounds = placement(configuration.getBounds(),
                Toolkit.getDefaultToolkit().getScreenInsets(configuration), window.getWidth(), window.getHeight());
        window.setLocation(bounds.x, bounds.y);
        dismissal.stop();
        surface.awaitPresentation();
        window.setVisible(true);
        surface.repaint();
    }

    private GraphicsConfiguration screen() {
        var pointer = MouseInfo.getPointerInfo();
        if (pointer != null) return pointer.getDevice().getDefaultConfiguration();
        if (owner != null) return owner.getGraphicsConfiguration();
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    static Rectangle placement(Rectangle screen, Insets insets, int width, int height) {
        int left = screen.x + insets.left, top = screen.y + insets.top;
        int usableWidth = screen.width - insets.left - insets.right;
        int usableHeight = screen.height - insets.top - insets.bottom;
        return new Rectangle(left + Math.max(0, (usableWidth - width) / 2),
                top + Math.max(0, usableHeight - height - 32), width, height);
    }

    private void hide() {
        dismissal.stop();
        if (window != null) window.setVisible(false);
    }

    @Override public void close() {
        closed = true;
        hide();
        if (window != null) {
            window.dispose();
            surface.destroy();
            window = null;
            surface = null;
        }
    }

    static final class Content extends GLContainer {
        final GLLabel title = new GLLabel("EasyKnack");
        final GLLabel percent = new GLLabel("");
        final GLLabel caption = new GLLabel("");
        int level;
        private final GLControl bar = new GLControl() {
            @Override protected void record(GLContext2D context) {
                var style = getGLTheme().sliderStyle();
                context.fillUIRoundRect(0, 0, getWidth(), getHeight(), style.cornerRadius(), style.background());
                if (level > 0) context.fillUIRoundRect(0, 0, getWidth() * level / 100f,
                        getHeight(), style.cornerRadius(), style.progressLightColor());
            }
        };
        Content() {
            percent.setAlignment(GLLabel.Alignment.RIGHT);
            for (var label : new GLLabel[]{title, percent, caption}) {
                label.setVerticalAlignment(GLLabel.VerticalAlignment.CENTER);
                addGL(label);
            }
            addGL(bar);
        }
        void update(VolumeKeyEvent event) {
            level = Math.max(0, Math.min(100, event.percent()));
            percent.setText(level + " %");
            caption.setText(event.targets() == 1 ? AppText.text("volume.overlay.single")
                    : AppText.format("volume.overlay.multiple", event.targets()));
            bar.invalidateRecording();
        }
        @Override protected void record(GLContext2D context) {
            var style = getGLTheme().menuBarStyle();
            context.fillRect(0, 0, getWidth(), getHeight(), style.background());
        }
        @Override protected void resized(float width, float height) {
            title.setBounds(16, 8, width - 104, 26);
            percent.setBounds(width - 88, 8, 72, 26);
            bar.setBounds(16, 43, width - 32, 8);
            caption.setBounds(16, 58, width - 32, 26);
        }
    }

    private static final class Surface extends GLSwingPanel {
        private final Content content;
        private final Runnable presented;
        private final java.util.concurrent.atomic.AtomicBoolean awaiting = new java.util.concurrent.atomic.AtomicBoolean();
        Surface(Content content, Runnable presented) {
            this.content = content; this.presented = presented; addGL(content);
        }
        void awaitPresentation() { awaiting.set(true); }
        @Override protected void render(GLContext2D context) {
            super.render(context);
            if (awaiting.getAndSet(false)) javax.swing.SwingUtilities.invokeLater(presented);
        }
        @Override protected void resized(com.jogamp.opengl.GL3 gl, int width, int height) {
            content.setBounds(0, 0, getWidth(), getHeight());
        }
    }
}
