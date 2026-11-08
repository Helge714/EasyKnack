package com.vin.easyknack.nodes;

import java.util.Map;
import java.util.Set;
import com.vingame.glswing.node.model.GLNodeValue;

/** Immutable per-instance parameters. Processor state and sample buffers live in C++. */
public record NodeState(Map<String, GLNodeValue> values, Set<PropertyId> connectedInputs) {
    public NodeState { if(GLNodeValue.of("event").equals(values.get("rate"))){var migrated=new java.util.HashMap<>(values);migrated.put("rate",GLNodeValue.of("audio"));values=migrated;} values = Map.copyOf(values); connectedInputs = Set.copyOf(connectedInputs); }
    public NodeState(Map<String, GLNodeValue> values) { this(values, Set.of()); }
    public boolean isConnected(PropertyId key) { return connectedInputs.contains(key); }
    public double number(PropertyId key, double fallback) { return number(key.value(), fallback); }
    public String text(PropertyId key, String fallback) { return text(key.value(), fallback); }
    public boolean flag(PropertyId key, boolean fallback) { return flag(key.value(), fallback); }
    public double number(String key, double fallback) {
        Object value = values.getOrDefault(key, GLNodeValue.of(fallback)).toJavaValue();
        if (!(value instanceof Number n) || !Double.isFinite(n.doubleValue()))
            throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid numeric parameter: ") + key);
        return n.doubleValue();
    }
    public String text(String key, String fallback) {
        Object value = values.getOrDefault(key, GLNodeValue.of(fallback)).toJavaValue();
        if (!(value instanceof String text)) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid text parameter: ") + key);
        return text;
    }
    public boolean flag(String key, boolean fallback) {
        Object value = values.getOrDefault(key, GLNodeValue.of(fallback)).toJavaValue();
        if (!(value instanceof Boolean flag)) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid flag: ") + key);
        return flag;
    }
}

