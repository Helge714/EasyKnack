package com.vin.easyknack.ui;

import java.awt.EventQueue;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import javax.swing.JFrame;
import javax.swing.Timer;
import com.vingame.glswing.component.*;
import com.vingame.glswing.document.*;
import com.vingame.glswing.file.*;
import com.vingame.glswing.node.document.GLNodeGraphDocument;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.*;

/** Single-document file actions; all live workspace access stays on the EDT. */
final class DocumentActions implements AutoCloseable {
    private final WorkspaceController controller;
    private final GLParent owner;
    private final JFrame window;
    private final NodeDocuments files = new NodeDocuments();
    private final RememberingFileDialogs dialogs = new RememberingFileDialogs();
    private final Timer titleTimer;
    private GLNodeGraphDocument checkpoint;
    private Path path;
    private boolean busy, closed;
    private String activity="";
    private final java.util.function.Consumer<String> pluginActivity;
    private CompletableFuture<Boolean> closeDecision;

    DocumentActions(WorkspaceController controller, GLParent owner, JFrame window,java.util.function.Consumer<String> pluginActivity) {
        this.controller = controller; this.owner = owner; this.window = window;
        this.pluginActivity=pluginActivity;
        checkpoint = NodeDocuments.comparable(controller.snapshotDocument());
        titleTimer = new Timer(700, event -> updateTitle()); titleTimer.start();
    }
    private boolean modified() { return !checkpoint.equals(NodeDocuments.comparable(controller.snapshotDocument())); }
    private void updateTitle() {
        if (!closed) window.setTitle((modified() ? "* " : "") + (path == null ? AppText.text("untitled") : path.getFileName()) + " — EasyKnack"+(activity.isEmpty()?"":" — "+activity));
    }
    void openHarmonizerExample() {
        openExample("Harmonizer",HarmonizerExample::create);
    }
    void newDocument(){openExample(AppText.text("untitled"),StarterDocument::create);}
    void openExample(Path source){
        if(busy||closed)return;busy=true;
        onUi(files.read(source),(document,failure)->{busy=false;if(failure!=null){error(GLDocumentOperation.OPEN,source,failure);return;}openExample(source.getFileName().toString(),()->document);});
    }
    void exampleFolder(){try{java.nio.file.Files.createDirectories(ApplicationSettings.examples());java.awt.Desktop.getDesktop().open(ApplicationSettings.examples().toFile());}catch(Exception e){error(GLDocumentOperation.OPEN,null,e);}}
    void populateExamples(GLPopupMenu menu,Runnable beforeOpen){
        menu.removeAllItems();
        try{addExamples(menu,ExampleLibrary.scan(ApplicationSettings.examples()),beforeOpen);}catch(Exception e){menu.addItem(AppText.text("examples.failed"),event->error(GLDocumentOperation.OPEN,ApplicationSettings.examples(),e));}
        menu.addSeparator();menu.addItem(AppText.text("examples.refresh"),e->populateExamples(menu,beforeOpen));menu.addItem(AppText.text("examples.folder"),e->exampleFolder());
    }
    private void addExamples(GLPopupMenu menu,java.util.List<ExampleLibrary.Entry> entries,Runnable beforeOpen){
        for(var entry:entries)if(entry.directory()){var child=new GLPopupMenu();addExamples(child,entry.children(),beforeOpen);menu.addSubMenu(entry.name(),child);}else menu.addItem(entry.name(),e->{beforeOpen.run();openExample(entry.path());});
    }
    void openSpectralExample() {
        openExample("Spectral processing",SpectralExample::create);
    }
    void openSpectralResynthesisExample(){openExample("Spectral resynthesis",SpectralResynthesisExample::create);}
    private void openExample(String title,java.util.function.Supplier<GLNodeGraphDocument> example) {
        if (busy || closed) return;
        onUi(requestClose(), (proceed, failure) -> {
            if (!Boolean.TRUE.equals(proceed) || closed) return;
            try {
                controller.openDocument(title, example.get());
                path = null; checkpoint = NodeDocuments.comparable(controller.snapshotDocument()); updateTitle();
            } catch (RuntimeException invalid) { error(GLDocumentOperation.OPEN, null, invalid); }
        });
    }
    void open() {
        if (busy || closed) return;
        busy = true;
        var request = GLFileDialogRequest.open().title(com.vin.easyknack.ui.AppText.literal("Node-Dokument öffnen"))
                .filter(com.vin.easyknack.ui.AppText.literal("EasyKnack (*.knack)"), "knack").filter(com.vin.easyknack.ui.AppText.literal("Ältere Node-Dokumente (*.glgraph)"), "glgraph");
        if (path != null) request.initialDirectory(path.getParent());
        onUi(dialogs.show(request.build(), owner), (selection, failure) -> {
            if (failure != null) { busy = false; error(GLDocumentOperation.OPEN, null, failure); return; }
            if (!selection.approved()) { busy = false; return; }
            var selected = selection.selectedPath();
            onUi(files.read(selected), (document, readFailure) -> {
                busy = false;
                if (readFailure != null) { error(GLDocumentOperation.OPEN, selected, readFailure); return; }
                onUi(requestClose(), (proceed, closeFailure) -> {
                    if (!Boolean.TRUE.equals(proceed)) return;
                    try {
                        controller.openDocument(selected.getFileName().toString(), document);
                        path = selected; checkpoint = NodeDocuments.comparable(controller.snapshotDocument()); updateTitle();
                    } catch (RuntimeException invalid) { error(GLDocumentOperation.OPEN, selected, invalid); }
                });
            });
        });
    }
    CompletionStage<Boolean> save(boolean saveAs) {
        if (busy || closed) return CompletableFuture.completedFuture(false);
        busy = true;
        var result = new CompletableFuture<Boolean>();
        if (!saveAs && path != null && isKnack(path)) write(path, result);
        else {
            var request = GLFileDialogRequest.save().title(com.vin.easyknack.ui.AppText.literal("Node-Dokument speichern"))
                    .filter(com.vin.easyknack.ui.AppText.literal("EasyKnack (*.knack)"), "knack")
                    .suggestedFileName(saveName(path));
            if (path != null) request.initialDirectory(path.getParent());
            onUi(dialogs.show(request.build(), owner), (selection, failure) -> {
                if (failure != null || !selection.approved()) {
                    busy = false;
                    if (failure != null) error(GLDocumentOperation.SAVE, path, failure);
                    result.complete(false); return;
                }
                if (!isKnack(selection.selectedPath())) {
                    busy = false;
                    error(GLDocumentOperation.SAVE, selection.selectedPath(), new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Bitte die Dateiendung .knack verwenden.")));
                    result.complete(false); return;
                }
                write(selection.selectedPath(), result);
            });
        }
        return result;
    }
    private static boolean isKnack(Path path) { return path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".knack"); }
    static String saveName(Path path) {
        if (path == null) return AppText.text("untitled")+".knack";
        String name = path.getFileName().toString();
        if (isKnack(path)) return name;
        int dot = name.lastIndexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name) + ".knack";
    }
    private void write(Path destination, CompletableFuture<Boolean> result) {
        final GLNodeGraphDocument snapshot;
        try { snapshot = controller.snapshotDocument(); }
        catch (RuntimeException failure) { busy = false; error(GLDocumentOperation.SAVE, destination, failure); result.complete(false); return; }
        onUi(files.write(destination, snapshot), (saved, failure) -> {
            busy = false;
            if (failure != null) { error(GLDocumentOperation.SAVE, destination, failure); result.complete(false); return; }
            path = saved; checkpoint = NodeDocuments.comparable(snapshot); updateTitle(); result.complete(true);
        });
    }
    void exportGroup() {
        if(busy||closed)return;
        final GroupSourceExporter.Export exported;
        try{exported=controller.exportSelectedGroup();}
        catch(RuntimeException failure){error(GLDocumentOperation.SAVE,null,failure);return;}
        busy=true;
        var request=GLFileDialogRequest.save().title(com.vin.easyknack.ui.AppText.literal("Export group as plugin")).filter(com.vin.easyknack.ui.AppText.literal("EasyKnack source plugin (*.knackplugin)"),"knackplugin").suggestedFileName(exported.suggestedFileName());
        onUi(dialogs.show(request.build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.SAVE,null,failure);return;}
            var destination=selection.selectedPath();
            onUi(CompletableFuture.runAsync(()->{try{exported.write(destination);}catch(java.io.IOException e){throw new CompletionException(e);}}),(ignored,writeFailure)->{
                busy=false;if(writeFailure!=null)error(GLDocumentOperation.SAVE,destination,writeFailure);
            });
        });
    }
    void importPlugin() {
        if(busy||closed)return;busy=true;
        var request=GLFileDialogRequest.open().title(com.vin.easyknack.ui.AppText.literal("Install source plugin")).filter(com.vin.easyknack.ui.AppText.literal("EasyKnack source plugin"), "knackplugin", "zip");
        onUi(dialogs.show(request.build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.OPEN,null,failure);return;}
            buildPlugin(selection.selectedPath());
        });
    }
    void importGroups(){
        if(busy||closed)return;busy=true;
        onUi(dialogs.show(GLFileDialogRequest.open().title(AppText.text("group.import.title")).filter(com.vin.easyknack.ui.AppText.literal("EasyKnack"),"knack","glgraph").build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.OPEN,null,failure);return;}
            onUi(files.read(selection.selectedPath()),(document,readFailure)->{
                busy=false;if(readFailure!=null){error(GLDocumentOperation.OPEN,selection.selectedPath(),readFailure);return;}
                record Choice(String id,String label){public String toString(){return label;}}
                var choices=new java.util.ArrayList<Choice>();choices.add(new Choice(document.root().id(),AppText.text("group.import.root")));
                document.embeddedAssets().values().stream().filter(a->a.compositeDescriptor()!=null).sorted(java.util.Comparator.comparing(a->new com.vin.easyknack.nodes.NodeState(a.metadata()).text("easyknack.title",a.id()))).forEach(a->choices.add(new Choice(a.id(),new com.vin.easyknack.nodes.NodeState(a.metadata()).text("easyknack.title",a.id()))));
                var ref=new GLDialog[1];var panel=new GLContainer(){
                    final GLComboBox<Choice> choice=new GLComboBox<>(choices);final GLButton add=new GLButton(AppText.text("group.import.action")),cancel=new GLButton(AppText.text("cancel"));final GLLabel message=new GLLabel("");
                    {addGL(choice);addGL(add);addGL(cancel);addGL(message);cancel.addActionListener(e->ref[0].close());add.addActionListener(e->{try{controller.importGroup(GroupImport.copy(document,choice.getSelectedItem().id(),selection.selectedPath().getFileName().toString().replaceFirst("\\.[^.]+$","")));ref[0].close();}catch(Exception invalid){message.setText(invalid.getMessage());}});}
                    protected void record(GLContext2D c){}
                    protected void resized(float w,float h){choice.setBounds(20,20,w-40,32);message.setBounds(20,62,w-40,42);cancel.setBounds(w-235,h-45,100,28);add.setBounds(w-125,h-45,105,28);}
                };ref[0]=new GLDialog(AppText.text("group.import.title"),panel);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(620,165);ref[0].show(owner);
            });
        });
    }
    void rebuildPlugins() { if(!busy&&!closed){busy=true;buildPlugin(null);} }
    private void buildPlugin(Path archive) {
        activity=com.vin.easyknack.ui.AppText.literal("Preparing plugin installation");pluginActivity.accept(activity);updateTitle();
        onUi(CompletableFuture.supplyAsync(()->{
            try {
                var manager=com.vin.easyknack.plugins.PluginManager.current();
                java.util.function.Consumer<String> progress=text->EventQueue.invokeLater(()->{if(!closed){activity=text;pluginActivity.accept(text);updateTitle();}});
                return archive==null?manager.rebuild(progress):manager.install(archive,progress);
            }catch(Exception e){throw new CompletionException(e);}
        }),(installation,failure)->{
            busy=false;activity="";pluginActivity.accept("");updateTitle();
            if(failure!=null){error(GLDocumentOperation.OPEN,archive,failure);return;}
            if(!installation.restartRequired())controller.pluginsInstalled(installation.build());
            notice(installation.restartRequired()?com.vin.easyknack.ui.AppText.literal("Plugin built. Restart EasyKnack to activate the updated nodes."):com.vin.easyknack.ui.AppText.literal("Plugin installed. Its nodes are available in the Add menu."));
        });
    }
    void openPluginSources() {
        try { var directory=com.vin.easyknack.plugins.PluginManager.current().sourcesDirectory();java.nio.file.Files.createDirectories(directory);java.awt.Desktop.getDesktop().open(directory.toFile()); }
        catch(Exception failure){error(GLDocumentOperation.OPEN,null,failure);}
    }
    void configurePluginCompiler() {
        if(busy||closed)return;busy=true;
        var request=GLFileDialogRequest.open().title(com.vin.easyknack.ui.AppText.literal("Select LLVM-MinGW clang++.exe")).filter(com.vin.easyknack.ui.AppText.literal("C++ compiler"), "exe");
        onUi(dialogs.show(request.build(),owner),(selection,failure)->{
            busy=false;
            if(failure!=null){error(GLDocumentOperation.OPEN,null,failure);return;}
            if(!selection.approved())return;
            try {
                var path=com.vin.easyknack.plugins.PluginManager.current().sourcesDirectory().getParent().resolve("toolchain.properties");
                java.nio.file.Files.createDirectories(path.getParent());var properties=new java.util.Properties();
                if(java.nio.file.Files.exists(path))try(var in=java.nio.file.Files.newBufferedReader(path)){properties.load(in);}
                properties.setProperty("cxx",selection.selectedPath().toAbsolutePath().toString());
                try(var out=java.nio.file.Files.newBufferedWriter(path)){properties.store(out,"EasyKnack plugin toolchain");}
            }catch(Exception e){error(GLDocumentOperation.SAVE,null,e);}
        });
    }
    private void notice(String text) {
        var reference=new GLDialog[1];
        var content=new GLContainer(){
            final GLLabel label=new GLLabel(text);final GLButton ok=new GLButton(com.vin.easyknack.ui.AppText.literal("OK"));
            {addGL(label);addGL(ok);ok.addActionListener(e->reference[0].close());}
            protected void record(GLContext2D context){}
            protected void resized(float w,float h){label.setBounds(20,20,w-40,32);ok.setBounds(w-100,h-50,80,30);}
        };
        var dialog=new GLDialog(com.vin.easyknack.ui.AppText.literal("Plugins"),content);reference[0]=dialog;dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(760,135);dialog.show(owner);
    }
    CompletionStage<Boolean> requestClose() {
        if (closeDecision != null) return closeDecision;
        if (busy) return CompletableFuture.completedFuture(false);
        if (!modified()) return CompletableFuture.completedFuture(true);
        var result = new CompletableFuture<Boolean>();
        closeDecision = result;
        result.whenComplete((value, failure) -> closeDecision = null);
        var saving = new boolean[1];
        var dialogRef = new GLDialog[1];
        var content = new GLContainer() {
            final GLLabel label = new GLLabel(com.vin.easyknack.ui.AppText.literal("Ungespeicherte Änderungen speichern?"));
            final GLButton save = new GLButton(com.vin.easyknack.ui.AppText.literal("Speichern")), discard = new GLButton(com.vin.easyknack.ui.AppText.literal("Verwerfen")), cancel = new GLButton(com.vin.easyknack.ui.AppText.literal("Abbrechen"));
            final GLDialogButtonBar buttons = new GLDialogButtonBar().addButton(cancel).addButton(discard).addButton(save);
            {
                addGL(label); addGL(buttons);
                cancel.addActionListener(event -> { result.complete(false); dialogRef[0].close(); });
                discard.addActionListener(event -> { result.complete(true); dialogRef[0].close(); });
                save.addActionListener(event -> {
                    // Hide the confirmation before opening a file picker.
                    saving[0] = true;
                    dialogRef[0].close();
                    onUi(DocumentActions.this.save(false), (saved, failure) -> {
                        if (!Boolean.TRUE.equals(saved)) result.complete(false);
                        else { closeDecision = null; onUi(requestClose(), (ready, ignored) -> result.complete(Boolean.TRUE.equals(ready))); }
                    });
                });
            }
            @Override protected void record(GLContext2D context) { }
            @Override protected void resized(float width, float height) {
                label.setBounds(20, 20, width - 40, 30); buttons.setBounds(20, height - 55, width - 40, 32);
            }
        };
        var dialog = new GLDialog(com.vin.easyknack.ui.AppText.literal("Dokument schließen"), content); dialogRef[0] = dialog;
        dialog.setDecoration(GLDialog.Decoration.THEMED); dialog.setSize(520, 150); dialog.setResizable(false);
        dialog.closedFuture().thenRun(() -> { if (!saving[0]) result.complete(false); });
        dialog.show(owner);
        return result;
    }
    private void error(GLDocumentOperation operation, Path file, Throwable failure) {
        while (failure instanceof CompletionException && failure.getCause() != null) failure = failure.getCause();
        GLDocumentDialogs.showFailure(owner, GLDocumentFailure.of(operation, file, failure));
    }
    private <T> void onUi(CompletionStage<T> stage, BiConsumer<T, Throwable> action) {
        stage.whenComplete((value, failure) -> EventQueue.invokeLater(() -> { if (!closed) action.accept(value, failure); }));
    }
    @Override public void close() { closed = true; titleTimer.stop(); dialogs.close(); files.close(); }
}
