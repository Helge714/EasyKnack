# Instance Group

## Overview

Runs an internal graph for multiple independent instances.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Count | [Value](../../user/en/data-types.md#value) | 0–256, integer | 0–Maximum, rounded down | 8 | — | [Control](../../user/en/rates.md#control) | Always |
| Frequency | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 220 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Maximum | [Value](../../user/en/data-types.md#value) | 1–256 | — | 16 | — | — | Inspector |
| General / IO / Editors | [Editor](../../user/en/reference-tables.md) | Group-defined | — | — | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Bake can fuse pure audio-rate math for frequency/phase modulation and unit conversion. Stateful modulators retain their normal DSP execution.

Optimized oscillator summation also supports audio-rate frequency and phase modulation with scalar instance weights. Distinct left/right input signals and sample feedback continue to use the general executor.

Stateless Formula Groups computing instance weights retain optimized oscillator summation. Bake caches unchanged calculations; time-, trigger- or buffer-dependent formulas continue to use the general executor.

Expose Count through an enclosing ordinary group using a copied slider editor. The outer port remains a normal, editable group input; the fixed Count role belongs to the Instance Group itself.

The initial graph creates harmonic oscillators and an [Instance Merge](instance-merge.md); Frequency is its initial public parameter, not a mandatory permanent port. Each instance has independent DSP state. [Instance Context](instance-context.md) supplies Index, Count, Position and Seed. Maximum reserves state/memory; Count selects active instances and is not an ordinary editable Group-IO port. Multiple merge branches and shared downstream processing are allowed. Nested instance/spectral groups are supported. Public inputs accept numeric, Buffer, Vector, Wavetable and Spectrum; public outputs currently accept numeric or Spectrum. MIDI and other resource boundaries remain unsupported. Both Buffer Readers are supported. External Buffers are shared read-only; each instance retains its own playback state. Internal live Buffers have independent storage per instance. Identical generators can share work until their context or history differs.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Configured group input | Configured group output |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Configured group input | Configured group output |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/instance-group.md)
