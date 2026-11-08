package com.vin.easyknack.ui;

import java.util.function.Consumer;
import com.vingame.glswing.ui.GLMarkdownView;

/** Application name retained for the shared Core Markdown presentation. */
final class DocumentationPage extends GLMarkdownView {
    DocumentationPage(Consumer<String> follow) { super(follow); }
}
