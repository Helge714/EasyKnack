# Audio and routing

[User guide](README.md)

## Choose an input and output

Add **Audio Input** for a microphone, interface input or supported system/application
capture source. Connect it through your processing nodes to **Audio Output**, then
choose **Audio → Start**. Use the Audio menu to refresh devices after connecting hardware.

Device lists identify their backend. WASAPI exposes Windows devices, including
virtual devices. ASIO exposes installed 64-bit ASIO drivers and their channel pairs.
ASIO availability depends on the driver and attached hardware; it does not create
a virtual routing device. A missing selected device does not silently switch to
another speaker; inspect the status and select an available device.

**Device volume** on a WASAPI output controls the corresponding Windows endpoint,
not just that node. **Signal** measures EasyKnack's signal before that device volume.
Use Gain for graph-local level adjustment. Multiple outputs can target different
available devices, but actual independent playback depends on the hardware/driver.

Enable **Link volume keys** on the outputs you want to control together. Selected
available devices change proportionally. Their node controls show the resulting
levels; EasyKnack does not display a separate volume overlay. ASIO outputs do not
provide this Windows device-volume control.

## Process system or application audio

System/application capture marked as Copy duplicates the original audio. The original
route continues to play; connecting the copy to speakers can cause doubling or echoes.

To replace the original route using an already installed virtual cable:

1. In Windows or the source application, select the cable's playback endpoint as
   that application's output.
2. In EasyKnack's **Audio Input**, select the matching recording endpoint.
3. Connect your effects and an **Audio Output** set to physical speakers/headphones.
4. Start EasyKnack audio and verify levels.

Endpoint names belong to the routing tool. Its playback endpoint receives audio from
other applications; its recording endpoint supplies that audio to EasyKnack. Do not
send EasyKnack's output back to the same cable unless you deliberately intend feedback.
External routing tools and drivers are not included or automatically configured.

## Levels and clipping

Audio uses floating-point samples. -1 to +1 is the usual full-scale range, but internal
signals can exceed it. Lower Gain when combining signals; summing voices or channels
can increase the level. **Level Detector** reports linear Peak and RMS without an FFT.
A unit-amplitude sine has a peak near 1 and an RMS near 0.707.

A low Windows device volume does not prevent clipping earlier in the graph. Use the
signal measurements and appropriate gain or limiting at the relevant processing stage.

## Diagnose clicks or overload

Open **Audio → Render diagnostics**, or click the DSP status control. Render and Peak
report processing load relative to an audio/spectral processing-cycle budget, not the
computer's total CPU usage. They measure throughput; separate capture/playback underflow
and driver-overload counters help identify actual delivery problems.

Reset the measurement after changing the graph, then compare a stable interval. Save a
diagnostic report when reporting a problem; it contains measurements, not recorded audio.
Device buffers, virtual routing tools and FFT windows all contribute latency. A larger
FFT window trades temporal resolution and latency for finer frequency spacing.

If audio pauses with a graph error, inspect the marked nodes/connections and their
tooltips. Unprotected feedback connections can be disabled with a warning while other
branches continue. Use **Delay** to protect a feedback loop; unsupported processors in
short sample-feedback loops require a different graph or longer supported delay.
