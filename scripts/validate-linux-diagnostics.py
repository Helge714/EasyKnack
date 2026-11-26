#!/usr/bin/env python3
"""Compile the diagnostic against the exact Java artifacts in a Linux package."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tarfile
import tempfile


def validate(archive_path, javac, source=None):
    with tempfile.TemporaryDirectory(prefix='easyknack-diagnostic-') as temporary:
        root = Path(temporary)
        jar = None
        with tarfile.open(archive_path, 'r|gz') as archive:
            for member in archive:
                if not member.isfile():
                    continue
                name = Path(member.name)
                selected = (len(name.parts) == 2 and name.name.startswith('easy-knack-') and name.suffix == '.jar')
                selected |= len(name.parts) == 3 and name.parts[:2] == ('EasyKnack', 'lib') and name.suffix == '.jar'
                selected |= member.name == 'EasyKnack/tests/NativeCompilationCheck.java'
                if selected:
                    target = root / name.name
                    with archive.extractfile(member) as stream, target.open('wb') as output:
                        shutil.copyfileobj(stream, output)
                    if len(name.parts) == 2:
                        jar = target
        if jar is None:
            raise ValueError('Missing application JAR')
        source = source.resolve() if source else root / 'NativeCompilationCheck.java'
        subprocess.run([str(javac.resolve()), '-proc:none', '-cp', str(jar) + os.pathsep + str(root / '*'),
                        '-d', str(root / 'classes'), str(source)], check=True)
        print('Diagnostic compiles against the exact packaged application/dependencies.', flush=True)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--archive', type=Path, required=True)
    parser.add_argument('--javac', type=Path, required=True)
    parser.add_argument('--source', type=Path)
    args = parser.parse_args()
    validate(args.archive, args.javac, args.source)
