package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.AnalysisChart;
import com.vingame.glswing.component.*;
import com.vingame.glswing.file.GLFileDialogRequest;
import com.vingame.glswing.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/** Image and tabular exports use the same immutable inspection snapshot. */
public final class ValueViewerExport {
    private ValueViewerExport(){}
    static void show(ValueHistory.Snapshot full,ValueHistory.Snapshot visible,boolean numbers,boolean overlay,Color[] colors,GLParent owner){
        if(full.size()==0)return;
        var dialog=new GLDialog[1];var files=new RememberingFileDialogs();
        var panel=new GLContainer(){
            final GLComboBox<String> format=new GLComboBox<>(java.util.List.of("PNG","JPG","CSV","CSV (Excel DE)"));
            final GLComboBox<String> area=new GLComboBox<>(java.util.List.of(AppText.text("analysis.visibleArea"),AppText.text("analysis.fullArea")));
            final GLNumberSpinner width=new GLNumberSpinner(320,16384,1600),height=new GLNumberSpinner(240,16384,900),quality=new GLNumberSpinner(1,100,95);
            final GLLabel dimensions=new GLLabel(AppText.text("viewer.exportDimensions")),message=new GLLabel(AppText.text("viewer.exportHint"));
            final GLButton save=new GLButton(AppText.text("image.action")),cancel=new GLButton(AppText.text("cancel"));
            boolean closed;
            {
                for(var c:java.util.List.of(format,area,width,height,quality,dimensions,message,save,cancel))addGL(c);
                width.setUnit("px");height.setUnit("px");quality.setUnit("%");for(var s:java.util.List.of(width,height,quality))s.setIntegerValuesOnly(true);
                quality.setVisible(false);
                format.addActionListener(e->{boolean image=format.getSelectedIndex()<2;width.setEnabled(image);height.setEnabled(image);quality.setVisible(format.getSelectedIndex()==1);});
                cancel.addActionListener(e->dialog[0].close());
                save.addActionListener(e->{
                    int selected=format.getSelectedIndex();String extension=selected<2?format.getSelectedItem().toLowerCase(java.util.Locale.ROOT):"csv";
                    var snapshot=area.getSelectedIndex()==0?visible:full;
                    int w=(int)width.getValue(),h=(int)height.getValue();float q=(float)quality.getValue()/100;
                    save.setEnabled(false);
                    files.show("viewer.export",GLFileDialogRequest.save().title(AppText.text("analysis.exportMenu")).filter(extension.toUpperCase(java.util.Locale.ROOT),extension).suggestedFileName("Values."+extension).build(),owner)
                        .whenComplete((choice,error)->EventQueue.invokeLater(()->{
                            if(closed)return;
                            if(error!=null||!choice.approved()){save.setEnabled(true);if(error!=null)message.setText(error.getMessage());return;}
                            message.setText(AppText.text("analysis.exporting"));
                            CompletableFuture.runAsync(()->{
                                try{
                                    if(selected>=2)writeCsv(snapshot,choice.selectedPath(),numbers,selected==3);
                                    else GraphImageExport.write(ValueViewerExport.render(snapshot,w,h,numbers,overlay,colors),extension,choice.selectedPath(),q);
                                }catch(Exception|OutOfMemoryError ex){throw new CompletionException(ex);}
                            }).whenComplete((v,failure)->EventQueue.invokeLater(()->{if(closed)return;save.setEnabled(true);if(failure==null)dialog[0].close();else message.setText(failure.getCause() instanceof OutOfMemoryError?AppText.text("image.memory"):failure.getCause().getMessage());}));
                        }));
                });
            }
            @Override protected void resized(float w,float h){
                format.setBounds(16,16,220,28);quality.setBounds(250,16,150,28);area.setBounds(16,54,w-32,28);
                dimensions.setBounds(16,94,w-32,24);width.setBounds(16,122,180,28);height.setBounds(210,122,180,28);
                message.setBounds(16,160,w-32,60);save.setBounds(w-240,h-42,110,28);cancel.setBounds(w-122,h-42,106,28);
            }
            @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        dialog[0]=new GLDialog(AppText.text("analysis.exportMenu"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);dialog[0].setSize(620,280);
        dialog[0].closedFuture().thenRun(()->{panel.closed=true;files.close();});dialog[0].show(owner);
    }
    public static BufferedImage render(ValueHistory.Snapshot s,int width,int height,boolean numbers,boolean overlay,Color[] colors){
        if(width<320||height<240)throw new IllegalArgumentException("Image dimensions must be at least 320 × 240");
        var image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
        try{
            g.setColor(Color.WHITE);g.fillRect(0,0,width,height);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            double scale=Math.max(1,Math.min(width/1000.,height/650.));g.scale(scale,scale);double w=width/scale,h=height/scale;
            g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
            ValueHistoryChart.draw(new AnalysisChart.Painter(){
                Shape old;
                public void line(double a,double b,double c,double d,Color color,float stroke){g.setColor(color);g.setStroke(new BasicStroke(stroke));g.draw(new java.awt.geom.Line2D.Double(a,b,c,d));}
                public void text(String text,double x,double y,Color color){g.setColor(color);g.drawString(text,(float)x,(float)y);}
                public double textWidth(String text){return g.getFontMetrics().stringWidth(text);}
                public void clip(double x,double y,double width,double height){old=g.getClip();g.clip(new java.awt.geom.Rectangle2D.Double(x,y,width,height));}
                public void unclip(){g.setClip(old);}
            },s,w,h-40,numbers,overlay,new Color(215,220,225),Color.DARK_GRAY,colors,Double.NaN,Double.NaN);
            g.setColor(Color.DARK_GRAY);g.drawString("EasyKnack · "+s.capturedAt()+" · "+AppText.text(s.frozen()?"analysis.frozen":"analysis.live"),12,(float)(h-20));
        }finally{g.dispose();}return image;
    }
    public static void writeCsv(ValueHistory.Snapshot s,Path destination,boolean numbers,boolean excel)throws IOException{
        var target=destination.toAbsolutePath();var temp=Files.createTempFile(target.getParent(),".easyknack-values-",".tmp");
        try{
            try(var out=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){encodeCsv(s,out,numbers,excel);}
            try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temp);}
    }
    public static void encodeCsv(ValueHistory.Snapshot s,Writer out,boolean numbers,boolean excel)throws IOException{
        String sep=excel?";":",";if(excel)out.write('\uFEFF');
        out.write("time_s");
        for(int p=0;p<s.channels();p++){
            String name=s.names()[p]+(s.units()[p].isBlank()?"":" ["+s.units()[p]+"]");
            out.write(sep+quote(name));if(!numbers)out.write(sep+quote(name+" min")+sep+quote(name+" max"));
        }
        out.write(sep+"snapshot_at_utc"+sep+"frozen\r\n");
        for(int i=numbers?Math.max(0,s.size()-1):0;i<s.size();i++){
            out.write(number(s.time()[i],excel));
            for(int p=0;p<s.channels();p++){
                out.write(sep+number(s.value()[p][i],excel));
                if(!numbers)out.write(sep+number(s.minimum()[p][i],excel)+sep+number(s.maximum()[p][i],excel));
            }
            out.write(sep+quote(s.capturedAt())+sep+s.frozen()+"\r\n");
        }
    }
    private static String quote(String s){return '"'+s.replace("\"","\"\"")+'"';}
    private static String number(double v,boolean excel){String text=Double.toString(v);return excel?text.replace('.',','):text;}
}
