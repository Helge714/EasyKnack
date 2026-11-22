#!/usr/bin/env python3
"""Check actual Mac distribution/source contents; not a legal clearance certificate."""
import argparse
import hashlib
import json
from pathlib import Path
import tarfile
import zipfile


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def audit(app, output, jdk):
    base = app / 'Contents/app'
    runtime = app / 'Contents/runtime/Contents/Home'
    metadata = json.loads((base / 'macos-build.json').read_text())
    required = ['LICENSE', 'THIRD-PARTY.md', 'licenses/loris/COPYING',
                'licenses/loris/OOURA-LICENSE.txt', 'licenses/lame/COPYING',
                'licenses/miniaudio/LICENSE.txt', 'licenses/dr_mp3/LICENSE.txt',
                'licenses/upstream/antlr-2.7.7-LICENSE.txt',
                'licenses/upstream/jogl-2.6.0-LICENSE.txt',
                'licenses/upstream/gluegen-2.6.0-LICENSE.txt']
    for name in required:
        assert (base / name).stat().st_size > 0, name
    assert (runtime / 'legal/java.base/LICENSE').stat().st_size > 0
    assert (runtime / 'legal/java.desktop/freetype.md').stat().st_size > 0
    sources = base / 'sources'
    with zipfile.ZipFile(sources / 'EasyKnack-project-sources.zip') as archive:
        snapshot = json.loads(archive.read('source-snapshot.json'))
        for name, expected in snapshot['files'].items():
            assert hashlib.sha256(archive.read('easy-knack/' + name)).hexdigest() == expected, name
        for name in ['pom.xml', 'LICENSE', 'scripts/package-macos.py',
                     'native/audio-host/CMakeLists.txt',
                     'native/audio-host/src/platform/MacPlatform.mm',
                     'native/audio-host/third_party/lame/COPYING']:
            assert name in snapshot['files'], name
    upstream = metadata['runtime']
    assert sha(jdk) == upstream['binary']['package']['checksum']
    legal_verified = 0
    with tarfile.open(jdk) as archive:
        for entry in archive:
            if '/Contents/Home/legal/' in entry.name and entry.isfile():
                relative = entry.name.split('/Contents/Home/', 1)[1]
                assert (runtime / relative).read_bytes() == archive.extractfile(entry).read(), relative
                legal_verified += 1
    assert legal_verified > 10, 'Upstream legal tree missing'
    assert sha(sources / upstream['source']['name']) == upstream['source']['sha256']
    assert sha(sources / 'temurin-build.tar.gz') == upstream['buildSources']['sha256']
    assert (sources / 'jdk-build-metadata.json').stat().st_size > 0
    assert list(sources.glob('*sbom*.json')), 'Missing runtime SBOM'
    with tarfile.open(sources / upstream['source']['name']) as archive:
        assert any(n.endswith('/src/java.base/share/classes/java/lang/Object.java') for n in archive.getnames())
    with zipfile.ZipFile(sources / 'glswing-build-inputs.zip') as archive:
        names = archive.namelist()
        assert any(n.endswith('pom.xml') for n in names)
        assert any(n.endswith('.java') for n in names)
        assert any('LICENSE' in n for n in names)
    jars = []
    for path in sorted((base / 'lib').glob('*.jar')):
        with zipfile.ZipFile(path) as archive:
            assert not any('oculusvr' in n.lower() or 'libovr' in n.lower() for n in archive.namelist()), 'Unreviewed Oculus component'
            notices = [n for n in archive.namelist() if any(w in n.lower() for w in ('license', 'notice', 'copying'))]
        # JogAmp supplemental release texts above also cover native JARs.
        assert notices or path.name.startswith(('jogl', 'gluegen', 'antlr')), path.name
        jars.append({'name': path.name, 'sha256': sha(path), 'embeddedNotices': notices})
    assert (base / 'native/libmp3lame.dylib').is_file(), 'LAME must remain replaceable'
    assert not list(base.rglob('*.dll')) and not list(base.rglob('MacOSX*.sdk'))
    output.mkdir(parents=True, exist_ok=True)
    stem = 'EasyKnack-macos-' + metadata['architecture'] + '-license-materials'
    report = {'scope': 'Technical distribution review, not legal clearance',
              'sourceCommit': snapshot['commit'], 'build': metadata,
              'projectSourceFilesVerified': len(snapshot['files']), 'jars': jars,
              'unchangedUpstreamJavaLegalFiles': legal_verified,
              'requiredNotices': required,
              'legalFiles': {str(p.relative_to(runtime)): sha(p) for p in sorted((runtime / 'legal').rglob('*')) if p.is_file()},
              'limitations': ['Physical hardware acceptance pending', 'Ad-hoc signed; not notarized',
                             'Apple SDK/compiler externally installed, not redistributed']}
    report_path = output / (stem + '.json')
    assert not report_path.exists(), 'Do not overwrite audit outputs'
    report_path.write_text(json.dumps(report, indent=2) + '\n')
    with zipfile.ZipFile(output / (stem + '.zip'), 'x', compression=zipfile.ZIP_DEFLATED) as archive:
        archive.write(report_path, 'REVIEW.json')
        for directory in (sources, base / 'licenses', runtime / 'legal'):
            for path in sorted(directory.rglob('*')):
                if path.is_file():
                    prefix = 'java-legal/' if directory == runtime / 'legal' else directory.name + '/'
                    archive.write(path, prefix + str(path.relative_to(directory)))
        for name in ('LICENSE', 'THIRD-PARTY.md', 'macos-build.json'):
            archive.write(base / name, name)
    print('Verified Mac license/source materials:', metadata['architecture'], flush=True)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--jdk', type=Path, required=True)
    args = parser.parse_args()
    audit(args.app, args.output, args.jdk)
