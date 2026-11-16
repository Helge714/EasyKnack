# Node Group

## Overview

Exposes a reusable interface to an internal graph.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Public inputs | [Interface-defined](../../user/en/data-types.md) | Interface-defined | Interface-defined | Interface-defined | — | [Auto](../../user/en/rates.md#auto) | Always |
| General | [Editor](../../user/en/reference-tables.md) | Name, category, type, routing, Bake | — | Group-defined | — | — | Inspector |
| IO | [Editor](../../user/en/reference-tables.md) | Ports, editors, units, defaults, rates | — | Group-defined | — | — | Inspector |
| Editors | [Editor](../../user/en/reference-tables.md) | Document editor templates | — | — | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Public outputs | [Interface-defined](../../user/en/data-types.md) | Interface-defined | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Disabling a bake retains its loaded result. An unchanged bake can be enabled again without recompiling; Force rebuild still forces a new build.

Bake shows completed build stages in a progress bar, plus the current phase and elapsed C++ compilation time. The bar does not predict remaining time and disappears when the build finishes; the status remains visible. It distinguishes a standard bake from available oscillator optimization. The tooltip explains limitations; this is not a live measurement of the selected execution path.

Grouping connected nodes preserves the semantic type of each internal port (for example Value or Audio), independently of its processing rate.

For an unconnected group input, select Copy Editor and then explicitly choose its source. No internal connection or editor coupling is created until a source is selected.

Open the group to edit its internal graph and public interface. Each public port retains its selected data type and rate; the table above denotes configurable ports, not a fixed numeric-only interface. Group-General sets title appearance and Mute/insertion port pairs; Group-IO sets port order, editors and ranges. Copied editors inherit their source's presentation and can control compatible internal parameters. Unit adapters preserve internal units at the boundary. A Bake optimizes the accepted internal definition; public parameter changes remain parameters. [Instance Groups](instance-group.md), [Spectral Groups](spectral-group.md) and [Formula Groups](formula-group.md) have distinct execution rules.

After changes, invalid connections to deleted nodes or removed/incompatible ports and editor properties are removed automatically. Valid remaining targets and public controls are retained. Undo restores the original nodes and bindings when deleting or grouping nodes.

Standalone editors of nested groups are available as Copy Editor sources. Inputs with visible sockets are connected by cables and are excluded from that list. Copy Editor is offered only when a compatible connected editor or standalone source is available; choosing it never creates an implicit cable.

Controlled group editors are disabled inside the parent group. Removing the coupling enables them again.

Changes to reusable combobox entries update existing group controls immediately. Undo and Redo restore both the template and its applied controls. Groups carry the group marker in the Add menu.

Undo and Redo apply to the currently open graph. Each group definition retains its own history when reopened; saving checkpoints the complete document.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Configured bypass input | Configured bypass output |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Configured insertion input | Configured insertion output |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Depends on internal graph |

[Node index](index.md) · [Deutsch](../de/node-group.md)
