package com.vin.easyknack.nodes;

import com.vingame.glswing.node.model.GLNodeInsertionPorts;

/** One explicit input/output pair for inserting an unconnected node into a wire. */
public final class InsertionRouting {
    private GLNodeInsertionPorts ports;
    public void connect(PropertyId input, PropertyId output) {
        ports = new GLNodeInsertionPorts(input.value(), output.value());
    }
    public GLNodeInsertionPorts ports() { return ports; }
}

