# Instance Context
Supplies the current instance index and count.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| — | — | — |

## Outputs
| Output | Signal |
| --- | --- |
| Index | Value; 0–Count−1; Control |
| Count | Value; 0–Maximum; Control |
| Position | Value; 0–1; Control |
| Seed | Value; 0–16 777 215; Control |

## Behavior
Inside an [Instance Group](instance-group.md), Index is zero-based and Count is the active count. Position is `Index/(Count−1)` for more than one instance, otherwise 0. Seed is stable per instance. Connecting Index or Seed makes otherwise identical generators instance-dependent.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/instance-context.md)
