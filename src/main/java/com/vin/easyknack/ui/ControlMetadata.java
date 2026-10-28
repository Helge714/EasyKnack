package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLDriverTarget;
import java.util.*;
import java.util.function.BiPredicate;

/** Transient execution metadata. It never adds graph nodes or changes connected signals. */
public final class ControlMetadata {
    private static final ThreadLocal<ControlInterpolationSettings> POLICY = new ThreadLocal<>();
    public static final String DISCRETE = "easyknack.runtime.discreteControl";
    private static final String ECHO = "externalEcho";

    private ControlMetadata() { }

    public static final class Scope implements AutoCloseable {
        private final ControlInterpolationSettings previous;
        private Scope(ControlInterpolationSettings policy) {
            previous = POLICY.get();
            POLICY.set(Objects.requireNonNull(policy));
        }
        @Override public void close() {
            if (previous == null) POLICY.remove();
            else POLICY.set(previous);
        }
    }

    public static Scope use(ControlInterpolationSettings policy) { return new Scope(policy); }

    /** Annotate assignment echoes in a temporary asset, leaving document properties untouched. */
    public static GLNodeGraphAsset prepare(GLNodeGraphAsset asset, BiPredicate<GLNodeId, String> external) {
        var nodes = asset.graph().nodes().stream().map(node -> {
            var values = new HashMap<>(node.properties());
            node.properties().forEach((key, value) -> {
                if (key.startsWith(GLDriverTarget.PROPERTY_PREFIX)
                        && value instanceof GLNodeValue.MapValue map && map.values().containsKey("device")) {
                    var binding = new HashMap<>(map.values());
                    binding.put(ECHO, GLNodeValue.of(external.test(node.id(), MidiDriverBinding.property(key))));
                    values.put(key, GLNodeValue.of(binding));
                }
            });
            return new GLGraphNode(node.id(), node.typeId(), node.typeVersion(), values);
        }).toList();
        var graph = new GLNodeGraphSnapshot(asset.graph().graphId(), asset.graph().revision(), nodes,
                asset.graph().connections());
        return new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(), graph, asset.viewState(),
                asset.metadata(), asset.compositeDescriptor());
    }

    public static String encode(GLGraphNode node, BasicNode definition, NodeBuilder layout, Set<String> connected) {
        var policy = POLICY.get();
        if (policy == null) return ""; // Diagnostic encoders and frozen source-export internals have no controls.
        var state = new NodeState(node.properties());
        var bindings = definition.manualControlPorts();
        var entries = new ArrayList<Entry>();
        for (var row : layout.rows()) {
            if (connected.contains(row.id())) continue;
            if (row.editor() instanceof VectorEditor vector) {
                int port = definition.processor().inputs().indexOf(row.key());
                if (port < 0 && !(definition instanceof VectorNode)) continue;
                port = Math.max(0, port);
                double[] defaults = {vector.x(), vector.y(), vector.z()};
                for (int axis = 0; axis < 3; axis++) {
                    entries.add(new Entry(32 + port * 3 + axis, policy.enabled(), policy,
                            VectorEditor.component(state, row.key(), axis, defaults[axis]), null, false, "-"));
                }
                continue;
            }
            var slider = row.editor() instanceof UnitSlider unit ? unit.spec()
                    : row.editor() instanceof Editor.Slider spec ? spec : null;
            Integer port = bindings.get(row.key());
            if (slider == null || port == null) continue;
            var field = definition.processor().contract().fields().stream()
                    .filter(f -> row.key().equals(f.id()) && (f.kind() == ParameterContract.Kind.VALUE
                            || f.kind() == ParameterContract.Kind.INTEGER || f.kind() == ParameterContract.Kind.MAPPED))
                    .findFirst().orElse(null);
            if (field == null) continue;
            double target;
            try {
                target = field.kind() == ParameterContract.Kind.MAPPED ? Double.parseDouble(field.mapping().apply(state))
                        : state.number(row.key(), slider.value());
            } catch (NumberFormatException unsupportedMapping) {
                continue; // Tokens/resources are not scalar manual controls.
            }
            MidiDriverBinding driver = null;
            var assignment = node.properties().get(MidiDriverBinding.key(row.id()));
            if (assignment instanceof GLNodeValue.MapValue map && map.values().containsKey("device"))
                driver = MidiDriverBinding.read(assignment);
            var selected = driver != null && driver.interpolation() != null ? driver.interpolation() : policy;
            boolean enabled = slider.continuousChanges() && selected.enabled() && !state.flag(DISCRETE, false);
            boolean direct = enabled && driver != null && driver.cc();
            boolean echo = assignment instanceof GLNodeValue.MapValue map && new NodeState(map.values()).flag(ECHO, false);
            String table = direct ? lookup(assignment, driver, field, row, state) : "-";
            entries.add(new Entry(port, enabled, selected, target, direct ? driver : null, echo, table));
        }
        return entries.isEmpty() ? "" : " ~ " + entries.size() + " "
                + String.join(" ", entries.stream().map(Entry::encode).toList()) + " ~";
    }

    /** Mapping precedes parameter/unit conversion. A small CC table preserves nonlinear conversions exactly. */
    private static String lookup(GLNodeValue assignment, MidiDriverBinding driver, ParameterContract.Field field,
            NodeBuilder.Row row, NodeState state) {
        var values = ((GLNodeValue.MapValue) assignment).values().get("nativeValues");
        if (values instanceof GLNodeValue.ListValue list) {
            if (list.values().size() != 128) throw new IllegalArgumentException("Invalid MIDI conversion table");
            return "LUT " + String.join(" ", list.values().stream()
                    .map(v -> Double.toString(((Number) v.toJavaValue()).doubleValue())).toList());
        }
        if (field.kind() != ParameterContract.Kind.MAPPED) return "-";
        var samples = new ArrayList<String>();
        for (int i = 0; i < 128; i++) {
            var mapped = new HashMap<>(state.values());
            mapped.put(row.id(), GLNodeValue.of(driver.mapping().map(i / 127.)));
            samples.add(Double.toString(Double.parseDouble(field.mapping().apply(new NodeState(mapped)))));
        }
        return "LUT " + String.join(" ", samples);
    }

    private record Entry(int port, boolean enabled, ControlInterpolationSettings policy, double target,
            MidiDriverBinding driver, boolean echo, String lookup) {
        String encode() {
            var out = new StringJoiner(" ");
            out.add(Integer.toString(port)).add(enabled ? "1" : "0")
                    .add(Double.toString(policy.minimumMs())).add(Double.toString(policy.maximumMs()))
                    .add(Double.toString(target)).add(driver == null ? "0" : driver.device())
                    .add(Integer.toString(driver == null ? 0 : driver.channel()))
                    .add(Integer.toString(driver == null ? 0 : driver.number()))
                    .add(Double.toString(driver == null ? 0 : driver.mapping().minimum()))
                    .add(Double.toString(driver == null ? 1 : driver.mapping().maximum()))
                    .add(driver != null && driver.mapping().reversed() ? "1" : "0")
                    .add(driver != null && driver.mapping().logarithmic() ? "1" : "0")
                    .add(driver != null && driver.mapping().pickup() ? "1" : "0")
                    .add(echo ? "1" : "0").add(lookup);
            return out.toString();
        }
    }
}
