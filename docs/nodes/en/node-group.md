# Node Group
Exposes a reusable interface to an internal graph.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Public inputs | Interface-defined; Default: Interface-defined | Interface-defined; Interface-defined; Auto |
| General | Name, category, type, routing, Bake; Default: Group-defined | Editor; Inspector |
| IO | Ports, editors, units, defaults, rates; Default: Group-defined | Editor; Inspector |
| Editors | Document editor templates | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Public outputs | Interface-defined |

## Behavior

[Group settings and Copy Editor](../../user/en/groups.md) explain setup and operation. The ports above describe a configurable interface, not a fixed numeric-only restriction.

For Copy Editor on an unconnected input, explicitly select the source; selecting the mode never creates an implicit cable. Copyable standalone editors of nested groups appear as sources; inputs with visible sockets are connected by cables. Coupled originals are disabled and become editable after unlinking.

Edits automatically remove invalid connections to deleted nodes, ports or editor properties. Valid targets remain; Undo restores removed nodes and bindings. Combobox template edits update existing group controls. Each open group definition keeps its own Undo history; saving covers the entire document.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Configured bypass input → Configured bypass output |
| Insertion | Configured insertion input → Configured insertion output |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/node-group.md)
