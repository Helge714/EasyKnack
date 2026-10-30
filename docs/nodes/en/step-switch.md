# Step Switch

## Overview

Selects one input by trigger or numeric index.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Data type | [Editor](../../user/en/reference-tables.md) | Value, Vector, Wavetable, Buffer, Spectrum, Analysis | — | Value | — | — | Inspector |
| Steps | [Value](../../user/en/data-types.md) | 1–32 | — | 4 | — | — | Always |
| Selection | [Editor](../../user/en/reference-tables.md) | Trigger, Index | — | Trigger | — | — | Inspector |
| Next | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Trigger mode |
| Reset | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Trigger mode |
| Selected index | [Value](../../user/en/data-types.md) | 0–31, integer | 0–Steps−1, rounded down | 0 | — | [Auto](../../user/en/rates.md) | Index mode |
| Input 1…N | [Selected type](../../user/en/data-types.md) | ±∞ for Value; no resource editor | Selected data type | 0 for Value | — | [Auto](../../user/en/rates.md) | Always |
| Step name 1…N | [Editor](../../user/en/reference-tables.md) | Text | — | Input 1…N | — | — | Inspector |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector; Value/Vector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Value / Selected | [Selected type](../../user/en/data-types.md) | Selected input | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Index | [Value](../../user/en/data-types.md) | 0–Steps−1 | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Next advances with wrap; Reset selects 0 and wins over simultaneous Next. Index mode floors and clamps its input; numbered selection buttons are disabled. Resource/vector modes replace the numeric step/output types with the selected type; resources have no fallback sliders. Resource selection is held per block and later triggers affect the next block; Index then reports the chosen block resource at Control rate. Vector contents can retain Audio rate. Switching spectra is hard, with no crossfade. Input labels can be renamed; names and selected step are saved. Inside Instance Groups the visible selection represents the first nested instance, manual selection affects all, and connected selectors remain independent. All connected sources still compute.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Input 1 | Value / Selected |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Input 1 | Value / Selected |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/step-switch.md)
