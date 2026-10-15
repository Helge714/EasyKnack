package com.vin.easyknack.ui;

import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.GLNodeWorkspace;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.GLLabel;
import com.vingame.glswing.ui.GLMenuBar;
import com.vingame.glswing.ui.GLPopupMenu;

/** All workspace widgets, including the menus, are rendered on the GLCanvas. */
public final class WorkspaceView extends GLContainer implements AutoCloseable, com.vingame.glswing.input.GLKeyTarget, com.vin.easyknack.nodes.AnalysisEditor.Host {
    private javax.swing.Timer launchTimer;
    private boolean openingExternal;
    public void acceptLaunches(com.vin.easyknack.DocumentLaunch launches, JFrame window) {
        var pending = new java.util.ArrayDeque<java.nio.file.Path>();
        // Consume the first startup document before showing the default workspace.
        // Later requests still use the normal unsaved-change confirmation.
        try {
            var request = launches.poll();
            if (request != null) pending.addAll(request);
            if (!pending.isEmpty()) {
                documents.openInitial(pending.getFirst());
                pending.removeFirst();
            }
        } catch (Exception failure) {
            // Keep failed files queued so the regular open action displays its error.
            failure.printStackTrace();
        }
        launchTimer = new javax.swing.Timer(300, event -> {
            if (!window.isShowing())return;
            documents.checkRecoveryOnStartup();
            if (openingExternal || !documents.canOpenExternal()) return;
            try {
                if (pending.isEmpty()) {
                    var request = launches.poll();
                    if (request == null) return;
                    pending.addAll(request);
                    window.setExtendedState(window.getExtendedState() & ~java.awt.Frame.ICONIFIED);
                    window.toFront(); window.requestFocus();
                }
                if (!pending.isEmpty()) {
                    closeAnalysis(); openingExternal = true;
                    documents.openExternal(pending.removeFirst()).whenComplete((opened, failure) -> openingExternal = false);
                }
            } catch (Exception failure) { failure.printStackTrace(); }
        });
        launchTimer.start();
    }
    private final AnalysisFocusView analysisFocus=new AnalysisFocusView(this::closeAnalysis);
    @Override public void openAnalysis(com.vin.easyknack.nodes.AnalysisEditor.Plot source){
        workspace.setVisible(false);analysisFocus.open(source);resized(getWidth(),getHeight());
    }
    void closeAnalysis(){analysisFocus.clear();workspace.setVisible(true);requestGLFocus(workspace.editor());}
    private final GLMenuBar menuBar = new GLMenuBar();
    private final GLLabel heading = new GLLabel(com.vin.easyknack.ui.AppText.literal("EASYKNACK  /  Audio Graph"));
    private final com.vingame.glswing.ui.GLButton audioLoad = new com.vingame.glswing.ui.GLButton("DSP —");
    private final PluginActivity pluginActivity=new PluginActivity(()->resized(getWidth(),getHeight()));
    private final WorkspaceController controller = new WorkspaceController();
    private final AudioDiagnosticsPanel diagnostics=new AudioDiagnosticsPanel(()->controller.resetDiagnostics());
    private final GLNodeWorkspace workspace = controller.workspace();
    private final DocumentActions documents;
    private final GLLabel status = new GLLabel(
            com.vin.easyknack.ui.AppText.literal("Audio gestoppt · Audio → Start · Parameter im Inspector bearbeiten"));

