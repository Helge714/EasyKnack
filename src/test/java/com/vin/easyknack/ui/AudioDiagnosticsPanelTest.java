package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.ui.*;
class AudioDiagnosticsPanelTest {
    @Test void pausedValuesStayVisibleAndHostReplacementClearsThem()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var p=new AudioDiagnosticsPanel(()->{});p.setBounds(0,0,1000,300);p.state(AudioHostService.State.RUNNING);
            var sample=new AudioDiagnostics(1,30,25,150,1000,9000,2,256,48000,0,1,0,0,0,0,2,"Clock: test","Buffers: test");p.accept(sample);
            java.util.function.Supplier<String> labels=()->p.getGLChildren().stream().filter(GLLabel.class::isInstance).map(GLLabel.class::cast).map(GLLabel::getText).reduce("",(a,b)->a+" "+b);
            assertTrue(labels.get().contains("150.0"));assertTrue(labels.get().contains("5.333"));
            p.state(AudioHostService.State.PAUSED);p.accept(new AudioDiagnostics(2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"",""));
            assertTrue(labels.get().contains("150.0"));assertTrue(labels.get().contains("PAUSED"));
            p.state(AudioHostService.State.RUNNING);
            p.getGLChildren().stream().filter(GLButton.class::isInstance).map(GLButton.class::cast).filter(b->b.getText().equals("Reset measurement")).findFirst().orElseThrow().doClick();
            p.state(AudioHostService.State.PAUSED);
            p.accept(new AudioDiagnostics(2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"",""));
            assertFalse(labels.get().contains("150.0"),"A reset acknowledged after pausing must clear stale values");
            p.accept(null);assertFalse(labels.get().contains("150.0"));
        });
    }
}
