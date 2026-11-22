# Formula Group

On macOS, native optimization requires Apple's Command Line Tools or Xcode. The
interpreter remains available without a native compiler. Build errors provide a
log; after installing the tools, retry native optimization.

## Overview

Computes custom formulas through a configurable group interface.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| signal | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Source | [Editor](../../user/en/reference-tables.md) | Formula editor | — | result = signal; | — | — | Inspector |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| Live preview | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Inspector |
| Group IO | [Editor](../../user/en/reference-tables.md) | Numeric, Trigger, Buffer ports | — | signal → result | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| result | [Value](../../user/en/data-types.md#value) | ±∞ | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Group-IO defines port labels, units and stable code identifiers. The initial interface is signal → result. For a Buffer input, choose **Buffer resource**; resources are supported as inputs only.

Audio executes per sample, Control per block; Auto selects the permitted rate from the used inputs. Used Audio/Trigger inputs must not be lost through an incompatible Control setting. In Spectral Groups, formulas execute per bin/frame.

The [Formula language reference](../../user/en/formula-language.md) explains syntax, state/final/const, types, context, Buffer access, functions and loops. See the [workflow](../../user/en/formula-group.md) for a first formula.

### Editor

Completion appears while typing identifiers or with Ctrl+Space. Hover, signature help and inline argument labels explain functions. Ctrl+left-click jumps to a user declaration or opens the corresponding built-in function in the language reference, in the independently selected documentation language. Enter and Format indent code; calls use `name(...)` and definitions `name(...) {`.

The status distinguishes Applied, Not applied yet and Invalid draft from native optimization progress. Live preview applies valid edits after a short pause; otherwise use Apply. Invalid drafts keep the last accepted program running and show a contrasting ! badge in the Formula node title; hover shows the error. A loading message appears if background editor preparation is still running.

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
| Supported |

[Node index](index.md) · [Deutsch](../de/formula-group.md)
