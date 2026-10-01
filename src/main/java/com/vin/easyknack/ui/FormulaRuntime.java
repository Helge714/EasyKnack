package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.formula.*;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;
import com.vingame.glswing.node.document.GLNodeGraphAsset;

/** Workspace-owned asynchronous optimization; the interpreter is always immediately available. */
final class FormulaRuntime implements AutoCloseable {
    private static final java.util.concurrent.Semaphore COMPILERS=new java.util.concurrent.Semaphore(1,true);
    private final Map<String,FormulaLiveSession> sessions=new HashMap<>();
    private final Map<String,String> requested=new HashMap<>(),states=new HashMap<>();
    private final Runnable changed;private boolean closed;
    FormulaRuntime(Runnable changed){this.changed=changed;}
    String status(GLNodeGraphAsset asset){return states.getOrDefault(asset.id(),AppText.text("formula.preview"));}
    void refresh(GLCompositeGraphRegistry groups){
        if(closed)return;
        for(var asset:groups.assets())if(FormulaGroups.isFormula(asset))try{
            var program=FormulaGroups.program(asset);String key=FormulaGroups.key(program);
            if(key.equals(requested.get(asset.id())))continue;requested.put(asset.id(),key);
            if(FormulaGroups.libraries.containsKey(key)){states.put(asset.id(),AppText.text("formula.optimized"));continue;}
            var session=sessions.computeIfAbsent(asset.id(),id->new FormulaLiveSession(p->{COMPILERS.acquire();try{return com.vin.easyknack.plugins.PluginManager.current().formulaCompiler().compile(p);}finally{COMPILERS.release();}},java.awt.EventQueue::invokeLater,status->{
                if(closed)return;
                if(status.phase()==FormulaLiveSession.Phase.READY){FormulaGroups.libraries.put(FormulaGroups.key(status.active().program()),com.vin.easyknack.nodes.FormulaNode.hex(status.active().nativeCode().library().toString()));states.put(id,AppText.text("formula.optimized"));}
                else if(status.phase()==FormulaLiveSession.Phase.ERROR)states.put(id,AppText.text("formula.preview")+" — "+status.error());
                else states.put(id,AppText.text("formula.compiling"));changed.run();
            }));
            session.submit(FormulaGroups.source(asset),FormulaGroups.acceptedPorts(asset),600,FormulaGroups.version(asset));
        }catch(RuntimeException e){states.put(asset.id(),e.getMessage());}
    }
    void reset(){sessions.values().forEach(FormulaLiveSession::close);sessions.clear();requested.clear();states.clear();}
    public void close(){closed=true;reset();}
}
