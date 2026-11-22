#!/usr/bin/env python3
"""Build an architecture-specific app/DMG on macOS; never publish or overwrite a release."""
import argparse
import hashlib
import json
import os
import platform
import plistlib
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def run(*args):
    subprocess.run([str(a) for a in args], check=True)


def digest(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def source_snapshot(destination):
    names = subprocess.check_output(['git', 'ls-files', '-z', '--cached', '--others', '--exclude-standard'], cwd=ROOT).decode().split('\0')
    files = {}
    with zipfile.ZipFile(destination, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for name in sorted(set(names)):
            if not re.match(r'^(src/|native/|scripts/|docs/|examples/|\.github/|[^/]+\.(md|xml|bat)$|LICENSE$|\.gitignore$|\.gitattributes$|\.editorconfig$)', name):
                continue
            if re.search(r'(^|/)(target|build|\.git|\.aws|\.codex|\.settings|__pycache__)/|\.(exe|dll|class|jar|zip|pdb|obj|key|pfx|pem|pyc)$', name):
                continue
            path = ROOT / name
            if path.is_file():
                if path.is_symlink():
                    raise ValueError('Source symlink requires review: ' + name)
                files[name] = digest(path)
                archive.write(path, 'easy-knack/' + name)
        archive.writestr('source-snapshot.json', json.dumps({'commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(), 'files': files}, indent=2) + '\n')


def package(args):
    if platform.system() != 'Darwin':
        raise RuntimeError('A real macOS build environment is required')
    if args.output.exists():
        raise ValueError('Use a fresh output directory')
    args.output.mkdir(parents=True)
    home = Path(os.environ['JAVA_HOME']).resolve()
    arch = platform.machine()
    if arch not in ('arm64', 'x86_64'):
        raise ValueError('Unsupported Mac architecture')
    namespace = {'m': 'http://maven.apache.org/POM/4.0.0'}
    version = ET.parse(ROOT / 'pom.xml').findtext('m:version', namespaces=namespace)
    jar_name = 'easy-knack-' + version + '.jar'
    metadata = json.loads((args.inputs / 'runtime-metadata.json').read_text())
    if metadata['binary']['os'] != 'mac' or digest(args.inputs / 'jdk.tar.gz') != metadata['binary']['package']['checksum']:
        raise ValueError('Wrong or changed bundled JDK')
    if metadata['binary']['architecture'] != ('aarch64' if arch == 'arm64' else 'x64'):
        raise ValueError('Wrong runtime architecture')
    if digest(args.inputs / metadata['source']['name']) != metadata['source']['sha256']:
        raise ValueError('OpenJDK sources do not match metadata')
    if digest(args.inputs / 'temurin-build.tar.gz') != metadata['buildSources']['sha256']:
        raise ValueError('Temurin build sources changed')
    with tempfile.TemporaryDirectory(prefix='easyknack-macos-') as temp:
        stage = Path(temp) / 'input'
        stage.mkdir()
        def put(source, name):
            target = stage / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, target)
        put(args.java_build / jar_name, jar_name)
        with zipfile.ZipFile(stage / jar_name) as jar:
            manifest = jar.read('META-INF/MANIFEST.MF').decode().replace('\r\n ', '').replace('\n ', '')
            dependencies = re.search(r'(?m)^Class-Path: (.+)$', manifest).group(1).strip().split()
        for name in dependencies:
            if not re.fullmatch(r'lib/[^/\\]+\.jar', name):
                raise ValueError('Unsafe manifest dependency')
            if '-natives-' in name and '-natives-macosx-universal' not in name:
                continue
            put(args.java_build / name, name)
            if '-natives-' in name:
                with zipfile.ZipFile(stage / name) as natives:
                    for entry in natives.namelist():
                        if entry.endswith(('.jnilib', '.dylib', '.so')):
                            extracted = Path(temp) / Path(entry).name
                            extracted.write_bytes(natives.read(entry))
                            run('/usr/bin/lipo', '-verify_arch', arch, extracted)
        if len(list((stage / 'lib').glob('*-natives-*.jar'))) != 2:
            raise ValueError('Expected two macOS universal JogAmp native JARs')
        put(args.native / 'easyknack-audio-host', 'native/easyknack-audio-host')
        put(args.native / 'libmp3lame.0.dylib', 'native/libmp3lame.dylib')
        for name in ('command-reader-test', 'audio-graph-test', 'midi-port-bank-test', 'routed-audio-test', 'mac-midi-test', 'lame-recording-test', 'portable-platform-test', 'libplatform-library-fixture.dylib'):
            put(args.native / name, 'tests/' + name)
        put(ROOT / 'native/audio-host/tests/HostProtocolTest.py', 'tests/HostProtocolTest.py')
        put(ROOT / 'scripts/linux/NativeCompilationCheck.java', 'tests/NativeCompilationCheck.java')
        put(ROOT / 'native/audio-host/tests/PlatformLibraryFixture.cpp', 'tests/PlatformLibraryFixture.cpp')
        put(ROOT / 'scripts/macos/clang++', 'tools/apple/bin/clang++')
        for path in (stage / 'native').iterdir():
            path.chmod(0o755)
            run('/usr/bin/lipo', '-verify_arch', arch, path)
        for path in (stage / 'tests').iterdir():
            if path.suffix not in ('.java', '.cpp', '.py'):
                path.chmod(0o755)
        (stage / 'tools/apple/bin/clang++').chmod(0o755)
        (stage / 'application-jar.txt').write_text(jar_name + '\n')
        (stage / 'installation.properties').write_text('format=1\nplatform=macos-' + arch + '\njava=bundled\n')
        for name in ('LICENSE', 'THIRD-PARTY.md'):
            put(ROOT / name, name)
        shutil.copytree(ROOT / 'src/main/resources/easyknack/notices', stage / 'licenses/upstream')
        for jar in (stage / 'lib').glob('*.jar'):
            with zipfile.ZipFile(jar) as archive:
                for entry in archive.infolist():
                    if not entry.is_dir() and re.search(r'(?i)(license|copying|notice)', entry.filename):
                        target = stage / 'licenses' / jar.stem / re.sub(r'[^a-zA-Z0-9._-]', '_', entry.filename)
                        target.parent.mkdir(parents=True, exist_ok=True)
                        target.write_bytes(archive.read(entry))
        for component in ('loris', 'lame', 'alsa', 'pulse'):
            for file in (ROOT / 'native/audio-host/third_party' / component).iterdir():
                if file.name in ('COPYING', 'LICENSE', 'LGPL', 'GPL', 'AUTHORS', 'PROVENANCE.md', 'PROVENANCE.json', 'OOURA-LICENSE.txt'):
                    put(file, 'licenses/' + component + '/' + file.name)
        for library in ('miniaudio', 'dr_mp3'):
            content = (ROOT / ('native/audio-host/third_party/' + library + '.h')).read_text()
            marker = content.rfind('This software is available as a choice of the following licenses.')
            if marker < 0:
                raise ValueError('Missing native notice: ' + library)
            target = stage / 'licenses' / library / 'LICENSE.txt';target.parent.mkdir(parents=True)
            target.write_text(content[marker:].replace('*/', '').strip() + '\n')
        sources = stage / 'sources';sources.mkdir()
        source_snapshot(sources / 'EasyKnack-project-sources.zip')
        for source in (args.inputs / 'glswing-build-inputs.zip', args.inputs / metadata['source']['name'], args.inputs / 'temurin-build.tar.gz', args.inputs / 'jdk-build-metadata.json'):
            shutil.copyfile(source, sources / source.name)
        for source in args.inputs.glob('*sbom*.json'):
            shutil.copyfile(source, sources / source.name)
        classpath = str(stage / jar_name) + ':' + str(stage / 'lib/*')
        for parameters in ([str(stage / 'Examples')], ['--verify', str(stage / 'Examples')]):
            run(home / 'bin/java', '-Deasyknack.language=en', '-cp', classpath, 'com.vin.easyknack.ui.ExampleLibrary', *parameters)
        (stage / 'macos-build.json').write_text(json.dumps({'version': version, 'architecture': arch, 'runtime': metadata, 'hardwareAcceptance': 'not performed; test on physical Mac', 'signing': 'ad-hoc test' if args.sign_identity == '-' else 'Developer ID'}, indent=2) + '\n')
        iconset = Path(temp) / 'EasyKnack.iconset';iconset.mkdir()
        for size in (16, 32, 128, 256, 512):
            for scale in (1, 2):
                run('/usr/bin/sips', '-z', size*scale, size*scale, ROOT / 'src/main/resources/branding/EKIcon.png', '--out', iconset / ('icon_' + str(size) + 'x' + str(size) + ('@2x' if scale == 2 else '') + '.png'))
        icon = Path(temp) / 'EasyKnack.icns'
        run('/usr/bin/iconutil', '-c', 'icns', iconset, '-o', icon)
        run(home / 'bin/jpackage', '--type', 'app-image', '--name', 'EasyKnack', '--app-version', version.removesuffix('-SNAPSHOT'), '--vendor', 'EasyKnack', '--input', stage,
            '--main-jar', jar_name, '--runtime-image', home, '--dest', args.output,
            '--java-options', '--enable-native-access=ALL-UNNAMED', '--java-options', '--add-opens=java.base/java.lang=ALL-UNNAMED',
            '--java-options', '-Dapple.awt.application.name=EasyKnack',
            '--java-options', '--add-opens=java.desktop/sun.awt=ALL-UNNAMED', '--java-options', '--add-opens=java.desktop/sun.java2d=ALL-UNNAMED',
            '--icon', icon, '--mac-package-identifier', 'com.vin.easyknack')
        app = args.output / 'EasyKnack.app'
        info = app / 'Contents/Info.plist'
        plist = plistlib.loads(info.read_bytes())
        plist.update({'LSMinimumSystemVersion': '14.2', 'NSHighResolutionCapable': True, 'CFBundleDevelopmentRegion': 'en', 'CFBundleLocalizations': ['en', 'de'],
                      'NSMicrophoneUsageDescription': 'EasyKnack uses microphone and audio-interface inputs only when selected in your graph.',
                      'NSAudioCaptureUsageDescription': 'EasyKnack captures system or application audio only when selected in your graph.',
                      'CFBundleDocumentTypes': [{'CFBundleTypeName': 'EasyKnack Project', 'CFBundleTypeExtensions': ['knack'], 'CFBundleTypeRole': 'Editor', 'LSHandlerRank': 'Owner'}]})
        info.write_bytes(plistlib.dumps(plist))
        localized = app / 'Contents/Resources/de.lproj';localized.mkdir(exist_ok=True)
        (localized / 'InfoPlist.strings').write_text('"NSMicrophoneUsageDescription" = "EasyKnack verwendet Mikrofon- und Audiointerface-Eingänge nur, wenn sie im Graphen ausgewählt sind.";\n"NSAudioCaptureUsageDescription" = "EasyKnack nimmt System- oder Anwendungsaudio nur auf, wenn es im Graphen ausgewählt ist.";\n', encoding='utf-8')
        if not (app / 'Contents/runtime/Contents/Home/bin/javac').is_file():
            raise ValueError('Full bundled JDK missing javac')
        # Sign inside-out. Entitlements permit HotSpot JIT and user-built Formula/JNI
        # libraries, not sandbox escape; this app is not an App Store sandbox build.
        macho = (b'\xcf\xfa\xed\xfe', b'\xfe\xed\xfa\xcf', b'\xca\xfe\xba\xbe', b'\xbe\xba\xfe\xca', b'\xca\xfe\xba\xbf')
        for path in sorted(app.rglob('*'), key=lambda p: len(p.parts), reverse=True):
            if path.is_file() and not path.is_symlink():
                with path.open('rb') as stream:
                    native = stream.read(4) in macho
                if native:
                    run('/usr/bin/codesign', '--force', '--sign', args.sign_identity, '--options', 'runtime', '--entitlements', ROOT / 'scripts/macos/entitlements.plist', '--timestamp=none' if args.sign_identity == '-' else '--timestamp', path)
        for bundle in (app / 'Contents/runtime', app):
            run('/usr/bin/codesign', '--force', '--sign', args.sign_identity, '--options', 'runtime', '--entitlements', ROOT / 'scripts/macos/entitlements.plist', '--timestamp=none' if args.sign_identity == '-' else '--timestamp', bundle)
        run('/usr/bin/codesign', '--verify', '--deep', '--strict', app)
        if args.notary_profile:
            if args.sign_identity == '-':
                raise ValueError('Notarization requires Developer ID signing')
            submission = args.output / 'notary-app.zip'
            run('/usr/bin/ditto', '-c', '-k', '--keepParent', app, submission)
            run('/usr/bin/xcrun', 'notarytool', 'submit', submission, '--keychain-profile', args.notary_profile, '--wait')
            run('/usr/bin/xcrun', 'stapler', 'staple', app)
        volume = args.output / 'image';volume.mkdir();shutil.move(app, volume / app.name);app = volume / app.name
        os.symlink('/Applications', volume / 'Applications')
        for source, target in [('README.md', 'README.md'), ('Check-Mac.command', 'Check-Mac.command')]:
            shutil.copyfile(ROOT / 'scripts/macos' / source, volume / target)
        (volume / 'Check-Mac.command').chmod(0o755)
        image = args.output / ('EasyKnack-' + version + '-macos-' + arch + '-experimental.dmg')
        run('/usr/bin/hdiutil', 'create', '-volname', 'EasyKnack', '-srcfolder', volume, '-format', 'UDZO', image)
        if args.sign_identity != '-':
            run('/usr/bin/codesign', '--sign', args.sign_identity, '--timestamp', image)
        if args.notary_profile:
            run('/usr/bin/xcrun', 'notarytool', 'submit', image, '--keychain-profile', args.notary_profile, '--wait')
            run('/usr/bin/xcrun', 'stapler', 'staple', image)
        image.with_suffix('.dmg.sha256').write_text(digest(image) + '  ' + image.name + '\n')
        print('macOS package created (hardware acceptance pending): ' + str(image))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java-build', 'native', 'inputs', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    parser.add_argument('--sign-identity', default='-')
    parser.add_argument('--notary-profile')
    package(parser.parse_args())
