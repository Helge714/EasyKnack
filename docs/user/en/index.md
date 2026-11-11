# Fundamentals

| Topic | Contents |
| --- | --- |
| [Manage groups](groups.md) | Usage counts, embedding locations and undoable removal of unused definitions |
| [Rates](rates.md) | Audio, Control, Auto and Trigger; sampling and rate conversion |
| [Data types](data-types.md) | Numbers, triggers, MIDI, vectors and resources; channels |
| [Units](units.md) | Display, interpretation of connected values and conversion |
| [Mute](muting.md) | Bypass, ignored connections and zero signals |
| [Reference tables](reference-tables.md) | Editor range, signal range and other table headings |

[Deutsch](../de/index.md) · [User guide](../README.md)

## Experimental features

Enable **Document settings → Enable experimental features** to show nodes and examples marked **Experimental**. The setting belongs to each document and is off for new ordinary documents. Older files automatically enable it if they contain experimental nodes; an explicitly saved off decision is retained. Disabling it asks for confirmation while experimental nodes remain in the document. Groups and newly exported plugins inherit the status of their dependencies. Existing documents remain loadable and retain their nodes when the option is off. Camera movement or window resizing alone does not trigger an unsaved-changes prompt; the current camera is still stored on save. Pitch Hold and examples that depend on it currently require this opt-in.

## Documentation language

In the documentation window, use **Documentation language** to choose **Same as application**, **Deutsch** or **English**. The choice is remembered independently of the application interface and applies to node help, the index and local navigation. Missing translations use English while keeping your chosen language; untranslated workflow guides are marked in the status line.
