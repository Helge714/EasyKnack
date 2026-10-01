package com.vin.easyknack.ui;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.vin.easyknack.formula.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Formula groups reuse the composite interface; their inner graph is generated only for execution. */
final class FormulaGroups {
    static final String MARKER="easyknack.formula",SOURCE=MARKER+".source",DRAFT=MARKER+".draft",PORTS=MARKER+".ports",RATE=MARKER+".rate",LIVE=MARKER+".live",ID=MARKER+".id",NAME=MARKER+".name",CREATE="easyknack.action.formulagroup";
    private static final Map<String,FormulaCompiler.Program> programs=Collections.synchronizedMap(new LinkedHashMap<>(){protected boolean removeEldestEntry(Map.Entry<String,FormulaCompiler.Program> e){return size()>128;}});
    static final Map<String,String> libraries=new ConcurrentHashMap<>();
    static boolean isFormula(GLNodeGraphAsset a){return a!=null&&new NodeState(a.metadata()).flag(MARKER,false);}
    static String source(GLNodeGraphAsset a){return new NodeState(a.metadata()).text(SOURCE,"result = signal;\n");}
    static String draft(GLNodeGraphAsset a){return new NodeState(a.metadata()).text(DRAFT,source(a));}
    static GLNodeId kernelId(GLNodeGraphAsset a){return new GLNodeId(UUID.nameUUIDFromBytes((a.id()+"/formula").getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
    static String portId(GLCompositePortMapping p){return new NodeState(p.presentation().editorOptions()).text(ID,p.publicPort().id());}
    static String codeName(GLCompositePortMapping p){return new NodeState(p.presentation().editorOptions()).text(NAME,identifier(p.publicPort().id()));}
    private static String identifier(String label){String s=label.replaceAll("[^A-Za-z0-9_]","_");if(s.isEmpty()||Character.isDigit(s.charAt(0)))s="value_"+s;return s;}
    static GLCompositePortMapping normalize(GLCompositePortMapping p){
        var options=new HashMap<>(p.presentation().editorOptions());options.putIfAbsent(ID,GLNodeValue.of(UUID.randomUUID().toString()));options.putIfAbsent(NAME,GLNodeValue.of(identifier(p.publicPort().id())));
        return new GLCompositePortMapping(p.publicPort(),null,new GLCompositePortPresentation(p.presentation().inputEditorId(),p.presentation().socketVisible(),options));
    }
    static List<FormulaCompiler.Port> ports(GLNodeGraphAsset a){
        var result=new ArrayList<FormulaCompiler.Port>();
        for(var p:a.compositeDescriptor().portMappings()){
            String type=p.publicPort().valueTypeId();if(!Set.of(SignalCategory.VALUE.valueTypeId(),SignalCategory.AUDIO.valueTypeId(),SignalCategory.TRIGGER.valueTypeId(),SignalCategory.HISTORY.valueTypeId()).contains(type)||type.equals(SignalCategory.HISTORY.valueTypeId())&&p.publicPort().direction()==GLPortDirection.OUTPUT)throw new IllegalArgumentException(AppText.text("formula.numericPorts"));
            result.add(new FormulaCompiler.Port(portId(p),codeName(p),type.equals(SignalCategory.HISTORY.valueTypeId())?FormulaCompiler.Type.BUFFER:type.equals(SignalCategory.TRIGGER.valueTypeId())?FormulaCompiler.Type.BOOLEAN:FormulaCompiler.Type.NUMBER,p.publicPort().direction()==GLPortDirection.OUTPUT));
        }
        long ins=result.stream().filter(p->!p.output()).count(),outs=result.size()-ins;
        if(ins>FormulaNode.INPUTS||outs<1||outs>FormulaNode.OUTPUTS)throw new IllegalArgumentException(AppText.text("formula.portLimit"));return List.copyOf(result);
    }
    static GLNodeValue storedPorts(List<FormulaCompiler.Port> ports){return GLNodeValue.of(ports.stream().map(p->Map.of("id",p.id(),"name",p.name(),"type",p.type().name(),"output",p.output())).toList());}
    static List<FormulaCompiler.Port> acceptedPorts(GLNodeGraphAsset a){
        var value=a.metadata().get(PORTS);if(value==null)return ports(a);var list=(List<?>)value.toJavaValue();
        return list.stream().map(v->{var p=(Map<?,?>)v;return new FormulaCompiler.Port((String)p.get("id"),(String)p.get("name"),FormulaCompiler.Type.valueOf((String)p.get("type")),(Boolean)p.get("output"));}).toList();
    }
    static String key(FormulaCompiler.Program program){return UUID.nameUUIDFromBytes((program.cpp()+program.ports()).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();}
    static GLNodeGraphAsset rename(GLNodeGraphAsset a,GLCompositePortMapping port,String name){
        var options=new HashMap<>(port.presentation().editorOptions());options.put(NAME,GLNodeValue.of(name));
        var replacement=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
        var d=a.compositeDescriptor();var after=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(p->portId(p).equals(portId(port))?replacement:p).toList(),d.properties(),d.propertyMappings()));
        String old=codeName(port);var accepted=acceptedPorts(a).stream().map(p->p.id().equals(portId(port))?new FormulaCompiler.Port(p.id(),name,p.type(),p.output()):p).toList();
        String source=renameText(source(a),old,name);FormulaCompiler.compile(source,accepted,version(a));
        // Validate new names even when the new port is not in the last valid program yet.
        var current=ports(after);var seen=new HashSet<String>();for(var p:current)if(!p.name().matches("[A-Za-z_][A-Za-z0-9_]*")||!seen.add(p.name())||FormulaCompiler.CONTEXT.contains(p.name())||FormulaCompiler.FUNCTIONS.containsKey(p.name())||Set.of("state","float","double","int","long","uint","ulong","unsigned","bool","if","else","true","false","pi","tau").contains(p.name()))throw new IllegalArgumentException("Invalid or duplicate code identifier: "+name);
        var metadata=new HashMap<>(after.metadata());metadata.put(SOURCE,GLNodeValue.of(source));metadata.put(DRAFT,GLNodeValue.of(renameText(draft(a),old,name)));metadata.put(PORTS,storedPorts(accepted));return metadata(after,metadata);
    }
    private static String renameText(String text,String old,String replacement){
        var matcher=java.util.regex.Pattern.compile("//[^\\r\\n]*|[A-Za-z_][A-Za-z0-9_]*|\\S").matcher(text);var out=new StringBuffer();String previous="";
        while(matcher.find()){String token=matcher.group();boolean comment=token.startsWith("//");matcher.appendReplacement(out,java.util.regex.Matcher.quoteReplacement(!comment&&token.equals(old)&&!previous.equals(".")?replacement:token));if(!comment)previous=token;}matcher.appendTail(out);return out.toString();
    }
    static int version(GLNodeGraphAsset a){return (int)new NodeState(a.metadata()).number(MARKER+".version",1);}
    static FormulaCompiler.Program program(GLNodeGraphAsset a){String key=version(a)+":"+source(a)+acceptedPorts(a);return programs.computeIfAbsent(key,k->FormulaCompiler.compile(source(a),acceptedPorts(a),version(a)));}
    static GLNodeGraphAsset accept(GLNodeGraphAsset a,String text){
        var ports=ports(a);var program=FormulaCompiler.compile(text,ports,version(a));
        FormulaRatePlan.resolve(program,ProcessingRate.fromId(new NodeState(a.metadata()).text(RATE,"auto")),Map.of());
        var meta=new HashMap<>(a.metadata());meta.put(MARKER+".version",GLNodeValue.of(version(a)));meta.put(SOURCE,GLNodeValue.of(text));meta.put(DRAFT,GLNodeValue.of(text));meta.put(PORTS,storedPorts(ports));
        return metadata(a,meta);
    }
    static GLNodeGraphAsset metadata(GLNodeGraphAsset a,Map<String,GLNodeValue> m){return new GLNodeGraphAsset(a.id(),a.version(),a.kind(),a.graph(),a.viewState(),m,a.compositeDescriptor());}
    static GLNodeGraphAsset create(){
        String id="formula-"+UUID.randomUUID();
        var in=normalize(new GLCompositePortMapping(GLNodePortDescriptor.input("signal",SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("default",GLNodeValue.of(0.),"minimumEnabled",GLNodeValue.of(false),"maximumEnabled",GLNodeValue.of(false)))));
        var out=normalize(new GLCompositePortMapping(GLNodePortDescriptor.output("result",SignalCategory.VALUE.valueTypeId()),null));
        var d=new GLCompositeNodeDescriptor("easyknack.group."+id,id,1,List.of(out,in));
        var a=new GLNodeGraphAsset(id,1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty(),Map.of(MARKER+".version",GLNodeValue.of(3),MARKER,GLNodeValue.of(true),RATE,GLNodeValue.of("auto"),LIVE,GLNodeValue.of(true),"easyknack.title",GLNodeValue.of("Formula"),"easyknack.category",GLNodeValue.of(NodeCategory.MATH.path()),"easyknack.kind",GLNodeValue.of("VALUE")),d);
        return accept(a,"result = signal;\n");
    }
    static GLNodeGraphAsset materialize(GLNodeGraphAsset asset){
        if(!isFormula(asset))return asset;
        var program=program(asset);var props=new HashMap<String,GLNodeValue>();int ins=0,outs=0,inMask=0,outMask=0,used=0,buffers=0;
        var endpoints=new HashMap<String,GLNodeEndpoint>();var types=new HashMap<String,FormulaCompiler.Port>();
        for(var p:program.ports()){
            int index=p.output()?outs++:ins++;if(p.type()==FormulaCompiler.Type.BOOLEAN){if(p.output())outMask|=1<<index;else inMask|=1<<index;}
            if(!p.output()&&program.usedInputs().contains(p.id()))used|=1<<index;
            if(p.type()==FormulaCompiler.Type.BUFFER)buffers|=1<<index;
            endpoints.put(p.id(),new GLNodeEndpoint(kernelId(asset),(p.output()?"out":"in")+index));types.put(p.id(),p);
        }
        props.put("inputs",GLNodeValue.of(ins));props.put("outputs",GLNodeValue.of(outs));props.put("triggerInputs",GLNodeValue.of(inMask));props.put("triggerOutputs",GLNodeValue.of(outMask));props.put("usedInputs",GLNodeValue.of(used));props.put("bytecode",GLNodeValue.of(program.bytecode()));props.put("cpp",GLNodeValue.of(program.cpp()));
        props.put("library",GLNodeValue.of(libraries.getOrDefault(key(program),"-")));
        props.put("bufferInputs",GLNodeValue.of(buffers));props.put("sampleContext",GLNodeValue.of(program.sampleContext()));props.put("seed",GLNodeValue.of(Integer.toUnsignedLong(kernelId(asset).toString().hashCode())));
        props.put("rate",GLNodeValue.of(new NodeState(asset.metadata()).text(RATE,"auto")));
        var mappings=new ArrayList<GLCompositePortMapping>();
        for(var p:asset.compositeDescriptor().portMappings()){
            var accepted=types.get(portId(p));var type=p.publicPort().valueTypeId().equals(SignalCategory.HISTORY.valueTypeId())?FormulaCompiler.Type.BUFFER:p.publicPort().valueTypeId().equals(SignalCategory.TRIGGER.valueTypeId())?FormulaCompiler.Type.BOOLEAN:FormulaCompiler.Type.NUMBER;
            var endpoint=accepted!=null&&accepted.output()==(p.publicPort().direction()==GLPortDirection.OUTPUT)&&accepted.type()==type?endpoints.get(portId(p)):null;
            mappings.add(p.withEndpoints(endpoint==null?List.of():List.of(endpoint)));
            if(endpoint!=null&&!accepted.output())props.put("unit."+endpoint.portId(),GLNodeValue.of(new NodeState(p.presentation().editorOptions()).text(GroupUnits.BASE,"factor")));
        }
        var d=asset.compositeDescriptor();var graph=new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),List.of(new GLGraphNode(kernelId(asset),"easyknack.formula.kernel",props)),List.of());
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph,asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),mappings,d.properties(),List.of()));
    }
}
