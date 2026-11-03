package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.formula.FormulaCompiler;
import com.vin.easyknack.nodes.SignalCategory;
import com.vingame.glswing.node.composite.GLCompositePortMapping;
import com.vingame.glswing.node.model.GLNodeValueTypeDescriptor;
import com.vingame.glswing.ui.GLComboBox;
import com.vingame.glswing.ui.GLList;

class FormulaBufferInputTest {
    @Test void bufferTypeCanBeSelectedInFormulaInterfaceAndSurvivesSaving() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                controller.addFormulaGroup(100, 100);
                var document = controller.snapshotDocument();
                var asset = document.embeddedAssets().values().stream()
                        .filter(FormulaGroups::isFormula).findFirst().orElseThrow();
                assertTrue(controller.workspace().openComposite(asset.id()));
                var editor = controller.workspace().interfaceEditor();
                var bufferType = editor.valueTypes().find(SignalCategory.HISTORY.valueTypeId()).orElseThrow();
                assertEquals(AppText.text("datatype.buffer"), bufferType.displayName());
                var input = asset.compositeDescriptor().portMappings().stream()
                        .filter(port -> port.publicPort().id().equals("signal")).findFirst().orElseThrow();
                var list = (GLList<?>) GroupEditingTest.descendants(editor)
                        .filter(child -> child instanceof GLList<?> items && indexOf(items, input) >= 0)
                        .findFirst().orElseThrow();
                list.setSelectedIndex(indexOf(list, input));
                @SuppressWarnings("unchecked")
                var types = (GLComboBox<GLNodeValueTypeDescriptor>) GroupEditingTest.descendants(editor)
                        .filter(child -> child instanceof GLComboBox<?> box && box.getItems().contains(bufferType))
                        .findFirst().orElseThrow();
                types.setSelectedItem(bufferType);
                assertEquals(bufferType.id(), editor.selectedPort().publicPort().valueTypeId());
                controller.refreshAppearance();
                assertEquals(AppText.text("datatype.buffer"), types.getSelectedItem().displayName());
                var saved = controller.snapshotDocument();
                var changed = saved.embeddedAssets().get(asset.id());
                var accepted = FormulaGroups.accept(changed, "result = signal.size;");
                assertTrue(FormulaGroups.ports(accepted).stream().anyMatch(port ->
                        port.name().equals("signal") && port.type() == FormulaCompiler.Type.BUFFER));
                var bytes = new java.io.ByteArrayOutputStream();
                var codec = new KnackDocumentCodec();
                codec.write(saved, bytes);
                var restored = codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()), codec.currentSchemaVersion());
                GLCompositePortMapping mapping = restored.embeddedAssets().get(asset.id()).compositeDescriptor()
                        .portMappings().stream().filter(port -> port.publicPort().id().equals("signal")).findFirst().orElseThrow();
                assertEquals(bufferType.id(), mapping.publicPort().valueTypeId());
            } catch (java.io.IOException failure) { throw new AssertionError(failure); }
        });
    }
    private static int indexOf(GLList<?> list, Object value) {
        for (int i = 0; i < list.getItemCount(); i++) if (value.equals(list.getItemAt(i))) return i;
        return -1;
    }
}
