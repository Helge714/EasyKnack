# Groups and group settings

Select nodes and create a group. Open it to edit; Input and Output connect its inner graph to the public interface. A definition can be reused: structural edits affect its instances, while their parameter values remain individual.

## Settings

| Area | Meaning |
| --- | --- |
| General | Name, node kind/title color and Add menu category control presentation and discovery. |
| Insertion | Choose the input and output used when inserting into an existing connection. |
| Mute bypass | Choose the input and output to connect while muted. |
| IO | Create, name, order, connect and remove inputs/outputs; choose type, rate and units for the internal target. |
| Visibility | A public input can show only an editor; its internal port and wiring remain. |
| Editors | None, Slider, Checkbox, Button, Combobox or Copy Editor. Checkboxes supply 0/1; buttons emit triggers. |
| Slider | Minimum/maximum, default, integer mode, bar/button style, allowed units and default unit. Empty bounds allow unbounded values. |
| Combobox | Document template with labels and numeric values, plus default selection. Manage templates in Editors. |
| Special groups | Instance: Maximum and mix; Spectral: skip empty bins and parallel processing; Formula: processing rate and code identifiers. See their chapters. |

Dragging a connection onto a boundary node creates a port quickly. Then check type, rate, editor and routing. Allowed units must include the default unit. A connected input takes priority over its manual value.

## Copy Editor

Copy Editor inherits a suitable inner editor's type, bounds and units. Public editors of nested groups can also be sources. For supported editors without their own socket, explicitly select the controlled target. Coupled originals appear disabled; a target cannot be controlled by several public editors at once. Not every custom editor supports copying. Interface changes may remove unusable bindings; check your wiring afterwards.

## Management and sharing

**Document settings → Manage groups** lists definitions, direct uses and locations. Only unused definitions outside the open navigation path can be deleted; Undo restores them. An Instance Group's Count does not multiply its usage count. Keeping unused groups is a separate save option.

Import copies selected definitions, dependencies and editor templates without a live link to the source. **Ungroup** unpacks an ordinary instance; special groups retain their execution context and cannot be unpacked this way.

**Bake** compiles locally while keeping the graph editable. Internal processing changes can require recompilation; Bake does not guarantee a speedup. Its local binary cache is not embedded in the document. Group export writes a source plugin; installation compiles it. Export of coupled standalone sliders, checkboxes and triggers is limited. Test the interface before sharing; external media remain external files.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/groups.md)

<a id="arrange-the-current-graph"></a>
<a id="manage-document-groups"></a>
