#!/usr/bin/env python3
"""Assemble a complete test archive from an immutable base and reviewed UI update.

Unchanged runtime/native/application binaries retain their original identity.
This is packaging, not Linux execution or a fresh native build.
"""
import argparse
import copy
import hashlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tarfile
import zipfile


def sha(data):
    return hashlib.sha256(data).hexdigest()


def package(args):
    with args.base.open('rb') as stream:
        base_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
    assert Path(str(args.base) + '.sha256').read_text().split()[0] == base_hash
    args.output.mkdir(parents=True, exist_ok=False)
    replacement = {}
    with zipfile.ZipFile(args.update) as update:
        manifest = json.loads(update.read('validation.json'))
        for name in update.namelist():
            if name.startswith('payload/'):
                relative = name.removeprefix('payload/')
                assert relative in ('lib/glswing-0.2.0-SNAPSHOT.jar', 'tools/zig/bin/clang++', 'tests/NativeCompilationCheck.java')
                replacement['EasyKnack/' + relative] = update.read(name)
        replacement['EasyKnack/sources/glswing-0.2.0-SNAPSHOT-sources.jar'] = update.read('sources/glswing-0.2.0-SNAPSHOT-sources.jar')
        core_sources = replacement['EasyKnack/sources/glswing-0.2.0-SNAPSHOT-sources.jar']
        with zipfile.ZipFile(io.BytesIO(core_sources)) as sources, zipfile.ZipFile(args.core_inputs) as inputs:
            for name in sources.namelist():
                if name.endswith('/') or name.startswith('META-INF/'):
                    continue
                candidates = [p for p in inputs.namelist() if p.endswith('/src/main/java/' + name) or p.endswith('/src/main/resources/' + name)]
                assert len(candidates) == 1 and sources.read(name) == inputs.read(candidates[0]), name
        supplement = args.output / 'EasyKnack-Linux-test-2-license-materials.zip'
        with zipfile.ZipFile(supplement, 'x', compression=zipfile.ZIP_DEFLATED) as materials:
            materials.write(Path(__file__), 'scripts/package-linux-update.py')
            materials.write(args.legacy_materials, 'legacy/EasyKnack-Linux-test-1-license-materials.zip')
            materials.write(args.core_inputs, 'sources/glswing-build-inputs.zip')
            for name in update.namelist():
                if name.startswith(('sources/', 'licenses/')) or name in ('validation.json', 'README.txt'):
                    materials.writestr('update/' + name, update.read(name))
            for name in ('tools/zig/bin/clang++', 'tests/NativeCompilationCheck.java'):
                materials.writestr('updated-scripts/' + name, replacement['EasyKnack/' + name])
            materials.writestr('README.txt', 'Matching sources for Linux test 2: the program archive retains its original EasyKnack/native/JDK sources. The legacy supplement retains all test 1 source/build materials. sources/glswing-build-inputs.zip and update/sources contain the replacement Core sources. updated-scripts contains the exact changed wrapper and diagnostic sources. Original test 1 recipients can use the retained legacy supplement. This is technical distribution evidence, not legal clearance.\n')
        replacement['EasyKnack/sources/' + supplement.name] = supplement.read_bytes()
    archive_path = args.output / 'EasyKnack-0.4.0-SNAPSHOT-linux-x86_64-test-2.tar.gz'
    unchanged, changed = {}, {}
    with tarfile.open(args.base, 'r:gz') as original, tarfile.open(archive_path, 'w:gz') as output:
        for member in original:
            entry = copy.copy(member)
            stream = original.extractfile(member) if member.isfile() else None
            if member.name == 'EasyKnack/lib/glswing-0.2.0-SNAPSHOT.jar':
                data = stream.read()
                assert sha(data) == manifest['oldCoreSha256'], 'Update requires exact test 1 Core'
            if member.name == 'EasyKnack/package-build.json':
                metadata = json.loads(stream.read())
                metadata['testRevision'] = 2
                metadata['glswingSourceSha256'] = sha(core_sources)
                for dependency in metadata['libraries']:
                    if dependency['file'] == 'lib/glswing-0.2.0-SNAPSHOT.jar':
                        dependency['sha256'] = manifest['newCoreSha256']
                metadata['update'] = {'coreCommit': manifest['coreCommit'], 'baseArchive': args.base.name,
                                      'scope': 'Core UI library, compiler version probe and diagnostics only',
                                      'linuxAcceptance': 'Original real-server audio/virtual MIDI checks passed on EndeavourOS; updated GUI/compiler require target testing'}
                replacement[member.name] = (json.dumps(metadata, indent=2) + '\n').encode()
            if member.name in replacement:
                data = replacement.pop(member.name)
                entry.size = len(data)
                output.addfile(entry, io.BytesIO(data))
                changed[member.name] = sha(data)
            else:
                if stream:
                    data = stream.read()
                    unchanged[member.name] = sha(data)
                    stream = io.BytesIO(data)
                output.addfile(entry, stream)
        for name, data in replacement.items():
            entry = tarfile.TarInfo(name)
            entry.size = len(data)
            entry.mode = 0o644
            output.addfile(entry, io.BytesIO(data))
            changed[name] = sha(data)
    # Read the actual finished archive and prove every other regular file unchanged.
    with tarfile.open(archive_path) as archive:
        seen = set()
        for entry in archive:
            if entry.isfile():
                assert hashlib.file_digest(archive.extractfile(entry), 'sha256').hexdigest() == (changed | unchanged)[entry.name], entry.name
                seen.add(entry.name)
        assert seen == set(changed) | set(unchanged)
    evidence = {'scope': 'Repackaging verification, not Linux execution', 'update': manifest,
                'baseArchiveSha256': base_hash,
                'unchangedRegularFiles': len(unchanged), 'changedOrAddedFiles': changed}
    (args.output / 'linux-test-2-package-review.json').write_text(json.dumps(evidence, indent=2) + '\n')
    for path in (archive_path, supplement):
        with path.open('rb') as stream:
            digest = hashlib.file_digest(stream, 'sha256').hexdigest()
        Path(str(path) + '.sha256').write_text(digest + '  ' + path.name + '\n')
    subprocess.run([sys.executable, str(Path(__file__).with_name('validate-linux-diagnostics.py')),
                    '--archive', str(archive_path), '--javac', str(args.javac)], check=True)
    print('Complete Linux test 2 package verified:', archive_path, flush=True)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('base', 'update', 'core-inputs', 'legacy-materials', 'output', 'javac'):
        parser.add_argument('--' + name, type=Path, required=True)
    package(parser.parse_args())
