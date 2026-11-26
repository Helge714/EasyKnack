package com.vin.easyknack;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Timer;
import com.jogamp.opengl.*;

/** Opt-in lifecycle trace; no listener, timer or toolkit initialization when disabled. */
public final class GraphicsDiagnostics implements AutoCloseable {
    private final PrintStream output;
    private final Set<Window> windows = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<GLAutoDrawable> drawables = Collections.newSetFromMap(new WeakHashMap<>());
    private final java.util.Map<Window, Timer> moves = new WeakHashMap<>();
    private final AWTEventListener events = this::event;
    private volatile boolean closed;
    private GraphicsDiagnostics(PrintStream output) { this.output = output; }

    public static void startIfEnabled() {
        if (!Boolean.getBoolean("easyknack.graphics.diagnostics")) return;
        var trace = start(System.err);
        Runtime.getRuntime().addShutdownHook(new Thread(trace::close, "EasyKnack graphics trace cleanup"));
    }

    static GraphicsDiagnostics start(PrintStream output) {
        var trace = new GraphicsDiagnostics(output);
        trace.log("session.start", "java=" + System.getProperty("java.version") + " os=" + System.getProperty("os.name"));
        for (var device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
            trace.log("display", configuration(device.getDefaultConfiguration()));
        Toolkit.getDefaultToolkit().addAWTEventListener(trace.events,
                AWTEvent.WINDOW_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
        EventQueue.invokeLater(() -> { for (var window : Window.getWindows()) trace.attach(window); });
        return trace;
    }

    private void event(AWTEvent event) {
        if (closed) return;
        if (event.getSource() instanceof Window window && event instanceof ComponentEvent) {
            attach(window);
            int id = event.getID();
            if (id == ComponentEvent.COMPONENT_MOVED || id == ComponentEvent.COMPONENT_RESIZED) {
                Timer timer = moves.computeIfAbsent(window, key -> {
                    var value = new Timer(250, ignored -> window("window.settled", key));
                    value.setRepeats(false); return value;
                });
                timer.restart();
            } else if (event instanceof WindowEvent || id == ComponentEvent.COMPONENT_SHOWN || id == ComponentEvent.COMPONENT_HIDDEN) {
                window("window.event." + id, window);
                if (id == WindowEvent.WINDOW_CLOSED) {
                    Timer timer = moves.remove(window);
                    if (timer != null) timer.stop();
                    windows.remove(window);
                }
            }
        }
        if (event instanceof ContainerEvent added && added.getID() == ContainerEvent.COMPONENT_ADDED)
            attach(added.getChild());
    }

    private void attach(Component component) {
        if (component instanceof Window window && windows.add(window)) window("window.observed", window);
        if (component instanceof GLAutoDrawable drawable && drawables.add(drawable)) {
            log("gl.observed", identity(component));
            drawable.addGLEventListener(new GLEventListener() {
                private boolean first;
                public void init(GLAutoDrawable value) {
                    first = true;
                    GL gl = value.getGL();
                    log("gl.init", identity(value) + " context=" + identity(value.getContext())
                            + " renderer=" + gl.glGetString(GL.GL_RENDERER) + " vendor=" + gl.glGetString(GL.GL_VENDOR)
                            + " version=" + gl.glGetString(GL.GL_VERSION) + " capabilities=" + value.getChosenGLCapabilities());
                }
                public void dispose(GLAutoDrawable value) { log("gl.dispose", identity(value) + " context=" + identity(value.getContext())); }
                public void reshape(GLAutoDrawable value, int x, int y, int width, int height) {
                    log("gl.reshape", identity(value) + " surface=" + width + "x" + height
                            + " logical=" + component.getWidth() + "x" + component.getHeight());
                }
                public void display(GLAutoDrawable value) {
                    if (first) { first = false; log("gl.first-frame", identity(value) + " context=" + identity(value.getContext())); }
                }
            });
        }
        if (component instanceof Container container)
            for (var child : container.getComponents()) attach(child);
    }

    private void window(String event, Window window) {
        log(event, identity(window) + " showing=" + window.isShowing() + " displayable=" + window.isDisplayable()
                + " bounds=" + window.getBounds() + " " + configuration(window.getGraphicsConfiguration()));
    }
    private static String configuration(GraphicsConfiguration gc) {
        return gc == null ? "graphics=null" : "device=" + gc.getDevice().getIDstring()
                + " bounds=" + gc.getBounds() + " scale=" + gc.getDefaultTransform();
    }
    private static String identity(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value));
    }
    private synchronized void log(String event, String detail) {
        if (closed) return;
        output.println("[graphics " + Instant.now() + " pid=" + ProcessHandle.current().pid()
                + " uptimeMs=" + ManagementFactory.getRuntimeMXBean().getUptime() + "] " + event + " " + detail);
    }
    @Override public void close() {
        synchronized (this) {
            if (closed) return;
            log("session.trace-closed", "");
            closed = true;
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(events);
        EventQueue.invokeLater(() -> { moves.values().forEach(Timer::stop); moves.clear(); });
    }
}