    public WorkspaceView(JFrame window) {
        setName("EasyKnack Workspace");
        status.setVerticalAlignment(GLLabel.VerticalAlignment.CENTER);
        status.setSelectable(true);
        setGLHistoryManager(controller.history());
        controller.enableAudio(status::setText, audioLoad::setText);
        controller.setDiagnosticsListener(diagnostics::accept);
        audioLoad.setGLStyle(audioLoad.getGLStyle().withBackground(ApplicationTheme.current().theme().menuBarStyle().background()).withBorder(0,new java.awt.Color(0,0,0,0)));
        audioLoad.setToolTipText(com.vin.easyknack.ui.AppText.literal("Open render diagnostics"));
        audioLoad.addActionListener(e->toggleDiagnostics());
        documents = new DocumentActions(controller, this, window,pluginActivity::setPhase);
        var editor = workspace.editor();
        editor.setName("Audio Nodeeditor");
        editor.setZoomRange(0.25f, 2.5f);
        GLPopupMenu file = new GLPopupMenu();
        file.addItem(AppText.text("new"),event->{closeAnalysis();documents.newDocument();}).setAcceleratorText("Ctrl+N");
        file.addItem(AppText.text("open"), event -> {closeAnalysis();documents.open();}).setAcceleratorText(shortcut("Ctrl+O"));
        file.addItem(AppText.text("save"), event -> documents.save(false)).setAcceleratorText(shortcut("Ctrl+S"));
        file.addItem(AppText.text("saveAs"), event -> documents.save(true)).setAcceleratorText(shortcut("Ctrl+Shift+S"));
        var examples=new GLPopupMenu();documents.populateExamples(examples,this::closeAnalysis);
        file.addSubMenu(AppText.text("examples"),examples);
        file.addSeparator();
        file.addItem(AppText.text("image.export"),event->GraphImageExport.show(controller,this));
        file.addItem(AppText.text("group.import"),event->documents.importGroups());
        file.addSeparator();
        file.addItem(com.vin.easyknack.ui.AppText.literal("Beenden"), event -> window.dispatchEvent(
                new WindowEvent(window, WindowEvent.WINDOW_CLOSING))).setAcceleratorText("Alt+F4");
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Datei"), file);

        GLPopupMenu edit = new GLPopupMenu();
        edit.addItem(AppText.text("undo"), event -> controller.history().undo()).setAcceleratorText(shortcut("history.undo"));
        edit.addItem(AppText.text("redo"), event -> controller.history().redo()).setAcceleratorText(shortcut("history.redo"));
        edit.addSeparator();
        edit.addItem(AppText.text("copy"), event -> { if (controller.actions().canCopy()) controller.actions().copy(); }).setAcceleratorText(shortcut("node.copy"));
        edit.addItem(AppText.text("cut"), event -> { if (controller.actions().canCut()) controller.actions().cut(); }).setAcceleratorText(shortcut("node.cut"));
        edit.addItem(AppText.text("paste"), event -> controller.paste()).setAcceleratorText(shortcut("node.paste"));
        edit.addItem(AppText.text("duplicate"), event -> controller.duplicate()).setAcceleratorText(shortcut("node.duplicate"));
        edit.addSeparator();
        edit.addItem(AppText.text("delete"), event -> editor.deleteSelection()).setAcceleratorText(shortcut("node.delete-selection"));
        edit.addItem(AppText.text("exclude"), event -> {
            if(controller.actions().canExclude()) controller.actions().exclude();
        }).setAcceleratorText(shortcut("node.exclude-selection"));
        edit.addItem(AppText.text("selectAll"), event -> editor.setSelectedNodes(editor.getNodes())).setAcceleratorText(shortcut("node.select-all"));
        edit.addItem(com.vin.easyknack.ui.AppText.literal("Auswahl aufheben"), event -> editor.clearSelection());
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Bearbeiten"), edit);

        GLPopupMenu nodes = new GLPopupMenu();
        nodes.addItem(AppText.text("addNode"), event -> controller.showNodeCatalog()).setAcceleratorText(shortcut("node.create"));
        nodes.addItem(AppText.text("rename"),event->{var node=editor.getActiveNode();if(node!=null&&node.isTitleEditable())node.editTitle();}).setAcceleratorText(shortcut("node.edit-title"));
        nodes.addItem(AppText.text("move"),event->{if(!editor.getSelectedNodes().isEmpty()){var point=editor.pointerGraphPosition();editor.beginPlacement(controller.actions().beginMoveSelection(point[0],point[1]));}}).setAcceleratorText(shortcut("node.move-selection"));
        nodes.addItem(AppText.text("arrange"), event -> workspace.arrangeAll()).setAcceleratorText(shortcut("node.arrange-all"));
        nodes.addSeparator();
        nodes.addItem(AppText.text("hide"), event -> controller.actions().toggleHidden()).setAcceleratorText(shortcut("node.toggle-hidden"));
        nodes.addItem(AppText.text("mute"), event -> controller.actions().toggleMuted()).setAcceleratorText(shortcut("node.toggle-muted"));
        nodes.addSeparator();
        nodes.addItem(AppText.text("group.create"), event -> {
            if (controller.actions().canExtractComposite()) controller.actions().extractComposite();
        }).setAcceleratorText(shortcut("node.extract-composite"));
        nodes.addItem(AppText.text("group.open"), event -> {
            if (controller.actions().canOpenSelectedComposite()) controller.actions().openSelectedComposite();
        }).setAcceleratorText(shortcut("node.open-composite"));
        nodes.addItem(com.vin.easyknack.ui.AppText.literal("Export group as plugin …"), event -> documents.exportGroup());
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Nodes"), nodes);
        GLPopupMenu plugins=new GLPopupMenu();
        plugins.addItem(com.vin.easyknack.ui.AppText.literal("Install source plugin …"),event->documents.importPlugin());
        plugins.addItem(com.vin.easyknack.ui.AppText.literal("Rebuild plugins"),event->documents.rebuildPlugins());
        plugins.addItem(com.vin.easyknack.ui.AppText.literal("Open plugin sources"),event->documents.openPluginSources());
        plugins.addItem(com.vin.easyknack.ui.AppText.literal("Configure C++ compiler …"),event->documents.configurePluginCompiler());
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Plugins"),plugins);

        GLPopupMenu audio = new GLPopupMenu();
        var startAudio=audio.addItem(com.vin.easyknack.ui.AppText.literal("Start"), event -> controller.startAudio());
        var stopAudio=audio.addItem(com.vin.easyknack.ui.AppText.literal("Stop"), event -> controller.stopAudio());
        startAudio.setAcceleratorText("Ctrl+Enter");
        stopAudio.setAcceleratorText("Ctrl+Shift+Enter");
        stopAudio.setEnabled(false);
        controller.setAudioStateListener(state->{
            diagnostics.state(state);
            startAudio.setEnabled(state==com.vin.easyknack.audio.AudioHostService.State.STOPPED);
            stopAudio.setEnabled(state==com.vin.easyknack.audio.AudioHostService.State.STARTING
                    || state==com.vin.easyknack.audio.AudioHostService.State.RUNNING
                    || state==com.vin.easyknack.audio.AudioHostService.State.PAUSED);
        });
        audio.addItem(com.vin.easyknack.ui.AppText.literal("Geräte und Anwendungen aktualisieren"), event -> controller.refreshAudioEndpoints());
        audio.addItem(AudioDiagnosticsPanel.text("title","Render diagnostics"),event->toggleDiagnostics());
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Audio"), audio);

        GLPopupMenu view = new GLPopupMenu();
        view.addItem(com.vin.easyknack.ui.AppText.literal("Vergrößern"), event -> editor.zoomBy(1.2f)).setAcceleratorText("Ctrl++");
        view.addItem(com.vin.easyknack.ui.AppText.literal("Verkleinern"), event -> editor.zoomBy(1 / 1.2f)).setAcceleratorText("Ctrl+-");
        view.addItem(com.vin.easyknack.ui.AppText.literal("Ansicht zurücksetzen"), event -> editor.resetView()).setAcceleratorText("Ctrl+0");
        view.addItem(AppText.text("view.fitAll"), event -> editor.fitAllNodes()).setAcceleratorText("Ctrl+Shift+C");
        view.addItem(AppText.text("inspector"), event -> workspace.setInspectorVisible(!workspace.isInspectorVisible())).setAcceleratorText(shortcut("N"));
        view.addSeparator();
        view.addItem(com.vin.easyknack.ui.AppText.literal("Zurück"), event -> {if(analysisFocus.isVisible())closeAnalysis();else workspace.navigateBack();}).setAcceleratorText("Alt+Left");
        view.addItem(com.vin.easyknack.ui.AppText.literal("Vorwärts"), event -> workspace.navigateForward()).setAcceleratorText("Alt+Right");
        menuBar.addMenu(com.vin.easyknack.ui.AppText.literal("Ansicht"), view);

        var settings=new GLPopupMenu();settings.addItem(AppText.text("settings.preferences"),e->SettingsDialog.show(this));settings.addItem(AppText.text("document.settings"),e->DocumentSettings.show(this,controller));menuBar.addMenu(AppText.text("settings"),settings);
        menuBar.addMenu(AppText.text("help"), HelpMenu.create(this));
        addGL(workspace);
        addGL(analysisFocus);

        addGL(audioLoad);
        addGL(diagnostics);
        addGL(pluginActivity);
        addGL(status);
        addGL(menuBar);
    }

