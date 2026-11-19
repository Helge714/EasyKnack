#!/usr/bin/env python3
"""Generate the native registry without PowerShell (CMake Linux/macOS builds)."""
import argparse,json,re
from pathlib import Path

def generate(nodes,output):
    pattern=re.compile(r'^\s*EASYKNACK_NODE\(\s*([A-Za-z_][A-Za-z0-9_:]*)\s*,\s*"([a-z][a-z0-9_-]*)"\s*\)\s*$',re.M)
    registrations=[];seen=set()
    for header in sorted(nodes.rglob('*.hpp'),key=lambda p:p.as_posix().casefold()):
        text=header.read_text(encoding='utf-8-sig');matches=list(pattern.finditer(text))
        if len(re.findall(r'^\s*EASYKNACK_NODE',text,re.M))!=len(matches):raise ValueError('Malformed EASYKNACK_NODE: '+str(header))
        for match in matches:
            kind,identity=match.groups()
            if identity in seen:raise ValueError('Duplicate native processor ID: '+identity)
            seen.add(identity);registrations.append({'id':identity,'type':kind,'header':'nodes/'+header.relative_to(nodes).as_posix()})
    if not registrations:raise ValueError('No native registrations')
    lines=['// Generated native node registry. Do not edit.','#pragma once','#include <memory>','#include <string>','#include <stdexcept>']
    for header in dict.fromkeys(r['header'] for r in registrations):lines.append('#include "'+header+'"')
    lines+=['namespace easyknack {','inline std::unique_ptr<NodeProcessor> createNodeProcessor(const std::string& id) {']
    lines+=['    if (id == "'+r['id']+'") return std::make_unique<'+r['type']+'>();' for r in registrations]
    lines+=['    throw std::runtime_error("unknown node type: " + id);','}','}']
    output.mkdir(parents=True,exist_ok=True)
    for name,content in [('GeneratedNodeRegistry.hpp','\n'.join(lines)+'\n'),('nodes.json',json.dumps(registrations,indent=2)+'\n')]:
        path=output/name
        if not path.exists() or path.read_text(encoding='utf-8')!=content:path.write_text(content,encoding='utf-8')
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--nodes',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args();generate(args.nodes,args.output)
