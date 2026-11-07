"""Offline, reproducible comparison against the separately installed MTG SMS Tools.

No reference implementation is copied, packaged or used by EasyKnack at runtime.
See docs/development/PARTIAL-REFERENCE.md for setup, conventions and limitations.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import importlib.metadata
import inspect
import json
from pathlib import Path
import subprocess
import sys

import numpy as np
from smstools.models import dftModel, sineModel, utilFunctions
from scipy.io import wavfile

RATE = 48000
ROOT = Path(__file__).resolve().parents[1]


def fixture(name, count):
    t = np.arange(count) / RATE
    rng = np.random.default_rng(5072007)
    if name == "noise":
        return rng.normal(0, .1, count), lambda _: []
    if name == "percussive":
        # Repeated short noise bursts: no persistent sinusoidal ground truth.
        age = t % .13
        return rng.normal(0, .2, count) * np.exp(-age / .008), lambda _: []
    frequencies = {"single": [440.0], "harmonic": [110.0 * h for h in range(1, 9)],
                   "inharmonic": [180.0, 317.0, 541.0, 997.0],
                   "vibrato": [110.0 * h for h in range(1, 5)],
                   "tones-noise": [180.0, 317.0, 541.0, 997.0]}[name]
    frequencies = np.asarray(frequencies)
    amplitudes = .4 / np.arange(1, len(frequencies) + 1)
    phase = 2 * np.pi * frequencies[:, None] * t
    if name == "vibrato":
        phase += frequencies[:, None] * .02 / 5 * (1 - np.cos(2 * np.pi * 5 * t))
    x = np.sum(amplitudes[:, None] * np.cos(phase + .37), axis=0)
    if name == "tones-noise":
        x += rng.normal(0, np.sqrt(np.sum(amplitudes**2) / 2 / 10), count)
    return x, lambda time: frequencies * (1 + .02 * np.sin(2 * np.pi * 5 * time)) if name == "vibrato" else frequencies


def sms_frames(samples, length, hop):
    """Public SMS functions, on exactly the same windows as the native adapter.

    SMS uses magnitude of the positive complex exponential (half the real sine
    amplitude). Its threshold is consequently 6.0206 dB below the host threshold.
    Phase is zero-phase-centred by SMS; only frequency/count metrics are compared.
    """
    window = np.hanning(length + 1)[:-1]
    window /= window.sum()
    previous = np.array([])
    rows = []
    for start in range(0, len(samples) - length + 1, hop):
        mag, phase = dftModel.dftAnal(samples[start:start + length], window, 2 * length)
        peaks = utilFunctions.peakDetection(mag, -60 - 20 * np.log10(2))
        location, amplitude, phase = utilFunctions.peakInterp(mag, phase, peaks)
        frequency = location * RATE / (2 * length)
        valid = (frequency >= 20) & (frequency <= 20000)
        previous, _, _ = sineModel.sineTracking(frequency[valid], amplitude[valid], phase[valid], previous, 20, .01)
        previous = previous[:64]
        row = np.zeros(64)
        row[:len(previous)] = previous
        rows.append(row)
    # Offline reference can remove complete short fragments retrospectively.
    tracks = sineModel.cleaningSineTracks(np.asarray(rows), round(RATE * .02 / hop))
    return {i * hop: row[row > 0] for i, row in enumerate(tracks)}


def native_frames(executable, data, length, hop, profile, path):
    subprocess.run([str(executable), str(data), str(length), str(hop), profile, str(path)], check=True)
    frames, ratios, errors = {}, {}, {}
    with path.open(newline="", encoding="utf-8") as file:
        for row in csv.DictReader(file):
            start = int(row["start"])
            values = frames.setdefault(start, [])
            if float(row["frequency"]) > 0:
                values.append(float(row["frequency"]))
            ratios[start] = float(row["residual_power_ratio"])
            errors[start] = float(row["reconstruction_error"])
    return frames, ratios, errors


def measure(frames, truth, length, hop):
    hits = total = false = frames_used = count = 0
    errors = []
    # Exclude causal track warm-up and offline reference end-cleanup boundaries.
    starts = sorted(frames)
    for start in starts:
        if start < max(.05 * RATE, 2 * hop) or start > starts[-1] - 2 * hop:
            continue
        expected = list(truth((start + length / 2) / RATE)) if truth is not None else []
        actual = list(frames[start])
        total += len(expected)
        count += len(actual)
        frames_used += 1
        pairs = sorted((abs(a - e), i, j) for i, e in enumerate(expected) for j, a in enumerate(actual))
        used_e, used_a = set(), set()
        for error, i, j in pairs:
            if i in used_e or j in used_a or error > max(2, expected[i] * .01):
                continue
            used_e.add(i)
            used_a.add(j)
            errors.append(error)
        hits += len(used_e)
        false += len(actual) - len(used_a)
    return {"recall": hits / total if total else None,
            "extra_tracks_per_frame": false / frames_used if frames_used and truth is not None else None,
            "tracks_per_frame": count / frames_used if frames_used else None,
            "mean_frequency_error_hz": float(np.mean(errors)) if errors else None,
            "evaluated_frames": frames_used}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cxx", required=True, type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "target/partial-reference")
    parser.add_argument("--native-executable", type=Path, help="Use a previously compiled adapter for a before/after comparison")
    parser.add_argument("--wav", type=Path, help="Also measure a 48 kHz WAV recording (left channel, no ground-truth recall)")
    parser.add_argument("--sizes", nargs="+", type=int, default=[512, 1024, 2048, 4096, 8192, 16384, 32768, 65536])
    args = parser.parse_args()
    if any(n < 512 or n > 65536 or n & (n - 1) for n in args.sizes):
        parser.error("Sizes must be powers of two between 512 and 65536")
    recording = None
    if args.wav:
        rate, recording = wavfile.read(args.wav)
        if rate != RATE:
            parser.error("The reference recording must have a 48000 Hz sample rate")
        if recording.ndim == 2:
            recording = recording[:, 0]
        if np.issubdtype(recording.dtype, np.integer):
            if recording.dtype == np.uint8:
                recording = (recording.astype(np.float64) - 128) / 128
            else:
                recording = recording.astype(np.float64) / (np.iinfo(recording.dtype).max + 1)
        if not np.all(np.isfinite(recording)):
            parser.error("Recording contains non-finite samples")
    out = args.output.resolve()
    out.mkdir(parents=True, exist_ok=True)
    executable = args.native_executable.resolve() if args.native_executable else out / "partial-reference.exe"
    command = [str(args.cxx.resolve()), "-std=c++17", "-O2", "-static", "-I", str(ROOT / "native/audio-host/src"),
               "-I", str(ROOT / "target/native/generated"), str(ROOT / "native/audio-host/benchmarks/PartialReference.cpp"),
               str(ROOT / "native/audio-host/src/nodes/LorisLibrary.cpp"), "-o", str(executable)]
    if not args.native_executable:
        subprocess.run(command, check=True)
    git = subprocess.run(["git", "describe", "--always", "--dirty"], cwd=ROOT, text=True, capture_output=True, check=True).stdout.strip()
    metadata = {"git": git, "sample_rate": RATE, "seed": 5072007,
                "python": sys.version, "sms_tools": importlib.metadata.version("sms-tools"),
                "numpy": np.__version__, "window": "periodic Hann", "fft_padding": 2,
                "reference_sha256": {m.__name__: hashlib.sha256(Path(inspect.getfile(m)).read_bytes()).hexdigest()
                                     for m in (sineModel, dftModel, utilFunctions)},
                "compiler": subprocess.run([str(args.cxx.resolve()), "--version"], text=True, capture_output=True, check=True).stdout,
                "compile_command": command if not args.native_executable else None,
                "native_executable": str(executable),
                "native_source_sha256": {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in
                                         (ROOT / "native/audio-host/src/nodes/PartialAnalysisNode.hpp",
                                          ROOT / "native/audio-host/benchmarks/PartialReference.cpp",
                                          ROOT / "native/audio-host/src/LorisStreamImplementation.hpp",
                                          ROOT / "native/audio-host/third_party/loris/SHA256SUMS.json")}
                                        if not args.native_executable else None,
                "native_profile": {"threshold_db": -60, "capacity": 64, "minimum_hz": 20, "maximum_hz": 20000,
                                   "minimum_duration_ms": 20, "analyzer": "Loris eccd42ac847b08769643b0d0c36442ef0fd27f38",
                                   "resolution_hz": "2*sample_rate/window_length", "bandwidth": "convergence", "convergence": .1},
                "sms_profile": {"tracking_offset_hz": 20, "tracking_slope": .01, "minimum_duration_ms": 20},
                "native_executable_sha256": hashlib.sha256(executable.read_bytes()).hexdigest()}
    if args.wav:
        metadata["recording"] = {"path": str(args.wav.resolve()), "sha256": hashlib.sha256(args.wav.read_bytes()).hexdigest(), "channel": 0}
    (out / "environment.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    results = []
    names = ["single", "harmonic", "inharmonic", "vibrato", "tones-noise", "noise", "percussive"]
    if recording is not None:
        names.append("recording")
    for name in names:
        for length in args.sizes:
            hop = length // 4
            if name == "recording":
                samples, truth = recording[:length + 64 * hop], None
                if len(samples) < length + 4 * hop:
                    print(f"recording: {length} skipped (too short)", flush=True)
                    continue
            else:
                samples, truth = fixture(name, length + 64 * hop)
            data = out / f"{name}-{length}.f64"
            samples.astype("<f8").tofile(data)
            reference = sms_frames(samples, length, hop)
            # Ragged reference rows are written as ordinary CSV, never pickled.
            with (out / f"{name}-{length}-sms.csv").open("w", newline="", encoding="utf-8") as file:
                writer = csv.writer(file)
                writer.writerow(["start", "frequency"])
                for start, frequencies in reference.items():
                    if not len(frequencies):
                        writer.writerow([start, 0])
                    for hz in frequencies:
                        writer.writerow([start, hz])
            results.append({"fixture": name, "size": length, "model": "sms-offline", **measure(reference, truth, length, hop)})
            for profile in ("defaults", "sinusoidal"):
                frames, ratios, errors = native_frames(executable, data, length, hop, profile, out / f"{name}-{length}-{profile}.csv")
                evaluated_ratios = [ratios[start] for start in frames if max(.05 * RATE, 2 * hop) <= start <= max(frames) - 2 * hop]
                result = {"fixture": name, "size": length, "model": profile, **measure(frames, truth, length, hop),
                          "mean_residual_power_ratio": float(np.mean(evaluated_ratios)) if evaluated_ratios else None,
                          "maximum_reconstruction_error": max(errors.values())}
                if result["maximum_reconstruction_error"] > 1e-7:
                    raise RuntimeError(f"Spectrum identity failed: {result}")
                results.append(result)
            print(f"{name}: {length} complete", flush=True)
    fields = list(dict.fromkeys(key for row in results for key in row))
    with (out / "summary.csv").open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=fields)
        writer.writeheader()
        writer.writerows(results)
    (out / "summary.json").write_text(json.dumps(results, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