    void applySettings(){
        AppText.reload();com.vin.easyknack.nodes.NodeText.reload();
        com.vingame.glswing.component.GLParent root=this;
        while(root instanceof com.vingame.glswing.component.GLContainer c && c.getGLParent()!=null)root=c.getGLParent();
        root.setGLTheme(ApplicationTheme.current().theme());
        root.setGLLocalizer(com.vingame.glswing.ui.GLLocalizer.forLocale(ApplicationSettings.locale()));
        controller.refreshAppearance();
        LiveLanguage.refresh(this);
        audioLoad.setGLStyle(audioLoad.getGLStyle().withBackground(ApplicationTheme.current().theme().menuBarStyle().background()).withBorder(0,new java.awt.Color(0,0,0,0)));
        resized(getWidth(),getHeight());
    }
    private String shortcut(String action){
        var bindings=getGLInputMap().getKeyBindings(action);
        return bindings.isEmpty()?action:bindings.stream().map(com.vingame.glswing.input.GLKeyBinding::toDisplayString).collect(java.util.stream.Collectors.joining(" / "));
    }
    @Override protected void record(GLContext2D context) { context.fillRect(0,Math.max(34,getHeight()-32),getWidth(),32,ApplicationTheme.current().theme().menuBarStyle().background()); }
    private void toggleDiagnostics(){diagnostics.setVisible(!diagnostics.isVisible());resized(getWidth(),getHeight());}

