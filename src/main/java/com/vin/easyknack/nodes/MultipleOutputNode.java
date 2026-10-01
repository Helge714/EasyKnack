package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.multiple.output",title="Instance Output",category=NodeCategory.ROUTING,kind=NodeKind.AUDIO)
public final class MultipleOutputNode extends BasicNode {
    public boolean isListedInCatalog(){return false;}
    public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("out"),"Out");n.addInput(new PropertyId("in"),"In");
    }
    public NativeProcessor processor(){return new NativeProcessor("multipleoutput","nodes/MultipleOutputNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
