package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.input.*;

class CurveNodeTest {
    @Test void bezierIsSingleValuedAndDocumentRoundTripsWithoutSamplingLoss(){
        var curve=GLFunctionCurve.identity().mode(0,GLFunctionCurve.Mode.BEZIER)
                .handle(0,false,1.0/3,0).handle(1,true,2.0/3,0);
        for(int n=0;n<=100;n++){double x=n/100.0;assertEquals(x*x*x,curve.evaluate(x),1e-8);}
        assertEquals(curve,CurveData.decode(CurveData.encode(curve)));
        curve=curve.insert(.4,.7).mode(1,GLFunctionCurve.Mode.BEZIER).linked(1,true);
        Random random=new Random(18);
        for(int i=0;i<100;i++){
            curve=curve.move(1,random.nextDouble(),random.nextDouble()).handle(1,i%2==0,random.nextDouble(),random.nextDouble());
            assertEquals(curve,CurveData.decode(CurveData.encode(curve)));
            for(int j=0;j<20;j++){double y=curve.evaluate(j/19.0);assertTrue(y>=0&&y<=1);}
        }
        assertThrows(IllegalArgumentException.class,()->CurveData.decode("v2 2"));
        assertThrows(IllegalArgumentException.class,()->CurveData.decode("v1 2 0 NaN 0 0 0 0 0 0 1 1 1 1 1 1 0 0"));
    }
    @Test void nodeAndInspectorShareCurveWithDragUndoAndCompactHide() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController();var reopened=new WorkspaceController()){
                var def=new CurveNode();var id=GLNodeId.random();var ws=controller.workspace();
                ws.currentGraph().edit("Curve",e->e.addNode(new GLGraphNode(id,def.registration().id(),def.defaults())));
                java.util.function.Supplier<GLNode> view=()->ws.editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                var original=view.get();ws.editor().setSelectedNodes(List.of(original));
                var curve=original.getContents().stream().filter(GLCurveEditor.class::isInstance).map(GLCurveEditor.class::cast).findFirst().orElseThrow();
                var inspector=ws.propertyInspector().getGLChildren().stream().filter(GLCurveEditor.class::isInstance).map(GLCurveEditor.class::cast).findFirst().orElseThrow();
                curve.setSize(180,110);
                curve.mouseClicked(mouse(curve,MouseEvent.MOUSE_CLICKED,90,55,2));
                assertEquals(3,curve.getCurve().points().size());assertEquals(curve.getCurve(),inspector.getCurve());
                var before=curve.getCurve();
                curve.mousePressed(mouse(curve,MouseEvent.MOUSE_PRESSED,90,55,1));
                curve.mouseDragged(mouse(curve,MouseEvent.MOUSE_DRAGGED,100,30,1));
                curve.mouseDragged(mouse(curve,MouseEvent.MOUSE_DRAGGED,105,20,1));
                curve.mouseReleased(mouse(curve,MouseEvent.MOUSE_RELEASED,105,20,1));
                var after=curve.getCurve();assertNotEquals(before,after);assertSame(original,view.get());
                assertEquals(after,inspector.getCurve());controller.history().undo();assertEquals(before,curve.getCurve());
                controller.history().redo();assertEquals(after,curve.getCurve());
                inspector.setSize(200,200);inspector.mouseClicked(mouse(inspector,MouseEvent.MOUSE_CLICKED,55,140,2));
                assertEquals(inspector.getCurve(),curve.getCurve());assertEquals(4,curve.getCurve().points().size());
                controller.history().undo();assertEquals(after,curve.getCurve());
                float height=curve.getPreferredGLSize().height();String audio=controller.audioGraph();
                controller.actions().toggleHidden();assertTrue(curve.isPreview());assertTrue(curve.isVisible());
                assertTrue(curve.getPreferredGLSize().height()<height);assertEquals(audio,controller.audioGraph());
                curve.mouseClicked(mouse(curve,MouseEvent.MOUSE_CLICKED,30,10,2));assertEquals(after,curve.getCurve());
                controller.actions().toggleHidden();assertFalse(curve.isPreview());assertEquals(height,curve.getPreferredGLSize().height());
                assertTrue(original.getPreferredGLSize().width()>=curve.getPreferredGLSize().width()+original.getGLStyle().contentPadding().horizontal());
                try{
                    var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(controller.snapshotDocument(),bytes);
                    reopened.openDocument("Curve",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
                    assertEquals(controller.audioGraph(),reopened.audioGraph());
                }catch(java.io.IOException e){throw new AssertionError(e);}
            }
        });
    }
    static GLMouseEvent mouse(GLCurveEditor control,int kind,float x,float y,int clicks){
        return new GLMouseEvent(new MouseEvent(new JPanel(),kind,0,0,(int)x,(int)y,clicks,false,MouseEvent.BUTTON1),
                new GLPickResult(0,control,x,y,x,y,1,0,0,1,0,0),0,0);
    }
}
