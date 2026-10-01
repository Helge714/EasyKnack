package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.PropertyId;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.model.*;

public record NodeViewContext(PropertyWriter writer, GLHistoryManager history) {
    @FunctionalInterface public interface PropertyWriter { void set(GLNodeId node, PropertyId key, GLNodeValue value); }
}
