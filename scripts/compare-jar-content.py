"""Compare a rebuilt GLSwing JAR with a distributed JAR (metadata excluded explicitly)."""
import argparse
import hashlib
import json
import pathlib
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('distributed', type=pathlib.Path)
parser.add_argument('rebuilt', type=pathlib.Path)
args = parser.parse_args()

def entries(path):
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError('Duplicate ZIP entries')
        return {name: archive.read(name) for name in names if not name.endswith('/')}

a, b = entries(args.distributed), entries(args.rebuilt)
allowed = {'META-INF/glswing/build.properties', 'META-INF/maven/com.vingame/glswing/pom.properties', 'META-INF/maven/io.github.helge714/glswing/pom.properties'}
changed = [name for name in sorted(a.keys() | b.keys()) if a.get(name) != b.get(name)]
classes = sum(name.endswith('.class') for name in a)
report = dict(binarySha256=hashlib.sha256(args.distributed.read_bytes()).hexdigest(),
              rebuiltSha256=hashlib.sha256(args.rebuilt.read_bytes()).hexdigest(),
              classCount=classes, differences=changed,
              classDifferences=[name for name in changed if name.endswith('.class')])
print(json.dumps(report, indent=2))
raise SystemExit(0 if classes and not (set(changed) - allowed) else 1)
