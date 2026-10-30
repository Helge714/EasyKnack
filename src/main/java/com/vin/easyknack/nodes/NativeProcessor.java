package com.vin.easyknack.nodes;

import java.util.Objects;

/** Link to a compiled processor, not C++ source executed by Java. */
public record NativeProcessor(String id, String header, ParameterContract contract) {
    public NativeProcessor {
        Objects.requireNonNull(id); Objects.requireNonNull(header); Objects.requireNonNull(contract);
    }
    public NativeProcessor(String id, String header) { this(id, header, ParameterContract.builder().build()); }
    public java.util.List<PropertyId> inputs(){return contract.inputs();}
    public java.util.List<PropertyId> inputs(NodeState state){return contract.inputs(state);}
}

