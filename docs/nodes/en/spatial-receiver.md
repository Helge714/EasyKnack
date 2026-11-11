# Spatial Receiver
Renders a positioned source at a mono or stereo receiver.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Channels | Mono, Stereo; Default: Stereo | Editor |
| Audio In | — | Audio; ±∞; Audio |
| Source Position | ±∞ per component; Default: (0, 0, 1) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Receiver Position | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Yaw | ±∞ °; Default: 0°; °, rad, turn | Value; ±∞ °; Auto; Stereo |
| Receiver Spacing | 0–∞ m; Default: 0.18 m; m, km, cm, mm | Value; 0–∞ m; Auto; Stereo |
| Distance Attenuation | Off / On; Default: On | Editor |
| Reference Distance | 0.001–∞ m; Default: 1 m; m, km, cm, mm | Value; Inspector |
| Sound Travel Time | Off / On; Default: Off | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±∞ |
| Distance | Value; 0–∞ m |

## Behavior
Stereo input is averaged to a mono point source. Stereo receiver positions are offset by half Spacing along the rotated right axis; Distance is measured separately per side. Mono uses the center and ignores Yaw/Spacing. Attenuation is `reference/max(reference,distance)`; stereo uses constant-power directional gains. Travel time delays by `distance/343` seconds, with a maximum of 2 seconds (686 m); more distant sources are silent when enabled. Changing distance then changes pitch. Use Audio-rate smooth motion for continuous Doppler trajectories. This is free-field point-source rendering, not room acoustics, microphone directivity or HRTF simulation.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Audio In → Audio Out |
| Insertion | Audio In → Audio Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spatial-receiver.md)
