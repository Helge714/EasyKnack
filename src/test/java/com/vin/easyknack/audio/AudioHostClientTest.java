package com.vin.easyknack.audio;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Real child process and TCP transport, using a silent null audio device. */
@EnabledIfSystemProperty(named = "easyknack.test.audio", matches = "true")
class AudioHostClientTest {
    @Test void midiProtocolAndMultipleOutputsRunOnTheHost() throws Exception {
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),true);
            assertEquals(java.util.List.of(MidiDevices.KEYBOARD,MidiDevices.NONE),MidiDevices.parse(client.command("MIDI_INPUTS")));
            assertEquals("OK",client.command("GRAPH 3 src midiin 0 @ c - note midinote 0 0 2 @ a c out out 257 0 @ a a"));
            assertEquals("OK",client.command("PLAY"));assertEquals("OK",client.command("MIDI_PANIC"));
            assertThrows(IOException.class,()->client.command("GRAPH 2 src midiin 0 @ c - out out 0 0 @ a a"));
            assertEquals("OK",client.command("GRAPH 2 f filter -1 -1 1200 -1 2 bandpass @ a aaa out out 0 0 @ a a"));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.device",matches="true")
    void discoversWindowsMidiDevicesWithoutOpeningThem() throws Exception {
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),false);
            var devices=MidiDevices.parse(client.command("MIDI_INPUTS"));
            System.out.println("Windows MIDI discovery: "+devices.stream().filter(d->!d.id().equals("0")).map(MidiDevices.Device::name).toList());
            assertTrue(devices.stream().allMatch(MidiDevices.Device::available));
        }
    }
    @Test void liveSliderUpdatesKeepOutputClockAndBuffersRunning() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), true);
            String graph = "GRAPH 3 osc additive -1 sine 144 1 %d -1 440 -1 0 inputs -1 -1 -1 @ a aaacaa silent math 0 0 -1 0 multiply 0 @ a aa out deviceout 1 64656661756c74 @ a a";
            client.command(graph.formatted(0)); client.command("PLAY");
            String clock = client.command("IO_CLOCK");
            long frames = Long.parseLong(client.command("IOSTATS").split(" ")[2]);
            long nanos = 0;
            for (int i = 0; i < 50; i++) {
                long start = System.nanoTime(); client.command(graph.formatted(i - 25)); nanos += System.nanoTime() - start;
                Thread.sleep(20);
                var stats = client.command("IOSTATS").split(" ");
                long next = Long.parseLong(stats[2]);
                // The asynchronous null backend is not a real-time clock. Check continuity
                // with a bounded wait rather than assuming a callback in every 20 ms window.
                long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(200);
                while(next==frames && System.nanoTime()<deadline){
                    Thread.sleep(5);stats=client.command("IOSTATS").split(" ");next=Long.parseLong(stats[2]);
                }
                assertTrue(next >= frames, "Parameter update reset the output frame counter");
                assertTrue(next > frames, "Output clock did not advance within 200 ms"); frames = next;
                assertEquals(clock,client.command("IO_CLOCK"));
                assertEquals("0", stats[3]); assertEquals("0", stats[4]);
            }
            assertEquals(clock, client.command("IO_CLOCK"));
            System.out.printf(java.util.Locale.ROOT, "Live slider: mean GRAPH round-trip %.3f ms; %s%n", nanos / 50e6, client.command("STATS"));
            client.command("STOP");
        }
    }
    @Test void outputOwnsClockAcrossRefreshStopAndTopologyChanges() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), true);
            assertEquals("OK timer", client.command("IO_CLOCK"));
            for (int cycle = 0; cycle < 3; cycle++) {
                client.command("GRAPH 1 out deviceout -1 64656661756c74 @ a a");
                client.command("PLAY");
                assertTrue(client.command("IO_CLOCK").startsWith("OK output 64656661756c74 "));
                Thread.sleep(250); // More than a ring's capacity: an unconsumed master ring would overflow.
                long before = Long.parseLong(client.command("IOSTATS").split(" ")[2]);
                client.command("ENDPOINTS");
                Thread.sleep(80);
                String[] stats = client.command("IOSTATS").split(" ");
                assertTrue(Long.parseLong(stats[2]) > before, "Refresh must not reset the master stream");
                assertEquals("0", stats[3], "Direct output has no ring underruns");
                assertEquals("0", stats[4], "Direct output must bypass the ring producer too");
                client.command("GRAPH 0"); // Change topology while playing.
                assertEquals("OK timer", client.command("IO_CLOCK"));
                client.command("STOP");
            }
            client.command("QUIT");
        }
    }
    @Test
    @EnabledIfSystemProperty(named = "easyknack.test.device", matches = "true")
    void availableAsioDriverRunsSilentGraphAndReturnsToWasapi() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), false);
            var asio = AudioEndpoints.parse(client.command("ENDPOINTS")).endpoints().stream()
                    .filter(e -> e.type() == AudioEndpoint.Type.ASIO_OUTPUT && e.available()).findFirst();
            if (asio.isEmpty()) {
                System.out.println("ASIO hardware check: no initializable driver; simulated backend test covers DSP and lifecycle.");
                return;
            }
            System.out.println("ASIO hardware check (silence only): " + asio.get().name());
            client.command("GRAPH 1 out deviceout -1 " + AudioEndpoint.hex(asio.get().id()) + " @ a a");
            client.command("PLAY");
            assertEquals("OK -", client.command("ROUTING_STATUS"));
            long before = Long.parseLong(client.command("PING").substring(3));
            Thread.sleep(150);
            assertTrue(Long.parseLong(client.command("PING").substring(3)) > before, "ASIO must clock DSP");
            assertTrue(Long.parseLong(client.command("IOSTATS").split(" ")[2]) > 0, "ASIO must deliver buffers");
            client.command("ENDPOINTS"); // Discovery while the driver is running.
            client.command("STOP");
            client.command("GRAPH 1 out deviceout -1 64656661756c74 @ a a");
            client.command("PLAY");
            assertEquals("OK -", client.command("ROUTING_STATUS"));
            client.command("STOP"); client.command("QUIT");
        }
    }
    @Test
    @EnabledIfSystemProperty(named = "easyknack.test.device", matches = "true")
    void defaultOutputOpensAndClosesWhileSilent() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), false);
            assertTrue(client.command("PING").startsWith("OK "));
            var endpoints = AudioEndpoints.parse(client.command("ENDPOINTS"));
            assertTrue(endpoints.endpoints().stream().anyMatch(e -> e.id().equals("default")));
            assertEquals("OK", client.command("GRAPH 1 out deviceout -1 64656661756c74 @ a a"));
            assertEquals("OK", client.command("PLAY"));
            assertEquals("OK -", client.command("ROUTING_STATUS"));
            client.command("STOP");
            client.command("QUIT");
        }
    }

    @Test void missingEndpointsRemainValidAndRouteModeIsRejected() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), true);
            assertFalse(AudioEndpoints.parse(client.command("ENDPOINTS")).capabilities().applicationRouting());
            String missing = AudioEndpoint.hex("in:missing");
            assertEquals("OK", client.command("GRAPH 2 in input " + missing + " copy stereo @ a - out deviceout 0 64656661756c74 @ a a"));
            assertTrue(AudioEndpoint.unhex(client.command("ROUTING_STATUS").substring(3)).contains("in:missing"));
            assertEquals("OK", client.command("PLAY"));
            assertThrows(IOException.class, () -> client.command("GRAPH 1 in input " + missing + " route stereo @ a -"));
            assertEquals("OK", client.command("GRAPH 0"));
            assertEquals("OK -", client.command("ROUTING_STATUS"));
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "easyknack.test.device", matches = "true")
    void systemAndApplicationCopyCaptureOpenWithoutOutputOrRoutingChanges() throws Exception {
        try (var producer = new AudioHostClient(); var consumer = new AudioHostClient()) {
            producer.start(AudioHostClient.defaultExecutable(), false);
            producer.command("GRAPH 1 out deviceout -1 64656661756c74 @ a a");
            producer.command("PLAY"); // A silent WASAPI session belonging to our test child.
            consumer.start(AudioHostClient.defaultExecutable(), false);
            var endpoints = AudioEndpoints.parse(consumer.command("ENDPOINTS"));
            if (endpoints.capabilities().applicationCapture()) {
                var application = endpoints.endpoints().stream().filter(e -> e.type() == AudioEndpoint.Type.APPLICATION
                        && e.name().equalsIgnoreCase("easyknack-audio-host.exe")).findFirst().orElseThrow();
                for (String endpoint : new String[] {"system", application.id()}) {
                    consumer.command("GRAPH 3 src input " + AudioEndpoint.hex(endpoint) + " copy stereo @ a - silent math 0 0 -1 0 multiply 0 @ a aa out deviceout 1 64656661756c74 @ a a");
                    consumer.command("PLAY");
                    long deadline = System.nanoTime() + 2_000_000_000L;
                    while (Long.parseLong(consumer.command("IOSTATS").split(" ")[1]) == 0
                            && System.nanoTime() < deadline) Thread.sleep(25);
                    assertEquals("OK -", consumer.command("ROUTING_STATUS"));
                    assertTrue(Long.parseLong(consumer.command("IOSTATS").split(" ")[1]) > 0, "Capture must actually deliver frames: " + endpoint);
                    consumer.command("STOP");
                }
            }
            producer.command("STOP");
        }
    }

    @Test void childProcessAcceptsGraphAndProcessesAudioBlocks() throws Exception {
        try (var client = new AudioHostClient()) {
            client.start(AudioHostClient.defaultExecutable(), true);
            assertEquals("OK", client.command("GRAPH 3 osc osc -1 440 @ a a gain gain 0 -12 @ a a out out 1 0 @ a a"));
            assertEquals("OK", client.command("PLAY"));
            int before = Integer.parseInt(client.command("PING").substring(3));
            Thread.sleep(150);
            int after = Integer.parseInt(client.command("PING").substring(3));
            assertTrue(after > before, "DSP clock callbacks must advance");
            var load = AudioLoad.parse(client.command("STATS"));
            assertTrue(load.averagePercent() >= 0 && Double.isFinite(load.averagePercent()));
            assertTrue(load.peakPercent() >= 0 && load.overBudget() >= 0);
            assertEquals("OK", client.command("GRAPH 3 osc osc -1 880 @ a a gain gain 0 -24 @ a a out out 1 0 @ a a"));
            assertEquals("OK", client.command("GRAPH 3 hz math -1 440 -1 2 multiply 0 @ a aa osc osc 0 440 @ a a out out 1 0 @ a a"));
            assertEquals("OK", client.command("GRAPH 3 osc osc -1 440 @ a a math math 0 0 -1 0.5 multiply 1 @ a aa out out 1 0 @ a a"));
            assertThrows(IOException.class, () -> client.command("GRAPH 2 a gain 1 0 @ a a b gain 0 0 @ a a"));
            assertEquals("OK", client.command("GRAPH 2 osc additive -1 square 144 1 25 -1 440 -1 90 @ a aaa out out 0 0 @ a a"));
            int additiveBefore = Integer.parseInt(client.command("PING").substring(3));
            Thread.sleep(150);
            assertTrue(Integer.parseInt(client.command("PING").substring(3)) > additiveBefore);
            assertEquals("OK", client.command("GRAPH 0"));
            assertEquals("OK", client.command("STOP"));
            assertEquals("OK", client.command("QUIT"));
        }
    }

    @Test void missingExecutableFailsClearly() {
        try (var client = new AudioHostClient()) {
            assertThrows(IOException.class, () -> client.start(Path.of("target/missing-audio-host.exe"), true));
        }
    }
}


