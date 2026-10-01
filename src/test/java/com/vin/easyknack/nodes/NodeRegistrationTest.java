package com.vin.easyknack.nodes;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.tools.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NodeRegistrationTest {
    @TempDir Path temporary;

    private String node(String name, String id, String category) {
        return "package example; import com.vin.easyknack.nodes.*;"
                + "@RegisterNode(id=\"" + id + "\",title=\"Example\",category=NodeCategory." + category + ",kind=NodeKind.VALUE)"
                + "public final class " + name + " extends BasicNode {"
                + "public void define(NodeBuilder node,NodeState state){node.addOutput(new PropertyId(\"out\"),\"Out\");}"
                + "public NativeProcessor processor(){return new NativeProcessor(\"osc\",\"nodes/OscillatorNode.hpp\");}"
                + "public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){return \"-1 440\";} }";
    }
    private boolean compile(Map<String, String> sources, DiagnosticCollector<JavaFileObject> diagnostics) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests need a JDK");
        List<java.io.File> files = new ArrayList<>();
        for (var source : sources.entrySet()) {
            Path file = temporary.resolve(source.getKey() + ".java"); Files.writeString(file, source.getValue()); files.add(file.toFile());
        }
        Path classes = Files.createDirectories(temporary.resolve("classes"));
        Path generated = Files.createDirectories(temporary.resolve("generated"));
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, java.nio.charset.StandardCharsets.UTF_8)) {
            return compiler.getTask(null, manager, diagnostics, List.of("--release", "17", "-classpath", System.getProperty("java.class.path"),
                    "-processorpath", Path.of("target/node-processor").toAbsolutePath().toString(),
                    "-processor", "com.vin.easyknack.processor.NodeProcessor", "-d", classes.toString(), "-s", generated.toString()),
                    null, manager.getJavaFileObjectsFromFiles(files)).call();
        }
    }
    @Test void addingOneAnnotatedClassGeneratesRegistrationWithoutEditingAList() throws Exception {
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        assertTrue(compile(Map.of("NewNode", node("NewNode", "example.newnode", "MATH")), diagnostics), diagnostics.getDiagnostics().toString());
        assertTrue(Files.readString(temporary.resolve("generated/com/vin/easyknack/nodes/GeneratedNodes.java")).contains("new example.NewNode()"));
    }
    @Test void duplicateIdsFailDuringCompilation() throws Exception {
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        assertFalse(compile(Map.of("First", node("First", "example.same", "MATH"), "Second", node("Second", "example.same", "IO")), diagnostics));
        assertTrue(diagnostics.getDiagnostics().stream().anyMatch(d -> d.getMessage(Locale.ROOT).contains("Duplicate node ID")));
    }
    @Test void misspelledCategoriesFailDuringCompilation() throws Exception {
        assertFalse(compile(Map.of("NewNode", node("NewNode", "example.newnode", "TYPO")), new DiagnosticCollector<JavaFileObject>()));
    }
    @Test void propertyIdsWorkForInputsOutputsAndEditors() throws Exception {
        String source = node("NewNode", "example.newnode", "MATH")
                .replace("node.addOutput(new PropertyId(\"out\"),\"Out\");",
                        "node.addOutput(new PropertyId(\"out\"),\"Out\");"
                        + "node.addInput(new PropertyId(\"in\"),\"In\",Editor.Slider.button(0,null,null));"
                        + "node.addEditor(new PropertyId(\"clamp\"),\"Clamp\",Editor.checkBox(false));");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        assertTrue(compile(Map.of("NewNode", source), diagnostics), diagnostics.getDiagnostics().toString());
    }
    @Test void standaloneEditorsRequirePropertyIds() throws Exception {
        String source = node("NewNode", "example.newnode", "MATH")
                .replace("node.addOutput(new PropertyId(\"out\"),\"Out\");", "node.addEditor(\"out\",\"Out\",Editor.checkBox(false));");
        assertFalse(compile(Map.of("NewNode", source), new DiagnosticCollector<JavaFileObject>()));
    }
    @Test void everyJavaDefinitionLinksToItsRegisteredNativeHeader() throws Exception {
        for (BasicNode node : NodeRegistry.definitionsList()) {
            var binding = node.processor();
            Path root = Path.of("native/audio-host/src").toAbsolutePath().normalize();
            Path header = root.resolve(binding.header()).normalize();
            assertTrue(header.startsWith(root), "Native headers must belong to the audio host");
            assertTrue(Files.isRegularFile(header), "Missing native header: " + binding.header());
            assertTrue(Pattern.compile("EASYKNACK_NODE\\(\\s*[A-Za-z_][A-Za-z0-9_:]*\\s*,\\s*\"" + Pattern.quote(binding.id()) + "\"\\s*\\)")
                    .matcher(Files.readString(header)).find(), "Processor ID/header mismatch: " + node.registration().id());
        }
    }
}

