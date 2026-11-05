# Groups and custom processing

[User guide](README.md)

## Create a reusable group

Select nodes and choose the group-creation command in Nodes. Open the group to edit
its contents. Input and output boundary nodes connect its interface to the inner graph.
Their positions are saved with the document.

The Inspector has these tabs:

| Tab | Purpose |
|---|---|
| Properties | Settings for selected nodes |
| Editors | Reusable combobox templates in this document |
| Group-General | Name, appearance/category and insertion/mute routing |
| Group-IO | Ordered inputs and outputs, editors, defaults, rates and units |

The last three appear inside a group. Names may contain spaces and accented characters.
A group input can display only an editor, with its outer connection point hidden;
its internal boundary port and wiring still exist.

Drag a connection onto a group boundary node to create a port quickly. Review its
copied type, rate and editor in Group-IO. Review the group's insertion and mute routing
as well, especially when several inputs or outputs could be used as a bypass.

## Choose editors and units

For usage counts, embedding locations and removal of unused definitions, open
**Settings → Document settings → Manage groups**. See
[Manage document groups](en/groups.md) · [Deutsch](de/groups.md).

Use **None** for an input without an editor. Numeric inputs can use sliders,
comboboxes or checkboxes; a checkbox supplies 0 or 1. Trigger inputs use a button.
Slider settings include the presentation, limits, default value and compatible units.
The default unit remains enabled in the allowed-unit list.

**Copy Editor** takes an editor from a connected inner input. Its type, bounds and
unit family come from that source; select its default value and, for sliders, default
unit. To define a reusable combobox, create a template in Editors, then choose it as
the input's model. Entries have values; numeric indices are useful for selecting
options in another combobox. Templates are saved with the document.

Standalone inner editors that permit coupling can also be exposed. Select the editor
type and the matching inner nodes it should control. Coupled originals are disabled
and show the configured default. Not all structural or custom editors support copying.
Check [export limitations](limitations.md) before baking or exporting coupled controls.

Group interfaces convert compatible units to the units expected inside the group.
Changing an exposed display/input unit should not require rewriting its inner formula.

## Bake or export

**Bake** in the group Inspector compiles the group for local use while keeping its
editable graph. Wait for completion. **Disable bake** returns to ordinary execution.
Changes to internal processing invalidate a Bake; moving nodes or adjusting exposed
runtime controls does not inherently require a new Bake. Measure your actual graph:
Bake does not guarantee a speedup for every configuration.

A saved document retains the graph and Bake information. A missing compatible local
cache can require recompilation on opening; native binaries are not embedded in the
document. Compilation and audio-host changes can briefly interrupt playback.

**Nodes → Export group as plugin** writes a source plugin. **Plugins → Install source
plugin** builds and installs it and exposes its nodes in the palette. The packaged
application includes the compilers. Install only plugins whose source you trust:
plugins contain native code. Media references still depend on their source/cache files.

## Instance and Spectral Groups

An **Instance Group** repeats processing with separate state per instance. Its maximum
capacity is set in Group-General; Count controls the active instances. **Instance Context**
provides Index (starting at 0), Count, Position (0–1) and Seed. Use **Instance Merge**
to combine contributions with Sum, Mean or Weighted Mean before shared processing.
The shortest feedback delay in a loop determines how finely that loop must execute;
more instances and short feedback can be expensive.

A **Spectral Group** derives its instances from FFT bins. Each **Bin Output** produces
its own spectrum; **Instance Merge** combines generated per-bin audio. Spectrum
and audio output storage follows the group's actual interface; there is no fixed
eight-output limit. Shared upstream nodes run once per bin, even when several Bin
Outputs use their results. See [spectrum processing](analysis.md) for the two
processing time scales.

To split a spectrum, add two Bin Outputs and expose both in Group-IO. Multiply
the original real and imaginary components by a mask for the first output and by
`1 - mask` for the second. Equivalently, mask the magnitude and retain the original
phase in Magnitude/phase mode. The two outputs then sum to the original spectrum.
**Bin Discard** skips the bin for every output; use it only when no output needs
that bin. Separate output masks do not duplicate the shared calculation.

Instance Groups also accept Spectrum outputs. Their complex coefficients are
summed, or averaged with the group's Sum/Mean setting, across active instances.
The streams must use the same FFT size, window, hop, sample rate and frame timing;
incompatible batches produce no frames rather than combining unrelated bins.
Stereo channels remain separate. Count changes select contributions per frame;
the audio Merge fade does not apply to Spectrum outputs.

Output buffers are prepared before playback. Existing graph-node, per-bin program
and instance budgets still apply when building large groups.

Group inputs are also sized from the interface. Instance Groups accept numeric,
Vector, Wavetable and Spectrum inputs without a fixed eight-input limit. Spectral
Groups keep one source Spectrum and accept a variable number of numeric/trigger
frame inputs, without the former 16-input limit. These frame inputs are sampled
at the FFT window's timestamp, including when asynchronous frames arrive late.

## Formula Groups

Create a Formula Group from the Math category and open it. Define ports in Group-IO
and use their code identifiers in the editor. The initial numeric ports are `signal`
and `result`:

```c
float amplified = signal * 2;
result = clamp(amplified, -1, 1);
```

Use Auto, Audio or Control in the group settings. Auto uses the required precision;
stateful formulas need audio precision unless you explicitly choose Control. Trigger
ports act as booleans. Assign every numeric output on every execution path. A true
Trigger output emits a trigger; it starts false on each evaluation.

Local variables are temporary. Persistent values use `state` declarations before
other statements. For example, after adding a numeric `frequency` input in Hz:

```c
state double phase = 0;
result = sin(tau * phase);
phase += frequency / sampleRate;
phase -= floor(phase);
```

Supported types include bool, int, uint, long, ulong, float and double, with casts,
if/else, arithmetic and common math functions. There are no arbitrary C++ calls,
pointers or user loops. Completion and hover help describe functions and typed inputs.
A Buffer input provides indexed reads and metadata; an ordinary audio input does not
provide arbitrary buffer history.

Live applies valid edits after a short pause; Apply works with Live disabled.
Formatting changes layout, not the formula's meaning. Invalid edits leave diagnostics
in the editor. A checked preview can run while native compilation finishes. Saving
preserves code and initial state values, not a snapshot of the running DSP state.
