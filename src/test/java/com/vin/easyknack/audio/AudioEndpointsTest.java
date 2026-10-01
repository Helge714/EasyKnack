package com.vin.easyknack.audio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.GLNodeValue;

class AudioEndpointsTest {
    @Test void asioReferencesPreserveChannelPairsAndDirection() {
        for (boolean input : new boolean[] {true, false}) {
            var endpoint = new AudioEndpoint("asio:{12345678-1234-1234-1234-123456789ABC}:" + (input ? "i:2" : "o:4"),
                    "Insert | 3–4", input ? AudioEndpoint.Type.ASIO_INPUT : AudioEndpoint.Type.ASIO_OUTPUT, input, true, false);
            var decoded = AudioEndpoint.reference(endpoint.reference());
            assertEquals(endpoint.id(), decoded.id()); assertEquals(input, decoded.input());
            assertEquals("ASIO", decoded.interfaceLabel());
            assertFalse(decoded.canRoute());
            assertDoesNotThrow(() -> new EndpointEditor(endpoint).validate(GLNodeValue.of(decoded.reference())));
            if (input) {
                var layout = new InputNode().layout(new NodeState(java.util.Map.of("endpoint", GLNodeValue.of(decoded.reference()))));
                assertFalse(layout.rows().stream().filter(r -> r.key().equals(InputNode.MODE)).findFirst().orElseThrow().isVisible());
            }
        }
        var parsed = AudioEndpoints.parse("OK 00000 1 i 6 " + AudioEndpoint.hex("asio:{TEST}:i:0") + " " + AudioEndpoint.hex("Insert") + " 1 0");
        assertEquals(AudioEndpoint.Type.ASIO_INPUT, parsed.endpoints().get(0).type());
        assertFalse(parsed.capabilities().applicationRouting());
    }
    @Test void referencePersistsIdentityAndFallbackWithoutRuntimeFlags() {
        var endpoint = new AudioEndpoint("in:{stable-guid}", "Mikrofon – Küche", AudioEndpoint.Type.HARDWARE_INPUT, true, true, false);
        var decoded = AudioEndpoint.reference(endpoint.reference());
        assertEquals(endpoint.id(), decoded.id()); assertEquals(endpoint.name(), decoded.name());
        assertFalse(decoded.available()); assertEquals(endpoint.reference(), decoded.reference());
        assertDoesNotThrow(() -> new InputNode().layout(new NodeState(java.util.Map.of("endpoint", GLNodeValue.of(decoded.reference())))));
        assertThrows(IllegalArgumentException.class, () -> new EndpointEditor(AudioEndpoint.DEFAULT).validate(GLNodeValue.of(decoded.reference())));
    }
    @Test void discoveryParsesCapabilitiesAndRejectsBrokenMessages() {
        var parsed = AudioEndpoints.parse("OK 11000 1 o 3 64656661756c74 53797374656d2044656661756c74 1 0");
        assertTrue(parsed.capabilities().applicationCapture()); assertFalse(parsed.capabilities().applicationRouting());
        assertEquals(AudioEndpoint.DEFAULT, parsed.endpoints().get(0));
        for (String invalid : new String[] {"OK", "OK 11000 -1", "OK 11000 1", "OK 11000 1 i 3 61 62 1 0"})
            assertThrows(IllegalArgumentException.class, () -> AudioEndpoints.parse(invalid));
    }
}

