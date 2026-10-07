# Vectors and spatial audio

Use **Routing → Spatial Receiver** to place one point source relative to a listener.
Connect the sound to **Audio In**, and connect Vector nodes to **Source Position**
and **Receiver Position**. Unconnected position inputs have their own XYZ editor.

Coordinates use **X = right, Y = up, Z = forward**. Positions are stored in metres.
The XYZ editor uses the shared slider-group gestures: drag vertically across
neighbouring component sliders to include them in the same edit. Its unit menu
selects metres, kilometres, centimetres or millimetres for all three components;
changing the display unit preserves the position.

## Receiver

- **Mono** uses the receiver centre. **Stereo** uses two receiving points, separated
  by **Receiver Spacing**. Spacing is shown only in Stereo; its value is retained
  while Mono is selected.
- **Yaw**, shown only in Stereo, rotates the receiver around the vertical axis. At 0° it faces +Z; at
  90° it faces +X. Positive source X is initially heard on the right.
- **Distance** outputs metres: one distance in Mono, or the distance to each
  receiving point in its corresponding stereo channel. Connect it to Math,
  Map Range or a filter parameter to build your own distance-dependent effect.
- **Distance Attenuation** applies `reference / max(reference, distance)`.
  Reference Distance is in the inspector and defaults to one metre. Disable
  attenuation to implement a different law downstream.
- **Sound Travel Time**, in the inspector, adds fractional-sample propagation
  delay at 343 m/s. It defaults to off. Storage supports two seconds (686 m);
  beyond this distance that receiving point is silent while travel time is on.
  Moving a source changes delay and can change pitch.

Stereo uses equal-power azimuth panning. Stereo input audio is averaged to mono
before positioning it as a point source. For multiple sources, use one receiver
per source, share the listener's position, and mix their audio outputs afterward.

This models direct sound. It does not simulate walls, reflections, head shadow,
or ear-related filtering (HRTF). Height affects distance; it does not independently
create an audible elevation cue. Opposite-phase stereo input can cancel during
the mono downmix.

## Vector nodes

**Inputs → Vector Input** supplies a manually edited XYZ value. The **Vector** menu contains:

| Node | Purpose |
| --- | --- |
| Vector Mapping | Component scale followed by X/Y/Z rotation |
| Vector Combine | Three scalar inputs become one vector |
| Vector Separate | Split a vector into X, Y and Z scalar outputs |
| Vector Math | Add, subtract, multiply/divide components, scale, normalize, negate, absolute components, min/max components, or cross product |
| Vector Distance | Euclidean distance between two vectors |
| Vector Length | Length of one vector |
| Vector Dot Product | Scalar dot product of two vectors |

Vector Math always produces a vector. Normalizing the zero vector produces zero;
division by zero produces zero for that component. Only the inputs used by the
selected operation are shown. General vectors can also represent directions or
other triples; they are not necessarily physical positions.

Vector connections are a distinct type and cannot connect directly to a scalar
audio/value port. Use Combine or Separate to cross that boundary. Vector ports
support Audio and Control rates, with Auto propagation. Control holds one vector
for the block; Audio can change all components per sample. Rate is independent of
stereo: vectors can differ between channels or MIDI voices. Receiver geometry uses
Control when its position/direction inputs allow it, even while processing audio.

Vector Combine's scalar unit menus interpret connected values in the chosen unit
and convert them to metres. Vector Separate emits the stored components (metres
for positions). Use Unit Converter for scalar distance-unit conversions.

Vector sockets can cross normal group boundaries and are included in group bake
and source export. The compound XYZ editor is currently edited on its own node;
it is not offered as a copied scalar group editor. Vector/spatial processors do
not opt into sample-feedback regions or per-bin spectral evaluation.

## Vector mapping

Vector Mapping scales X/Y/Z componentwise, then rotates about the origin in X,
Y, Z order (right-handed Euler angles). Scale defaults to (1, 1, 1); rotation
defaults to zero. Scale uses factors or percent, rotation uses degrees, radians
or turns. Connected rotation vectors contain degrees; connected scale vectors
contain factors. Display-unit choices do not change these contracts.
Use Vector Combine to assemble externally controlled component values.

Vector input sockets align with the first component slider. A connected vector
input replaces its three sliders with one compact labelled row.

## Continuous rotation

Connect **Context Input → Time [s]** to Math (Multiply), multiply by the desired
degrees per second, and connect the result to Receiver Yaw. For example, 36 gives
one revolution every ten seconds. No wrapping or Map Range is needed. Time runs
per sample while that Context Input is processed and survives graph edits; it
pauses when the node is inactive. The signal uses the usual floating-point audio
format, so very long-running absolute time has less fractional precision.

Avoid using an audio sawtooth as a phase ramp: its band-limited reset has ringing
and a transition which cause a brief, unintended rotation after mapping to angles.
