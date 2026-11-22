#!/usr/bin/env python3
"""Fetch hash-checked public build inputs on macOS; no Apple SDK is redistributed."""
import argparse
import hashlib
import io
import json
import os
import platform
from pathlib import Path
import tarfile
import urllib.request
from urllib.parse import urlsplit
import zipfile


def fetch(url):
    headers = {'User-Agent': 'EasyKnack-macOS-build'}
    if urlsplit(url).hostname == 'api.github.com' and os.environ.get('GH_TOKEN'):
        headers['Authorization'] = 'Bearer ' + os.environ['GH_TOKEN']
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=120) as response:
        return response.read()


def unpack_core(inputs, output):
    with zipfile.ZipFile(io.BytesIO(inputs)) as archive:
        for entry in archive.infolist():
            path = Path(entry.filename)
            if path.is_absolute() or '..' in path.parts or not path.parts or path.parts[0] != 'glswing':
                raise ValueError('Unsafe Core source path')
            destination = output / Path(*path.parts[1:])
            if entry.is_dir():
                destination.mkdir(parents=True, exist_ok=True)
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(archive.read(entry))


def prepare(output):
    if platform.system() != 'Darwin':
        raise RuntimeError('Run on the target macOS runner')
    if output.exists():
        raise ValueError('Use a fresh input directory')
    output.mkdir(parents=True)
    architecture = 'aarch64' if platform.machine() == 'arm64' else 'x64'
    # The Adoptium API may reject shared CI IPs. Its official GitHub release
    # provides the same signed archives, checksum sidecars and build metadata.
    release = json.loads(fetch('https://api.github.com/repos/adoptium/temurin25-binaries/releases/latest'))
    runtime_asset = next(a for a in release['assets'] if a['name'].startswith('OpenJDK25U-jdk_' + architecture + '_mac_hotspot_') and a['name'].endswith('.tar.gz'))
    runtime_url = runtime_asset['browser_download_url']
    metadata_url = runtime_url + '.json'
    package = {'name': runtime_asset['name'], 'link': runtime_url, 'metadata_link': metadata_url,
               'checksum': fetch(runtime_url + '.sha256.txt').decode().split()[0]}
    asset = {'release_link': release['html_url'], 'release_name': release['tag_name'],
             'binary': {'os': 'mac', 'architecture': architecture, 'image_type': 'jdk', 'package': package}}
    runtime = fetch(package['link'])
    assert hashlib.sha256(runtime).hexdigest() == package['checksum']
    (output / 'jdk.tar.gz').write_bytes(runtime)
    with tarfile.open(fileobj=io.BytesIO(runtime)) as archive:
        archive.extractall(output / 'runtime', filter='data')
    java = next((output / 'runtime').glob('*/Contents/Home/bin/java'))
    home = java.parent.parent.resolve()
    source = next(a for a in release['assets'] if a['name'].startswith('OpenJDK25U-jdk-sources_') and a['name'].endswith('.tar.gz'))
    source_bytes = fetch(source['browser_download_url'])
    checksum = fetch(source['browser_download_url'] + '.sha256.txt').decode().split()[0]
    assert hashlib.sha256(source_bytes).hexdigest() == checksum
    (output / source['name']).write_bytes(source_bytes)
    asset['source'] = {'name': source['name'], 'url': source['browser_download_url'], 'sha256': checksum}
    metadata = json.loads(fetch(package['metadata_link']))
    assert metadata['sha256'] == package['checksum']
    revision = metadata['buildRef'].rsplit('/', 1)[1]
    build_sources = fetch('https://codeload.github.com/adoptium/temurin-build/tar.gz/' + revision)
    (output / 'temurin-build.tar.gz').write_bytes(build_sources)
    (output / 'jdk-build-metadata.json').write_text(json.dumps(metadata, indent=2) + '\n')
    asset['buildSources'] = {'revision': revision, 'sha256': hashlib.sha256(build_sources).hexdigest()}
    for item in release['assets']:
        if item['name'].startswith('OpenJDK25U-sbom_' + architecture + '_mac_hotspot_') and item['name'].endswith('.json'):
            (output / item['name']).write_bytes(fetch(item['browser_download_url']))
    core_url = 'https://github.com/Helge714/EasyKnack/releases/download/v0.4.0-linux-experimental.2/EasyKnack-Linux-test-2-license-materials.zip'
    core = fetch(core_url)
    assert hashlib.sha256(core).hexdigest() == 'd296f032ccffa2c635f90c74963c7f352175af64fc78471c35eafd37ee2639ef'
    with zipfile.ZipFile(io.BytesIO(core)) as supplement:
        inputs = supplement.read('sources/glswing-build-inputs.zip')
    (output / 'glswing-build-inputs.zip').write_bytes(inputs)
    unpack_core(inputs, output / 'core')
    (output / 'runtime-metadata.json').write_text(json.dumps(asset, indent=2) + '\n')
    (output / 'java-home.txt').write_text(str(home) + '\n')
    print('JAVA_HOME=' + str(home))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    prepare(parser.parse_args().output)
