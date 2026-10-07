package com.vin.easyknack;

import com.vin.easyknack.ui.WorkspaceView;
import com.vingame.glswing.application.GLApplication;
import com.vingame.glswing.application.GLBackend;
import com.vin.easyknack.ui.ApplicationTheme;

/** Desktop entry point. GLSwing owns the window and rendering lifecycle. */
public final class EasyKnack {
    private EasyKnack() { }

    public static void main(String[] args) {
        final DocumentLaunch launches;
        try {
            launches = DocumentLaunch.start(LaunchArguments.decode(args));
            if (launches == null) return;
        } catch (Exception failure) {
            failure.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, failure.getMessage(), "EasyKnack", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(launches::close, "EasyKnack instance cleanup"));
        GLApplication.builder()
                .locale(com.vin.easyknack.ui.ApplicationSettings.locale())
                .title(com.vin.easyknack.ui.AppText.literal("EasyKnack — Audio Workspace"))
                .backend(GLBackend.CANVAS)
                .size(1280, 800)
                .minimumSize(900, 600)
                .prepare(plan -> plan
                        .background(com.vin.easyknack.ui.AppText.literal("Loading EasyKnack theme"), task -> {
                            ApplicationTheme.current();
                            return null;
                        })
                        .background(com.vin.easyknack.ui.AppText.literal("Preparing audio plugins"), task -> {
                            var plugins=com.vin.easyknack.plugins.PluginManager.current();
                            var build=plugins.startup(task::detail);
                            if(build!=null)plugins.activate(build);
                            return null;
                        })
                        .background(com.vin.easyknack.ui.AppText.literal("Preparing examples"), task -> {com.vin.easyknack.ui.ExampleLibrary.initialize();return null;})
                        .awt(com.vin.easyknack.ui.AppText.text("startup.formulaEditor"), task -> {
                            com.vin.easyknack.ui.FormulaEditorPreparation.prepare();
                            return null;
                        })
                        .gl(com.vin.easyknack.ui.AppText.text("startup.formulaFont"), task -> {
                            com.vin.easyknack.ui.FormulaEditorPreparation.prepareFont(task.requireGL());
                            return null;
                        }))
                .surface(context -> {
                    context.parent().setGLTheme(ApplicationTheme.current().theme());
                    var view = new WorkspaceView(context.window());
                    view.acceptLaunches(launches, context.window());
                    return view;
                })
                .onCloseRequest(context -> context.parent().getGLChildren().stream()
                        .filter(WorkspaceView.class::isInstance).map(WorkspaceView.class::cast).findFirst()
                        .map(WorkspaceView::requestClose).orElseGet(() -> java.util.concurrent.CompletableFuture.completedFuture(true)))
                .onClose(context -> { context.parent().getGLChildren().stream()
                        .filter(WorkspaceView.class::isInstance)
                        .map(WorkspaceView.class::cast).forEach(WorkspaceView::close); launches.close(); })
                .onFailure(error -> {
                    launches.close();
                    System.err.println("EasyKnack konnte nicht gestartet werden.");
                    error.printStackTrace();
                })
                .launch();
    }
}
