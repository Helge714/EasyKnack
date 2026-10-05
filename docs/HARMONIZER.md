> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Harmonizer example

Open **Datei > Harmonizer-Beispiel öffnen …**. Unsaved work uses the normal save/discard dialog. Audio stays stopped until started explicitly. The patch starts with a 660 Hz oscillator feeding a reusable Harmonizer group, targeting 440 Hz. Replace the oscillator with Audio Input for a live source. Double-click the group to inspect or modify it; copy it into other documents or export it through the existing plugin export.

| Group control | Meaning |
|---|---|
| Target | Fixed destination frequency in Hz. |
| Mode | Fixed: use Target; Scale: nearest allowed note. |
| Root / Scale | Musical root and scale, directly on the group and in its inspector. Default C / Chromatic, A4 = 440 Hz. |
| Strength | 0: unchanged pitch; 1: full correction. Intermediate values scale the pitch interval logarithmically: reader speed = (target / held frequency)^strength. |
| Glide | Rise and fall time constant in seconds. Default 0.03 s; 63% of a change after one time constant, approximately 95% after three. |
| Confidence | Minimum accepted pitch confidence. Lower-confidence frames retain the last accepted positive frequency. Initial fallback: 440 Hz. |
| Mix | 0: dry; 1: corrected. Default 1. Linear dry/wet mix. |
| Hysteresis | Extra margin beyond the midpoint between notes before changing target in Scale mode. Default 0.1 semitone (10 cent); select semitones/cent through the slider's Unit menu. Zero restores immediate nearest-note selection. Fixed mode is unaffected. |

The internal path is FFT (4096 samples, rectangular, 2x padding, DC removal, 75% overlap, stereo Mix) -> unbiased autocorrelation -> Pitch Detection (60–5000 Hz) -> Pitch Hold -> target selection -> ratio/strength -> Slew -> Buffer Reader (Speed). Its Read position drives Buffer Reader (Position) at offset 0.5. Both read the same Buffer. The follower's Distance controls the linear two-head crossfade. The group has audio bypass/mute and insertion ports.

The compact group readout shows detected note/frequency, target note/frequency and confidence. Detection is the raw detector result, before Pitch Hold: low confidence can therefore show a changing detected pitch while the target remains stable. Note names use the A4=440 Hz convention. While audio is stopped, the readout shows `--`. Updates use the existing asynchronous analysis snapshots at up to 20 Hz. The inline **Pitch Monitor** (Analysis) passes Target through unchanged; it also remains in baked/exported groups, whose generated Java class includes the same readout. Each top-level group instance has its own telemetry mapping; inside a reused group definition, the first active instance is shown, like the analysis graphs.

Reopen the bundled example to get the updated group interface. Saved user documents are not rewritten. The Scale Quantizer retains its hysteresis state across compatible graph handovers and resets it when root, scale, tuning or mode changes. Previously exported source plans using its old parameter contract remain compatible and keep zero hysteresis until re-exported.

The buffer defaults to 8192 samples at 48 kHz (170.7 ms); adjust its size inside the group. The reader limits speed to its supported maximum of 8. The live analysis has window/worker latency and detects one fundamental, so this is intended for single notes/monophonic material, not independent correction of notes in a chord. Stereo channels use one detected pitch from their mix; opposite-phase channels can cancel in that analysis. The wet signal has varying buffer delay, so blending dry and wet can cause phase cancellation. Formant preservation is not implemented.

## Reusable nodes

- **Slew** (Modulation): independent Rise/Fall time constants, zero means immediate. Default Audio output interpolates Control steps sample by sample. Selecting Control updates once per block with the correct elapsed time. First finite input initializes without a fade from zero; graph handover preserves the current state. No smoothing is introduced into other nodes.
- **Pitch Hold** (Analysis): Frequency, Confidence and Minimum confidence inputs; Frequency and numeric Valid (0/1) outputs. Nonpositive/non-finite frequencies and insufficient confidence hold the last accepted value. The inspector sets the initial fallback. Auto/Audio/Control supported.
- **Scale Quantizer** (Converters): Hz in, target Hz out. Scale or Fixed mode. Chromatic, major, natural minor, major/minor pentatonic scales, twelve roots and adjustable A4 tuning. Selects the closest pitch in semitones; exact ties prefer the lower note. Zero/invalid input returns zero in Scale mode; Fixed mode uses its positive target. Auto/Audio/Control supported.

Native tests cover asymmetric step responses, sample-rate/block-size independence, state inheritance, confidence dropouts, invalid values and all roots/scales. The integration test saves and reloads the example, validates group units, runs the full graph and compiled group export with asynchronous analysis, and checks 660 Hz detection and approximately 440 Hz output. It writes an additional example copy to `target/examples/Harmonizer.knack`.

Pitch Detection Minimum/Maximum restrict candidate periods, not the input bandwidth. A fundamental above Maximum can be reported as a subharmonic (half/third frequency); configure the range to include the source. Pitch Hold cannot reject a confident subharmonic on confidence alone. Existing documents retain their saved range; adjust Maximum inside their group.

## Group comboboxes

Group Settings > Expose an inner combobox lists eligible inner node selections, including selections already exposed by nested groups. Select one and click Expose selection. The options and readable labels come from the inner editor; values remain stable string IDs. These are editor-only group properties, not numeric audio ports. Exposure and value changes use undo/redo and are saved in the document.

Choices that change ports or processing-rate layout are excluded. Plugin export retains exposed selections as validated parameters, including nested mappings. Changing a selection does not require recompilation or invalidate a local bake. The host hands over the configured DSP state for parameterized exports; selections are not applied through the scalar-only fallback update. Math fusion remains enabled in regions unaffected by an exposed choice. Existing documents are unchanged; reopen the bundled example for the new Harmonizer interface or expose the desired choices in your own group.
