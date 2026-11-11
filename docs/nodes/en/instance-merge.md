# Instance Merge
Reduces instance contributions to a shared signal.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Combine | Sum, Mean, Weighted Mean; Default: Mean | Editor |
| Per-instance signal | ±∞; Default: 0 | Value; ±∞; Auto |
| Per-instance weight | 0–1; Default: 1 | Value; 0–∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Combined | Audio; ±∞ |

## Behavior
Used inside [Instance Groups](instance-group.md) and their spectral equivalent. Sum is `Σ(wᵢxᵢ)`, Mean divides that by active count, Weighted Mean divides by `Σwᵢ`. Negative weights become zero; zero total weight produces silence. Nodes after the merge form shared processing. Several merge branches may be mixed downstream.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/instance-merge.md)
