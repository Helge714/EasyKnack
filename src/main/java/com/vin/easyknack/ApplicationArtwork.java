package com.vin.easyknack;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.vingame.glswing.application.GLSplashViewContext;

/** Product artwork shared by the startup window and workspace. */
public final class ApplicationArtwork {
    private ApplicationArtwork() { }

    private static Image image(String name) {
        return new ImageIcon(Objects.requireNonNull(ApplicationArtwork.class.getResource(
                "/branding/" + name), "Missing application artwork: " + name)).getImage();
    }

    public static void icon(Window window) {
        window.setIconImage(image("EKIcon.png"));
    }

    public static JComponent splash(GLSplashViewContext context) {
        Image background = image("EKSplash.png");
        JPanel content = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };
        // Apply the taskbar icon as soon as Core attaches this view to its window.
        content.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                Window window = SwingUtilities.getWindowAncestor(content);
                if (window != null) icon(window);
            }
        });
        JPanel status = new JPanel();
        status.setLayout(new BoxLayout(status, BoxLayout.Y_AXIS));
        status.setBackground(context.configuration().background());
        status.setBorder(BorderFactory.createEmptyBorder(12, 20, 14, 20));
        status.add(context.title("EasyKnack"));
        status.add(context.currentTask());
        status.add(context.taskHistory(2, 0, 0.6, false));
        status.add(context.progressBar());
        content.add(status, BorderLayout.SOUTH);
        return content;
    }
}
