#pragma once
#include <algorithm>
#include <array>
#include <cmath>
#include <cstddef>
namespace easyknack {
struct Wavetables {
    static constexpr std::size_t tableSize = 2048, levels = 10;
    static constexpr double tau = 6.2831853071795864769;
    struct Tables {
        std::array<std::array<std::array<float, tableSize + 1>, levels>, 4> waves{};
        Tables() {
            for (unsigned kind = 0; kind < 4; ++kind) for (unsigned level = 0; level < levels; ++level) {
                auto& table = waves[kind][level];
                double peak = 1;
                for (std::size_t i = 0; i < tableSize; ++i) {
                    double angle = tau * i / tableSize, value = 0;
                    if (kind == 0) value = std::sin(angle);
                    else for (unsigned n = 1; n <= (1u << level); ++n) {
                        double coefficient = 0;
                        if (kind == 1 && n % 2) coefficient = (n % 4 == 1 ? 1.0 : -1.0) * 8 / (tau * tau / 4 * n * n);
                        if (kind == 2) coefficient = -4 / (tau * n);
                        if (kind == 3 && n % 2) coefficient = 8 / (tau * n);
                        value += coefficient * std::sin(angle * n);
                    }
                    table[i] = static_cast<float>(value); peak = std::max(peak, std::abs(value));
                }
                for (auto& value : table) value = static_cast<float>(value / peak);
                table[tableSize] = table[0];
            }
        }
    };
    // Initialized by configure() on the control thread, never by process().
    static const Tables& builtinTables() { static const Tables tables; return tables; }
    static double wrap(double phase) noexcept { return phase - std::floor(phase); }
    static double clean(double value) noexcept { return std::isfinite(value) ? value : 0; }
    static double lookup(const float* wave, std::size_t size, double phase) noexcept {
        double position = wrap(phase) * size;
        auto index = static_cast<std::size_t>(position);
        double first = std::clamp(clean(wave[index]), -1.0, 1.0);
        double second = std::clamp(clean(wave[(index + 1) % size]), -1.0, 1.0);
        return first + (second - first) * (position - index);
    }
};
}

