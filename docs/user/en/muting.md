# Mute

Mute changes a node's effective signal path. It does not always mean setting its
output to zero. Each node reference lists support and bypass wiring under **Mute**.

## Bypass or ignore

| Case | Effect |
| --- | --- |
| Mutable: No | The node does not support Mute. |
| Mutable: Yes, input and output specified | The signal connected to that input is forwarded to the output, bypassing the node's processing. |
| Mutable: Yes, input and output both — | There is no bypass signal; outgoing connections are ignored. |
| Bypass input unconnected | No connected source means no bypass signal. An editor's default value does not replace that source. |

A muted Math node can forward its designated input. A muted oscillator does not
generate a replacement signal. Groups can define their own bypass ports. When
several inputs are possible, the node reference specifies the selection or
fallback rule.

## Connections and editors

Wires remain in the document. Ignored connections appear subdued. This also
applies to inputs of a muted node that do not contribute to an effective bypass.

When Mute leaves a downstream input without an effective source, its editor can
reappear and supply its own value. A required resource input without an editor
does not receive a replacement resource; its requirements still apply.

## Zero signals and devices

A **connected signal with value 0** remains an effective connection. Its receiver
does not substitute its editor value. This differs from an ignored wire.

Audio Input is a special case: muting it supplies a connected zero signal.
Muting Audio Output stops that graph signal from being output. It does not mute
the Windows device itself or change its volume.

Mute is neither deletion nor hiding. It also does not promise a crossfade when
toggled.

[Fundamentals](index.md) · [Deutsch](../de/muting.md)
