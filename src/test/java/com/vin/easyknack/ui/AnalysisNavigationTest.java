package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.input.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class AnalysisNavigationTest {
    @Test void correlationCursorDragAndReopeningKeepIndependentCursors()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                var source=new AnalysisEditor.Plot(true,true);source.bind("cursor-drag");
                AudioAnalysis.publish(java.util.Map.of("cursor-drag",new AudioAnalysis.Frame(0,0,.010,true,new float[101])));
                var focus=new AnalysisFocusView(()->{});focus.setBounds(0,0,1018,548);focus.open(source);var p=focus.plot();
                p.mousePressed(mouse(p,MouseEvent.MOUSE_PRESSED,1,0,278,100));p.mouseReleased(mouse(p,MouseEvent.MOUSE_RELEASED,1,0,278,100));
                double a=p.visibleSnapshot().cursorA();assertTrue(a<0);
                p.mousePressed(mouse(p,MouseEvent.MOUSE_PRESSED,1,0,278,100));
                try{
                    var tooltip=AnalysisEditor.Plot.class.getDeclaredMethod("toolTipTextAt",GLMouseEvent.class);tooltip.setAccessible(true);
                    assertNull(tooltip.invoke(p,mouse(p,MouseEvent.MOUSE_MOVED,0,0,278,100)));
                }catch(ReflectiveOperationException e){throw new AssertionError(e);}
                p.mouseDragged(mouse(p,MouseEvent.MOUSE_DRAGGED,0,0,328,100));p.mouseReleased(mouse(p,MouseEvent.MOUSE_RELEASED,1,0,328,100));
                assertTrue(p.visibleSnapshot().cursorA()>a);assertTrue(Double.isNaN(p.visibleSnapshot().cursorB()));
                p.mousePressed(mouse(p,MouseEvent.MOUSE_PRESSED,1,InputEvent.SHIFT_DOWN_MASK,678,100));p.mouseReleased(mouse(p,MouseEvent.MOUSE_RELEASED,1,0,678,100));
                var before=p.visibleSnapshot();focus.clear();focus.open(source);p=focus.plot();
                assertEquals(before.cursorA(),p.visibleSnapshot().cursorA());assertEquals(before.cursorB(),p.visibleSnapshot().cursorB());
                p.clearCursor(true);assertTrue(Double.isNaN(p.visibleSnapshot().cursorA()));assertEquals(before.cursorB(),p.visibleSnapshot().cursorB());assertTrue(p.visibleSnapshot().measurements().startsWith("B:"));
                focus.clear();focus.open(source);assertTrue(Double.isNaN(focus.plot().visibleSnapshot().cursorA()));assertEquals(before.cursorB(),focus.plot().visibleSnapshot().cursorB());focus.clear();
            }finally{AudioAnalysis.clear();}
        });
    }
    @Test void viewportPreservesAnchorAndCoordinatesForLinearAndLog(){
        for(boolean log:new boolean[]{false,true}){
            var full=AnalysisInspectionTest.snapshot(new float[2049],log,false);
            var zoom=full.withView(AnalysisChart.View.FULL.zoom(.4,.25,false),100,200);
            assertEquals(full.xAt(.4),zoom.xAt(.4),1e-8);
            assertEquals(.6,zoom.position(zoom.xAt(.6)),1e-10);
            assertEquals(full.sampleX(17),zoom.sampleX(17));
            assertEquals(full.nearestIndex(200),zoom.nearestIndex(200));
            assertTrue(AnalysisChart.ticks(zoom).stream().allMatch(t->t.value()>=zoom.xAt(0)&&t.value()<=zoom.xAt(1)+1e-8));
            assertEquals(0,zoom.view().pan(-100,0).x0());assertEquals(1,zoom.view().pan(100,0).x1(),1e-12);
        }
        var y=AnalysisChart.View.FULL.zoom(.5,.5,true);assertEquals(-.5,y.y0());assertEquals(.5,y.y1());
    }
    private static GLMouseEvent mouse(AnalysisEditor.Plot p,int type,int button,int modifiers,int x,int y){
        var event=new MouseEvent(new java.awt.Canvas(),type,0,modifiers,x,y,1,false,button);
        return new GLMouseEvent(event,new GLPickResult(1,p,x,y,x,y,1,0,0,1,0,0),0,0);
    }
    @Test void cursorClicksSnapAndWheelZoomResetsWithoutChangingRawExport()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                var source=new AnalysisEditor.Plot(false);source.bind("navigation");
                AudioAnalysis.publish(java.util.Map.of("navigation",new AudioAnalysis.Frame(0,0,.010,new float[10])));
                var p=source.expandedCopy();p.setBounds(0,0,1002,500);
                p.mousePressed(mouse(p,MouseEvent.MOUSE_PRESSED,1,0,278,100));p.mouseReleased(mouse(p,MouseEvent.MOUSE_RELEASED,1,0,278,100));
                p.mousePressed(mouse(p,MouseEvent.MOUSE_PRESSED,1,0,478,100));p.mouseReleased(mouse(p,MouseEvent.MOUSE_RELEASED,1,0,478,100));
                var measured=p.visibleSnapshot();assertEquals(2,measured.cursorA());assertEquals(4,measured.cursorB());
                assertTrue(measured.measurements().contains("500 Hz"));
                var wheel=new MouseWheelEvent(new java.awt.Canvas(),MouseEvent.MOUSE_WHEEL,0,0,478,100,0,false,MouseWheelEvent.WHEEL_UNIT_SCROLL,1,-3);
                var e=new GLMouseWheelEvent(wheel,new GLPickResult(1,p,478,100,478,100,1,0,0,1,0,0));p.mouseWheelMoved(e);
                assertTrue(e.isConsumed());assertNotEquals(AnalysisChart.View.FULL,p.visibleSnapshot().view());
                assertEquals(AnalysisChart.View.FULL,p.snapshot().view());assertEquals(2,p.visibleSnapshot().cursorA());
                p.resetView();assertEquals(AnalysisChart.View.FULL,p.visibleSnapshot().view());p.clearCursors();assertEquals("",p.visibleSnapshot().measurements());
            }finally{AudioAnalysis.clear();}
        });
    }
    @Test void croppedAndFullImagesDifferAndExportKeepsCursors()throws Exception{
        float[] data=new float[2049];for(int i=0;i<data.length;i++)data[i]=(float)Math.sin(i*.03);
        var full=AnalysisInspectionTest.snapshot(data,false,false).withView(AnalysisChart.View.FULL,3000,6000);
        var cropped=full.withView(new AnalysisChart.View(.1,.4,-.5,.5),3000,6000);
        var a=AnalysisImageExport.render(full,1200,700);var b=AnalysisImageExport.render(cropped,1200,700);
        int changes=0;for(int y=0;y<700;y++)for(int x=0;x<1200;x++)if(a.getRGB(x,y)!=b.getRGB(x,y))changes++;
        assertTrue(changes>10000);
        javax.imageio.ImageIO.write(b,"png",new java.io.File("target/analysis-cursors-zoom.png"));
    }
}
