# Buffers and recording

A buffer contains audio samples. **File Buffer** supplies file data; **Recording Buffer** holds a continuously written ring buffer. **Audio File Player** is the direct playback alternative. Audio files stay external; the player supports mono/stereo, not arbitrary multichannel media.

**Buffer Reader (Speed)** moves its own read head at the selected speed. Speed also changes pitch and is not independent time stretching. **Buffer Reader (Position)** reads a specified position; jumping controls can click.

Distinguish actual read position, write position and the reader's distance output. Read position alone does not describe distance from a moving write head. **Buffer Context** provides the connected buffer's size and write position; use these for your own relative-distance calculation. In a ring buffer, beginning and end are neighbors; simple linear subtraction does not account for that.

Inside an Instance Group, readers can share an external buffer while keeping separate playback state. An internal Recording Buffer instead owns storage per instance. Many or large buffers increase memory requirements.

**Audio Recorder** records its connected signal using Start/Stop; wait for the completed take before exporting or reusing it. Destination and supported formats appear on the node. Use Measurement Recorder for numeric series instead of audio files.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/buffers.md)
