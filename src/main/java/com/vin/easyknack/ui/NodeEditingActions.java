package com.vin.easyknack.ui;

import java.util.function.BooleanSupplier;
import com.vingame.glswing.node.*;

/** Only adds the definition's mute capability; all mutations stay with the Core workspace. */
final class NodeEditingActions implements GLNodeEditingActions {
    @Override public void arrangeAll() { core.arrangeAll(); }
    private final GLNodeEditingActions core;
    private final BooleanSupplier muteable;
    NodeEditingActions(GLNodeEditingActions core, BooleanSupplier muteable) { this.core = core; this.muteable = muteable; }
    public boolean canCopy() { return core.canCopy(); }
    public boolean canCut() { return core.canCut(); }
    public boolean canPaste() { return core.canPaste(); }
    public boolean canDuplicate() { return core.canDuplicate(); }
    public boolean canExtractComposite() { return core.canExtractComposite(); }
    public boolean canDelete() { return core.canDelete(); }
    public boolean canExclude() { return core.canExclude(); }
    public void exclude() { core.exclude(); }
    public boolean canToggleHidden() { return core.canToggleHidden(); }
    public boolean canToggleMuted() { return core.canToggleMuted() && muteable.getAsBoolean(); }
    public boolean canOpenSelectedComposite() { return core.canOpenSelectedComposite(); }
    public void copy() { core.copy(); }
    public void cut() { core.cut(); }
    public void paste(float x, float y) { core.paste(x, y); }
    public void duplicate() { core.duplicate(); }
    public void extractComposite() { core.extractComposite(); }
    public void delete() { core.delete(); }
    public void toggleHidden() { core.toggleHidden(); }
    public void toggleMuted() { if (canToggleMuted()) core.toggleMuted(); }
    public void openSelectedComposite() { core.openSelectedComposite(); }
    public GLNodePlacement beginPaste(float x, float y) { return core.beginPaste(x, y); }
    public GLNodePlacement beginDuplicate(float x, float y) { return core.beginDuplicate(x, y); }
    public GLNodePlacement beginMoveSelection(float x, float y) { return core.beginMoveSelection(x, y); }
    public void requestNodeCreation(float x, float y, float screenX, float screenY) { core.requestNodeCreation(x, y, screenX, screenY); }
}
