# Channels and voices

An audio channel is a signal stream within a connection: Mono has one, Stereo has Left and Right. A stereo connection therefore does not need two visible wires. **Channel Separate** splits channels; **Channel Combine** joins them. **Pan** distributes audio between Left and Right.

**Auto** means channel layout is derived from inputs and node behavior. Explicit Left/Right selects one side; Mix can average both and cancel opposite-phase signals. Check channel mode when measuring or combining signals.

**Voices/instances** are additional independent processing states. An Instance Group can contain many instances, each with stereo audio. Instance Merge combines instance contributions; Channel Combine combines channels. These operations are not interchangeable. Voice Merge concerns the voice dimension.

A **MIDI channel** addresses messages and is neither a Left/Right audio channel nor automatically its own DSP voice. The MIDI and group nodes determine how messages produce sound.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/channels.md)
