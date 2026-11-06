"""Calibrate ZRR07 descriptor bounds from its modulated sinusoid model.

Uses the experimental distribution method (sections 2-4), not the approximate
polynomial noise patterns of sections 5-6. Generated data belongs to EasyKnack.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
SNRS = [5, 10, 15, 20, 25, 30, 40, 50, 60]
WINDOWS = ["hann", "hamming", "blackman"]


def window_values(name, size):
    t = 2 * np.pi * np.arange(size) / size
    if name == "hann":
        return .5 - .5 * np.cos(t), np.pi / size * np.sin(t)
    if name == "hamming":
        return .54 - .46 * np.cos(t), .92 * np.pi / size * np.sin(t)
    return .42 - .5 * np.cos(t) + .08 * np.cos(2 * t), (np.pi * np.sin(t) - .32 * np.pi * np.sin(2 * t)) / size


def descriptors(spectrum, time_spectrum, derivative_spectrum, peak, length):
    p = np.abs(spectrum)**2
    left = right = int(peak)
    while left > 0 and p[left - 1] < p[left]:
        left -= 1
    while right + 1 < len(p) and p[right + 1] < p[right]:
        right += 1
    sl = slice(left, right + 1)
    weights = p[sl]
    total = weights.sum()
    if total <= 1e-30:
        return np.inf, np.inf, np.inf
    bins = np.arange(left, right + 1)
    mean_bin = np.dot(bins, weights) / total
    nbd = np.sqrt(np.dot((bins - mean_bin)**2, weights) / total) / len(bins)
    xt = time_spectrum[sl]
    mean_time = np.real(np.sum(xt * np.conj(spectrum[sl]))) / total
    ndd = np.sqrt(max(0, np.sum(np.abs(xt)**2) / total - mean_time**2)) / length
    power = weights > total * 1e-12
    offset = np.imag(derivative_spectrum[sl][power] * np.conj(spectrum[sl][power])) / weights[power]
    nfft = (len(spectrum) - 1) * 2
    fcd = nfft / (2 * np.pi) * np.min(np.abs(offset))
    return nbd, ndd, fcd


def calibrate():
    length = 1024
    n = np.arange(length)
    time = n / length
    rng = np.random.default_rng(2007047)
    rows = []
    for name in WINDOWS:
        w, dw = window_values(name, length)
        for padding in (1, 2):
            fft = length * padding
            for snr in SNRS:
                values = []
                for alpha in np.arange(8) * 2 * np.pi / 8:
                    for beta in np.arange(8) * 2 * np.pi / 8:
                        for fraction in (0.0, .25, .5):
                            frequency = 64 / length + fraction / fft
                            carrier = 2 * np.pi * frequency * n
                            # ZRR07 equation 11, modulation frequencies relative
                            # to window duration: FM=1/(4.2*M), AM=2*FM.
                            modulation = 2 * np.pi * time / 4.2
                            x = np.cos(carrier + 10 * np.sin(modulation + alpha)) * (1 + .5 * np.cos(2 * modulation + beta))
                            clean = np.fft.rfft(x * w, fft)
                            peak = int(np.argmax(np.abs(clean)))
                            sigma = abs(clean[peak]) / np.sqrt(np.sum(w*w)) * 10**(-snr/20)
                            # Include the stationary model as well as AM/FM.
                            for signal in (x, np.cos(carrier + alpha)):
                                for _ in range(3):
                                    y = signal + rng.normal(0, sigma, length)
                                    spectrum = np.fft.rfft(y*w, fft)
                                    # Select the dominant peak of this component,
                                    # excluding arbitrary noise maxima elsewhere.
                                    lo, hi = max(1, peak - 5*padding), min(fft//2, peak + 5*padding + 1)
                                    k = lo + int(np.argmax(np.abs(spectrum[lo:hi])))
                                    values.append(descriptors(spectrum, np.fft.rfft((n-length/2)*y*w, fft), np.fft.rfft(y*dw, fft), k, length))
                a = np.asarray(values)
                # Reproducible finite-distribution bounds, not a probability
                # guarantee for arbitrary audio. Held-out seeds test coverage.
                row = dict(window=name, padding=padding, snr=snr,
                           nbd_max=float(np.quantile(a[:,0], .995)),
                           ndd_min=float(np.quantile(a[:,1], .0025)),
                           ndd_max=float(np.quantile(a[:,1], .9975)))
                rows.append(row)
                print(name, padding, snr, row, flush=True)
            # Lower assumed peak SNR must never tighten the decision region.
            selected = [r for r in rows if r['window']==name and r['padding']==padding]
            for low, high in zip(selected[-2::-1], selected[:0:-1]):
                low['nbd_max'] = max(low['nbd_max'], high['nbd_max'])
                low['ndd_min'] = min(low['ndd_min'], high['ndd_min'])
                low['ndd_max'] = max(low['ndd_max'], high['ndd_max'])
    return rows


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT/'target/partial-classifier-calibration')
    parser.add_argument('--header', type=Path, help='Also replace the embedded source table explicitly')
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    rows = calibrate()
    metadata = dict(model='ZRR07 eq.11, NBD/NDD bounds from experimental distributions',
                    length=1024, seed=2007047, numpy=np.__version__, quantiles=[.995,.0025,.9975],
                    generator_sha256=hashlib.sha256(Path(__file__).read_bytes()).hexdigest(), rows=rows)
    (args.output/'calibration.json').write_text(json.dumps(metadata, indent=2), encoding='utf-8')
    text = ['#pragma once', '// Generated by scripts/partial_classifier_calibration.py; see docs/development/PARTIAL-CLASSIFIER.md.',
            'namespace easyknack {', 'struct PartialClassifierBounds {double nbdMax,nddMin,nddMax;};',
            'inline constexpr double partialClassifierSnrs[]={'+','.join(map(str,SNRS))+'};',
            f'inline constexpr PartialClassifierBounds partialClassifierBounds[{len(WINDOWS)}][2][{len(SNRS)}]={{']
    for name in WINDOWS:
        text.append('  { // '+name)
        for padding in (1,2):
            a = [r for r in rows if r['window']==name and r['padding']==padding]
            text.append('    {'+','.join('{'+','.join(format(r[k],'.17g') for k in ('nbd_max','ndd_min','ndd_max'))+'}' for r in a)+'},')
        text.append('  },')
    text += ['};','}']
    header='\n'.join(text)+'\n'
    (args.output/'PartialClassifierCalibration.hpp').write_text(header,encoding='utf-8')
    if args.header:
        args.header.write_text(header,encoding='utf-8')


if __name__=='__main__':
    main()
