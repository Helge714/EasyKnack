# Formula Group
Computes custom formulas through a configurable group interface.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| signal | ±∞; Default: 0 | Value; ±∞; Auto |
| Source | Formula editor; Default: result = signal; | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| Live preview | Off / On; Default: On | Editor; Inspector |
| Group IO | Numeric, Trigger, Buffer ports; Default: signal → result | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| result | Value; ±∞ |

## Behavior

[Writing formulas](../../user/en/formula-group.md) explains ports, state and evaluation rate. Buffer resources are supported as inputs only. State is separate per channel, voice and instance. Rate validation prevents silently losing used Audio/Trigger inputs.

Inside Spectral Groups, valid formulas execute per bin/frame; temporal sample context and bin context differ. Invalid new code retains the last accepted program. Function help and completion describe supported functions and domains. `tanh` provides smooth saturation; oversampling and gain compensation are not automatic.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Configured group input → Configured group output |
| Insertion | Configured group input → Configured group output |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/formula-group.md)
