#!/usr/bin/env python3
"""Record concrete Linux distribution evidence; does not certify legal compliance.

Run against the immutable binary archive and its exact upstream tool archives.
The output directory must be new. No network access or package changes are made.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import tarfile
import zipfile


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def scan(path, strip, retain=False):
    files, saved = {}, {}
    with tarfile.open(path, 'r|*') as archive:
        for entry in archive:
            name = '/'.join(entry.name.split('/')[strip:])
            if not name or entry.isdir():
                continue
            if name in files:
                raise ValueError('Duplicate member: ' + name)
            if entry.issym() or entry.islnk():
                files[name] = {'kind': 'symlink' if entry.issym() else 'hardlink', 'target': entry.linkname}
                continue
            if not entry.isfile():
                raise ValueError('Unsupported member: ' + name)
            stream = archive.extractfile(entry)
            content = stream.read() if retain and (name.endswith('.jar') or name in ('sources/EasyKnack-project-sources.zip', 'package-build.json')) else None
            digest = hashlib.sha256(content).hexdigest() if content is not None else hashlib.file_digest(stream, 'sha256').hexdigest()
            files[name] = {'kind': 'file', 'size': entry.size, 'sha256': digest}
            if content is not None:
                saved[name] = content
    return files, saved


def audit(args):
    if args.output.exists():
        raise ValueError('Use a new output directory')
    files, saved = scan(args.archive, 1, True)
    metadata = json.loads(saved['package-build.json'])
    assert sha(args.jdk) == metadata['runtime']['binary']['package']['checksum'], 'JDK hash mismatch'
    assert sha(args.zig) == metadata['compiler']['shasum'], 'Zig hash mismatch'
    for prefix, upstream in [('runtime/', args.jdk), ('tools/zig/', args.zig)]:
        expected, _ = scan(upstream, 1)
        for name, value in expected.items():
            assert files.get(prefix + name) == value, 'Upstream archive changed: ' + prefix + name
        extras = set(n.removeprefix(prefix) for n in files if n.startswith(prefix)) - set(expected)
        assert extras == ({'bin/clang++'} if prefix == 'tools/zig/' else set()), extras
    required = ['LICENSE', 'THIRD-PARTY.md', 'licenses/loris/COPYING', 'licenses/loris/OOURA-LICENSE.txt',
                'licenses/lame/COPYING', 'licenses/alsa/COPYING', 'licenses/pulse/LICENSE',
                'licenses/miniaudio/LICENSE.txt', 'licenses/dr_mp3/LICENSE.txt',
                'licenses/upstream/antlr-2.7.7-LICENSE.txt', 'licenses/upstream/jogl-2.6.0-LICENSE.txt',
                'licenses/upstream/gluegen-2.6.0-LICENSE.txt', 'licenses/zig-runtime/lib_libcxx_LICENSE.TXT',
                'runtime/legal/java.base/LICENSE', 'runtime/legal/java.desktop/freetype.md']
    for name in required:
        assert files.get(name, {}).get('size', 0) > 0, 'Missing notice: ' + name
    jars = []
    for name, content in saved.items():
        if not name.endswith('.jar'):
            continue
        with zipfile.ZipFile(io.BytesIO(content)) as jar:
            entries = jar.namelist()
            assert not any('oculusvr' in n.lower() or 'libovr' in n.lower() for n in entries), 'Review Oculus artifacts'
            jars.append({'path': name, 'sha256': files[name]['sha256'],
                         'fonts': [n for n in entries if n.lower().endswith(('.ttf', '.otf', '.woff', '.woff2'))],
                         'nativeLibraries': [n for n in entries if n.endswith(('.so', '.dll', '.dylib'))]})
    with zipfile.ZipFile(io.BytesIO(saved['sources/EasyKnack-project-sources.zip'])) as source:
        snapshot = json.loads(source.read('source-snapshot.json'))
        for name, digest in snapshot['files'].items():
            assert hashlib.sha256(source.read('easy-knack/' + name)).hexdigest() == digest, 'Source snapshot mismatch: ' + name
        assert all('easy-knack/' + n in source.namelist() for n in ['pom.xml', 'scripts/build-linux-cross.py',
                   'scripts/build-posix.sh', 'native/audio-host/CMakeLists.txt',
                   'native/audio-host/third_party/lame/easyknack/config.h'])
        lock = json.loads(source.read('easy-knack/scripts/sources.lock.json'))
        pins = {item['file']: item['sha256'] for item in lock['binaries']}
        for name, value in files.items():
            filename = name.rsplit('/', 1)[-1]
            if name.startswith('lib/') and filename in pins and not filename.startswith('glswing-'):
                assert value['sha256'] == pins[filename], 'Previously reviewed JAR changed: ' + name
        with zipfile.ZipFile(io.BytesIO(saved['easy-knack-' + metadata['version'] + '.jar'])) as app:
            with zipfile.ZipFile(io.BytesIO(app.read('easyknack/plugin-sdk.zip'))) as sdk:
                for name in sdk.namelist():
                    original = 'easy-knack/native/audio-host/' + name
                    if name.startswith(('src/', 'third_party/')) and original in source.namelist():
                        assert sdk.read(name) == source.read(original), 'Native SDK/source disagreement: ' + name
    core_source = next(content for name, content in saved.items() if name.startswith('sources/glswing-') and name.endswith('-sources.jar'))
    with zipfile.ZipFile(io.BytesIO(core_source)) as source, zipfile.ZipFile(args.core_inputs) as build:
        for name in source.namelist():
            if name.endswith('/') or name.startswith('META-INF/'):
                continue
            candidates = [p for p in build.namelist() if p.endswith('/src/main/java/' + name) or p.endswith('/src/main/resources/' + name)]
            assert len(candidates) == 1 and source.read(name) == build.read(candidates[0]), 'Core source mismatch: ' + name
    with zipfile.ZipFile(args.core_rebuilt) as rebuilt, zipfile.ZipFile(io.BytesIO(saved['lib/glswing-0.2.0-SNAPSHOT.jar'])) as original:
        names = set(rebuilt.namelist()) | set(original.namelist())
        differences = [n for n in sorted(names) if not n.endswith('/') and (n not in rebuilt.namelist() or n not in original.namelist() or rebuilt.read(n) != original.read(n))]
        allowed = {'META-INF/glswing/build.properties', 'META-INF/maven/com.vingame/glswing/pom.properties', 'META-INF/maven/com.vingame/glswing/pom.xml'}
        assert set(differences) <= allowed, 'Core runtime rebuild mismatch: ' + str(differences)
    jdk_build = json.loads(args.jdk_metadata.read_text())
    assert jdk_build['sha256'] == metadata['runtime']['binary']['package']['checksum']
    with tarfile.open(args.jdk_build_sources) as source:
        roots = {e.name.split('/')[0] for e in source}
        assert roots == {'temurin-build-' + jdk_build['buildRef'].rsplit('/', 1)[1]}, 'Wrong JDK build revision'
    report = {'format': 1, 'archive': args.archive.name, 'sha256': sha(args.archive),
              'scope': 'File/provenance/source checks, not legal clearance or Linux execution',
              'checks': {'jdkAndCompilerUnmodified': True, 'requiredNoticesPresent': True,
                         'projectSourceSnapshotVerified': True, 'coreBuildSourcesMatchShippedSources': True,
                         'coreRuntimeRebuildMatches': True, 'nativeSdkMatchesProjectSources': True,
                         'previouslyReviewedSharedJarHashesMatch': True},
              'coreMetadataOnlyRebuildDifferences': differences, 'jars': jars,
              'jdkBuildRef': jdk_build['buildRef'], 'jdkSourceRef': jdk_build['openjdk_source'],
              'supplement': {p.name: sha(p) for p in [args.core_inputs, args.jdk_build_sources, args.jdk_metadata]},
              'files': files}
    args.output.mkdir(parents=True)
    (args.output / 'linux-license-evidence.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print('Verified Linux license evidence for ' + str(len(files)) + ' files/links and ' + str(len(jars)) + ' JARs.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('archive', 'jdk', 'zig', 'core-inputs', 'core-rebuilt', 'jdk-metadata', 'jdk-build-sources', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    audit(parser.parse_args())
