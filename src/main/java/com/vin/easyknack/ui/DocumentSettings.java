package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;

/** Document-local persistence policy and entry point for explicit group management. */
final class DocumentSettings {
    static final String EXPERIMENTAL="easyknack.document.experimentalFeatures";
    static boolean experimental(GLNodeGraphDocument document){return document.root().metadata().getOrDefault(EXPERIMENTAL,GLNodeValue.of(false)).equals(GLNodeValue.of(true));}
    /** Only legacy documents without a stored decision inherit availability from their contents. */
    static GLNodeGraphDocument initializeExperimental(GLNodeGraphDocument document){
        if(document.root().metadata().containsKey(EXPERIMENTAL)||!ExperimentalFeatures.usedInDocument(document))return document;
        var root=document.root();var metadata=new HashMap<>(root.metadata());
        metadata.put(EXPERIMENTAL,GLNodeValue.of(true));
        return new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),root.graph(),root.viewState(),metadata,root.compositeDescriptor()),document.embeddedAssets(),document.externalAssets());
    }
    static boolean needsExperimentalWarning(WorkspaceController controller,boolean requested){
        return controller.experimentalFeatures&&!requested&&ExperimentalFeatures.usedInDocument(controller.captureDocument());
    }
    static final String PRESERVE="easyknack.document.preserveUnusedGroups";
    static boolean preserve(GLNodeGraphDocument document){return document.root().metadata().getOrDefault(PRESERVE,GLNodeValue.of(false)).equals(GLNodeValue.of(true));}
    static GLNodeGraphDocument forSave(GLNodeGraphDocument document){
        if(preserve(document))return document;
        var byType=new HashMap<String,GLNodeGraphAsset>();
        for(var asset:document.embeddedAssets().values())if(asset.compositeDescriptor()!=null)byType.put(asset.compositeDescriptor().nodeTypeId(),asset);
        var retained=new LinkedHashMap<String,GLNodeGraphAsset>();
        var pending=new ArrayDeque<GLNodeGraphAsset>();pending.add(document.root());
        var visited=new HashSet<String>();
        while(!pending.isEmpty()){
            var asset=pending.removeFirst();if(!visited.add(asset.id()))continue;
            for(var node:asset.graph().nodes()){
                var dependency=byType.get(node.typeId());
                if(dependency!=null){retained.put(dependency.id(),dependency);pending.add(dependency);}
            }
        }
        return new GLNodeGraphDocument(document.root(),retained,document.externalAssets());
    }
    static void show(GLParent owner,WorkspaceController controller){
        var ref=new GLDialog[1];
        var content=new GLContainer(){
            final GLCheckBox preserve=new GLCheckBox(AppText.text("document.preserveGroups"));
            final GLCheckBox experimental=new GLCheckBox(AppText.text("document.experimental"));
            final GLLabel hint=new GLLabel(AppText.text("document.pruningHint"));
            final GLCheckBox interpolate=new GLCheckBox(AppText.text("control.interpolation"));
            final GLLabel minLabel=new GLLabel(AppText.text("control.minimum")),maxLabel=new GLLabel(AppText.text("control.maximum"));
            final GLNumberSpinner min=new GLNumberSpinner(0,60000,controller.controlInterpolation.minimumMs()),max=new GLNumberSpinner(0,60000,controller.controlInterpolation.maximumMs());
            final GLCheckBox buffered=new GLCheckBox(AppText.text("control.buffered")),smoothing=new GLCheckBox(AppText.text("control.smoothing"));
            final GLNumberSpinner bufferMs=new GLNumberSpinner(0,1000,controller.controlInterpolation.bufferMs()),smoothMs=new GLNumberSpinner(0,100,controller.controlInterpolation.smoothingMs());
            boolean adjusting;
            void bounds(boolean fromMin){if(adjusting)return;adjusting=true;try{if(min.getValue()>max.getValue()){if(fromMin)max.setValue(min.getValue());else min.setValue(max.getValue());}}finally{adjusting=false;}}
            final GLButton save=new GLButton(AppText.text("save")),cancel=new GLButton(AppText.text("cancel"));
            final GLButton groups=new GLButton(AppText.text("groups.manager.title"));
            {buffered.setSelected(controller.controlInterpolation.buffered());smoothing.setSelected(controller.controlInterpolation.smoothing());
                bufferMs.setUnit("ms");smoothMs.setUnit("ms");bufferMs.setEnabled(buffered.isSelected());smoothMs.setEnabled(smoothing.isSelected());
                buffered.addActionListener(e->bufferMs.setEnabled(buffered.isSelected()));smoothing.addActionListener(e->smoothMs.setEnabled(smoothing.isSelected()));
                buffered.setToolTipText(AppText.text("control.buffered.hint"));smoothing.setToolTipText(AppText.text("control.smoothing.hint"));
                for(var c:List.of(buffered,bufferMs,smoothing,smoothMs))addGL(c);
                interpolate.setSelected(controller.controlInterpolation.enabled());min.setUnit("ms");max.setUnit("ms");
                min.addChangeListener(e->bounds(true));max.addChangeListener(e->bounds(false));
                for(var c:List.of(interpolate,minLabel,min,maxLabel,max))addGL(c);
                interpolate.setToolTipText(AppText.text("control.interpolation.hint"));
                preserve.setSelected(controller.preserveUnusedGroups);addGL(preserve);addGL(hint);addGL(save);addGL(cancel);
                experimental.setSelected(controller.experimentalFeatures);addGL(experimental);
                save.addActionListener(e->{
                    if(needsExperimentalWarning(controller,experimental.isSelected())){
                        save.setEnabled(false);confirmExperimentalDisable(this,accepted->{save.setEnabled(true);if(accepted)apply();});
                    }else apply();
                });
                addGL(groups);groups.addActionListener(e->GroupManagerPanel.show(this,controller));
                cancel.addActionListener(e->ref[0].close());}
            void apply(){controller.preserveUnusedGroups=preserve.isSelected();controller.experimentalFeatures=experimental.isSelected();controller.controlInterpolation=new ControlInterpolationSettings(interpolate.isSelected(),min.getValue(),max.getValue(),buffered.isSelected(),bufferMs.getValue(),smoothing.isSelected(),smoothMs.getValue());controller.controlSettingsChanged();ref[0].close();}
            protected void record(com.vingame.glswing.rendering.GLContext2D c){}
            protected void resized(float w,float h){preserve.setBounds(20,20,w-40,30);hint.setBounds(20,60,w-40,40);interpolate.setBounds(20,108,w-40,30);minLabel.setBounds(20,146,200,28);min.setBounds(230,146,w-250,28);maxLabel.setBounds(20,180,200,28);max.setBounds(230,180,w-250,28);buffered.setBounds(20,220,210,28);bufferMs.setBounds(230,220,w-250,28);smoothing.setBounds(20,254,210,28);smoothMs.setBounds(230,254,w-250,28);experimental.setBounds(20,294,w-40,28);groups.setBounds(20,334,230,28);cancel.setBounds(w-230,h-45,100,28);save.setBounds(w-120,h-45,100,28);}
        };
        ref[0]=new GLDialog(AppText.text("document.settings"),content);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(650,460);ref[0].show(owner);
    }
    private static void confirmExperimentalDisable(GLParent owner,java.util.function.Consumer<Boolean> result){
        var ref=new GLDialog[1];var accepted=new boolean[1];
        var content=new GLContainer(){
            final GLLabel used=new GLLabel(AppText.text("document.experimental.used")),retained=new GLLabel(AppText.text("document.experimental.retained")),question=new GLLabel(AppText.text("document.experimental.question"));
            final GLButton cancel=new GLButton(AppText.text("cancel")),disable=new GLButton(AppText.text("document.experimental.disable"));
            {for(var c:List.of(used,retained,question,cancel,disable))addGL(c);cancel.addActionListener(e->ref[0].close());disable.addActionListener(e->{accepted[0]=true;ref[0].close();});}
            protected void record(com.vingame.glswing.rendering.GLContext2D c){}
            protected void resized(float w,float h){used.setBounds(20,15,w-40,28);retained.setBounds(20,50,w-40,28);question.setBounds(20,85,w-40,28);cancel.setBounds(w-340,h-45,140,28);disable.setBounds(w-190,h-45,170,28);}
        };
        ref[0]=new GLDialog(AppText.text("experimental"),content);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(680,195);ref[0].closedFuture().thenRun(()->result.accept(accepted[0]));ref[0].show(owner);
    }
}
