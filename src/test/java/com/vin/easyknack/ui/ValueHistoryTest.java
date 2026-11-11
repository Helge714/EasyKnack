package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import java.io.StringWriter;
import java.awt.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.rendering.*;
import static org.junit.jupiter.api.Assertions.*;

class ValueHistoryTest {
    static AudioAnalysis.Frame packet(long end,int stride,float... envelope){
        var values=new float[8+envelope.length];values[0]=1;values[1]=stride;values[2]=envelope.length/3;
        for(int j=0;j<4;j++)values[4+j]=(end>>>(16*j))&65535;
        System.arraycopy(envelope,0,values,8,envelope.length);return new AudioAnalysis.Frame(1,1000,0,false,values);
    }
    @Test void packetsDeduplicatePreservePeaksAndKeepExactTime(){
        var h=new ValueHistory();var p=packet(20,10,0,9,0,1,2,1);h.accept(p);h.accept(p);
        h.accept(packet(30,10,1,2,1,3,4,3));
        var s=h.snapshot(null,null,false);assertEquals(3,s.size());assertEquals(9,s.maximum()[0][0]);assertEquals(.03,s.end(),1e-12);
        var reduced=h.snapshot(null,null,false,1);assertEquals(1,reduced.size());assertEquals(9,reduced.maximum()[0][0]);
        h.accept(packet(1L<<40,10,5,5,5));assertEquals((double)(1L<<40)/1000,h.snapshot(null,null,false).end(),1e-9);
        h.accept(packet(10,10,7,7,7));assertEquals(1,h.snapshot(null,null,false).size());
    }
    @Test void boundedHistoryAndSnapshotsAreIndependent(){
        var h=new ValueHistory();h.configure(1,.1);h.accept(packet(30,10,1,1,1,2,2,2,3,3,3));
        var frozen=h.snapshot(new String[]{"Pitch"},new String[]{"Hz"},true);
        h.accept(packet(150,10,4,4,4));var live=h.snapshot(null,null,false);
        assertEquals(3,frozen.size());assertEquals(.03,frozen.end(),1e-12);assertTrue(frozen.frozen());assertEquals(1,live.size());
        assertEquals(2,frozen.range(.015,.035).size());
    }
    @Test void csvEscapingExcelColumnsAndImageSnapshots()throws Exception{
        var h=new ValueHistory();h.accept(packet(20,10,0,9,1,2,3,2));
        var s=h.snapshot(new String[]{"A,\"B"},new String[]{"Hz"},true);
        var csv=new StringWriter();ValueViewerExport.encodeCsv(s,csv,false,true);
        assertTrue(csv.toString().startsWith("\uFEFFtime_s;\"A,\"\"B [Hz]\""));assertTrue(csv.toString().contains("0,01;1,0;0,0;9,0;"));assertEquals(3,csv.toString().lines().count());
        var numbers=new StringWriter();ValueViewerExport.encodeCsv(s,numbers,true,false);assertEquals(2,numbers.toString().lines().count());assertFalse(numbers.toString().contains(" min"));
        for(boolean numeric:new boolean[]{false,true})assertEquals(800,ValueViewerExport.render(s,800,480,numeric,false,new Color[]{Color.BLUE}).getWidth());
    }
    @Test void freezeRetainsTheSnapshotWhileLiveCaptureContinues()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            try{
                var panel=new ValueViewerPanel(v->{});panel.bind("values-freeze");
                AudioAnalysis.publish(java.util.Map.of("values-freeze",packet(20,10,0,9,1,2,3,2)));
                panel.setPaused(true);var frozen=panel.snapshot(false);assertEquals(.02,frozen.end(),1e-12);
                AudioAnalysis.publish(java.util.Map.of("values-freeze",packet(30,10,3,4,3)));
                assertSame(frozen,panel.snapshot(false));panel.setPaused(false);assertEquals(.03,panel.snapshot(false).end(),1e-12);
            }finally{AudioAnalysis.clear();}
        });
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void actualMultiInputControlRendersInBothModes()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,1000,720);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try{
                var panel=new ValueViewerPanel(v->{});var definition=new com.vin.easyknack.nodes.ValueViewerNode();
                var properties=new java.util.HashMap<>(definition.defaults());properties.put("inputs",com.vingame.glswing.node.model.GLNodeValue.of(8.));
                panel.configure(new com.vin.easyknack.nodes.NodeState(properties));panel.bind("values-render");
                float[] payload=new float[8+100*8*3];payload[0]=1;payload[1]=10;payload[2]=100;payload[4]=1000;
                for(int b=0;b<100;b++)for(int p=0;p<8;p++){float value=(float)Math.sin(b*.1+p);int i=8+(b*8+p)*3;payload[i]=value-.05f;payload[i+1]=value+.05f;payload[i+2]=value;}
                AudioAnalysis.publish(java.util.Map.of("values-render",new AudioAnalysis.Frame(8,1000,0,payload)));
                panel.setGLTheme(ApplicationTheme.current().theme());fonts.attach(panel);panel.setBounds(0,0,1000,720);
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,1000,720);renderer=GLRenderer2D.create(gl);
                for(String mode:new String[]{"history","values"}){
                    panel.options(mode);var frame=renderer.beginFrame(gl,1000,720,Projection2D.TOP_LEFT_PIXELS.create(1000,720));frame.clear(Color.BLACK);panel.renderComponent(frame);renderer.endFrame();
                    var pixels=java.nio.ByteBuffer.allocateDirect(1000*720*4);gl.glReadPixels(0,0,1000,720,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                    var image=new java.awt.image.BufferedImage(1000,720,java.awt.image.BufferedImage.TYPE_INT_RGB);
                    for(int y=0;y<720;y++)for(int x=0;x<1000;x++){int i=((719-y)*1000+x)*4;image.setRGB(x,y,(pixels.get(i)&255)<<16|(pixels.get(i+1)&255)<<8|(pixels.get(i+2)&255));}
                    assertEquals(1000,image.getWidth());javax.imageio.ImageIO.write(image,"png",new java.io.File("target/value-viewer-"+mode+".png"));
                    assertEquals(GL.GL_NO_ERROR,gl.glGetError());
                }
            }catch(java.io.IOException ex){throw new AssertionError(ex);}finally{AudioAnalysis.clear();fonts.dispose(renderer);}
        });
    }
}
