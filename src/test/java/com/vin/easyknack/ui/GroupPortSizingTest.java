package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.GLNodeTypeRegistry;

class GroupPortSizingTest {
    @Test void removingAGroupPortRepacksHeightAfterNavigationAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()) {
            c.openDocument("Group sizing",GroupUnitsTest.document(false));
            var before=GroupUnitsTest.group(c);float originalHeight=before.getHeight();before.setSize(600,originalHeight);
            c.workspace().openComposite("test.asset");
            var groups=field(c,"composites",GLCompositeGraphRegistry.class);
            var types=field(c,"types",GLNodeTypeRegistry.class);
            GLCompositeInterfaceTransactions.removePort(groups,types,"test.asset","Pitch",c.history(),"Remove input");
            c.workspace().navigateBack();
            var after=GroupUnitsTest.group(c);
            assertEquals(600,after.getWidth());assertTrue(after.getHeight()<originalHeight);
            assertEquals(after.getPreferredGLSize().height(),after.getHeight(),.01);
            c.workspace().openComposite("test.asset");assertTrue(c.history().undo());c.workspace().navigateBack();
            after=GroupUnitsTest.group(c);assertEquals(600,after.getWidth());assertEquals(originalHeight,after.getHeight(),.01);
        }});
    }
    private static <T> T field(Object owner,String name,Class<T> type) {
        try {var field=owner.getClass().getDeclaredField(name);field.setAccessible(true);return type.cast(field.get(owner));}
        catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
    }
}
