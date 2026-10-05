> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Trigger Delay and Trigger Merge

Both nodes are listed under Modulation.

Trigger Delay accepts trigger input, Time (Control), and Reset. Time uses seconds,
milliseconds or samples through the slider's Unit menu. Delay is rounded to whole
samples and clamped to 1 sample–10 seconds, including externally connected values.
Every arriving trigger keeps its scheduled due sample when Time changes later.
Different triggers due at the same sample coalesce.

Reset clears pending triggers before output at that sample. It does not itself
generate a trigger; a simultaneous new input can schedule a new delayed trigger.
Time and Reset must originate outside a feedback loop. Pending triggers survive
ordinary graph rebuilds with the same node identity and sample rate.

Trigger Merge combines 2–16 trigger inputs in sample order. The Inputs slider
controls the port count. Simultaneous triggers become one output trigger; manual
buttons on unconnected inputs work as on the other trigger nodes.

## Repeating feedback

Connect a Trigger Input to Merge Input 1, Merge output to Trigger Delay input,
and Trigger Delay output back to Merge Input 2. Connect the Delay output to the
desired consumer as well. Press Trigger Input once to start. Trigger Delay Reset
stops the queued sequence. There is no separate Start button on Trigger Delay.

The host reads delayed output before committing current input. Feedback uses
chunks no larger than the current delay, down to one sample. Merge, Counter,
Filter and Divider preserve event positions in these chunks. Counter/Comparator/
Filter can therefore terminate a sequence after a chosen number of repetitions.
Unsupported processors are still rejected in sample-feedback loops. Independent
loops use independent schedules; connected loops share the smallest safe step.

The bounded trigger calendar is allocated before rendering. Scheduling and normal
reset do not allocate memory on the audio thread.
