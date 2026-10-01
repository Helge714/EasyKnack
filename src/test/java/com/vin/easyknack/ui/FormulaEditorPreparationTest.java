package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FormulaEditorPreparationTest {
    @Test void preparedEditorIsCleanAndOwnershipIsTransferred() throws Exception {
        java.awt.EventQueue.invokeAndWait(() -> {
            FormulaEditorPreparation.prepare();
            FormulaEditorPreparation.prepare();
            var first = FormulaEditorPreparation.acquire();
            assertEquals("", first.getText());
            assertEquals("easyknack-formula", first.getLanguage().id());
            first.setText("double privateValue = 1;");
            var second = FormulaEditorPreparation.acquire();
            assertNotSame(first, second);
            assertEquals("", second.getText());
            first.clearDiagnosticProvider();
            second.clearDiagnosticProvider();
        });
    }
}
