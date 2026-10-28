#include "AudioGraph.hpp"
#include "nodes/ValueNode.hpp"
#include "nodes/VectorNode.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
using namespace easyknack;
thread_local bool rendering = false;
void *operator new(std::size_t size) {
  assert(!rendering);
  if (auto *p = std::malloc(size ? size : 1))
    return p;
  throw std::bad_alloc();
}
void operator delete(void *p) noexcept {
  assert(!rendering || !p);
  std::free(p);
}
void operator delete(void *p, std::size_t) noexcept { ::operator delete(p); }
std::string entry(int port, double value, bool enabled = true,
                  std::uint64_t device = 0, bool echo = false) {
  return "1 " + std::to_string(port) + " " + (enabled ? "1" : "0") + " 5 50 " +
         std::to_string(value) + " " + std::to_string(device) +
         " 0 7 0 1 0 0 0 " + (echo ? "1" : "0") + " -";
}
void close(double a, double b) { assert(std::abs(a - b) < 1e-5); }
struct Source : MidiSource {
  MidiBlock block;
  void beginBlock(std::size_t, double) noexcept override {}
  const MidiBlock *events(std::uint64_t id) const noexcept override {
    return id == 1 ? &block : nullptr;
  }
  void cc(unsigned offset, unsigned value) {
    block.append(offset, 0xb0 | 7 << 8 | value << 16);
  }
};
int main() {
  ControlInterpolation control(entry(0, 0));
  control.begin(10, 1000);
  close(control.input(0, 0, 0)[9], 0);
  ControlInterpolation one(entry(0, 1));
  control.apply(one);
  control.begin(10, 1000);
  rendering = true;
  auto *data = control.input(0, 0, 0);
  close(data[0], 0);
  close(data[9], .45);
  rendering = false;
  // A new target interrupts from .5, never from the previous target 1.
  ControlInterpolation zero(entry(0, 0));
  control.apply(zero);
  control.begin(10, 1000);
  data = control.input(0, 0, 0);
  close(data[0], .5);
  close(data[9], .05);
  for (int i = 0; i < 20; i++) {
    control.begin(10, 1000);
    control.input(0, 0, 0);
  }
  control.apply(one);
  control.begin(10, 1000);
  data = control.input(0, 0, 0);
  close(data[9], .18); // paused: max 50ms
  // A physical source bypasses the entire path, at its own sampling rate.
  float wire[10] = {.7f};
  const float *inputs[] = {wire};
  float out[10]{};
  ProcessContext c{inputs, 1, out, 10, 1000, out};
  c.controls = &control;
  close(c.numericInput(0)[0], .7);
  assert(!control.entries[0].initialized);
  // A disconnect starts from the current editor target, not a stale
  // interpolator buffer.
  c.inputs = nullptr;
  control.begin(10, 1000);
  close(c.numericInput(0)[9], 1);
  ControlInterpolation off(entry(0, -2, false));
  control.apply(off);
  control.begin(10, 1000);
  for (int i = 0; i < 10; i++)
    close(control.input(0, 0, 0)[i], -2);
  // Native MIDI uses sample offsets; GUI echoes must never restart that ramp.
  Source midi;
  ControlInterpolation remote(entry(0, 0, true, 1));
  midi.cc(3, 127);
  remote.begin(10, 1000, &midi);
  data = remote.input(0, 0, 0);
  close(data[3], 0);
  close(data[4], .05);
  close(data[9], .3);
  ControlInterpolation echo(entry(0, 1, true, 1, true));
  remote.apply(echo);
  midi.block.clear();
  remote.begin(10, 1000, &midi);
  data = remote.input(0, 0, 0);
  close(data[0], .35);
  close(data[9], .8);
  remote.begin(10, 1000, &midi);
  data = remote.input(0, 0, 0);
  close(data[0], .85);
  close(data[9], 1);
  // Control-rate evaluations advance the ramp clock by the complete block.
  ControlInterpolation held(entry(0, 0));
  held.begin(10, 1000);
  held.input(0, 0, 0);
  held.apply(one);
  held.begin(10, 1000);
  close(held.input(0, 0, 0)[0], 0);
  held.begin(10, 1000);
  close(held.input(0, 0, 0)[0], .5);
  // No heap work while generating ramps or applying snapshots.
  rendering = true;
  held.apply(zero);
  held.begin(256, 48000);
  held.input(0, 0, 0);
  rendering = false;
  bool invalid = false;
  try {
    ControlInterpolation bad("1 0 1 50 5 0 0 0 7 0 1 0 0 0 0");
  } catch (...) {
    invalid = true;
  }
  assert(invalid);
  // Converted unit mappings use the exact MIDI table, after the original
  // mapping.
  auto table = entry(0, .1, true, 1);
  table = table.substr(0, table.size() - 1) + "LUT";
  for (int i = 0; i < 128; i++)
    table += " " + std::to_string(.1 + .9 * i / 127.);
  ControlInterpolation mapped(table);
  close(ControlInterpolation::map(mapped.entries[0], 64. / 127),
        .1 + .9 * 64 / 127);
  // Unit changes jump to the same physical value in the new numeric domain.
  mapped.begin(10, 1000);
  mapped.input(0, 0, 0);
  mapped.apply(one);
  mapped.resetTargets();
  mapped.begin(10, 1000);
  close(mapped.input(0, 0, 0)[0], 1);
  // Ordinary graph live updates preserve the interpolator; explicit Control
  // stays block-held.
  auto graph = [](int value, const char *rate) {
    return AudioGraph::parse("GRAPH 2 v value " + std::to_string(value) +
                             " @ " + rate + " - ~ " + entry(0, value) +
                             " ~ out out 0 0 @ a a");
  };
  auto live = graph(0, "a");
  auto changed = graph(1, "a");
  assert(live.parameterShape() == changed.parameterShape());
  std::array<float, 256> audio{};
  live.process(audio.data(), 256);
  std::vector<std::shared_ptr<const void>> resources(
      live.parameterResourceSlots());
  ParameterRetirement retired{resources.data(), resources.size()};
  rendering = true;
  live.applyLiveParameters(changed, retired);
  live.process(audio.data(), 256);
  rendering = false;
  assert(audio[0] == 0 && audio[255] > .1 && audio[255] < .5);
  auto block = graph(0, "c");
  block.process(audio.data(), 256);
  auto next = graph(1, "c");
  resources.clear();
  resources.resize(block.parameterResourceSlots());
  retired = {resources.data(), resources.size()};
  block.applyLiveParameters(next, retired);
  block.process(audio.data(), 256);
  for (int i = 1; i < 256; i++)
    close(audio[i], audio[0]);
}
