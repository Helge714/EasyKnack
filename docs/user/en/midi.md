# MIDI and control

Connect **MIDI Input** to suitable MIDI nodes. **MIDI Note** supplies musical controls; **MIDI CC** reads controllers. MIDI messages are not directly audio or frequency values. Use graph nodes when events need sample-accurate processing.

## MIDI Learn

Open an editor's **MIDI Driver → Learn MIDI** and move the controller. Sliders learn CC; other supported editors can learn Note On. The learning gesture only assigns. Device, MIDI channel and controller/note number are saved in the document.

Sliders map CC 0–127 to their target range. Choices select by CC or advance per Note On. Checkboxes toggle or hold by note and use a threshold for CC. Triggers fire on Note On or an upward CC threshold crossing. Note On with velocity 0 is Note Off.

**Configure MIDI driver** offers target range, reversal, logarithmic mapping for positive ranges and **Pickup**. Pickup waits until hardware reaches/crosses the current software value. Mapping endpoints use the indicated stored parameter unit. A graph connection overrides the editor; after disconnecting, MIDI resumes with a new message. Coupled group editors do not accept a competing driver.

## Interpolation

Document settings offer buffered interpolation, edge rounding or adaptive transitions. Buffering smooths with extra control latency; adaptive transitions need no lookahead. An individual driver can use Document default, custom settings or Off. Discrete choices, counts and seeds switch directly. Per-sample changes require Audio rate in the signal path.

Assignment and mapping edits support Undo; incoming MIDI values do not create Undo entries but are saved as parameter values.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/midi.md)
