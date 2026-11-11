# Getting started

Choose **File → New**, select your device and a low volume on **Audio Output**, then start audio. The initial graph connects an Oscillator to the output. Use Audio Input for a microphone or external signal.

Add nodes from the Add menu. Drag an output to a compatible input; warnings explain rejected connections. The Inspector shows the selection's settings. **N** toggles it; **Ctrl+Shift+L** arranges the current graph. Menus show other shortcuts. Hiding reduces clutter but does not stop processing.

Save as `.knack`. Examples open a new document; save your variations under a new name. Undo/Redo belongs to the open graph; saving covers the whole document. Camera movement alone does not mark it modified. After a crash, startup offers available recovery snapshots; they do not replace regular saving.

## Audio and files

WASAPI uses Windows devices; ASIO requires a suitable installed driver. Device volume on a WASAPI output changes the Windows endpoint; Gain changes the graph signal. Copy capture of system/application audio preserves the original playback route. For processed-only sound, route the source through a separately installed virtual cable into EasyKnack, then to the physical output.

Audio files and plugins are not embedded in `.knack`. Check them after sharing or moving a document. For glitches, inspect device status and Render diagnostics. A useful bug report includes **Help → About**, the message and a small example document.

## Settings and help

Application preferences control language and appearance. Document settings include control interpolation, unused groups and experimental features. Legacy documents using experimental nodes enable the option automatically unless explicitly saved off. Disabling it while such nodes exist requires warning confirmation; existing nodes remain intact.

The documentation window remembers **Same as application / Deutsch / English** independently of application language. Node context menus and Help open the relevant node reference; Back/Forward follows local links.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/start.md)
