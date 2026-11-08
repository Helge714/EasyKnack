package com.vin.easyknack.ui;

import java.util.function.Function;
import com.vingame.glswing.ui.GLMarkdown;

/** Application HTML title policy over the shared Markdown renderer. */
final class DocumentationMarkdown {
    private DocumentationMarkdown() { }
    static String render(String markdown, String language, Function<String,String> resolve) {
        return GLMarkdown.render(markdown, language, resolve).replace("</title>", " · EasyKnack</title>");
    }
}
