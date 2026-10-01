package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Empty routing silences all outputs; a bypass forwards an upstream signal. */
public final class MuteRouting {
    private final List<GLNodeMuteBypass> bypasses = new ArrayList<>();
    public void connect(PropertyId input, PropertyId output) { bypasses.add(new GLNodeMuteBypass(input.value(), output.value())); }
    public void silence() { bypasses.clear(); }
    public List<GLNodeMuteBypass> bypasses() { return List.copyOf(bypasses); }
}

