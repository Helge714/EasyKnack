package com.vin.easyknack;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
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
        status.setOpaque(false);
        int padding = context.configuration().padding();
        JPanel textArea = new JPanel(new GridBagLayout());
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        // Match Core's standard splash recipe, with transparent content over the artwork.
        status.add(context.title());
        status.add(Box.createVerticalStrut(4));
        status.add(context.subtitle(BuildInfo.application().version()));
        status.add(Box.createVerticalStrut(22));
        status.add(context.currentTask());
        status.add(Box.createVerticalStrut(8));
        status.add(context.taskHistory(5, 0, 1, true));
        for (var component : status.getComponents())
            if (component instanceof JComponent child) child.setAlignmentX(0f);
        var constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        textArea.add(status, constraints);
        content.add(textArea, BorderLayout.CENTER);
        content.add(context.progressBar(), BorderLayout.SOUTH);
        return content;
    }
}
