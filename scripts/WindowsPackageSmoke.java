package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import com.vin.easyknack.audio.*;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vin.easyknack.formula.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.plugins.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Runs against the packaged JARs, not target/classes or the developer's Maven cache. */
public final class WindowsPackageSmoke {
    public static void main(String[] args) {
        try { run(Path.of(args[0])); System.out.println("PACKAGE SMOKE PASSED"); System.exit(0); }
        catch (Throwable e) { e.printStackTrace(); System.exit(1); }
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void run(Path work) throws Exception {
        System.out.println("Runtime: " + System.getProperty("java.home"));
        System.out.println("Application: " + com.vin.easyknack.InstallationPaths.applicationDirectory());
        check(Files.isRegularFile(Path.of(System.getProperty("java.home"), "bin/javac.exe")), "No external javac");
        ApplicationTheme.current();
        ExampleLibrary.initialize();
        var plugins = new PluginManager(work.resolve("plugins"));
        check(plugins.startup(System.out::println) == null, "Fresh startup unexpectedly needs a plugin build");
        check(PluginSdk.bytes().length > 0, "Missing SDK");
        var graph = new java.util.concurrent.atomic.AtomicReference<String>();
        SwingUtilities.invokeAndWait(() -> {
            try (var workspace = new WorkspaceController()) {
                graph.set(workspace.audioGraph());
                var codec = new KnackDocumentCodec();
                var file = work.resolve("roundtrip.knack");
                try (var out = Files.newOutputStream(file)) { codec.write(workspace.snapshotDocument(), out); }
                try (var in = Files.newInputStream(file)) {
                    workspace.openDocument("Reopened", codec.read(in, codec.currentSchemaVersion()));
                }
                check(graph.get().equals(workspace.audioGraph()), "Document roundtrip changed the graph");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        try (var host = new AudioHostClient()) {
            host.start(AudioHostClient.defaultExecutable(), true);
            host.command(graph.get()); host.command("PLAY"); Thread.sleep(200);
            check(host.command("STATS").startsWith("OK "), "Host did not render"); host.command("STOP");
        }
        System.out.println("Document and native audio host OK");
        Path mp3 = work.resolve("tone.mp3");
        Files.write(mp3,Base64.getMimeDecoder().decode(Files.readAllBytes(work.resolve("tone.mp3.b64"))));
        check(AudioFileDecoder.decode(mp3, work.resolve("media")).frames() > 0, "MP3 decode failed");
        System.out.println("MP3 media support OK");
        var program = FormulaCompiler.compile("result = sin(tau * 440 * (double) samplePosition / sampleRate);",
                List.of(new FormulaCompiler.Port("result", "result", FormulaCompiler.Type.NUMBER, true)));
        var compiler = plugins.formulaCompiler();
        check(Files.isRegularFile(compiler.compile(program).library()), "Formula DLL missing");
        check(compiler.compile(program).cached(), "Formula cache not reused");
        System.out.println("Formula compilation and cache OK");
        var oscillator = new OscillatorNode(); var id = GLNodeId.random();
        var inner = new GLNodeGraph(); inner.edit("Oscillator", e -> e.addNode(new GLGraphNode(id, oscillator.registration().id(), oscillator.defaults())));
        var descriptor = new GLCompositeNodeDescriptor("smoke.group", "smoke.asset", 1, List.of(
                new GLCompositePortMapping(GLNodePortDescriptor.output("Out", SignalCategory.AUDIO.valueTypeId()), new GLNodeEndpoint(id, "audio-out"))));
        var asset = new GLNodeGraphAsset("smoke.asset", 1, GLNodeGraphAssetKind.COMPOSITE, inner.snapshot(), GLNodeEditorViewState.empty(), Map.of(), descriptor);
        var group = new GLGraphNode(GLNodeId.random(), "smoke.group", Map.of());
        var root = new GLNodeGraph(); root.edit("Group", e -> e.addNode(group));
        var document = new GLNodeGraphDocument(new GLNodeGraphAsset("root", 1, GLNodeGraphAssetKind.PROJECT, root.snapshot(), GLNodeEditorViewState.empty()), Map.of(asset.id(), asset), List.of());
        var archive = work.resolve("oscillator.knackplugin");
        GroupSourceExporter.create(document, group).write(archive);
        var built = plugins.install(archive, System.out::println).build();
        check(!built.cached() && !built.definitions().isEmpty(), "Plugin was not compiled and loaded");
        check(new PluginManager(work.resolve("plugins")).startup(System.out::println).cached(), "Plugin startup rebuilt an unchanged plugin");
        System.out.println("Group export, Java/C++ plugin compilation, host contracts and restart cache OK");
    }
}
