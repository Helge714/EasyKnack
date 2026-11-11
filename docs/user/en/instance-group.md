# Instance Group

An Instance Group executes the same graph for independent instances, such as an oscillator bank or several Buffer Readers. Each instance keeps its own state. Instances are not stereo channels.

**Maximum** reserves capacity (1–256); **Count** selects the active number up to that maximum. A larger Maximum needs more memory. **Instance Context** provides the zero-based Index, Count, normalized Position and stable Seed. Position is 0 for one instance.

Use **Instance Merge** to combine contributions as Sum, Mean or Weighted Mean. Processing after the merge runs jointly. A starting example is `Instance Context → frequency calculation → Oscillator → Instance Merge`.

Public inputs support numbers, Buffer, Vector, Wavetable and Spectrum; outputs support numbers or Spectrum. MIDI and other resource boundaries are unsupported. External buffers are shared for reading, but reader state remains individual. Internal live buffers belong to their respective instance.

Nested Instance/Spectral Groups are supported within graph and memory budgets. Count is a special Instance Group control, not an ordinary IO port. A copied Count editor exposed by an ordinary outer group remains an ordinary public editor there.

Spectrum outputs combine according to **Instance mix: sum/mean**. FFT size, window, hop, sample rate and frame times must match. Audio channels stay separate; Spectrum does not use the audio Merge fade.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/instance-group.md)
