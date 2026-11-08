package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.AnalysisEditor;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.GLButton;
import com.vingame.glswing.ui.GLCheckBox;

/** A second view of the same analysis snapshot; the editor and graph stay intact. */
final class AnalysisFocusView extends GLContainer {
    private final GLButton back=new GLButton(com.vin.easyknack.i18n.AppText.literal("Back to nodes (Esc)"));
    private AnalysisEditor.Plot plot;
    private final GLButton pause=new GLButton(AppText.text("analysis.pause")),export=new GLButton(AppText.text("analysis.export"));
    private final GLCheckBox logarithmic=new GLCheckBox(AppText.text("analysis.log"));
    private final javax.swing.Timer controls=new javax.swing.Timer(150,e->syncControls());
    private void syncControls(){
        if(plot==null)return;
        pause.setText(AppText.text(plot.isPaused()?"analysis.resume":"analysis.pause"));pause.setEnabled(plot.hasFrame());
        export.setEnabled(plot.hasFrame());logarithmic.setVisible(plot.isSpectrum());logarithmic.setSelected(plot.isLogarithmic());
    }
    AnalysisFocusView(Runnable close){
        back.addActionListener(e->close.run());for(var child:java.util.List.of(back,pause,logarithmic,export))addGL(child);
        pause.addActionListener(e->{if(plot!=null){plot.setPaused(!plot.isPaused());syncControls();}});
        logarithmic.addActionListener(e->{if(plot!=null)plot.setLogarithmic(logarithmic.isSelected());});
        export.addActionListener(e->{
            if(plot==null)return;
            var menu=new com.vingame.glswing.ui.GLPopupMenu();
            menu.addItem(AppText.text("analysis.export"),a->AnalysisImageExport.show(plot.visibleSnapshot(),this));
            menu.addItem(AppText.text("analysis.csv"),a->AnalysisCsvExport.show(plot.snapshot(),this));
            menu.show(this,export.getX(),export.getY()+export.getHeight());
        });export.setText(AppText.text("analysis.exportMenu"));setVisible(false);
    }
    void open(AnalysisEditor.Plot source){
        clear();plot=source.expandedCopy();addGL(plot);setVisible(true);resized(getWidth(),getHeight());requestGLFocus(back);syncControls();controls.start();
    }
    void clear(){controls.stop();if(plot!=null){plot.retainInspection();removeGL(plot);plot=null;}setVisible(false);}
    AnalysisEditor.Plot plot(){return plot;}
    @Override public boolean preservesKeyboardFocusOnPress(){return true;}
    @Override protected void resized(float width,float height){
        boolean compact=width<800;
        back.setBounds(8,4,190,28);pause.setBounds(206,4,145,28);logarithmic.setBounds(359,4,205,28);export.setBounds(Math.max(8,width-212),compact?38:4,204,28);
        float top=compact?74:40;
        if(plot!=null)plot.setBounds(8,top,Math.max(0,width-16),Math.max(0,height-top-8));
    }
    @Override protected void record(GLContext2D c){c.fillRect(0,0,getWidth(),getHeight(),getGLTheme().menuBarStyle().background());}
}
