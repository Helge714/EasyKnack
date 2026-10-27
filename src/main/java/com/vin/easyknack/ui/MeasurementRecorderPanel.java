package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.file.GLFileDialogRequest;
import java.awt.EventQueue;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Compact node summary and a scrollable, independently inspectable document table. */
public final class MeasurementRecorderPanel extends GLContainer {
    private final Consumer<GLNodeValue> changed;
    private final boolean expanded;
    private GLNodeValue table=MeasurementTable.empty();
    private NodeState state=new NodeState(Map.of());
    private final GLLabel status=new GLLabel("");
    private final GLButton open=new GLButton(""),export=new GLButton(""),clear=new GLButton("");
    private final Grid grid=new Grid();
    private final GLScrollPane scroll=new GLScrollPane(grid);
    private final java.util.List<MeasurementRecorderPanel> children=new ArrayList<>();
    public MeasurementRecorderPanel(Consumer<GLNodeValue> changed){this(changed,false);}
    private MeasurementRecorderPanel(Consumer<GLNodeValue> changed,boolean expanded){
        this.changed=changed;this.expanded=expanded;
        for(var component:java.util.List.of(status,open,export,clear))addGL(component);
        if(expanded)addGL(scroll);else setPreferredGLSize(210,62);
        open.addActionListener(e->openTable());export.addActionListener(e->exportMenu());
        clear.addActionListener(e->changed.accept(MeasurementTable.clear()));refresh();
    }
    public void update(GLNodeValue value){table=value;refresh();for(var child:children)child.update(value);}
    public void configure(NodeState value){state=value;refresh();for(var child:children)child.configure(value);}
    private java.util.List<ValueSeries.Entry> series(){return ValueSeries.visible(state,MeasurementTable.columns(table,MeasurementRecorderNode.count(state)));}
    private String[] names(){return series().stream().map(ValueSeries.Entry::heading).toArray(String[]::new);}
    private void refresh(){
        int count=MeasurementTable.rows(table).size();status.setText(count+" / "+MeasurementTable.MAX_ROWS+" "+AppText.text(count==MeasurementTable.MAX_ROWS?"recorder.full":"recorder.rows"));
        open.setText(AppText.text("recorder.open"));open.setVisible(!expanded);
        export.setText("CSV…");export.setToolTipText(AppText.text("recorder.export"));clear.setText(AppText.text("recorder.clear"));
        export.setEnabled(count>0);clear.setEnabled(count>0);
        grid.setPreferredGLSize((names().length+3)*140,Math.max(28,(count+1)*28));
        grid.invalidateRecording();grid.repaintGL();
    }
    private void openTable(){
        var content=new MeasurementRecorderPanel(changed,true);content.configure(state);content.update(table);children.add(content);
        var dialog=new GLDialog(AppText.text("recorder.title"),content);dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(1000,650);
        dialog.closedFuture().thenRun(()->children.remove(content));dialog.show(this);
    }
    private void exportMenu(){
        var menu=new GLPopupMenu();menu.addItem("CSV",e->export(false));menu.addItem("CSV (Excel DE)",e->export(true));
        var bounds=export.getLastRenderedScreenBounds();if(bounds!=null)menu.show(this,bounds[0],bounds[1]+bounds[3]);
    }
    private void export(boolean excel){
        var files=new RememberingFileDialogs();var snapshot=table;var headings=names();var order=series().stream().mapToInt(ValueSeries.Entry::input).toArray();
        files.show("recorder.csv",GLFileDialogRequest.save().title(AppText.text("recorder.export")).filter("CSV","csv").suggestedFileName("Measurements.csv").build(),this)
            .whenComplete((choice,failure)->EventQueue.invokeLater(()->{
                files.close();if(failure!=null){status.setText(failure.getMessage());return;}if(!choice.approved())return;
                CompletableFuture.runAsync(()->{
                    try{writeCsv(snapshot,headings,order,choice.selectedPath(),excel);}catch(java.io.IOException ex){throw new CompletionException(ex);}
                }).whenComplete((v,error)->EventQueue.invokeLater(()->{if(error!=null)status.setText(error.getCause().getMessage());}));
            }));
    }
    static void writeCsv(GLNodeValue table,String[] names,Path target,boolean excel)throws java.io.IOException{
        writeCsv(table,names,java.util.stream.IntStream.range(0,names.length).toArray(),target,excel);
    }
    static void writeCsv(GLNodeValue table,String[] names,int[] order,Path target,boolean excel)throws java.io.IOException{
        target=target.toAbsolutePath();var temporary=Files.createTempFile(target.getParent(),".easyknack-measurements-",".tmp");
        try{
            try(var writer=Files.newBufferedWriter(temporary,StandardCharsets.UTF_8)){MeasurementTable.csv(table,names,order,writer,excel);}
            try{Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException ex){Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temporary);}
    }
    @Override protected void resized(float w,float h){
        status.setBounds(0,0,w,26);float buttonWidth=Math.max(0,w-4)/(expanded?2:3);
        open.setBounds(0,30,buttonWidth,28);export.setBounds(expanded?0:buttonWidth+2,30,buttonWidth,28);clear.setBounds(w-buttonWidth,30,buttonWidth,28);
        if(expanded)scroll.setBounds(0,66,w,Math.max(0,h-66));
    }
    @Override protected void record(GLContext2D c){}
    private final class Grid extends GLContainer {
        @Override protected void record(GLContext2D c){
            var names=names();var series=series();var rows=MeasurementTable.rows(table);var font=getGLFont(getGLTheme().labelStyle().font());var color=getGLTheme().labelStyle().textColor();
            var border=getGLTheme().sliderStyle().borderColor();
            int first=Math.max(0,(int)(-getY()/28)-1),last=Math.min(rows.size(),first+(int)(scroll.getHeight()/28)+3);
            for(int i=first;i<=last;i++){
                java.util.List<GLNodeValue> row=i==0?java.util.List.of():((GLNodeValue.ListValue)rows.get(i-1)).values();
                for(int p=0;p<names.length+3;p++){
                    String text;
                    if(i==0)text=p==0?"#":p==1?AppText.text("recorder.session"):p==2?AppText.text("recorder.time"):names[p-3];
                    else {
                        int slot=p<3?p-1:series.get(p-3).input()+2;
                        text=p==0?Integer.toString(i):p==1?(String)row.get(0).toJavaValue():slot<row.size()&&row.get(slot)!=GLNodeValue.NullValue.INSTANCE?row.get(slot).toJavaValue().toString():"—";
                    }
                    c.beginClip(p*140+5,i*28,130,28);c.drawUIText(font,text,p*140+6,i*28+19,color);c.endClip();
                }
                c.drawLine(0,(i+1)*28,getWidth(),(i+1)*28,border);
            }
        }
    }
}
