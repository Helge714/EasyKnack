"""Add MSI action timings to an isolated toolchain comparison's results.json."""
import argparse
import json
import re
from pathlib import Path


def milliseconds(text):
    hours, minutes, seconds, fraction = map(int, text.split(':'))
    return ((hours * 60 + minutes) * 60 + seconds) * 1000 + fraction


def summarize(path):
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    for run in result['runs']:
        raw = Path(run['log']).read_bytes()
        text = raw.decode('utf-16' if raw[:2] in (b'\xff\xfe', b'\xfe\xff') else 'utf-8-sig', errors='replace')
        actions = re.findall(r'MSI \(s\) \([^)]*\) \[(\d{2}:\d{2}:\d{2}:\d{3})\]: Doing action: ([^\r\n]+)', text)
        stages = []
        for (start, name), (end, _) in zip(actions, actions[1:]):
            stages.append({'action': name, 'milliseconds': (milliseconds(end) - milliseconds(start)) % 86400000})
        costing = [stage['milliseconds'] for stage in stages if stage['action'] == 'CostFinalize']
        if not costing:
            raise ValueError(f"No timed CostFinalize action in {run['log']}")
        run['costFinalizeMilliseconds'] = sum(costing)
        run['costFinalizeCalls'] = len(costing)
        run['longestActions'] = sorted(stages, key=lambda stage: stage['milliseconds'], reverse=True)[:5]
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('results', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    report = summarize(args.results)
    output = args.output or args.results.with_name('timings.json')
    output.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    for run in report['runs']:
        print(f"{run['profile']:8} {run['operation']:9} total {run['seconds']:7.3f} s; CostFinalize {run['costFinalizeMilliseconds']/1000:7.3f} s")
    print(output)
