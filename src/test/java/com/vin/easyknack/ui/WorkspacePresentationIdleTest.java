package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class WorkspacePresentationIdleTest {
    @Test void startupRefreshSettlesInsteadOfSchedulingItselfForever() throws Exception {
        var controller = new WorkspaceController[1];
        try {
            SwingUtilities.invokeAndWait(() -> controller[0] = new WorkspaceController());
            for (int i = 0; i < 4; i++) SwingUtilities.invokeAndWait(() -> {});
            var pending = WorkspaceController.class.getDeclaredField("presentationRefreshPending");
            pending.setAccessible(true);
            SwingUtilities.invokeAndWait(() -> {
                try {
                    assertFalse(pending.getBoolean(controller[0]), "An idle workspace must stop scheduling presentation updates");
                } catch (IllegalAccessException e) { throw new AssertionError(e); }
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> { if (controller[0] != null) controller[0].close(); });
        }
    }
}
