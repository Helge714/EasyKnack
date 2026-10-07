package com.vin.easyknack.plugins;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Notices travel with SDK and generated source archives, not only the application. */
public final class PluginLicensing {
    private PluginLicensing() { }

    public static Map<String, String> files() {
        try (var input = PluginLicensing.class.getResourceAsStream("/META-INF/easyknack/legal/LICENSE")) {
            if (input == null) throw new IOException("EasyKnack license resource is missing");
            return Map.of("LICENSE", new String(input.readAllBytes(), StandardCharsets.UTF_8),
                    "LICENSING.md", """
                    # Source and SDK licensing

                    EasyKnack-authored SDK code and copied implementation/template material
                    are provided under GPL-3.0-only; see LICENSE. There is no additional
                    plugin/linking exception. Preserve existing third-party notices.

                    This notice does not claim ownership of your graphs, recordings or
                    independently authored material. Check the licenses of code included
                    in or linked by your plugin before distributing it. Generated output
                    is not automatically exempt from the licenses of copied SDK code.

                    The SDK retains ASIO's LICENSE.txt and COPYING (GPLv3 option) and
                    miniaudio's license in its header (MIT-0 option). GLSwing is a separate
                    Apache-2.0 dependency and is not included in this source archive.
                    EasyKnack project: https://github.com/Helge97/EasyKnack
                    """);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }
}
