# Manage document groups

Open **Settings → Document settings → Manage groups**.

The left list shows every embedded group definition and its number of direct
instances. The right list shows the containing graphs and the individual
instances of the selected group. Counts include hidden nodes and instances inside
unused group definitions. An Instance Group node counts as one authored instance;
its active voice count does not multiply the number shown here.

Select an unused group and choose **Delete unused group**. Deletion removes the
definition from this document and its Add menu. It does not delete a file from
disk. Undo restores the definition and its menu entry; Redo removes it again.

A group cannot be deleted while it has any instance, even inside an unused
parent, or while it belongs to the currently open group navigation path. Remove
the instances or delete unused parent definitions first, and leave the group
before deleting it. Deleting a previously visited group clears stale forward
navigation so it cannot reappear through the Forward button.

These explicit deletions take effect immediately and can be undone after closing
the dialog. The separate document option to preserve unused groups controls what
is retained when saving; it does not bypass these deletion checks.

Use **Import groups** to select several definitions from another document. Shared dependencies are copied once; the imported instances are added together and can be undone together.

Undo/Redo acts on the currently open graph. Group histories are retained when navigating away and reopening a definition. Saving applies to all graphs. Press **N** to toggle the inspector outside text input. **Esc** closes a focused inspector; an active text edit or popup handles the first Esc.

[Deutsch](../de/groups.md) · [Groups reference](../../nodes/en/node-group.md)
