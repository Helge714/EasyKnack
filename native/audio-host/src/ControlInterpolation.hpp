#pragma once
#include "midi/MidiEvents.hpp"
#include <algorithm>
#include <array>
#include <cmath>
#include <cstdint>
#include <sstream>
#include <stdexcept>
#include <vector>
namespace easyknack {
/** State belongs to one processor lane. Only unsourced numeric defaults are
 * interpolated. */
struct ControlInterpolation {
  struct Entry {
    std::size_t port = 0;
    bool enabled = true, initialized = false;
    double minimumMs = 5, maximumMs = 50, value = 0, current = 0, target = 0,
           step = 0, low = 0, high = 1, previous = 0;
    std::uint64_t device = 0;
    int channel = 0, number = 0;
    bool reverse = false, log = false, pickup = false, external = false,
         acquired = false, previousValid = false, manualPending = false;
    std::uint64_t lastChange = 0, remaining = 0, epoch = 0;
    bool changedBefore = false, constantBuffer = false;
    std::array<float, 256> samples{};
    std::array<float, 128> mappedValues{};
    bool lookup = false;
  };
  std::vector<Entry> entries;
  std::uint64_t clock = 0, blockStart = 0, epoch = 0;
  std::size_t frames = 0;
  double rate = 48000;
  MidiSource *midi = nullptr;
  explicit ControlInterpolation(const std::string &text) {
    std::istringstream in(text);
    std::size_t count;
    if (!(in >> count) || count > 64)
      throw std::runtime_error("Invalid interpolation count");
    entries.resize(count);
    for (auto &e : entries) {
      int enabled, reverse, log, pickup, external;
      if (!(in >> e.port >> enabled >> e.minimumMs >> e.maximumMs >> e.value >>
            e.device >> e.channel >> e.number >> e.low >> e.high >> reverse >>
            log >> pickup >> external) ||
          e.port >= 64 || (enabled != 0 && enabled != 1) ||
          !std::isfinite(e.minimumMs) || !std::isfinite(e.maximumMs) ||
          e.minimumMs < 0 || e.maximumMs < e.minimumMs || e.maximumMs > 60000)
        throw std::runtime_error("Invalid interpolation bounds");
      std::string mapping;
      if (!(in >> mapping) || (mapping != "-" && mapping != "LUT"))
        throw std::runtime_error("Invalid interpolation lookup");
      e.lookup = mapping == "LUT";
      if (e.lookup)
        for (auto &value : e.mappedValues)
          if (!(in >> value) || !std::isfinite(value))
            throw std::runtime_error("Invalid interpolation lookup value");
      e.enabled = enabled != 0;
      e.reverse = reverse != 0;
      e.log = log != 0;
      e.pickup = pickup != 0;
      e.external = external != 0;
      if (!std::isfinite(e.value) || !std::isfinite(e.low) ||
          !std::isfinite(e.high) || e.low > e.high || e.channel < 0 ||
          e.channel > 15 || e.number < 0 || e.number > 127 ||
          e.log && (e.low <= 0 || e.high <= 0))
        throw std::runtime_error("Invalid interpolation mapping");
    }
    std::string extra;
    if (in >> extra)
      throw std::runtime_error("Unexpected interpolation data");
    for (std::size_t i = 0; i < entries.size(); i++)
      for (std::size_t j = 0; j < i; j++)
        if (entries[i].port == entries[j].port)
          throw std::runtime_error("Duplicate interpolation port");
  }
  std::string shape() const {
    std::string out;
    for (const auto &e : entries)
      out += std::to_string(e.port) + ",";
    return out;
  }
  void apply(const ControlInterpolation &source) noexcept {
    if (entries.size() != source.entries.size())
      return;
    for (std::size_t i = 0; i < entries.size(); i++) {
      auto &e = entries[i];
      const auto &n = source.entries[i];
      bool mappingChanged = e.lookup != n.lookup ||
                            (n.lookup && e.mappedValues != n.mappedValues);
      e.manualPending |= e.value != n.value && (!n.external || !n.device);
      e.value = n.value;
      e.lookup = n.lookup;
      if (n.lookup)
        e.mappedValues = n.mappedValues;
      e.enabled = n.enabled;
      e.minimumMs = n.minimumMs;
      e.maximumMs = n.maximumMs;
      if (mappingChanged || e.device != n.device || e.channel != n.channel ||
          e.number != n.number || e.low != n.low || e.high != n.high ||
          e.reverse != n.reverse || e.log != n.log || e.pickup != n.pickup)
        e.acquired = e.previousValid = e.changedBefore = false;
      e.device = n.device;
      e.channel = n.channel;
      e.number = n.number;
      e.low = n.low;
      e.high = n.high;
      e.reverse = n.reverse;
      e.log = n.log;
      e.pickup = n.pickup;
      e.external = n.external;
    }
  }
  void inherit(const ControlInterpolation &old) noexcept {
    if (entries.size() != old.entries.size())
      return;
    for (std::size_t i = 0; i < entries.size(); i++)
      if (entries[i].port != old.entries[i].port)
        return;
    for (std::size_t i = 0; i < entries.size(); i++) {
      auto config = entries[i];
      entries[i] = old.entries[i];
      entries[i].value = config.value;
      entries[i].lookup = config.lookup;
      if (config.lookup)
        entries[i].mappedValues = config.mappedValues;
      entries[i].enabled = config.enabled;
      entries[i].minimumMs = config.minimumMs;
      entries[i].maximumMs = config.maximumMs;
      entries[i].manualPending |= config.value != old.entries[i].value &&
                                  (!config.external || !config.device);
      entries[i].device = config.device;
      entries[i].channel = config.channel;
      entries[i].number = config.number;
      entries[i].low = config.low;
      entries[i].high = config.high;
      entries[i].reverse = config.reverse;
      entries[i].log = config.log;
      entries[i].pickup = config.pickup;
      entries[i].external = config.external;
    }
    clock = old.clock;
    epoch = old.epoch;
  }
  // A unit-domain change preserves the represented signal and must not ramp
  // between numbers expressed in different units.
  void resetTargets() noexcept {
    for (auto &e : entries) {
      e.current = e.target = e.value;
      e.remaining = 0;
      e.manualPending = e.changedBefore = e.constantBuffer = false;
      e.initialized = true;
    }
  }
  void connected(std::size_t port) noexcept {
    for (auto &e : entries)
      if (e.port == port) {
        e.initialized = e.changedBefore = e.acquired = e.previousValid =
            e.constantBuffer = false;
        e.remaining = 0;
        e.manualPending = false;
      }
  }
  std::size_t stride(std::size_t port) const noexcept {
    for (const auto &e : entries)
      if (e.port == port)
        return e.constantBuffer ? 0 : 1;
    return 0;
  }
  bool requiresGeneralScheduling() const noexcept {
    for (const auto &e : entries)
      if (e.enabled && (e.remaining || e.manualPending || e.device))
        return true;
    return false;
  }
  bool changing() const noexcept {
    for (const auto &e : entries)
      if (e.enabled && (e.remaining || e.manualPending ||
                        e.device && midi && midi->events(e.device) &&
                            midi->events(e.device)->count))
        return true;
    return false;
  }
  void devices(std::vector<std::uint64_t> &result) const {
    for (const auto &e : entries)
      if (e.device)
        result.push_back(e.device);
  }
  static double map(const Entry &e, double t) noexcept {
    t = std::clamp(t, 0., 1.);
    if (e.lookup)
      return e.mappedValues[std::size_t(std::lround(t * 127))];
    if (e.reverse)
      t = 1 - t;
    return e.log ? std::exp(std::log(e.low) +
                            t * (std::log(e.high) - std::log(e.low)))
                 : e.low + t * (e.high - e.low);
  }
  void retarget(Entry &e, double value, std::uint64_t now) noexcept {
    if (value == e.target)
      return;
    const double interval =
        e.changedBefore ? double(now - e.lastChange) * 1000 / rate : 0.;
    // Min is the first/restart duration, not a lower bound on frequent changes.
    const double ms =
        !e.changedBefore || interval > e.maximumMs ? e.minimumMs : interval;
    e.lastChange = now;
    e.changedBefore = true;
    e.target = value;
    if (ms <= 0) {
      e.current = value;
      e.remaining = 0;
      e.step = 0;
      e.constantBuffer = false;
      return;
    }
    e.remaining = std::max<std::uint64_t>(1, std::llround(ms * rate / 1000));
    e.step = (value - e.current) / e.remaining;
  }
  void begin(std::size_t count, double sampleRate,
             MidiSource *source = nullptr) noexcept {
    midi = source;
    frames = std::min(count, std::size_t{256});
    rate = sampleRate;
    blockStart = clock;
    clock += frames;
    ++epoch;
  }
  const float *input(std::size_t port, double, std::size_t offset) noexcept {
    for (auto &e : entries)
      if (e.port == port) {
        if (!frames)
          return nullptr;
        if (e.epoch != epoch) {
          e.epoch = epoch;
          if (!e.initialized) {
            e.current = e.target = e.value;
            e.constantBuffer = false;
            e.initialized = true;
            e.manualPending = false;
          }
          if (!e.enabled) {
            if (e.current != e.value)
              e.constantBuffer = false;
            e.current = e.target = e.value;
            e.remaining = 0;
            e.manualPending = false;
          } else if (e.manualPending) {
            retarget(e, e.value, blockStart);
            e.manualPending = false;
          }
          auto *events =
              e.enabled && e.device && midi ? midi->events(e.device) : nullptr;
          std::size_t event = 0;
          if (events && events->panic)
            e.acquired = e.previousValid = e.changedBefore = false;
          if (e.constantBuffer && !e.remaining && (!events || !events->count)) {
            return e.samples.data() + std::min(offset, frames - 1);
          }
          e.constantBuffer = false;
          bool steady = !e.remaining;

          for (std::size_t i = 0; i < frames; i++) {
            while (events && event < events->count &&
                   events->events[event].offset <= i) {
              auto message = events->events[event++].message;
              if ((message & 0xf0) != 0xb0 || int(message & 15) != e.channel ||
                  int((message >> 8) & 127) != e.number)
                continue;
              double t = ((message >> 16) & 127) / 127., value = map(e, t);
              if (e.pickup && !e.acquired) {
                double a = map(e, t - 1. / 127), b = map(e, t + 1. / 127);
                e.acquired =
                    e.target >= std::min(a, b) - 1e-9 &&
                        e.target <= std::max(a, b) + 1e-9 ||
                    e.previousValid &&
                        (e.previous - e.target) * (value - e.target) <= 0;
                e.previous = value;
                e.previousValid = true;
                if (!e.acquired)
                  continue;
              }
              retarget(e, value, blockStart + i);
            }
            e.samples[i] = float(e.current);
            if (e.remaining) {
              e.current += e.step;
              if (--e.remaining == 0)
                e.current = e.target;
            }
          }
          e.constantBuffer =
              steady && !e.remaining && (!events || !events->count);
          if (e.constantBuffer)
            e.samples.fill(float(e.current));
        }
        return e.samples.data() + std::min(offset, frames - 1);
      }
    return nullptr;
  }
};
} // namespace easyknack
