package com.vin.easyknack.ui;

/** Local dialog adapter; preview rendering and theme changes are provided by Core. */
final class ExportPreview extends com.vingame.glswing.ui.GLImagePreview {
    void image(java.awt.image.BufferedImage source) { setImage(source); }
}
