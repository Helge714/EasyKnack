package com.vin.easyknack.ui;

import java.util.function.BooleanSupplier;
import com.vingame.glswing.node.*;

/** Application mute policy and grouped validation around Core editing commands. */
final class NodeEditingActions implements GLNodeEditingActions {
    @Override public void arrangeAll() { core.arrangeAll(); }
    @Override public boolean canMirror() { return core.canMirror(); }
    @Override public void setSelectedMirrored(boolean mirrored) { core.setSelectedMirrored(mirrored); }
    @Override public void toggleMirrored() { core.toggleMirrored(); }
    private final GLNodeEditingActions core;
    private final BooleanSupplier muteable;
    private final java.util.function.BiConsumer<String,Runnable> edit;
    NodeEditingActions(GLNodeEditingActions core, BooleanSupplier muteable) { this(core,muteable,(name,action)->action.run()); }
    NodeEditingActions(GLNodeEditingActions core, BooleanSupplier muteable,java.util.function.BiConsumer<String,Runnable> edit) { this.core = core; this.muteable = muteable;this.edit=edit; }
    public boolean canCopy() { return core.canCopy(); }
    public boolean canCut() { return core.canCut(); }
    public boolean canPaste() { return core.canPaste(); }
    public boolean canDuplicate() { return core.canDuplicate(); }
    public boolean canExtractComposite() { return core.canExtractComposite(); }
    public boolean canDelete() { return core.canDelete(); }
    public boolean canExclude() { return core.canExclude(); }
    public void exclude() { edit.accept("Exclude nodes",core::exclude); }
    public boolean canToggleHidden() { return core.canToggleHidden(); }
    public boolean canToggleMuted() { return core.canToggleMuted() && muteable.getAsBoolean(); }
    public boolean canOpenSelectedComposite() { return core.canOpenSelectedComposite(); }
    public void copy() { core.copy(); }
    public void cut() { edit.accept("Cut nodes",core::cut); }
    public void paste(float x, float y) { edit.accept("Paste nodes",()->core.paste(x, y)); }
    public void duplicate() { edit.accept("Duplicate nodes",core::duplicate); }
    public void extractComposite() { edit.accept("Group nodes",core::extractComposite); }
    public void delete() { edit.accept("Delete nodes",core::delete); }
    public void toggleHidden() { core.toggleHidden(); }
    public void toggleMuted() { if (canToggleMuted()) core.toggleMuted(); }
    public void openSelectedComposite() { core.openSelectedComposite(); }
    public GLNodePlacement beginPaste(float x, float y) { return core.beginPaste(x, y); }
    public GLNodePlacement beginDuplicate(float x, float y) { return core.beginDuplicate(x, y); }
    public GLNodePlacement beginMoveSelection(float x, float y) { return core.beginMoveSelection(x, y); }
    public void requestNodeCreation(float x, float y, float screenX, float screenY) { core.requestNodeCreation(x, y, screenX, screenY); }
}
