> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Instance Groups

Choose **New Instance Group** under Routing in the Add menu. The initial graph is
an editable additive example: Instance Index + 1, multiplied by the shared Frequency,
feeds a normal Oscillator. Every instance owns its processing state.

- **Count** is a fixed execution slider on the group and in Properties, not an
  editable Group-IO entry. Its fixed Control socket remains available for external
  modulation. It is clamped to the reserved maximum; zero is supported.
  Instance Context has no input and cannot change the count. Multiple Context nodes
  report the same information within an instance.
- **Group-General > Maximum instances** reserves capacity (1–256, default 16).
  Changing capacity rebuilds the graph outside the audio callback.
- **Instance Combine** ends the instance area. Signal and nonnegative Weight are
  evaluated per instance. Sum adds weighted signals; Mean divides by active count;
  Weighted Mean divides by summed weights (zero weight produces silence).
  Left and right are reduced independently. Up to eight Combines may share one
  instance computation, with independent modes and weights.
- Nodes after Combine run once in the shared outer scheduler. You can filter,
  mix, or otherwise process multiple Combine outputs before the group output.
  Titles identify Instance, Combine, and Shared areas. Crossing from an instance
  into shared processing without Combine, or feeding shared results back into an
  instance, is rejected. Use another Instance Group for a new instance domain.
- Older groups without explicit Combines retain their former sum/mean behavior;
  Group-General shows the legacy Instance mix setting only for those groups.
- Instance Context exposes a zero-based Index, effective Count, Position
  (`index / (count - 1)`, clamped to 0–1 during fade-out; zero for one instance), and a reproducible 24-bit Seed.
  The seed depends on the instance index and is identical for the two stereo sides.
- Additional numeric group inputs are shared by all instances. Audio and Control
  sources use the existing scheduler and unit conversion. Unconnected inputs keep
  their normal editor/default values.

Each instance has separate oscillator, noise, filter and delay state. Active-count
changes fade instances over 5 ms without allocating memory in the audio callback.
Fully inactive instances stop processing and retain their state; reactivating them
resumes that state. Existing instances preserve compatible state when the graph is
rebuilt, including when capacity changes. Newly allocated instances start fresh.
Mean uses the summed fade weights (at least 1) as divisor, including transitions.

Noise and Digital Noise have a **Seed** Control input. `-1` selects an automatic
seed derived from the logical node identity; channel and instance copies receive
the same seed. A fixed seed produces a reproducible sequence. Connect Instance
Context's Index/Seed for independent per-instance sequences, or Context Channel
for channel-dependent noise. Other channel-dependent inputs can also require
stereo processing. An unconnected Noise now produces mono, like Digital Noise.
Changing the effective seed resets the generator, including coloured-noise state.

Each instance still owns its generator state. Noise blocks may be reused across
copies of the same logical node only when their exact pre-state, parameters,
sample rate, block length and input samples match. Digital Noise also reproduces
trigger positions. Different histories (e.g. after being inactive), seeds or
frequency/strength inputs prevent reuse. Audio modulation is compared sample by
sample; feedback chunks use the ordinary per-sample path. Cache storage is
allocated during graph construction, never in the audio callback. This shares
the generator computation, not arbitrary upstream stateful subgraphs.

Complex Oscillator's unused Noise
editor has been removed; old saved Noise properties are discarded when normalized.
Its waveform/harmonic synthesis and native parameter contract are unchanged.

Instance Groups support ordinary nested groups, Spectral Groups, the Vector node
family and Spatial Receiver, plus the numeric processors offered in the Add menu.
Vector Combine can use Instance Context to build different positions per instance.
A Vector group input shares an external position, rotation or scale; both Control
and Audio rates and separate stereo channels are preserved. Vector inputs can
also feed shared processing after Instance Merge. Convert the per-instance result
to an audio/value signal before merging; nonnumeric outputs are not combined.

Limits are eight additional numeric/Vector/Spectrum inputs, eight numeric outputs,
and 4096 total node instances across all nesting levels. MIDI boundaries and sample
feedback crossing the group boundary remain unsupported. Feedback inside an
instance uses the existing scheduler and requires the usual Delay. Unsupported
processors are rejected rather than silently shared between instances.

Instance Groups may contain other Instance Groups (up to 16 nested group levels).
Each Instance Context describes its nearest Instance Group. Each outer instance
owns independent inner states and Count; pass outer context values through inputs
to use them inside. Maximum capacities multiply, even when fewer instances are
currently active. Each level needs its own Instance Merge before its output.

Groups are stored as editable assets in .knack documents. Source/plugin export
uses the same native instance runtime and noise-sharing rules. Supported
oscillator/control programs additionally use the specialized kernel described
in `DSP-FUSION.md`; arbitrary repeated subgraphs retain the general scheduler.

The former Count-to-Context binding is migrated to the fixed execution port when
loading older documents. Its existing external connections and saved value are
preserved. The port is identified by its application-owned role, not its display
name, and cannot be edited/deleted in Group-IO. It does not appear on the inner
Inputs boundary. Ordinary user inputs named `count` have no special meaning.

Exports retain the fixed Count as an externally modulatable slider input and keep
the execution split between repeated and shared processing.
