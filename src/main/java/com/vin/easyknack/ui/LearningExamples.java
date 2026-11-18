package com.vin.easyknack.ui;

import java.util.*;
import java.util.function.Supplier;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Small, self-contained teaching graphs. No files, device IDs or native caches are stored. */
final class LearningExamples {
    record Entry(String path, Supplier<GLNodeGraphDocument> document) {}
    static List<Entry> entries() {
        return List.of(
            new Entry("01 Basics/01 First tone.knack", LearningExamples::tone),
            new Entry("01 Basics/02 Filtered noise.knack", LearningExamples::filteredNoise),
            new Entry("01 Basics/03 Tremolo.knack", LearningExamples::tremolo),
            new Entry("01 Basics/04 Live buffer - speed.knack", () -> buffer(false)),
            new Entry("01 Basics/05 Live buffer - position.knack", () -> buffer(true)),
            new Entry("01 Basics/06 Group with controls.knack", LearningExamples::simpleGroup),
            new Entry("02 MIDI/01 Mono instrument.knack", () -> midi(1)),
            new Entry("02 MIDI/02 Poly instrument.knack", () -> midi(8)),
            new Entry("03 Instance Groups/01 Harmonic bank.knack", () -> instances(false)),
            new Entry("03 Instance Groups/02 Odd harmonics.knack", () -> instances(true)),
            new Entry("04 Spectral Groups/01 FFT round trip.knack", () -> spectral(false)),
            new Entry("04 Spectral Groups/02 Bin lowpass.knack", () -> spectral(true)),
            new Entry("05 Formula Groups/01 Logarithmic mapping.knack", LearningExamples::mapping),
            new Entry("05 Formula Groups/02 Function waveshaper.knack", LearningExamples::waveshaper),
            new Entry("06 Feedback/01 Echo.knack", () -> feedback(false)),
            new Entry("06 Feedback/02 Resonator.knack", () -> feedback(true)));
    }
    private static final class Graph {
        final GLNodeGraph graph = new GLNodeGraph();
        final Map<GLNodeId, GLNodeViewState> views = new LinkedHashMap<>();
        final Map<String, GLNodeGraphAsset> assets = new LinkedHashMap<>();
        GLGraphNode add(BasicNode definition, float x, float y, Object... properties) {
            var values = new HashMap<>(definition.defaults());
            for (int i = 0; i < properties.length; i += 2) values.put((String) properties[i], GLNodeValue.of(properties[i + 1]));
            return add(new GLGraphNode(GLNodeId.random(), definition.registration().id(), values), x, y);
        }
        GLGraphNode add(GLGraphNode node, float x, float y) {
            graph.edit("Example node", e -> e.addNode(node));
            views.put(node.id(), new GLNodeViewState(x, y));
            return node;
        }
        GLGraphNode group(GLNodeGraphAsset asset, float x, float y, Object... properties) {
            assets.put(asset.id(), asset);
            var values = new HashMap<String, GLNodeValue>();
            for (int i = 0; i < properties.length; i += 2) values.put((String) properties[i], GLNodeValue.of(properties[i + 1]));
            return add(new GLGraphNode(GLNodeId.random(), asset.compositeDescriptor().nodeTypeId(), values), x, y);
        }
        void wire(GLGraphNode from, String output, GLGraphNode to, String input) {
            graph.edit("Example connection", e -> e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),
                new GLNodeEndpoint(from.id(), output), new GLNodeEndpoint(to.id(), input))));
        }
        void label(GLGraphNode node, String title) {
            var view = views.get(node.id());
            views.put(node.id(), new GLNodeViewState(view.x(), view.y(), 0, 0, title));
        }
        void output(GLGraphNode source, String port, float x, float y) {
            var gain = add(new GainNode(), x, y, "gainDb", -18.0);
            var out = add(new OutputNode(), x + 270, y);
            wire(source, port, gain, "audio-in"); wire(gain, "audio-out", out, "audio-in");
        }
        GLNodeGraphDocument document() {
            return NodeDocuments.prepare(new GLNodeGraphDocument(new GLNodeGraphAsset("example.root", 1, GLNodeGraphAssetKind.PROJECT,
                graph.snapshot(), new GLNodeEditorViewState(views)), assets, List.of()));
        }
    }
    private static GLNodeGraphDocument tone() {
        var g = new Graph(); var osc = g.add(new OscillatorNode(), 40, 60, "frequency", 220.0);
        g.output(osc, "audio-out", 320, 60); return g.document();
    }
    private static GLNodeGraphDocument filteredNoise() {
        var g = new Graph(); var noise = g.add(new NoiseNode(), 40, 60, "seed", 17.0);
        var filter = g.add(new FilterNode(), 320, 60, "cutoff", 1200.0);
        g.wire(noise, "audio-out", filter, "audio-in"); g.output(filter, "audio-out", 600, 60); return g.document();
    }
    private static GLNodeGraphDocument tremolo() {
        var g = new Graph(); var osc = g.add(new OscillatorNode(), 40, 40, "frequency", 220.0);
        var lfo = g.add(new OscillatorNode(), 40, 370, "frequency", 4.0);
        var scale = g.add(new MathNode(), 320, 370, "b", 0.5);
        var offset = g.add(new MathNode(), 600, 370, "operation", "add", "b", 0.5);
        var multiply = g.add(new MathNode(), 880, 40);
        g.label(lfo, "LFO"); g.label(scale, "Scale by 0.5"); g.label(offset, "Shift to 0..1"); g.label(multiply, "Amplitude");
        g.wire(lfo, "audio-out", scale, "a"); g.wire(scale, "audio-out", offset, "a");
        g.wire(osc, "audio-out", multiply, "a"); g.wire(offset, "audio-out", multiply, "b");
        g.output(multiply, "audio-out", 1160, 40); return g.document();
    }
    private static GLNodeGraphDocument buffer(boolean position) {
        var g = new Graph(); var osc = g.add(new OscillatorNode(), 40, 60, "frequency", 220.0, "waveform", "saw");
        var buffer = g.add(new HistoryBufferNode(), 320, 60, "seconds", 1.0);
        var reader = g.add(new HistoryReaderNode(), 600, 60, "speed", 0.75, "offset", 0.25);
        g.wire(osc, "audio-out", buffer, "audio-in"); g.wire(buffer, "history", reader, "history");
        if (position) {
            var follower = g.add(new PositionReaderNode(), 900, 60, "offset", 0.0);
            var context = g.add(new BufferContextNode(), 320, 430);
            var read = g.add(new ValueViewerNode(), 900, 460);
            g.wire(buffer, "history", context, "buffer");
            g.wire(buffer, "history", follower, "history"); g.wire(reader, "read-position", follower, "position");
            g.wire(context, "write-position", read, "value-0");
            g.output(follower, "audio-out", 1200, 60);
        } else g.output(reader, "audio-out", 900, 60);
        return g.document();
    }
    private static GLCompositePortPresentation slider(double value) {
        return new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER, true,
            Map.of("default", GLNodeValue.of(value), "minimumEnabled", GLNodeValue.of(false), "maximumEnabled", GLNodeValue.of(false)));
    }
    private static GLCompositePortMapping input(String name, GLGraphNode target, String port, double value) {
        return new GLCompositePortMapping(GLNodePortDescriptor.input(name, SignalCategory.VALUE.valueTypeId(), false, 1),
            new GLNodeEndpoint(target.id(), port), slider(value));
    }
    private static GLNodeGraphAsset asset(String id, String title, Graph graph, List<GLCompositePortMapping> ports, Map<String, GLNodeValue> flags) {
        var metadata = new HashMap<>(flags); metadata.put("easyknack.title", GLNodeValue.of(title));
        metadata.putIfAbsent("easyknack.kind", GLNodeValue.of("AUDIO"));
        return new GLNodeGraphAsset(id, 1, GLNodeGraphAssetKind.COMPOSITE, graph.graph.snapshot(), new GLNodeEditorViewState(graph.views),
            metadata, new GLCompositeNodeDescriptor("easyknack.group." + id, id, 1, ports));
    }
    private static GLNodeGraphDocument simpleGroup() {
        var inner = new Graph(); var osc = inner.add(new OscillatorNode(), 40, 60, "waveform", "saw", "frequency", 220.0);
        var filter = inner.add(new FilterNode(), 340, 60, "cutoff", 900.0); inner.wire(osc, "audio-out", filter, "audio-in");
        var group = asset("example.filtered-tone", "Filtered tone", inner, List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.output("out", SignalCategory.AUDIO.valueTypeId()), new GLNodeEndpoint(filter.id(), "audio-out")),
            input("frequency", osc, "frequency", 220), input("cutoff", filter, "cutoff", 900)), Map.of());
        var g = new Graph(); var node = g.group(group, 40, 60); g.output(node, "out", 340, 60); return g.document();
    }
    private static GLNodeGraphDocument midi(int voices) {
        var g = new Graph(); var input = g.add(new MidiInputNode(), 40, 40);
        var note = g.add(new MidiNoteNode(), 340, 40, "voices", voices);
        var osc = g.add(new OscillatorNode(), 650, 40, "waveform", "saw");
        var envelope = g.add(new EnvelopeNode(), 650, 370, "duration", 15.0, "decay", 180.0, "release", 450.0);
        var velocity = g.add(new MathNode(), 980, 400); var signal = g.add(new MathNode(), 1270, 40);
        g.label(velocity, "Velocity x envelope"); g.label(signal, "Voice amplitude");
        g.wire(input, "midi-out", note, "midi-in"); g.wire(note, "frequency", osc, "frequency");
        g.wire(note, "gate", envelope, "gate"); g.wire(note, "trigger", envelope, "trigger");
        g.wire(note, "velocity", velocity, "a"); g.wire(envelope, "audio-out", velocity, "b");
        g.wire(osc, "audio-out", signal, "a"); g.wire(velocity, "audio-out", signal, "b");
        if (voices > 1) {
            var merge = g.add(new VoiceMergeNode(), 1560, 40, "mode", "sum"); g.wire(signal, "audio-out", merge, "audio-in");
            var gain = g.add(new GainNode(), 1840, 40, "gainDb", -30.0); var out = g.add(new OutputNode(), 2120, 40);
            g.wire(merge, "audio-out", gain, "audio-in"); g.wire(gain, "audio-out", out, "audio-in");
        } else g.output(signal, "audio-out", 1560, 40);
        return g.document();
    }
    private static GLNodeGraphAsset formula(String id, String title, SignalCategory signal, String code, String... inputs) {
        var ports = new ArrayList<GLCompositePortMapping>();
        ports.add(FormulaGroups.normalize(new GLCompositePortMapping(GLNodePortDescriptor.output("result", signal.valueTypeId()), null)));
        for (String name : inputs) ports.add(FormulaGroups.normalize(new GLCompositePortMapping(
            GLNodePortDescriptor.input(name, signal.valueTypeId(), false, 1), null, slider(0))));
        return FormulaGroups.accept(asset(id, title, new Graph(), ports, Map.of(FormulaGroups.MARKER, GLNodeValue.of(true),
            FormulaGroups.MARKER + ".version", GLNodeValue.of(3), FormulaGroups.RATE, GLNodeValue.of("auto"),
            "easyknack.kind", GLNodeValue.of(signal == SignalCategory.AUDIO ? "AUDIO" : "VALUE"))), code);
    }
    private static GLNodeGraphDocument instances(boolean odd) {
        var group = MultipleGroups.create(); var graph = new Graph();
        for (var node : group.graph().nodes()) graph.add(node, (float) group.viewState().nodes().get(node.id()).x(), (float) group.viewState().nodes().get(node.id()).y());
        for (var connection : group.graph().connections()) graph.graph.edit("Instance wiring", e -> e.addConnection(connection));
        var context = group.graph().nodes().stream().filter(n -> NodeRegistry.require(n.typeId()) instanceof InstanceContextNode).findFirst().orElseThrow();
        var merge = group.graph().nodes().stream().filter(n -> NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode).findFirst().orElseThrow();
        var weights = formula("example.harmonic-weights", "Harmonic weights", SignalCategory.VALUE,
            odd ? "// Index starts at zero: keep harmonics 1, 3, 5, ...\nresult = 0;\nif (signal % 2 == 0) result = 1.0 / (signal + 1);\n" : "// Quieter high harmonics\nresult = 1.0 / (signal + 1);\n", "signal");
        var weight = graph.group(weights, 720, 430); graph.wire(context, "index", weight, "signal"); graph.wire(weight, "result", merge, "weight");
        var metadata = new HashMap<>(group.metadata()); metadata.put("easyknack.title", GLNodeValue.of(odd ? "Odd harmonics" : "Harmonic bank"));
        var d = group.compositeDescriptor();
        group = new GLNodeGraphAsset(group.id(), group.version(), group.kind(), graph.graph.snapshot(), new GLNodeEditorViewState(graph.views), metadata, d);
        var g = new Graph(); g.assets.put(weights.id(), weights);
        var node = g.group(group, 40, 60, MultipleGroups.COUNT_PROPERTY, 8.0);
        g.output(node, "out", 350, 60); return g.document();
    }
    private static GLNodeGraphDocument spectral(boolean lowpass) {
        var group = SpectralGroups.create(); var g = new Graph();
        if (lowpass) {
            var inner = new Graph(); var context = inner.add(new BinContextNode(), 40, 60);
            var filter = formula("example.bin-mask", "Bin mask", SignalCategory.VALUE,
                "// One evaluation per FFT bin, not per audio sample.\nresult = signal;\nif (frequency > 1200) result = 0;\n", "signal", "frequency");
            var mask = inner.group(filter, 360, 60); var output = inner.add(new BinOutputNode(), 680, 60, "mode", "polar");
            inner.wire(context, "magnitude", mask, "signal"); inner.wire(context, "frequency", mask, "frequency");
            inner.wire(mask, "result", output, "a"); inner.wire(context, "phase", output, "b");
            var d = group.compositeDescriptor(); var ports = List.of(
                new GLCompositePortMapping(GLNodePortDescriptor.output("out", SignalCategory.SPECTRUM.valueTypeId()), new GLNodeEndpoint(output.id(), "spectrum")),
                new GLCompositePortMapping(GLNodePortDescriptor.input("spectrum", SignalCategory.SPECTRUM.valueTypeId(), false, 1), new GLNodeEndpoint(context.id(), "spectrum")));
            group = asset(group.id(), "Bin lowpass", inner, ports, group.metadata()); g.assets.put(filter.id(), filter);
        }
        var noise = g.add(new NoiseNode(), 40, 60, "seed", 17.0);
        var fft = g.add(new SpectrumNode(), 320, 60, "size", "1024", "synchronous", true);
        var node = g.group(group, 600, 60); var ifft = g.add(new IFFTNode(), 900, 60);
        g.wire(noise, "audio-out", fft, "audio-in"); g.wire(fft, "analysis", node, "spectrum"); g.wire(node, "out", ifft, "spectrum");
        g.output(ifft, "out", 1180, 60); return g.document();
    }
    private static GLNodeGraphDocument mapping() {
        var g = new Graph(); var control = g.add(new ValueNode(), 40, 60, "value", 0.5, "slider-style", "bar", "minimum", 0.0, "maximum", 1.0);
        var formula = formula("example.mapping", "Logarithmic pitch", SignalCategory.VALUE,
            "// 0 -> 110 Hz, 0.5 -> 220 Hz, 1 -> 440 Hz\nresult = 110 * pow(4, signal);\n", "signal");
        var mapped = g.group(formula, 340, 60); var osc = g.add(new OscillatorNode(), 650, 60);
        g.wire(control, "value-out", mapped, "signal"); g.wire(mapped, "result", osc, "frequency");
        g.output(osc, "audio-out", 930, 60); return g.document();
    }
    private static GLNodeGraphDocument waveshaper() {
        var g = new Graph(); var osc = g.add(new OscillatorNode(), 40, 60, "frequency", 110.0);
        var formula = formula("example.waveshaper", "Soft clipping function", SignalCategory.AUDIO,
            "double saturate(final double value) {\n    return value / (1 + abs(value));\n}\nconst double DRIVE = 4;\nresult = saturate(DRIVE * signal);\n", "signal");
        var shaped = g.group(formula, 340, 60); g.wire(osc, "audio-out", shaped, "signal");
        g.output(shaped, "result", 660, 60); return g.document();
    }
    private static GLNodeGraphDocument feedback(boolean resonator) {
        var g = new Graph(); var clock = g.add(new ClockNode(), 40, 380, "frequency", resonator ? 1.0 : 0.5);
        var noise = g.add(new NoiseNode(), 40, 40, "seed", 17.0);
        var envelope = g.add(new EnvelopeNode(), 340, 380, "hold", false, "duration", 2.0, "decay", 8.0, "release", 10.0);
        var pulse = g.add(new MathNode(), 660, 40);
        var sum = g.add(new MathNode(), 960, 40, "operation", "add");
        var delay = g.add(new DelayNode(), 1260, 40, "time", resonator ? 1.0 / 147 : 0.3);
        var filter = g.add(new FilterNode(), 1260, 380, "cutoff", resonator ? 2200.0 : 4000.0);
        var attenuation = g.add(new MathNode(), 960, 380, "b", resonator ? 0.92 : 0.5);
        g.label(pulse, "Excitation"); g.label(sum, "Dry + feedback"); g.label(attenuation, "Feedback amount"); g.label(filter, "Damping");
        g.wire(clock, "value-out", envelope, "trigger"); g.wire(noise, "audio-out", pulse, "a");
        g.wire(envelope, "audio-out", pulse, "b"); g.wire(pulse, "audio-out", sum, "a");
        g.wire(sum, "audio-out", delay, "audio-in"); g.wire(delay, "audio-out", filter, "audio-in");
        g.wire(filter, "audio-out", attenuation, "a"); g.wire(attenuation, "audio-out", sum, "b");
        g.output(sum, "audio-out", 1570, 40); return g.document();
    }
}
