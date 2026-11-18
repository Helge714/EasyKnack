package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vingame.glswing.node.document.*;

class LearningExamplesTest {
    @TempDir Path directory;
    @Test void completeLibraryRoundTripsOpensAndCompilesIncludingNestedGroups() throws Exception {
        assertEquals(16, LearningExamples.entries().size());
        var codec = new KnackDocumentCodec();
        for (var entry : LearningExamples.entries()) {
            var original = entry.document().get();
            assertFalse(ExperimentalFeatures.requiresOptIn(original), entry.path());
            if (entry.path().startsWith("06 Feedback")) {
                var types = new com.vingame.glswing.node.model.GLNodeTypeRegistry();
                new com.vin.easyknack.nodes.NodeRegistry(types, new com.vingame.glswing.node.model.GLNodeCatalog());
                assertTrue(com.vin.easyknack.audio.FeedbackConnections.resolve(original.root().graph(), types).ignored().isEmpty(), entry.path());
            }
            var bytes = new ByteArrayOutputStream(); codec.write(original, bytes);
            var restored = NodeDocuments.prepare(codec.read(new ByteArrayInputStream(bytes.toByteArray()), codec.currentSchemaVersion()));
            assertEquals(NodeDocuments.comparable(original), NodeDocuments.comparable(restored), entry.path());
            SwingUtilities.invokeAndWait(() -> {
                try (var c = new WorkspaceController()) {
                    c.openDocument(entry.path(), restored);
                    assertTrue(c.audioGraph().startsWith("GRAPH "), entry.path());
                    for (var asset : restored.embeddedAssets().values()) {
                        assertTrue(c.workspace().openComposite(asset.id()), entry.path() + ": " + asset.id());
                        assertDoesNotThrow(c::audioGraph, entry.path());
                        c.workspace().navigateToDepth(0);
                    }
                }
            });
        }
    }
    @Test void installRetiresOldGeneratedFilesPreservesEditsAndKeepsBackupsOutOfMenu() throws Exception {
        Files.writeString(directory.resolve(".easyknack-examples-v1"), "old marker");
        Files.createDirectories(directory.resolve("Spectral"));
        Files.writeString(directory.resolve("Spectral/Processing.knack"), "edited old example");
        Files.writeString(directory.resolve("My patch.knack"), "user document");
        ExampleLibrary.main(new String[]{directory.toString()});
        assertFalse(Files.exists(directory.resolve("Spectral/Processing.knack")));
        assertEquals("edited old example", Files.readString(directory.resolve(".previous-examples/Spectral/Processing.knack")));
        assertEquals("user document", Files.readString(directory.resolve("My patch.knack")));
        var first = directory.resolve(LearningExamples.entries().get(0).path());
        var codec = new KnackDocumentCodec();
        try (var in = Files.newInputStream(first)) { assertEquals(3, codec.read(in, codec.currentSchemaVersion()).root().graph().nodes().size()); }
        Files.writeString(first, "edited new example");
        ExampleLibrary.main(new String[]{directory.toString()});
        assertEquals("edited new example", Files.readString(first));
        assertTrue(Files.readString(directory.resolve("README-de.md")).contains("06 Feedback"));
        assertTrue(Files.readString(directory.resolve("README-en.md")).contains("06 Feedback"));
        var menu = ExampleLibrary.scan(directory);
        assertEquals(7, menu.size()); // Six categories plus the independent user document.
        assertFalse(menu.stream().anyMatch(e -> e.name().startsWith(".")));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio", matches="true")
    void everyExampleRendersFiniteAudibleAudioAtConservativeDefaults() throws Exception {
        var patches = new LinkedHashMap<String, String>();
        SwingUtilities.invokeAndWait(() -> {
            for (var entry : LearningExamples.entries()) try (var c = new WorkspaceController()) {
                c.openDocument(entry.path(), entry.document().get()); patches.put(entry.path(), c.audioGraph());
            }
        });
        var source = new StringBuilder("#include \"AudioGraph.hpp\"\n#include \"")
            .append(Path.of("native/audio-host/tests/FilterMidiTest.hpp").toAbsolutePath().toString().replace('\\','/'))
            .append("\"\n#include <iostream>\n#include <cmath>\nusing namespace easyknack;int main(){\n");
        for (var entry : patches.entrySet()) {
            source.append("{auto graph=AudioGraph::parse(").append(AdvancedSourceExportTest.quote(entry.getValue())).append(");")
                .append("filter_midi_tests::Source midi;std::array<float,256> out{};double peak=0,energy=0;std::size_t samples=0;")
                .append("for(int b=0;b<750;b++){midi.block.clear();if(b==0){midi.block.append(0,filter_midi_tests::message(144,60,100));midi.block.append(0,filter_midi_tests::message(144,64,100));}")
                .append("if(b==200){midi.block.append(0,filter_midi_tests::message(128,60));midi.block.append(0,filter_midi_tests::message(128,64));}")
                .append("graph.process(out.data(),256,0,nullptr,&midi);for(float v:out){if(!std::isfinite(v))throw std::runtime_error(\"Nonfinite output\");peak=std::max(peak,double(std::abs(v)));energy+=v*v;samples++;}}")
                .append("std::cout<<").append(AdvancedSourceExportTest.quote(entry.getKey())).append("<<\" peak=\"<<peak<<\" rms=\"<<std::sqrt(energy/samples)<<std::endl;")
                .append("if(peak<.001||peak>.95||std::sqrt(energy/samples)<.0001)throw std::runtime_error(\"Silent or excessive example output\");}\n");
        }
        source.append("}\n"); var cpp = directory.resolve("examples.cpp"); Files.writeString(cpp, source);
        RateConverterTest.compileAndRun(cpp, directory.resolve("examples.exe"), directory);
    }
}
