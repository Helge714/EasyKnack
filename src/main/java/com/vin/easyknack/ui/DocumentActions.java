package com.vin.easyknack.ui;
import com.vin.easyknack.document.RecoveryStore;

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
    private final NodeDocuments files = new NodeDocuments(count->EventQueue.invokeLater(this::bufferMigrationWarning));
    private final RememberingFileDialogs dialogs = new RememberingFileDialogs();
    private final Timer titleTimer;
    private final Timer recoveryTimer;
    private final ExecutorService recoveryWorker=Executors.newSingleThreadExecutor(r->{var thread=new Thread(r,"EasyKnack autosave");thread.setPriority(Thread.MIN_PRIORITY);return thread;});
    private final CompletableFuture<RecoveryStore> recoveryStore;
    private GLNodeGraphDocument lastRecovery;
    private long recoveryGeneration;
    private RecoveryStore.Entry recoveredEntry;
    private boolean recoveryStarted,recoveryWriting,recoveryFailed,titleChecking;

    private GLNodeGraphDocument checkpoint;
    private boolean initialViewCheckpointScheduled;
    private Path path;
    private boolean busy, closed;
    private long examplesGeneration;
    private final ExecutorService examplesWorker=Executors.newSingleThreadExecutor(r->{
        var thread=new Thread(r,"EasyKnack examples");thread.setDaemon(true);thread.setPriority(Thread.MIN_PRIORITY);return thread;
    });
    private String activity="";
    private final java.util.function.Consumer<String> pluginActivity;
    private CompletableFuture<Boolean> closeDecision;

    DocumentActions(WorkspaceController controller, GLParent owner, JFrame window,java.util.function.Consumer<String> pluginActivity) {
        this(controller,owner,window,pluginActivity,Path.of(System.getProperty("user.home"),".easyknack","recovery"));
    }
    DocumentActions(WorkspaceController controller, GLParent owner, JFrame window,java.util.function.Consumer<String> pluginActivity,Path recoveryDirectory) {
        this.controller = controller; this.owner = owner; this.window = window;
        this.pluginActivity=pluginActivity;
        recoveryStore=CompletableFuture.supplyAsync(()->{try{return new RecoveryStore(recoveryDirectory);}catch(Exception e){throw new CompletionException(e);}},recoveryWorker);
        recoveryTimer=new Timer(ApplicationSettings.autosaveSeconds()*1000,event->autosave());recoveryTimer.start();
        checkpoint = NodeDocuments.comparable(controller.snapshotDocument());
        titleTimer = new Timer(700, event -> refreshTitleInBackground()); titleTimer.start();
    }
    private boolean modified() { return checkpoint==null || !checkpoint.equals(NodeDocuments.comparable(controller.snapshotDocument())); }
    /** The first themed viewport is presentation initialization, before user interaction. */
    void initializeViewCheckpoint() {
        if (initialViewCheckpointScheduled) return;
        initialViewCheckpointScheduled = true;
        var saved = checkpoint;
        if (saved == null) return; // A recovered document deliberately remains unsaved.
        EventQueue.invokeLater(() -> {
            if (closed || checkpoint != saved) return;
            var current = controller.snapshotDocument().root();
            var previous = saved.root();
            if (!current.graph().graphId().equals(previous.graph().graphId())) return;
            // Preserve the original graph/metadata checkpoint: real edits must remain dirty.
            var initialized = new com.vingame.glswing.node.document.GLNodeGraphAsset(
                    previous.id(), previous.version(), previous.kind(), previous.graph(),
                    current.viewState(), previous.metadata(), previous.compositeDescriptor());
            checkpoint = NodeDocuments.comparable(new GLNodeGraphDocument(initialized, saved.embeddedAssets(), saved.externalAssets()));
            updateTitle();
        });
    }
    boolean canOpenExternal() { return !busy && !closed && closeDecision == null; }
    /** Startup only: no user document exists to discard before the window is shown. */
    void openInitial(Path source) {
        if (window.isShowing()) throw new IllegalStateException("Initial document must load before showing the workspace");
        var document = files.read(source).toCompletableFuture().join();
        controller.openDocument(source.getFileName().toString(), document);
        path = source;
        checkpoint = NodeDocuments.comparable(controller.snapshotDocument());
        updateTitle();
    }
    CompletionStage<Boolean> openExternal(Path source) {
        var result = new CompletableFuture<Boolean>();
        if (!canOpenExternal()) return CompletableFuture.completedFuture(false);
        busy = true;
        onUi(files.read(source), (document, failure) -> {
            busy = false;
            if (failure != null) { error(GLDocumentOperation.OPEN, source, failure); result.complete(false); return; }
            onUi(requestClose(), (proceed, closeFailure) -> {
                if (!Boolean.TRUE.equals(proceed)) { result.complete(false); return; }
                try {
                    controller.openDocument(source.getFileName().toString(), document);
                    path = source; checkpoint = NodeDocuments.comparable(controller.snapshotDocument()); updateTitle();
                    result.complete(true);
                } catch (RuntimeException invalid) { error(GLDocumentOperation.OPEN, source, invalid); result.complete(false); }
            });
        });
        return result;
    }
    void checkRecoveryOnStartup(){
        if(recoveryStarted||closed)return;recoveryStarted=true;busy=true;
        onUi(recoveryStore.thenApplyAsync(store->{try{return store.scan(java.time.Instant.now(),java.time.Duration.ofDays(ApplicationSettings.recoveryDays()));}catch(Exception e){throw new CompletionException(e);}},recoveryWorker),(entries,failure)->{
            if(failure!=null){busy=false;recoveryError(failure);return;}
            if(entries.isEmpty()){busy=false;return;}
            showRecovery(entries);
        });
    }
    private void showRecovery(java.util.List<RecoveryStore.Entry> entries){
        var ref=new GLDialog[1];
        record Choice(RecoveryStore.Entry entry){public String toString(){return (entry.original()==null?AppText.text("untitled"):entry.original().toString())+" — "+java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault()).format(entry.saved());}}
        var content=new GLContainer(){
            final GLLabel hint=new GLLabel(AppText.text("recovery.hint"));
            final GLComboBox<Choice> choice=new GLComboBox<>(entries.stream().map(Choice::new).toList());
            final GLButton restore=new GLButton(AppText.text("recovery.open")),discard=new GLButton(AppText.text("recovery.discard")),later=new GLButton(AppText.text("recovery.later"));
            {addGL(hint);addGL(choice);addGL(restore);addGL(discard);addGL(later);
                later.addActionListener(e->ref[0].close());
                discard.addActionListener(e->{var selected=choice.getSelectedItem();if(selected==null)return;
                    discard.setEnabled(false);restore.setEnabled(false);
                    onUi(recoveryStore.thenAcceptAsync(store->{try{store.delete(selected.entry());}catch(Exception failure){throw new CompletionException(failure);}},recoveryWorker),(v,f)->{
                        discard.setEnabled(true);restore.setEnabled(true);if(f!=null){recoveryError(f);return;}
                        var remaining=choice.getItems().stream().filter(c->c!=selected).toList();choice.removeAllItems();remaining.forEach(choice::addItem);
                        if(remaining.isEmpty())ref[0].close();else choice.setSelectedItem(remaining.get(0));
                    });
                });
                restore.addActionListener(e->{var selected=choice.getSelectedItem();if(selected==null)return;
                    busy=false;
                    onUi(requestClose(),(proceed,ignored)->{busy=true;if(!Boolean.TRUE.equals(proceed))return;
                        try{restoreSnapshot(selected.entry());ref[0].close();busy=false;autosave();
                        }catch(Exception failure){error(GLDocumentOperation.OPEN,selected.entry().file(),failure);}
                    });
                });
            }
            protected void record(GLContext2D c){}
            protected void resized(float w,float h){hint.setBounds(20,15,w-40,42);choice.setBounds(20,65,w-40,32);later.setBounds(20,h-45,150,28);discard.setBounds(w-350,h-45,160,28);restore.setBounds(w-180,h-45,160,28);}
        };
        ref[0]=new GLDialog(AppText.text("recovery.title"),content);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(820,165);
        ref[0].closedFuture().thenRun(()->busy=false);ref[0].show(owner);
    }
    void restoreSnapshot(RecoveryStore.Entry entry){
        controller.openDocument(entry.original()==null?AppText.text("untitled"):entry.original().getFileName().toString(),entry.document());
        path=entry.original();checkpoint=null;recoveredEntry=entry;lastRecovery=null;recoveryGeneration++;updateTitle();
    }
    Path currentPath(){return path;}
    void applyRecoverySettings(){
        int interval=ApplicationSettings.autosaveSeconds()*1000;
        recoveryTimer.setDelay(interval);recoveryTimer.setInitialDelay(interval);recoveryTimer.restart();
    }
    private void autosave(){
        if(closed||busy||recoveryWriting||!window.isShowing())return;
        try{
            var captured=controller.captureDocument();var saved=checkpoint;var previous=lastRecovery;
            recoveryWriting=true;var generation=recoveryGeneration;var original=path;var inherited=recoveredEntry;
            onUi(recoveryStore.thenApplyAsync(store->{try{
                var snapshot=WorkspaceController.prepareDocumentSnapshot(captured);var comparable=NodeDocuments.comparable(snapshot);
                if(comparable.equals(saved)){store.clear();return (GLNodeGraphDocument)null;}
                if(comparable.equals(previous)){store.touch();return comparable;}
                store.write(snapshot,original);if(inherited!=null)store.delete(inherited);return comparable;
            }catch(Exception failure){throw new CompletionException(failure);}},recoveryWorker),(comparable,f)->{
                recoveryWriting=false;if(f!=null){recoveryError(f);return;}
                if(generation==recoveryGeneration){lastRecovery=comparable;if(recoveredEntry==inherited)recoveredEntry=null;}
            });
        }catch(Exception failure){recoveryWriting=false;recoveryError(failure);}
    }
    private void refreshTitleInBackground(){
        if(closed||busy||titleChecking)return;
        var captured=controller.captureDocument();var saved=checkpoint;long generation=recoveryGeneration;
        titleChecking=true;
        onUi(CompletableFuture.supplyAsync(()->saved==null||!saved.equals(NodeDocuments.comparable(WorkspaceController.prepareDocumentSnapshot(captured))),recoveryWorker),(dirty,failure)->{
            titleChecking=false;if(failure!=null){failure.printStackTrace();return;}
            if(generation==recoveryGeneration&&checkpoint==saved)renderTitle(dirty);
        });
    }
    private void clearRecovery(){
        recoveryGeneration++;var inherited=recoveredEntry;recoveredEntry=null;lastRecovery=null;
        recoveryStore.thenAcceptAsync(store->{try{store.clear();if(inherited!=null)store.delete(inherited);}catch(Exception failure){throw new CompletionException(failure);}},recoveryWorker)
            .exceptionally(failure->{EventQueue.invokeLater(()->recoveryError(failure));return null;});
    }
    private void recoveryError(Throwable failure){
        failure.printStackTrace();if(!closed&&!recoveryFailed){recoveryFailed=true;error(GLDocumentOperation.SAVE,Path.of(System.getProperty("user.home"),".easyknack","recovery"),failure);}
    }
    private void updateTitle() {
        renderTitle(modified());
    }
    private void renderTitle(boolean dirty){
        if (!closed) window.setTitle((dirty ? "* " : "") + (path == null ? AppText.text("untitled") : path.getFileName()) + " — EasyKnack"+(activity.isEmpty()?"":" — "+activity));
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
        if(closed)return;
        long generation=++examplesGeneration;
        boolean experimental=controller.experimentalFeatures;
        menu.removeAllItems();menu.addItem(AppText.text("examples.loading"),event->{}).setEnabled(false);
        onUi(CompletableFuture.supplyAsync(()->{
            try{
                ExampleLibrary.initialize();
                return ExampleLibrary.scan(ApplicationSettings.examples(),experimental);
            }catch(Exception failure){throw new CompletionException(failure);}
        },examplesWorker),(entries,failure)->{
            if(generation!=examplesGeneration)return;
            if(experimental!=controller.experimentalFeatures){populateExamples(menu,beforeOpen);return;}
            menu.removeAllItems();
            if(failure==null)addExamples(menu,entries,beforeOpen);
            else menu.addItem(AppText.text("examples.failed"),event->error(GLDocumentOperation.OPEN,ApplicationSettings.examples(),failure));
            menu.addSeparator();
            menu.addItem(AppText.text("examples.refresh"),event->populateExamples(menu,beforeOpen));
            menu.addItem(AppText.text("examples.folder"),event->exampleFolder());
        });
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
        var request = GLFileDialogRequest.open().title(com.vin.easyknack.i18n.AppText.literal("Node-Dokument öffnen"))
                .filter(com.vin.easyknack.i18n.AppText.literal("EasyKnack (*.knack)"), "knack").filter(com.vin.easyknack.i18n.AppText.literal("Ältere Node-Dokumente (*.glgraph)"), "glgraph");
        if (path != null) request.initialDirectory(path.getParent());
        onUi(dialogs.show("document.open",request.build(), owner), (selection, failure) -> {
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
            var request = GLFileDialogRequest.save().title(com.vin.easyknack.i18n.AppText.literal("Node-Dokument speichern"))
                    .filter(com.vin.easyknack.i18n.AppText.literal("EasyKnack (*.knack)"), "knack")
                    .suggestedFileName(saveName(path));
            if (path != null) request.initialDirectory(path.getParent());
            onUi(dialogs.show("document.save",request.build(), owner), (selection, failure) -> {
                if (failure != null || !selection.approved()) {
                    busy = false;
                    if (failure != null) error(GLDocumentOperation.SAVE, path, failure);
                    result.complete(false); return;
                }
                if (!isKnack(selection.selectedPath())) {
                    busy = false;
                    error(GLDocumentOperation.SAVE, selection.selectedPath(), new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Bitte die Dateiendung .knack verwenden.")));
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
        onUi(files.write(destination, DocumentSettings.forSave(snapshot)), (saved, failure) -> {
            busy = false;
            if (failure != null) { error(GLDocumentOperation.SAVE, destination, failure); result.complete(false); return; }
            clearRecovery();path = saved; checkpoint = NodeDocuments.comparable(snapshot); updateTitle(); result.complete(true);
        });
    }
    void exportGroup() {
        if(busy||closed)return;
        final GroupSourceExporter.Export exported;
        try{exported=controller.exportSelectedGroup();}
        catch(RuntimeException failure){error(GLDocumentOperation.SAVE,null,failure);return;}
        busy=true;
        var request=GLFileDialogRequest.save().title(com.vin.easyknack.i18n.AppText.literal("Export group as plugin")).filter(com.vin.easyknack.i18n.AppText.literal("EasyKnack source plugin (*.knackplugin)"),"knackplugin").suggestedFileName(exported.suggestedFileName());
        onUi(dialogs.show("plugin.export",request.build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.SAVE,null,failure);return;}
            var destination=selection.selectedPath();
            onUi(CompletableFuture.runAsync(()->{try{exported.write(destination);}catch(java.io.IOException e){throw new CompletionException(e);}}),(ignored,writeFailure)->{
                busy=false;if(writeFailure!=null)error(GLDocumentOperation.SAVE,destination,writeFailure);
            });
        });
    }
    void importPlugin() {
        if(busy||closed)return;busy=true;
        var request=GLFileDialogRequest.open().title(com.vin.easyknack.i18n.AppText.literal("Install source plugin")).filter(com.vin.easyknack.i18n.AppText.literal("EasyKnack source plugin"), "knackplugin", "zip");
        onUi(dialogs.show("plugin.install",request.build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.OPEN,null,failure);return;}
            buildPlugin(selection.selectedPath());
        });
    }
    void importGroups(){
        if(busy||closed)return;busy=true;
        onUi(dialogs.show("group.import",GLFileDialogRequest.open().title(AppText.text("group.import.title")).filter(com.vin.easyknack.i18n.AppText.literal("EasyKnack"),"knack","glgraph").build(),owner),(selection,failure)->{
            if(failure!=null||!selection.approved()){busy=false;if(failure!=null)error(GLDocumentOperation.OPEN,null,failure);return;}
            onUi(files.read(selection.selectedPath()),(document,readFailure)->{
                busy=false;if(readFailure!=null){error(GLDocumentOperation.OPEN,selection.selectedPath(),readFailure);return;}
                var ref=new GLDialog[1];
                var panel=new GroupImportPanel(document,ids->controller.importGroups(GroupImport.copyMany(document,ids,selection.selectedPath().getFileName().toString().replaceFirst("\\.[^.]+$",""))),()->ref[0].close());
                ref[0]=new GLDialog(AppText.text("group.import.title"),panel);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(620,420);ref[0].show(owner);
            });
        });
    }
    void rebuildPlugins() { if(!busy&&!closed){busy=true;buildPlugin(null);} }
    private void buildPlugin(Path archive) {
        activity=com.vin.easyknack.i18n.AppText.literal("Preparing plugin installation");pluginActivity.accept(activity);updateTitle();
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
            notice(installation.restartRequired()?com.vin.easyknack.i18n.AppText.literal("Plugin built. Restart EasyKnack to activate the updated nodes."):com.vin.easyknack.i18n.AppText.literal("Plugin installed. Its nodes are available in the Add menu."));
        });
    }
    void openPluginSources() {
        try { var directory=com.vin.easyknack.plugins.PluginManager.current().sourcesDirectory();java.nio.file.Files.createDirectories(directory);java.awt.Desktop.getDesktop().open(directory.toFile()); }
        catch(Exception failure){error(GLDocumentOperation.OPEN,null,failure);}
    }
    void configurePluginCompiler() {
        if(busy||closed)return;busy=true;
        var request=GLFileDialogRequest.open().title(com.vin.easyknack.i18n.AppText.literal("Select LLVM-MinGW clang++.exe")).filter(com.vin.easyknack.i18n.AppText.literal("C++ compiler"), "exe");
        onUi(dialogs.show("compiler.select",request.build(),owner),(selection,failure)->{
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
            final GLLabel label=new GLLabel(text);final GLButton ok=new GLButton(com.vin.easyknack.i18n.AppText.literal("OK"));
            {addGL(label);addGL(ok);ok.addActionListener(e->reference[0].close());}
            protected void record(GLContext2D context){}
            protected void resized(float w,float h){label.setBounds(20,20,w-40,32);ok.setBounds(w-100,h-50,80,30);}
        };
        var dialog=new GLDialog(com.vin.easyknack.i18n.AppText.literal("Plugins"),content);reference[0]=dialog;dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(760,135);dialog.show(owner);
    }
    CompletionStage<Boolean> requestClose() {
        if (closeDecision != null) return closeDecision;
        if (busy) return CompletableFuture.completedFuture(false);
        if (!modified()) {clearRecovery();return CompletableFuture.completedFuture(true);}
        var result = new CompletableFuture<Boolean>();
        closeDecision = result;
        result.whenComplete((value, failure) -> closeDecision = null);
        var saving = new boolean[1];
        var dialogRef = new GLDialog[1];
        var content = new GLContainer() {
            final GLLabel label = new GLLabel(com.vin.easyknack.i18n.AppText.literal("Ungespeicherte Änderungen speichern?"));
            final GLButton save = new GLButton(com.vin.easyknack.i18n.AppText.literal("Speichern")), discard = new GLButton(com.vin.easyknack.i18n.AppText.literal("Verwerfen")), cancel = new GLButton(com.vin.easyknack.i18n.AppText.literal("Abbrechen"));
            final GLDialogButtonBar buttons = new GLDialogButtonBar().addButton(cancel).addButton(discard).addButton(save);
            {
                addGL(label); addGL(buttons);
                cancel.addActionListener(event -> { result.complete(false); dialogRef[0].close(); });
                discard.addActionListener(event -> { clearRecovery();result.complete(true); dialogRef[0].close(); });
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
        var dialog = new GLDialog(com.vin.easyknack.i18n.AppText.literal("Dokument schließen"), content); dialogRef[0] = dialog;
        dialog.setDecoration(GLDialog.Decoration.THEMED); dialog.setSize(520, 150); dialog.setResizable(false);
        dialog.closedFuture().thenRun(() -> { if (!saving[0]) result.complete(false); });
        dialog.show(owner);
        return result;
    }
    private void error(GLDocumentOperation operation, Path file, Throwable failure) {
        while (failure instanceof CompletionException && failure.getCause() != null) failure = failure.getCause();
        GLDocumentDialogs.showFailure(owner, GLDocumentFailure.of(operation, file, failure));
    }
    private void bufferMigrationWarning(){
        if(closed)return;
        var ref=new GLDialog[1];
        var content=new GLContainer(){
            final GLLabel text=new GLLabel(AppText.text("buffer.context.migration"));
            final GLLabel detail=new GLLabel(AppText.text("buffer.context.migration.detail"));
            final GLButton ok=new GLButton("OK");
            {addGL(text);addGL(detail);addGL(ok);ok.addActionListener(e->ref[0].close());}
            protected void record(GLContext2D c){}
            protected void resized(float w,float h){text.setBounds(20,20,w-40,30);detail.setBounds(20,52,w-40,30);ok.setBounds(w-100,h-45,80,28);}
        };
        ref[0]=new GLDialog(AppText.text("buffer.context.migration.title"),content);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(880,155);ref[0].show(owner);
    }
    private <T> void onUi(CompletionStage<T> stage, BiConsumer<T, Throwable> action) {
        stage.whenComplete((value, failure) -> EventQueue.invokeLater(() -> { if (!closed) action.accept(value, failure); }));
    }
    @Override public void close() { closed = true; examplesWorker.shutdownNow(); titleTimer.stop();recoveryTimer.stop(); dialogs.close(); files.close();
        recoveryWorker.submit(()->{try{recoveryStore.join().close();}catch(Exception e){e.printStackTrace();}});
        recoveryWorker.shutdown();try{recoveryWorker.awaitTermination(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();} }
}
