package com.vin.easyknack.ui;

import com.vingame.glswing.rendering.GLImageExportDimensions;

/** Local name for the shared, application-independent export geometry model. */
final class ExportDimensions extends GLImageExportDimensions {
    ExportDimensions(double width, double height, double padding) {
        super(width, height, padding);
    }
}
