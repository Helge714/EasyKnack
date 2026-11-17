# Formula Group

Create a Formula Group and define its ports in Group-IO. Use their code identifiers in the editor. The initial numeric ports are `signal` and `result`:

```c
result = clamp(signal * 2, -1, 1);
```

Assign every numeric output on every execution path. Trigger inputs are booleans; a true Trigger output emits an event and starts false again on every evaluation.

Local variables last for one evaluation. Persistent values use `state` before other statements. With an added `frequency` input in Hz and Audio rate:

```c
state double phase = 0;
result = sin(tau * phase);
phase += frequency / sampleRate;
phase -= floor(phase);
```

Auto, Audio or Control selects evaluation rate. The separate [language reference](formula-language.md) explains syntax, state/final/const, types, functions, loops and Buffer access. Completion and hover help show available functions and context values. Digital Noise is an alternative for changing random control signals.

Buffer inputs provide indexed reads and metadata; an audio input does not provide arbitrary past history. **Live** applies valid code after a short pause; otherwise use **Apply**. Invalid code shows diagnostics. Saving retains code and initial states, not running DSP state.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/formula-group.md)
