# Instance Group
Runs an internal graph for multiple independent instances.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Count | 0–256, integer; Default: 8 | Value; 0–Maximum, rounded down; Control |
| Frequency | ±∞; Default: 220 | Value; ±∞; Auto |
| Maximum | 1–256; Default: 16 | Value; Inspector |
| General / IO / Editors | Group-defined | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Audio; ±∞ |

## Behavior

[Guide](../../user/en/instance-group.md)
Expose Count through an enclosing ordinary group using a copied slider editor. The outer port remains a normal, editable group input; the fixed Count role belongs to the Instance Group itself.

The initial graph creates harmonic oscillators and an [Instance Merge](instance-merge.md); Frequency is its initial public parameter, not a mandatory permanent port. Each instance has independent DSP state. [Instance Context](instance-context.md) supplies Index, Count, Position and Seed. Maximum reserves state/memory; Count selects active instances and is not an ordinary editable Group-IO port. Multiple merge branches and shared downstream processing are allowed. Nested instance/spectral groups are supported. Public inputs accept numeric, Buffer, Vector, Wavetable and Spectrum; public outputs currently accept numeric or Spectrum. MIDI and other resource boundaries remain unsupported. Both Buffer Readers are supported. External Buffers are shared read-only; each instance retains its own playback state. Internal live Buffers have independent storage per instance. Identical generators can share work until their context or history differs.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Configured group input → Configured group output |
| Insertion | Configured group input → Configured group output |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/instance-group.md)
