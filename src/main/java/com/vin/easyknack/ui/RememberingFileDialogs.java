package com.vin.easyknack.ui;

import com.vingame.glswing.file.*;
import com.vingame.glswing.component.GLParent;
import java.nio.file.*;
import java.util.concurrent.CompletionStage;

/** Independent last-used directories for each dialog, persisted by Java Preferences. */
final class RememberingFileDialogs implements AutoCloseable {
    private final GLFileDialogs delegate=GLFileDialogs.themed();
    private static final java.util.prefs.Preferences prefs=java.util.prefs.Preferences.userRoot().node("com/vin/easyknack/dialogs");
    CompletionStage<GLFileDialogResult> show(GLFileDialogRequest request,GLParent owner){
        String key=Integer.toHexString(AppText.identity(request.title()).hashCode());Path directory=request.initialDirectory();
        try{var saved=Path.of(prefs.get(key,prefs.get(Integer.toHexString(request.title().hashCode()),"")));if(Files.isDirectory(saved)&&!saved.toString().isEmpty())directory=saved;}catch(RuntimeException ignored){}
        var restored=new GLFileDialogRequest(request.title(),request.mode(),request.selectionMode(),request.multiple(),directory,request.suggestedFileName(),request.filters(),request.confirmOverwrite());
        return delegate.show(restored,owner).thenApply(result->{
            if(result.approved())try{prefs.put(key,result.selectedPath().toAbsolutePath().getParent().toString());prefs.flush();}catch(Exception ignored){}
            return result;
        });
    }
    public void close(){delegate.close();}
}
