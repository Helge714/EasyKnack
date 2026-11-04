# Granulation with an Instance Group

Use one Recording Buffer outside an Instance Group. Inside the group, connect a
Buffer Reader (Speed) to Instance Merge's signal input. Select Speed 1, Wrap on,
and Combine Sum. Each instance provides one reusable grain player.

Create an Audio-rate Formula Group with the following code identifiers, then paste
the [controller source](granulation-controller.formula) into its editor.

| Input | Type | Connection or initial value |
| --- | --- | --- |
| buffer | Buffer resource | Shared Recording Buffer |
| grainMin | Value, seconds | 0.02 |
| grainMax | Value, seconds | 0.08 |
| density | Value, Hz | 40 grain starts per second across all instances |
| jitter | Value | 0–1; randomness of each player's silent gap |
| strength | Value | 0–1; random read-position displacement |
| index | Value | Instance Context: Index |
| count | Value | Instance Context: Count |
| seed | Value | Instance Context: Seed |

| Output | Type | Connection |
| --- | --- | --- |
| offset | Value, Audio | Reader: Offset |
| gain | Value, Audio | Instance Merge: Per-instance weight |
| reset | Trigger | Reader: Reset |

Expose the six user inputs through the Instance Group as needed; index, count and
seed remain connected to Instance Context. Set Maximum to reserve sufficient
players and Count to select their number. The controller caps each grain's duration
at `count / density` seconds so one player never overlaps its own next grain.

## Time-shuffling settings

For two players and fixed grain duration `d` seconds, use Count 2, grainMin =
grainMax = d, density = 2/d, jitter 0, and strength 1. This gives two Hann-windowed
streams offset by half a grain. Examples:

| Grain duration | Density |
| --- | --- |
| 50 ms | 40 Hz |
| 5 ms | 400 Hz |

Changing Count, Density or Jitter restarts the players' temporal arrangement. The
controller clears the old grains and staggers the new starts consistently; a short
fade-in occurs. Grain position and duration are otherwise captured at grain start.
Rapid continuous changes to scheduling parameters can therefore repeatedly restart
the pattern. Use settled values when comparing effects.

## Strength zero

At the time-shuffling settings above, strength 0 makes both players read the same
delayed signal. After startup and buffer filling, their Hann windows sum to 1.
The result retains a fixed Buffer delay and fractional-sample interpolation; it is
not a zero-latency dry path. With other lengths, density, jitter or speed, strength
0 does not remove gaps, envelope modulation or pitch changes.

The controller resynchronizes scheduling changes explicitly. A controller that
only initializes its start offsets once can leave the windows in phase after a
density change, causing amplitude modulation even when strength is zero.
