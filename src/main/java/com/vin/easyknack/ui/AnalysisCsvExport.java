package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.AnalysisChart;
import com.vingame.glswing.component.*;
import com.vingame.glswing.file.GLFileDialogRequest;
import com.vingame.glswing.ui.*;
import java.awt.EventQueue;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/** Raw snapshot values, independent of display zoom and pixel reduction. */
final class AnalysisCsvExport {
    enum Format {
        STANDARD(",", "analysis.csvStandard"), EXCEL_DE(";", "analysis.csvExcel");
        final String separator,key;
        Format(String separator,String key){this.separator=separator;this.key=key;}
        String number(String value){return this==EXCEL_DE?value.replace('.',','):value;}
        @Override public String toString(){return AppText.text(key);}
    }
    static void show(AnalysisChart.Snapshot snapshot,GLParent owner){
        if(snapshot.frame()==null)return;
        var dialog=new GLDialog[1];
        var panel=new GLContainer(){
            final GLComboBox<Format> format=new GLComboBox<>(java.util.List.of(Format.values()));
            final GLButton save=new GLButton(AppText.text("image.action")),cancel=new GLButton(AppText.text("cancel"));
            {
                addGL(format);addGL(save);addGL(cancel);
                save.addActionListener(e->{var selected=format.getSelectedItem();dialog[0].close();chooseFile(snapshot,owner,selected);});
                cancel.addActionListener(e->dialog[0].close());
            }
            @Override protected void resized(float w,float h){format.setBounds(16,16,w-32,28);save.setBounds(w-240,h-42,110,28);cancel.setBounds(w-122,h-42,106,28);}
            @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        dialog[0]=new GLDialog(AppText.text("analysis.csv"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);dialog[0].setSize(620,135);dialog[0].show(owner);
    }
    private static void chooseFile(AnalysisChart.Snapshot snapshot,GLParent owner,Format format){
        var files=new RememberingFileDialogs();
        files.show(GLFileDialogRequest.save().title(AppText.text("analysis.csv")).filter("CSV","csv").suggestedFileName("Analysis.csv").build(),owner)
            .whenComplete((choice,error)->EventQueue.invokeLater(()->{
                files.close();
                if(error!=null){message(owner,error.getMessage());return;}
                if(!choice.approved())return;
                CompletableFuture.runAsync(()->{try{write(snapshot,choice.selectedPath(),format);}catch(IOException e){throw new CompletionException(e);}})
                    .whenComplete((ignored,failure)->EventQueue.invokeLater(()->message(owner,failure==null?AppText.text("analysis.csvSaved"):failure.getCause().getMessage())));
            }));
    }
    private static void message(GLParent owner,String text){
        var dialog=new GLDialog[1];
        var panel=new GLContainer(){
            final GLLabel label=new GLLabel(text);final GLButton ok=new GLButton("OK");
            {addGL(label);addGL(ok);ok.addActionListener(e->dialog[0].close());}
            @Override protected void resized(float w,float h){label.setBounds(16,12,w-32,h-60);ok.setBounds(w-100,h-40,84,28);}
            @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        dialog[0]=new GLDialog(AppText.text("analysis.csv"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);dialog[0].setSize(620,150);dialog[0].show(owner);
    }
    static void write(AnalysisChart.Snapshot snapshot,Path destination)throws IOException{
        write(snapshot,destination,Format.STANDARD);
    }
    static void write(AnalysisChart.Snapshot snapshot,Path destination,Format format)throws IOException{
        if(snapshot.frame()==null)throw new IllegalArgumentException("No analysis frame");
        var target=destination.toAbsolutePath();var temp=Files.createTempFile(target.getParent(),".easyknack-analysis-",".tmp");
        try{
            try(var out=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){encode(snapshot,out,format);}
            try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temp);}
    }
    static void encode(AnalysisChart.Snapshot s,Writer out)throws IOException{
        encode(s,out,Format.STANDARD);
    }
    static void encode(AnalysisChart.Snapshot s,Writer out,Format format)throws IOException{
        var f=s.frame();boolean symmetric=f.symmetric();
        if(format==Format.EXCEL_DE)out.write('\uFEFF');
        String separator=format.separator;
        out.write(((s.spectrum()?"bin,frequency_hz,value":symmetric?"lag_index,lag_ms,value":"sample_index,time_ms,value")+",received_at_utc,frozen\r\n").replace(",",separator));
        int n=f.values().length;
        for(int i=symmetric?1-n:0;i<n;i++){
            double x=Math.copySign(s.sampleX(Math.abs(i)),i);
            out.write(i+separator+format.number(Double.toString(x))+separator+format.number(Float.toString(f.values()[Math.abs(i)]))+separator+"\""+s.capturedAt().replace("\"","\"\"")+"\""+separator+s.paused()+"\r\n");
        }
    }
}
