"""Literal source checks for ZRR07; never generates production thresholds.

This records unresolved reproduction details instead of replacing them with a
new calibration. See docs/development/ZRR07-SOURCE-AUDIT.md.
"""
from pathlib import Path
import argparse
import hashlib
import json
import numpy as np

COEFFICIENTS = {
    "hann": {"a_rows": [.0174, -.5770, 10.6280], "b_rows": [-.0006, .1211, .8279]},
    "blackman": {"a_rows": [.0081, -.3903, 9.0630], "b_rows": [-.0022, .1472, .7083]},
    "hamming": {"a_rows": [.0003, -.2816, 2.4716], "b_rows": [-.0037, .1615, .7230]},
}


def normalized_duration(samples):
    """Equations 3/4 in time domain, with n expressed in samples."""
    energy = np.abs(samples)**2
    energy /= energy.sum()
    n = np.arange(len(samples))
    mean = np.dot(n, energy)
    return float(np.sqrt(np.dot((n-mean)**2, energy))/len(samples))


def audit():
    # Literal example parameters: 40 ms, 44 ksample/s, carrier 880 Hz.
    rate, length = 44000, 1760
    n = np.arange(length)
    w = .5-.5*np.cos(2*np.pi*n/length)
    carrier = np.cos(2*np.pi*880*n/rate)
    modulation = 4*np.pi*n/(4.2*length)
    xmin = carrier*(1+.5*np.cos(modulation-np.pi/2))*w
    xmax = carrier*(1+.5*np.cos(modulation+np.pi/2))*w
    polynomials = []
    for window, coefficients in COEFFICIENTS.items():
        for snr in (5, 10, 20, 30, 40):
            row = dict(window=window, snr_db=snr)
            for name in ("a_rows", "b_rows"):
                c = coefficients[name]
                row[name+"_descending"] = float(np.polyval(c, snr))
                row[name+"_ascending"] = float(np.polynomial.polynomial.polyval(snr, c))
            polynomials.append(row)
    return {
        "source": "https://www.dafx.de/paper-archive/2007/Papers/p047.pdf",
        "table_1_hann_noise_free": {"nbd_max": .13, "ndd_min": .13, "ndd_max": .16},
        "equation_13_time_moments": {"sample_rate": rate, "window_samples": length,
                                     "ndd_min_pattern": normalized_duration(xmin),
                                     "ndd_max_pattern": normalized_duration(xmax)},
        "table_3_literal_rows": COEFFICIENTS,
        "equation_15_ordering_checks": polynomials,
        "unresolved": [
            "Table 3 does not label the exponent associated with each coefficient row.",
            "Descending Hamming a rows at 10 dB give -0.3144, conflicting with section 5.2's m_min > 1 requirement.",
            "Section 5.2 specifies A qualitatively via spectral energy below the main lobe, without an explicit normalization formula.",
        ],
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=Path("target/zrr07-source-audit.json"))
    parser.add_argument("--paper", type=Path)
    args = parser.parse_args()
    result = audit()
    if args.paper:
        result["source_pdf_sha256"] = hashlib.sha256(args.paper.read_bytes()).hexdigest()
    # Equation 13's stated approximation is consistent within 1% with the
    # published infinite-SNR NDD extrema. This does not validate finite-SNR A.
    moments = result["equation_13_time_moments"]
    assert abs(moments["ndd_min_pattern"]/.1345-1)<.01
    assert abs(moments["ndd_max_pattern"]/.1601-1)<.01
    row = next(r for r in result["equation_15_ordering_checks"] if r["window"]=="hamming" and r["snr_db"]==10)
    assert abs(row["a_rows_descending"]-(-.3144))<1e-12
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result,indent=2)+"\n", encoding="utf-8")
    print(json.dumps({"equation_13": moments, "hamming_10_db": row, "unresolved": result["unresolved"]},indent=2))


if __name__=="__main__":
    main()
