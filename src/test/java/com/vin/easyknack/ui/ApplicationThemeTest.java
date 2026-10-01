package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.ui.GLThemeLoader;

class ApplicationThemeTest {
    @TempDir Path directory;
    @Test void bundledThemeLoadsTheApplicationResourceIncludingItsOverrides() {
        var theme = ApplicationTheme.load(null);
        for (var kind : NodeKind.values()) {
            assertNotEquals(Color.MAGENTA, theme.title(kind).light());
            assertNotEquals(Color.MAGENTA, theme.title(kind).dark());
        }
        var resource = GLThemeLoader.loadResource("/easyknack.gltheme");
        assertEquals(resource.sliderStyle(), theme.theme().sliderStyle());
        assertEquals(resource.values().asMap(), theme.theme().values().asMap());
        assertEquals(resource.values().getColor("easyknack.signal.AUDIO", Color.MAGENTA), theme.signal(SignalCategory.AUDIO));
    }
    @Test void externalThemeReloadsColorsAndGeometryIncludingSiblingDefault() throws Exception {
        var base = directory.resolve("default.gltheme");
        Files.writeString(base, "slider.height=23\n");
        var file = directory.resolve("custom.gltheme");
        Files.writeString(file, "theme.extends=default\n@COLOR easyknack.signal.VALUE=#654321\n");
        var first = ApplicationTheme.load(file);
        assertEquals(23f, first.theme().sliderStyle().height());
        assertEquals(new Color(0x654321), first.signal(SignalCategory.VALUE));
        Files.writeString(base, "slider.height=31\n");
        Files.writeString(file, "theme.extends=default\n@COLOR easyknack.signal.VALUE=#123456\n");
        var second = ApplicationTheme.load(file);
        assertEquals(31f, second.theme().sliderStyle().height());
        assertEquals(new Color(0x123456), second.signal(SignalCategory.VALUE));
        assertEquals(first.signal(SignalCategory.AUDIO), second.signal(SignalCategory.AUDIO));
    }
}

