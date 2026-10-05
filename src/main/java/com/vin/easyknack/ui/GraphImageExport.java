package com.vin.easyknack.ui;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLConstraints;
import com.vingame.glswing.file.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.rendering.*;

/** Renders an isolated editor using the same GL renderer; never moves the live graph. */
final class GraphImageExport {
    enum Area {ALL,VISIBLE,SELECTED;public String toString(){return AppText.text("image."+name().toLowerCase(Locale.ROOT));}}
    static void show(WorkspaceController controller,GLParent owner){
        var ref=new GLDialog[1];var dialogs=new RememberingFileDialogs();
        var panel=createPanel(controller,owner,ref,dialogs);
        ref[0]=new GLDialog(AppText.text("image.export"),panel.content());ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(1120,535);ref[0].closedFuture().thenRun(()->{panel.close().run();dialogs.close();});ref[0].show(owner);
    }
    record Panel(GLContainer content,Runnable close,Runnable preview){}
    static Panel createPanel(WorkspaceController controller,GLParent owner,GLDialog[] ref,RememberingFileDialogs dialogs){
        var panel=new GLContainer(){
            final GLComboBox<Area> area=new GLComboBox<>(availableAreas(controller));
            final GLNumberSpinner width=new GLNumberSpinner(1,8192,640),height=new GLNumberSpinner(1,8192,400),padding=new GLNumberSpinner(0,4096,32),quality=new GLNumberSpinner(1,100,90);
            ExportDimensions dimensions;
            boolean writing,synchronizing,closed;
            long estimateVersion;
            final javax.swing.Timer estimateTimer=new javax.swing.Timer(400,e->estimate());
            final GLComboBox<String> format=new GLComboBox<>(List.of("PNG","JPG"));
            final ExportPreview preview=new ExportPreview();
            final GLLabel previewLabel=new GLLabel(AppText.text("image.preview")),pixelSize=new GLLabel("");
            final GLCheckBox linked=new GLCheckBox(AppText.text("image.linked")),addPadding=new GLCheckBox(AppText.text("image.addPadding"));
            final GLNumberSpinnerGroup dimensionsGroup=new GLNumberSpinnerGroup(width,height){
                @Override protected void applyValues(Map<GLNumberSpinner,Double> values){
                    if(dimensions==null)return;
                    dimensions.size(values.getOrDefault(width,width.getValue()).intValue(),values.getOrDefault(height,height.getValue()).intValue());
                    sync();
                }
            };
            final GLLayoutGroup paddingGroup=new GLLayoutGroup();
            final GLCheckBox transparent=new GLCheckBox(AppText.text("image.transparent"));
            final GLLabel scopeLabel=new GLLabel(AppText.text("image.scope")),widthLabel=new GLLabel(AppText.text("image.width")),heightLabel=new GLLabel(AppText.text("image.height")),paddingLabel=new GLLabel(AppText.text("image.padding")),qualityLabel=new GLLabel(AppText.text("image.quality")),size=new GLLabel(""),message=new GLLabel("");
            final GLButton export=new GLButton(AppText.text("image.action")),cancel=new GLButton(AppText.text("cancel"));
            {
                for(var child:List.of(scopeLabel,area,widthLabel,heightLabel,paddingLabel,linked,addPadding,dimensionsGroup,paddingGroup,qualityLabel,quality,format,transparent,size,pixelSize,previewLabel,preview,message,export,cancel))addGL(child);
                paddingGroup.addGL(padding,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
                linked.setSelected(true);
                linked.addActionListener(e->{if(dimensions!=null){dimensions.linked(linked.isSelected());sync();}});
                addPadding.addActionListener(e->{if(dimensions!=null){dimensions.addPadding(addPadding.isSelected());sync();}});
                estimateTimer.setRepeats(false);
                for(var spinner:List.of(width,height,padding))spinner.setUnit("px");
                for(var spinner:List.of(width,height))spinner.setResetAction(()->{if(dimensions!=null){dimensions.resetSize();sync();}});
                padding.setResetAction(()->{if(dimensions!=null){dimensions.padding(dimensions.defaultPadding);sync();}});
                width.setIntegerValuesOnly(true);height.setIntegerValuesOnly(true);quality.setIntegerValuesOnly(true);quality.setUnit("%");
                padding.setStep(1);quality.setDefaultValue(90);
                width.addChangeListener(e->{if(!synchronizing&&dimensions!=null){dimensions.width((int)width.getValue());sync();}});
                height.addChangeListener(e->{if(!synchronizing&&dimensions!=null){dimensions.height((int)height.getValue());sync();}});
                padding.addChangeListener(e->{if(!synchronizing&&dimensions!=null){dimensions.padding(padding.getValue());sync();}});
                quality.addChangeListener(e->scheduleEstimate());transparent.addActionListener(e->scheduleEstimate());
                area.addActionListener(e->measure());
                format.addActionListener(e->formatChanged());measure();formatChanged();
                cancel.addActionListener(e->ref[0].close());
                export.addActionListener(e->{
                    String extension=format.getSelectedItem().toLowerCase(Locale.ROOT);writing=true;export.setEnabled(false);
                    dialogs.show("graph.image",GLFileDialogRequest.save().title(AppText.text("image.export")).filter(format.getSelectedItem(),extension).suggestedFileName("Graph."+extension).build(),owner).whenComplete((choice,error)->java.awt.EventQueue.invokeLater(()->{
                        if(closed)return;
                        if(error!=null||!choice.approved()){writing=false;sync();if(error!=null)message.setText(error.getMessage());return;}
                        try{
                            var image=capturePixels(controller,area.getSelectedItem(),dimensions.outputWidth(),dimensions.outputHeight(),dimensions.padding(),extension.equals("png")&&transparent.isSelected());
                            float compression=(float)(quality.getValue()/100);
                            CompletableFuture.runAsync(()->{try{write(image,extension,choice.selectedPath(),compression);}catch(Exception ex){throw new CompletionException(ex);}}).whenComplete((ignored,failure)->java.awt.EventQueue.invokeLater(()->{if(closed)return;writing=false;sync();if(failure==null)ref[0].close();else message.setText(failure.getCause().getMessage());}));
                        }catch(Exception|OutOfMemoryError failure){writing=false;sync();message.setText(failure instanceof OutOfMemoryError?AppText.text("image.memory"):failure.getMessage());}
                    }));
                });
            }
            void formatChanged(){boolean jpg=format.getSelectedItem().equals("JPG");quality.setVisible(jpg);qualityLabel.setVisible(jpg);transparent.setVisible(!jpg);scheduleEstimate();}
            void measure(){
                try(var prepared=prepare(controller,area.getSelectedItem(),false)){
                    double border=area.getSelectedItem()==Area.VISIBLE?0:32;
                    dimensions=new ExportDimensions(prepared.width()-2*border,prepared.height()-2*border,border);dimensions.linked(linked.isSelected());dimensions.addPadding(addPadding.isSelected());sync();
                }catch(Exception error){dimensions=null;message.setText(error.getMessage());export.setEnabled(false);}
            }
            void sync(){
                if(dimensions==null)return;
                synchronizing=true;
                try{
                    width.setRange(1,Math.max(2,dimensions.maxWidth()));height.setRange(1,Math.max(2,dimensions.maxHeight()));padding.setRange(0,Math.max(1,dimensions.maxPadding()));
                    width.setValue(dimensions.width());height.setValue(dimensions.height());padding.setValue(dimensions.padding());
                    width.setEnabled(!writing&&dimensions.maxWidth()>1);height.setEnabled(!writing&&dimensions.maxHeight()>1);padding.setEnabled(!writing&&dimensions.maxPadding()>0);
                }finally{synchronizing=false;}
                export.setEnabled(!writing);pixelSize.setText(dimensions.outputWidth()+" × "+dimensions.outputHeight()+" px");message.setText("");scheduleEstimate();
            }
            void scheduleEstimate(){if(closed||dimensions==null)return;estimateVersion++;size.setText(AppText.text("image.estimating"));estimateTimer.restart();}
            void estimate(){
                if(closed||dimensions==null)return;
                long ticket=estimateVersion;int w=dimensions.outputWidth(),h=dimensions.outputHeight();
                double ratio=Math.min(1,1024.0/Math.max(w,h));int pw=Math.max(1,(int)Math.round(w*ratio)),ph=Math.max(1,(int)Math.round(h*ratio));
                String extension=format.getSelectedItem().toLowerCase(Locale.ROOT);float compression=(float)(quality.getValue()/100);
                try{
                    var preview=capturePixels(controller,area.getSelectedItem(),pw,ph,Math.min(dimensions.padding()*ratio,(Math.min(pw,ph)-.01)/2),extension.equals("png")&&transparent.isSelected());
                    this.preview.image(preview);
                    CompletableFuture.supplyAsync(()->{try{var out=new java.io.ByteArrayOutputStream();encode(preview,extension,compression,out);return out.size()*((double)w*h/(pw*(double)ph));}catch(Exception e){throw new CompletionException(e);}})
                        .whenComplete((bytes,failure)->java.awt.EventQueue.invokeLater(()->{if(!closed&&ticket==estimateVersion)size.setText(failure==null?AppText.format("image.estimated",byteSize(bytes)):AppText.text("image.estimateUnavailable"));}));
                }catch(Exception failure){size.setText(AppText.text("image.estimateUnavailable"));}
            }
            void dispose(){closed=true;estimateVersion++;estimateTimer.stop();}
            protected void record(GLContext2D c){}
            protected void resized(float w,float h){
                float left=Math.max(260,w*.48f),right=left+20,rw=w-right-20;
                previewLabel.setBounds(20,15,left-20,28);preview.setBounds(20,50,left-20,h-110);
                scopeLabel.setBounds(right,15,100,28);area.setBounds(right+100,15,rw-100,28);
                dimensionsGroup.setBounds(right+100,65,145,56);
                widthLabel.setBounds(right,65,90,28);heightLabel.setBounds(right,93,90,28);linked.setBounds(right+255,79,rw-255,28);
                paddingGroup.setBounds(right+100,160,145,28);paddingLabel.setBounds(right,160,90,28);addPadding.setBounds(right,200,rw,28);
                format.setBounds(right,285,95,28);qualityLabel.setBounds(right+110,285,120,28);quality.setBounds(right+235,285,rw-235,28);transparent.setBounds(right+110,285,rw-110,28);
                size.setBounds(right,375,rw*.6f,28);pixelSize.setBounds(right+rw*.6f,375,rw*.4f,28);
                message.setBounds(20,h-80,w-40,28);cancel.setBounds(w-230,h-45,100,28);export.setBounds(w-120,h-45,100,28);
            }
        };return new Panel(panel,panel::dispose,panel::estimate);
    }
    static List<Area> availableAreas(WorkspaceController source){return source.selectedNodeIds().isEmpty()?List.of(Area.ALL,Area.VISIBLE):List.of(Area.ALL,Area.VISIBLE,Area.SELECTED);}
    static String byteSize(double bytes){
        String[] units={"KB","MB","GB"};int i=0;double value=bytes/1000;
        while(value>=1000&&i<2){value/=1000;i++;}
        return String.format(ApplicationSettings.locale(),"%.1f %s",value,units[i]);
    }
    static void write(BufferedImage image,String format,Path destination)throws java.io.IOException{write(image,format,destination,.9f);}
    static void write(BufferedImage image,String format,Path destination,float quality)throws java.io.IOException{
        try { GLImageExport.write(image,format,destination,quality); }
        catch(GLImageExport.EncoderUnavailableException error){throw new java.io.IOException(AppText.text("image.encoder"),error);}
    }
    static void encode(BufferedImage image,String format,float quality,java.io.OutputStream output)throws java.io.IOException{
        try { GLImageExport.encode(image,format,quality,output); }
        catch(GLImageExport.EncoderUnavailableException error){throw new java.io.IOException(AppText.text("image.encoder"),error);}
    }
    static BufferedImage capturePixels(WorkspaceController source,Area area,int width,int height,double padding,boolean transparent){
        if(!ExportDimensions.valid(width,height)||!Double.isFinite(padding)||padding<0||padding*2>=Math.min(width,height))throw new IllegalArgumentException(AppText.text("image.limit"));
        try(var prepared=prepare(source,area,transparent)){
            var editor=prepared.controller().workspace().editor();double old=area==Area.VISIBLE?0:32;
            double cw=prepared.width()-old*2,ch=prepared.height()-old*2;
            double scale=Math.min((width-2*padding)/cw,(height-2*padding)/ch);
            float logicalWidth=(float)(width/scale),logicalHeight=(float)(height/scale);
            editor.setBounds(0,0,logicalWidth,logicalHeight);
            editor.setPan(editor.getPanX()-(float)old+(float)((logicalWidth-cw)/2),editor.getPanY()-(float)old+(float)((logicalHeight-ch)/2));
            return renderPixels(editor,logicalWidth,logicalHeight,width,height,transparent);
        }
    }
    record Prepared(WorkspaceController controller,float width,float height) implements AutoCloseable {
        public void close(){controller.close();}
    }
    static BufferedImage capture(WorkspaceController source,Area area,double scale,boolean transparent){
        try(var prepared=prepare(source,area,transparent)){
            return render(prepared.controller().workspace().editor(),prepared.width(),prepared.height(),scale,transparent);
        }
    }
    static Prepared prepare(WorkspaceController source,Area area,boolean transparent){
        var live=source.workspace().editor();var document=source.snapshotDocument();
        String id=source.workspace().currentAssetId();var root=id==null?document.root():document.findAsset(id).orElseThrow();
        if(area==Area.SELECTED){
            var selected=source.selectedNodeIds();
            if(selected.isEmpty())throw new IllegalArgumentException(AppText.text("image.empty"));
            var graph=root.graph();root=new GLNodeGraphAsset(root.id(),root.version(),GLNodeGraphAssetKind.PROJECT,new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes().stream().filter(n->selected.contains(n.id())).toList(),graph.connections().stream().filter(c->selected.contains(c.input().nodeId())&&selected.contains(c.output().nodeId())).toList()),root.viewState(),root.metadata(),null);
        }
        var embedded=new LinkedHashMap<String,GLNodeGraphAsset>(document.embeddedAssets());embedded.remove(root.id());
        // Export is presentation only: never restore/compile saved bakes.
        var clean=new LinkedHashMap<String,GLNodeGraphAsset>();var all=new ArrayList<>(embedded.values());all.add(root);
        for(var a:all){var metadata=new LinkedHashMap<>(a.metadata());metadata.remove(GroupBakes.SAVED);clean.put(a.id(),new GLNodeGraphAsset(a.id(),a.version(),a.kind(),a.graph(),a.viewState(),metadata,a.compositeDescriptor()));}
        root=clean.remove(root.id());
        var clone=new WorkspaceController();
        try{
            clone.openDocument("Image",new GLNodeGraphDocument(root,clean,List.of()));var editor=clone.workspace().editor();
            var theme=ApplicationTheme.current().theme();if(transparent)theme=theme.withValues(theme.values().with("nodeEditor.background","#00000000").with("nodeEditor.gridVisible","false"));
            clone.workspace().setGLTheme(theme);clone.workspace().setGLLocalizer(GLLocalizer.forLocale(ApplicationSettings.locale()));
            float width,height;
            if(area==Area.VISIBLE){
                var bounds=visibleBounds(live.getWidth(),live.getHeight(),live.getZoom(),live.getPanX(),live.getPanY());
                width=bounds[2];height=bounds[3];editor.setBounds(0,0,width,height);editor.setZoom(1);editor.setPan(-bounds[0],-bounds[1]);
            }
            else{
                float x=0,y=0,right=640,bottom=400;boolean first=true;
                for(var node:editor.getNodes()){if(first){x=node.getX();y=node.getY();right=x+node.getWidth();bottom=y+node.getHeight();first=false;}else{x=Math.min(x,node.getX());y=Math.min(y,node.getY());right=Math.max(right,node.getX()+node.getWidth());bottom=Math.max(bottom,node.getY()+node.getHeight());}}
                width=right-x+64;height=bottom-y+64;editor.setBounds(0,0,width,height);editor.setZoom(1);editor.setPan(32-x,32-y);
            }
            editor.clearSelection();return new Prepared(clone,width,height);
        }catch(RuntimeException|Error failure){clone.close();throw failure;}
    }
    static float[] visibleBounds(float width,float height,float zoom,float panX,float panY){
        return new float[]{width/2+(-width/2-panX)/zoom,height/2+(-height/2-panY)/zoom,width/zoom,height/zoom};
    }
    static boolean validDimensions(float width,float height,double scale){
        double w=Math.ceil(width*scale),h=Math.ceil(height*scale);
        return Double.isFinite(scale)&&scale>=0.01&&w>=1&&h>=1&&w<=ExportDimensions.MAX_SIDE&&h<=ExportDimensions.MAX_SIDE&&w*h<=ExportDimensions.MAX_PIXELS;
    }
    static BufferedImage render(GLContainer view,float width,float height,double scale,boolean transparent){
        if(!validDimensions(width,height,scale))throw new IllegalArgumentException(AppText.text("image.limit"));
        int w=(int)Math.ceil(width*scale),h=(int)Math.ceil(height*scale);
        return renderPixels(view,width,height,w,h,transparent);
    }
    static BufferedImage renderPixels(GLContainer view,float width,float height,int w,int h,boolean transparent){
        try { return GLComponentImageCapture.capture(view,width,height,w,h,transparent); }
        catch (RuntimeException failure) {
            for (Throwable cause=failure;cause!=null;cause=cause.getCause())
                if(cause instanceof GLComponentImageCapture.SizeLimitException limit)
                    throw new IllegalArgumentException(AppText.format("image.gpuLimit",limit.maximumSide()),failure);
            throw failure;
        }
    }
}
