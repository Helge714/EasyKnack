#!/usr/bin/env python3
"""Install EasyKnack's checksum-pinned public GLSwing SDK locally."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

def fetch(url, sha256, output):
    if not output.exists():
        with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent':'EasyKnack-build'}), timeout=120) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != sha256:
            raise ValueError('GLSwing download checksum mismatch')
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(data)
    if hashlib.sha256(output.read_bytes()).hexdigest() != sha256:
        raise ValueError('GLSwing cache checksum mismatch: ' + str(output))
    return output

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--maven',default='mvn')
    parser.add_argument('--repository',type=Path)
    args=parser.parse_args()
    root=Path(__file__).resolve().parents[1]
    pin=json.loads((root/'scripts/glswing-release.json').read_text())
    version=ET.parse(root/'pom.xml').findtext('{*}properties/{*}glswing.version')
    if version!=pin['version']:
        if version.endswith('-SNAPSHOT'):
            raise SystemExit('This branch requires external GLSwing '+version+'. Install it from a separate GLSwing checkout with mvn -pl glswing-core -am install. The public SDK bootstrap remains pinned to '+pin['version']+'.')
        raise ValueError('Maven version and GLSwing pin disagree')
    archive=fetch(pin['sdk']['url'],pin['sdk']['sha256'],root/'target/glswing-sdk'/pin['sdk']['file'])
    destination=root/'target/glswing-sdk/extracted'
    with zipfile.ZipFile(archive) as z:
        for item in z.infolist():
            path=Path(item.filename)
            if path.is_absolute() or '..' in path.parts or '\\' in item.filename:raise ValueError('Unsafe SDK path')
        z.extractall(destination)
    sdk=destination/('GLSwing-'+version)
    for artifact in ['glswing-parent','glswing','glswing-lsp','glswing-filedialogs-swt']:
        name=artifact+'-'+version;pom=sdk/'poms'/(name+'.pom')
        command=[args.maven,'-B','-N','org.apache.maven.plugins:maven-install-plugin:3.1.3:install-file','-DpomFile='+str(pom),'-DgeneratePom=false']
        command+=['-Dfile='+str(pom if artifact=='glswing-parent' else sdk/'lib'/(name+'.jar'))]
        if artifact!='glswing-parent':command+=['-Dsources='+str(sdk/'sources'/(name+'-sources.jar')),'-Djavadoc='+str(sdk/'javadoc'/(name+'-javadoc.jar'))]
        if args.repository:command+=['-DlocalRepositoryPath='+str(args.repository.resolve())]
        subprocess.run(command,cwd=root,check=True)
    print('Installed verified public GLSwing '+version+' locally.')

if __name__=='__main__':main()
