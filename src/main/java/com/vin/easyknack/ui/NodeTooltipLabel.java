package com.vin.easyknack.ui;

import com.vingame.glswing.ui.GLLabel;
import com.vingame.glswing.layout.GLInsets;
import com.vingame.glswing.rendering.GLFont;

/** Application translation and the framed step-row inset policy. */
final class NodeTooltipLabel extends GLLabel {
    private final boolean framed;
    NodeTooltipLabel(String text) { this(text, false); }
    NodeTooltipLabel(String text, boolean right) { this(text, right, false); }
    NodeTooltipLabel(String text, boolean right, boolean framed) {
        super(text);
        this.framed = framed;
        setAlignment(right ? Alignment.RIGHT : Alignment.LEFT);
        setVerticalAlignment(VerticalAlignment.CENTER);
    }
    void refreshLanguage() { setText(AppText.translateExisting(getText())); }
    @Override protected GLInsets textPadding(GLFont font) {
        var padding = super.textPadding(font);
        if (!framed) return padding;
        float top = padding.top() + Math.max(0, getHeight() - padding.vertical() - font.getLineHeight()) * .5f;
        return new GLInsets(padding.top(), Math.max(padding.left(), top), padding.bottom(), padding.right());
    }
}
