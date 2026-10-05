package com.vin.easyknack.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import com.vin.easyknack.nodes.AnalysisChart;
import com.vingame.glswing.component.*;
import com.vingame.glswing.file.GLFileDialogRequest;
import com.vingame.glswing.ui.*;

/** Exports the snapshot captured when the dialog opens, independent of subsequent live frames. */
final class AnalysisImageExport {
    private AnalysisImageExport(){}
    static void show(AnalysisChart.Snapshot snapshot,GLParent owner){
        if(snapshot.frame()==null)return;
        var dialog=new GLDialog[1];var files=new RememberingFileDialogs();
        var panel=new GLContainer(){
            final GLNumberSpinner width=new GLNumberSpinner(320,16384,1600),height=new GLNumberSpinner(240,16384,900),quality=new GLNumberSpinner(1,100,95);
            final GLComboBox<String> format=new GLComboBox<>(java.util.List.of("PNG","JPG"));
            final GLComboBox<String> area=new GLComboBox<>(java.util.List.of(AppText.text("analysis.visibleArea"),AppText.text("analysis.fullArea")));
            final GLLabel widthLabel=new GLLabel(AppText.text("image.width")),heightLabel=new GLLabel(AppText.text("image.height")),qualityLabel=new GLLabel(AppText.text("image.quality")),message=new GLLabel(AppText.text("analysis.exportHint"));
            final GLButton save=new GLButton(AppText.text("image.action")),cancel=new GLButton(AppText.text("cancel"));
            boolean closed;
            {
                for(var c:java.util.List.of(width,height,quality,format,area,widthLabel,heightLabel,qualityLabel,message,save,cancel))addGL(c);
                width.setUnit("px");height.setUnit("px");quality.setUnit("%");width.setIntegerValuesOnly(true);height.setIntegerValuesOnly(true);quality.setIntegerValuesOnly(true);
                quality.setVisible(false);qualityLabel.setVisible(false);
                format.addActionListener(e->{boolean jpg="JPG".equals(format.getSelectedItem());quality.setVisible(jpg);qualityLabel.setVisible(jpg);});
                cancel.addActionListener(e->dialog[0].close());
                save.addActionListener(e->{
                    save.setEnabled(false);String extension=format.getSelectedItem().toLowerCase(java.util.Locale.ROOT);
                    int w=(int)width.getValue(),h=(int)height.getValue();float q=(float)quality.getValue()/100;
                    var selected=area.getSelectedItem().equals(AppText.text("analysis.fullArea"))?snapshot.withView(AnalysisChart.View.FULL,snapshot.cursorA(),snapshot.cursorB()):snapshot;
                    files.show("analysis.image",GLFileDialogRequest.save().title(AppText.text("analysis.export")).filter(format.getSelectedItem(),extension).suggestedFileName("Analysis."+extension).build(),owner)
                    .whenComplete((choice,error)->EventQueue.invokeLater(()->{
                        if(closed)return;
                        if(error!=null||!choice.approved()){save.setEnabled(true);if(error!=null)message.setText(error.getMessage());return;}
                        message.setText(AppText.text("analysis.exporting"));
                        CompletableFuture.runAsync(()->{try{GraphImageExport.write(AnalysisImageExport.render(selected,w,h),extension,choice.selectedPath(),q);}catch(Exception|OutOfMemoryError ex){throw new CompletionException(ex);}})
                        .whenComplete((ignored,failure)->EventQueue.invokeLater(()->{if(closed)return;save.setEnabled(true);if(failure==null)dialog[0].close();else message.setText(failure.getCause() instanceof OutOfMemoryError?AppText.text("image.memory"):failure.getCause().getMessage());}));
                    }));
                });
            }
            @Override protected void resized(float w,float h){
                widthLabel.setBounds(16,16,100,24);width.setBounds(125,16,170,26);heightLabel.setBounds(16,54,100,24);height.setBounds(125,54,170,26);
                format.setBounds(320,16,150,26);qualityLabel.setBounds(320,54,90,24);quality.setBounds(414,54,90,26);
                area.setBounds(16,92,w-32,28);message.setBounds(16,130,w-32,52);save.setBounds(w-240,h-42,110,28);cancel.setBounds(w-122,h-42,106,28);
            }
            @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        dialog[0]=new GLDialog(AppText.text("analysis.export"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);dialog[0].setSize(620,255);
        dialog[0].closedFuture().thenRun(()->{panel.closed=true;files.close();});dialog[0].show(owner);
    }
    static BufferedImage render(AnalysisChart.Snapshot snapshot,int width,int height){
        if(width<320||height<240)throw new IllegalArgumentException("Image dimensions must be at least 320 × 240");
        var image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
        try{
            g.setColor(Color.WHITE);g.fillRect(0,0,width,height);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double scale=Math.max(1,Math.min(width/1000.,height/650.));g.scale(scale,scale);int w=(int)(width/scale),h=(int)(height/scale);
            g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
            String metadata="EasyKnack · "+snapshot.frame().values().length+" "+AppText.text("analysis.values")+" · "+AppText.text("analysis.received")+" "+snapshot.capturedAt();
            String details=AppText.text("analysis.exportNote")+" "+snapshot.measurements();
            if(snapshot.log())details+=" · DC = "+AnalysisChart.number(snapshot.frame().values()[0]);
            var notes=new java.util.ArrayList<String>();
            for(String text:java.util.List.of(metadata,details)){
                String line="";
                for(String word:text.split(" ")){String next=line.isEmpty()?word:line+" "+word;
                    if(!line.isEmpty()&&g.getFontMetrics().stringWidth(next)>w-32){notes.add(line);line=word;}else line=next;}
                if(!line.isEmpty())notes.add(line);
            }
            int footer=notes.size()*16+14;
            AnalysisChart.draw(new AnalysisChart.Painter(){
                Shape clip;
                public void line(double x1,double y1,double x2,double y2,Color color,float stroke){g.setColor(color);g.setStroke(new BasicStroke(stroke));g.draw(new java.awt.geom.Line2D.Double(x1,y1,x2,y2));}
                public void text(String text,double x,double y,Color color){g.setColor(color);g.drawString(text,(float)x,(float)y);}
                public double textWidth(String text){return g.getFontMetrics().stringWidth(text);}
                public void clip(double x,double y,double width,double height){clip=g.getClip();g.clip(new java.awt.geom.Rectangle2D.Double(x,y,width,height));}
                public void unclip(){g.setClip(clip);}
            },snapshot,w,h-footer,true,scale,new AnalysisChart.Palette(new Color(215,220,225),new Color(30,35,40),new Color(0,75,160),new Color(140,70,15),new Color(145,30,60)));
            g.setColor(Color.DARK_GRAY);
            for(int i=0;i<notes.size();i++)g.drawString(notes.get(i),16,h-footer+18+i*16);
        }finally{g.dispose();}
        return image;
    }
}
