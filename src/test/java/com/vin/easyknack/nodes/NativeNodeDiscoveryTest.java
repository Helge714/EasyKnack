package com.vin.easyknack.nodes;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.io.TempDir;

@EnabledOnOs(OS.WINDOWS)
class NativeNodeDiscoveryTest {
    @TempDir Path temporary;
    private Path fixture() throws Exception {
        var nodes = Files.createDirectories(temporary.resolve("nodes"));
        Files.writeString(nodes.resolve("ProbeNode.hpp"), """
                #pragma once
                #include "NodeProcessor.hpp"
                namespace easyknack {
                struct ProbeNode : NodeProcessor {
                    void configure(std::istream&) override { }
                    void process(ProcessContext& context) noexcept override {
                        for (std::size_t i=0; i<context.bufferSize; ++i) context.output[i] = 0.25f;
                    }
                };
                EASYKNACK_NODE(ProbeNode, "probe")
                }
                """);
        return nodes;
    }
    private int run(String... command) throws Exception {
        Path log = temporary.resolve("process.log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) { process.destroyForcibly(); fail("Process timed out: " + Arrays.toString(command)); }
        return process.exitValue();
    }
    private int generate(Path nodes) throws Exception {
        return run("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File",
                Path.of("scripts/generate-native-nodes.ps1").toAbsolutePath().toString(), "-NodeDirectory", nodes.toString(),
                "-OutputDirectory", temporary.resolve("generated").toString());
    }
    @Test void discoversNewHeaderAndRejectsDuplicateIdsAndMalformedDeclarations() throws Exception {
        var nodes = fixture(); assertEquals(0, generate(nodes));
        assertTrue(Files.readString(temporary.resolve("generated/GeneratedNodeRegistry.hpp")).contains("std::make_unique<ProbeNode>()"));
        Files.writeString(nodes.resolve("Duplicate.hpp"), "EASYKNACK_NODE(OtherNode, \"probe\")\n");
        assertNotEquals(0, generate(nodes));
        assertTrue(Files.readString(temporary.resolve("process.log")).contains("Duplicate native processor ID"));
        Files.writeString(nodes.resolve("Duplicate.hpp"), "EASYKNACK_NODE(Broken, NOT_A_STRING)\n");
        assertNotEquals(0, generate(nodes));
        assertTrue(Files.readString(temporary.resolve("process.log")).contains("Malformed EASYKNACK_NODE"));
    }
    @Test @EnabledIfSystemProperty(named = "easyknack.test.audio", matches = "true")
    void compilesAndProcessesNewNodeWithoutChangingHostRegistry() throws Exception {
        assertEquals(0, generate(fixture()));
        var source = temporary.resolve("probe.cpp");
        Files.writeString(source, """
                #include "GeneratedNodeRegistry.hpp"
                int main() {
                    auto node = easyknack::createNodeProcessor("probe");
                    float output[3]{};
                    easyknack::ProcessContext context{nullptr, 0, output, 3, 48000, nullptr};
                    node->process(context);
                    return output[0] == .25f && output[2] == .25f ? 0 : 1;
                }
                """);
        var executable = temporary.resolve("probe.exe");
        assertEquals(0, run(System.getProperty("easyknack.test.cxx", "clang++"), "-std=c++17", "-static",
                "-I", Path.of("native/audio-host/src").toAbsolutePath().toString(), "-I", temporary.toString(),
                "-I", temporary.resolve("generated").toString(), source.toString(), "-o", executable.toString()),
                Files.readString(temporary.resolve("process.log")));
        assertEquals(0, run(executable.toString()));
    }
}

