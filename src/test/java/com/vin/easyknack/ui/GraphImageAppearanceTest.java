package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class GraphImageAppearanceTest {
 @Test void exportUsesLiveConnectionCurvatureWithoutChangingTheSource()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{try(var source=new WorkspaceController()){
   source.openDocument("Export",StarterDocument.create());source.workspace().setBounds(0,0,1000,700);
   var live=source.workspace().editor();live.setZoom(1.5f);live.setPan(120,80);
   for(double curvature:new double[]{0,.35,1}){
    ConnectionAppearance.apply(live,curvature);
    for(var area:java.util.List.of(GraphImageExport.Area.ALL,GraphImageExport.Area.VISIBLE))
     try(var export=GraphImageExport.prepare(source,area,false)){
      var editor=export.controller().workspace().editor();
      assertEquals(live.getConnectionHandleFactor(),editor.getConnectionHandleFactor());
      assertEquals(live.getMinimumConnectionHandleLength(),editor.getMinimumConnectionHandleLength());
      assertEquals(1.5f,live.getZoom());assertEquals(120,live.getPanX());assertEquals(80,live.getPanY());
     }
   }
  }});
 }
}