    @Override protected void resized(float width, float height) {
        menuBar.setBounds(0, 0, width, 34);
        float bottom=Math.max(34,height-32);
        float diagnosticHeight=diagnostics.isVisible()?Math.min(300,Math.max(200,height*.45f)):0;
        diagnostics.setBounds(0,bottom-diagnosticHeight,width,diagnosticHeight);
        workspace.setBounds(0,34,width,Math.max(0,bottom-34-diagnosticHeight));
        analysisFocus.setBounds(0,34,width,Math.max(0,bottom-34-diagnosticHeight));
        float loadWidth=Math.min(460,width*.48f);
        audioLoad.setBounds(width-loadWidth-8,bottom,loadWidth,28);
        status.setBounds(12,bottom,Math.max(0,width-loadWidth-28),32);
        pluginActivity.setBounds(12,bottom,Math.max(0,width-loadWidth-28),28);
        status.setVisible(!pluginActivity.isVisible());
    }

    @Override public void keyPressed(com.vingame.glswing.input.GLKeyEvent event) {
        if (!analysisFocus.isVisible() && !event.isConsumed()
                && getGLInputMap().matches(com.vingame.glswing.input.GLInputActions.NODE_ARRANGE,event)) {
            workspace.editor().keyPressed(event); return;
        }
        if(!event.isConsumed()&&analysisFocus.isVisible()&&event.getKeyCode()==java.awt.event.KeyEvent.VK_ESCAPE){closeAnalysis();event.consume();return;}
        if (!event.isConsumed()) {
            int modifiers = event.getModifiersEx();
            int ctrl = java.awt.event.InputEvent.CTRL_DOWN_MASK, shift = java.awt.event.InputEvent.SHIFT_DOWN_MASK;
            int key=event.getKeyCode();
            if(key==java.awt.event.KeyEvent.VK_C && modifiers==(ctrl|shift) && !analysisFocus.isVisible()) {
                workspace.editor().fitAllNodes();event.consume();return;
            }
            if(key==java.awt.event.KeyEvent.VK_ENTER && (modifiers==ctrl || modifiers==(ctrl|shift))) {
                if(modifiers==ctrl)controller.startAudio();else controller.stopAudio();event.consume();return;
            }
            if(modifiers==java.awt.event.InputEvent.ALT_DOWN_MASK&&(key==java.awt.event.KeyEvent.VK_LEFT||key==java.awt.event.KeyEvent.VK_RIGHT)){
                if(key==java.awt.event.KeyEvent.VK_LEFT){if(analysisFocus.isVisible())closeAnalysis();else workspace.navigateBack();}else workspace.navigateForward();event.consume();return;
            }
            if(modifiers==ctrl){
                if(key==java.awt.event.KeyEvent.VK_PLUS||key==java.awt.event.KeyEvent.VK_ADD||key==java.awt.event.KeyEvent.VK_EQUALS){workspace.editor().zoomBy(1.2f);event.consume();return;}
                if(key==java.awt.event.KeyEvent.VK_MINUS||key==java.awt.event.KeyEvent.VK_SUBTRACT){workspace.editor().zoomBy(1/1.2f);event.consume();return;}
                if(key==java.awt.event.KeyEvent.VK_0){workspace.editor().resetView();event.consume();return;}
            }
            if (event.getKeyCode() == java.awt.event.KeyEvent.VK_S && (modifiers == ctrl || modifiers == (ctrl | shift))) {
                event.consume(); documents.save((modifiers & shift) != 0); return;
            }
            if (event.getKeyCode() == java.awt.event.KeyEvent.VK_N && modifiers == ctrl) {event.consume();closeAnalysis();documents.newDocument();return;}
            if (event.getKeyCode() == java.awt.event.KeyEvent.VK_O && modifiers == ctrl) {
                event.consume(); closeAnalysis();documents.open(); return;
            }
        }
        // Bubble only unhandled keys: typing in text fields keeps its normal behavior.
        if (!analysisFocus.isVisible() && !event.isConsumed() && event.getKeyCode() == java.awt.event.KeyEvent.VK_N
                && event.getModifiersEx() == 0) {
            workspace.setInspectorVisible(!workspace.isInspectorVisible());
            event.consume();
        }
    }

    @Override public boolean isFocusTraversable() { return false; }
    @Override public boolean preservesKeyboardFocusOnPress(){return analysisFocus.isVisible();}

    public java.util.concurrent.CompletionStage<Boolean> requestClose() { return documents.requestClose(); }
    @Override public void close() { if (launchTimer != null) launchTimer.stop(); com.vin.easyknack.audio.FileAudition.stop(); documents.close(); pluginActivity.close(); controller.close(); }
}
