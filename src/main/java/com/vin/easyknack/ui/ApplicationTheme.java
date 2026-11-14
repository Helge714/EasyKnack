package com.vin.easyknack.ui;

import java.awt.Color;
import java.nio.file.Path;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.GLNodeStyle;
import com.vingame.glswing.ui.GLTheme;
import com.vingame.glswing.ui.GLThemeLoader;

/** One standard GLSwing theme for both control geometry and application colors. */
public final class ApplicationTheme {
    public static final String DEFAULT_RESOURCE = "/easyknack-slider-mint.gltheme";
    public record TitleColors(Color light, Color dark) { }
    private final GLTheme theme;
    private final GLTheme defaults;
    private static final class Default { static ApplicationTheme INSTANCE = loadConfigured(); }
    public static ApplicationTheme current() { return Default.INSTANCE; }
    public static void activate(ApplicationTheme theme){Default.INSTANCE=theme;}
    public static ApplicationTheme loadSelection(String id){
        if(id.equals("classpath:/easyknack-slider-mint-nodes.gltheme"))id="classpath:/easyknack-slider-mint.gltheme";
        if(id.startsWith("classpath:"))return new ApplicationTheme(GLThemeLoader.loadResource(id.substring(10)),GLThemeLoader.loadResource("/easyknack.gltheme"));
        return load(id.isBlank()?null:Path.of(id));
    }
    private ApplicationTheme(GLTheme theme, GLTheme defaults) { this.theme = theme; this.defaults = defaults; }
    private static ApplicationTheme loadConfigured() {
        String file = ApplicationSettings.theme();
        if(file.startsWith("classpath:"))return loadSelection(file);
        if(!file.isBlank()&&!java.nio.file.Files.isRegularFile(Path.of(file)))file="";
        return load(file.isBlank() ? null : Path.of(file));
    }
    /** File themes are read afresh; missing application colors inherit the bundled palette. */
    public static ApplicationTheme load(Path file) {
        var defaults = GLThemeLoader.loadResource("/easyknack.gltheme");
        var selected = file == null ? GLThemeLoader.loadResource(DEFAULT_RESOURCE) : GLThemeLoader.load(file);
        System.out.println("EasyKnack theme: " + (file == null ? "classpath:" + DEFAULT_RESOURCE : file.toAbsolutePath().normalize()));
        return new ApplicationTheme(selected, defaults);
    }
    public GLTheme theme() { return theme; }
    public Color color(String key) {
        String fullKey = "easyknack." + key;
        return theme.values().getColor(fullKey, defaults.values().getColor(fullKey));
    }
    public TitleColors title(NodeKind category) {
        return new TitleColors(color("node." + category.name() + ".title.light"), color("node." + category.name() + ".title.dark"));
    }
    @Deprecated public TitleColors title(NodeCategory category) {return title(switch(category){case GENERATOR,EFFECT,IO,ROUTING,BUFFER->NodeKind.AUDIO;case MIDI->NodeKind.MIDI;case ANALYSIS->NodeKind.ANALYSIS;default->NodeKind.VALUE;});}
    public Color signal(SignalCategory category) { return color("signal." + category); }
    public GLNodeStyle nodeStyle(GLTheme base, NodeKind category) {
        var colors = title(category);
        var text = color("node.title.text");
        String hex = String.format("#%06X", text.getRGB() & 0xFFFFFF);
        return GLNodeStyle.fromTheme(base.withValues(base.values().with("node.titleText", hex)))
                .withTitleGradient(colors.light(), colors.dark());
    }
}
