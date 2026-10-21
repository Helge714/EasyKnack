# Workspace and documents

[User guide](README.md)

## Build and edit a graph

Choose **Nodes → Add Node** and search by name or browse a category. Menu categories
organize tasks; a node's colour identifies its kind and need not match its category.
Drag node titles to move them and drag from an output port to a compatible input.
Hover a warning or rejected connection for its explanation.

Use the Edit and Nodes menus to copy, duplicate, delete, hide, mute or group nodes.
The menus show the current shortcuts. **Hide** reduces visual detail; it does not
stop processing. **Mute** follows each node's bypass or silence behaviour, so it
does not always mean a zero-valued output. Mute on an audio input/output silences
that node without changing the Windows device mute state.

An eligible unconnected node can be dropped on a highlighted wire to insert it.
**Ctrl+Delete** removes a node while reconnecting compatible neighbours where possible.
Use Undo if the resulting routing is not what you intended.

The Inspector shows settings for the selected node. **N** toggles it when no text
editor is consuming the key. Inputs with connected sources can replace their
editors with labels. Units and rates are described in the [node reference](nodes.md).

## Documents and examples

**File → New** creates a small Oscillator-to-Audio-Output graph. Open and Save use
`.knack` documents. Before replacing or closing a modified document, choose Save,
Discard or Cancel. The application edits one document at a time.

**File → Examples** opens a fresh document from an example. Saving it asks for a
new destination. The example folder is normally
`%USERPROFILE%/Documents/EasyKnack/Beispiele`. Add your own `.knack` files there;
subfolders become submenus. Use the example menu's refresh action after adding files.
Bundled examples do not overwrite files already present in that folder.

**File → Import Groups** copies a group, or the source document's main graph,
into the current document. Required nested groups and editor templates are copied
with it. This is not a live link to the source file. Imported groups can be edited
independently; recreate their Bakes if needed. This general import is not offered
inside a Spectral Group.

## Recover after a crash

EasyKnack saves a separate recovery snapshot every 30 seconds by default while your document
has unsaved changes. Your original file is not modified. After a crash, the next
startup offers **Recover**, **Discard snapshot**, or **Later** for available copies.
Recovery snapshots are kept for seven days after their last update by default.
Change the interval (5–3600 seconds) and retention (1–365 days) in
**Settings → Preferences**; changes apply without restarting. Shorter retention
is used at the next startup recovery scan.

A recovered document is marked unsaved. **Save** writes to its original path;
if the document had never been saved, it opens **Save As**. Successful saving or
explicitly discarding your changes removes that document's recovery copy.
**Later** leaves a copy available at a subsequent startup. Changes since the last
completed snapshot may be lost in a crash. External audio files are referenced,
not copied into recovery snapshots.

Ignored connections retain their signal colour but appear translucent. Hover one
for the reason it is ignored. Groups have an overlapping-frames symbol before
their title.

## Connection appearance

In **Settings → Preferences**, adjust **Connection curvature** from 0 (straight
lines) to 1 (the current theme's curves). The graph updates while you drag.
Save to keep the setting; Cancel restores the previous value.

## Navigate

| Action | Shortcut |
|---|---|
| New / Open | Ctrl+N / Ctrl+O |
| Save / Save As | Ctrl+S / Ctrl+Shift+S |
| Undo / Redo | Use the bindings shown in Edit |
| Zoom in / out / reset view | Ctrl++ / Ctrl+- / Ctrl+0 |
| Back / Forward | Alt+Left / Alt+Right |
| Toggle Inspector | N |
| Arrange nodes | Ctrl+Shift+L |
| Center and fit all nodes | Ctrl+Shift+C |
| Start / Stop audio | Ctrl+Enter / Ctrl+Shift+Enter |
| Remove node and reconnect | Ctrl+Delete |
| Ungroup selected instance | Alt+G |

Select a group and choose **Nodes → Ungroup** (`Alt+G`) to unpack that instance.
Its nodes, values and connections move into the current graph; nested groups stay
as groups. Undo restores the instance. The definition remains available in the
Add menu. Saving still follows the document setting for unused group definitions.
Unmute a group before unpacking it. Formula, Instance and Spectral groups retain
their execution context and cannot be unpacked this way.

Group navigation appears while a group is open. Use Back to return to its parent.
Double-click an analysis plot to enlarge it; Escape returns to the graph.

## Export a graph image

Choose **File → Export Graph as Image**. Export the current graph, visible section
or selected nodes (available only with a selection). The preview includes padding.
Width and height can be proportional or independent; nodes keep their proportions
and are centred within the resulting image. Padding can be included in the dimensions
or added outside them. Check the final pixel dimensions before exporting.

PNG supports transparency; JPG provides a compression-quality setting. The estimated
file size is approximate. Large exports require more memory and remain subject to
graphics and image-buffer limits. Image export does not alter the document.

For measured data rather than a graph diagram, use [analysis export](analysis.md).

## Appearance

**Settings → Preferences** previews language, theme and connection curvature
while the dialog is open. Save keeps the changes; Cancel restores the previous
appearance. Your own
node names, file names and plugin names are not translated. File dialogs remember
the last folder used for each purpose across application restarts.

## Themes

In **Settings → Preferences**, choose EasyKnack, EasyKnack Blue, EasyKnack Dark Blue
or EasyKnack Light. The choice previews immediately, including existing nodes.
Save keeps the selection for the next launch; Cancel restores the previous theme.
Signal colours continue to identify the same types across all four variants.
