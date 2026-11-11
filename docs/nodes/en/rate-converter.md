# Rate Converter
Converts numeric signals between Audio and Control.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| In | ±∞; Default: 0 | Value; ±∞; Audio |
| Output Rate | Control, Audio; Default: Control | Editor |
| Reduction | First, Last, Mean, RMS, Minimum, Maximum, Absolute peak; Default: First | Editor; Control output |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞; Audio / Control |

## Behavior
Audio preserves samples. Control reduces each actual block using the selected method and holds that result. Mean is signed; RMS and Absolute peak are nonnegative. Control input is held when upsampling; no intermediate values are invented. Trigger conversion is handled by separate nodes.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/rate-converter.md)
