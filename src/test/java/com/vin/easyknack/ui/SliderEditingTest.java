package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.ui.*;

class SliderEditingTest {
    static class Probe extends GLSlider {
        Probe() { super("Frequency", 0, 20000, 440); }
        void assertNoSliderDrawing() { super.record(null); }
    }
    @Test void unitsStayOutsideTheDocumentAndEditingSuppressesSliderDrawing() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (var presentation : GLSlider.Presentation.values()) {
                var slider = new Probe(); slider.setPresentation(presentation); slider.setBounds(0, 0, 180, 28);
                slider.setUnit("Hz"); slider.startValueEditing();
                assertEquals(1, slider.getGLChildren().size(), "The suffix must not introduce a pickable component");
                var editor = (GLTextEditor) slider.getGLChildren().get(0);
                assertEquals("440", editor.getText());
                assertTrue(editor.getGLStyle().padding().right() > slider.getGLStyle().padding().right());
                assertEquals(slider.getGLStyle().padding().left(), editor.getGLStyle().padding().left());
                assertDoesNotThrow(slider::assertNoSliderDrawing);
                editor.setText("880"); slider.setUnit("kHz");
                assertEquals("880", editor.getText(), "Changing the suffix must not touch the active document");
                assertTrue(editor.commit()); assertEquals(880, slider.getValue());
                assertEquals("kHz", slider.getUnit()); assertFalse(slider.isValueEditing());
                slider.startValueEditing(); editor = (GLTextEditor) slider.getGLChildren().get(0);
                assertEquals("880", editor.getText()); slider.cancelValueEditing();
                slider.setUnit(""); slider.startValueEditing(); editor = (GLTextEditor) slider.getGLChildren().get(0);
                assertEquals(slider.getGLStyle().padding(), editor.getGLStyle().padding());
                slider.cancelValueEditing();
            }
        });
    }
}
