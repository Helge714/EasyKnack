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
| Remove node and reconnect | Ctrl+Delete |

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

**Settings → Preferences** applies language and theme changes immediately. Your own
node names, file names and plugin names are not translated. File dialogs remember
the last folder used for each purpose across application restarts.
