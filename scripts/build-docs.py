#!/usr/bin/env python3
"""Build and validate the bilingual documentation preview using Python 3.10+."""
import argparse
import json
from html.parser import HTMLParser
import os
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit

from documentation_renderer import render

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / 'docs'


def validate_catalog(data):
    """Audit identities and maintained pages, never derive reference content."""
    if data.get('schema') != 1:
        raise ValueError('Unsupported node documentation catalog schema')
    entries = data['nodes']
    excluded = data.get('excluded', [])
    for key in ('id', 'slug'):
        if len({entry[key] for entry in entries}) != len(entries):
            raise ValueError(f'Duplicate node documentation {key}')
    ids = {entry['id'] for entry in entries}
    omitted = {entry['id'] for entry in excluded}
    if len(omitted) != len(excluded) or ids & omitted:
        raise ValueError('Duplicate or documented exclusion in node catalog')
    if any(not entry.get('reason', '').strip() for entry in excluded):
        raise ValueError('Every node exclusion needs a reason')
    registered = {}
    for source in (ROOT / 'src/main/java/com/vin/easyknack/nodes').glob('*Node.java'):
        text = source.read_text(encoding='utf-8')
        match = re.search(r'@RegisterNode\(\s*id\s*=\s*"([^"]+)"', text)
        if match:
            hidden = bool(re.search(r'isListedInCatalog\s*\(\)\s*\{\s*return false', text))
            registered[match[1]] = hidden
    missing = registered.keys() - ids - omitted
    if missing:
        raise ValueError(f'Node documentation coverage missing: {sorted(missing)}')
    invalid = {id for id in omitted if id not in registered or not registered[id]}
    if invalid:
        raise ValueError(f'Only registered internal/legacy nodes may be excluded: {sorted(invalid)}')
    unknown = {entry['id'] for entry in entries
               if entry['id'] not in registered and entry.get('kind') != 'group'}
    if unknown:
        raise ValueError(f'Unknown documented node IDs: {sorted(unknown)}')
    for language in ('en', 'de'):
        index = DOCS / 'nodes' / language / 'index.md'
        index_text = index.read_text(encoding='utf-8')
        for entry in entries:
            slug = entry['slug']
            if not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', slug):
                raise ValueError(f'Invalid documentation slug: {slug}')
            page = index.parent / (slug + '.md')
            if not page.is_file():
                raise ValueError(f'Node translation missing: {page}')
            if f']({slug}.md)' not in index_text:
                raise ValueError(f'Node missing from {language} index: {slug}')
            text = page.read_text(encoding='utf-8')
            required = (['Overview', 'Inputs', 'Outputs', 'Behavior', 'Mute', 'Insertion', 'Delayable']
                        if language == 'en' else
                        ['Übersicht', 'Eingänge', 'Ausgänge', 'Verhalten', 'Mute', 'Einfügen', 'Delayable'])
            headings = re.findall(r'^## (.+)$', text, flags=re.MULTILINE)
            if headings != required:
                raise ValueError(f'Unexpected node reference sections: {page}: {headings}')
            expected = ([['Name', 'Data type', 'Editor range', 'Signal range', 'Default', 'Unit', 'Rate', 'Visibility'],
                         ['Name', 'Data type', 'Range', 'Rate', 'Channel'],
                         ['Mutable', 'Input', 'Output'], ['Input', 'Output'], ['Sample-wise feedback']]
                        if language == 'en' else
                        [['Bezeichnung', 'Datentyp', 'Reglerbereich', 'Signalbereich', 'Standardwert', 'Einheit', 'Rate', 'Sichtbarkeit'],
                         ['Bezeichnung', 'Datentyp', 'Wertebereich', 'Rate', 'Kanal'],
                         ['Mutable', 'Eingang', 'Ausgang'], ['Eingang', 'Ausgang'], ['Sampleweises Feedback']])
            for heading, columns in zip([required[i] for i in (1, 2, 4, 5, 6)], expected):
                section = re.search(r'^## ' + re.escape(heading) + r'\n(.*?)(?=^## |\Z)', text,
                                    flags=re.MULTILINE | re.DOTALL)[1]
                rows = [line.strip().strip('|').split('|') for line in section.splitlines() if line.startswith('|')]
                if len(rows) < 3 or any(len(row) != len(columns) for row in rows):
                    raise ValueError(f'Wrong table columns or missing rows: {page}: {heading}')
                labels = [re.sub(r'\[([^]]+)\]\([^)]+\)', r'\1', cell.strip()) for cell in rows[0]]
                if labels != columns:
                    raise ValueError(f'Wrong table headings: {page}: {heading}: {labels}')
    return len(entries)


class Links(HTMLParser):
    def __init__(self):
        super().__init__()
        self.ids = []
        self.links = []

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if 'id' in attrs:
            self.ids.append(attrs['id'])
        if tag == 'a' and 'href' in attrs:
            self.links.append(attrs['href'])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'target/docs')
    parser.add_argument('--page', type=Path, action='append', default=[],
                        help='Additional Markdown page relative to the repository root')
    args = parser.parse_args()
    output = args.output.resolve()
    sources = set((DOCS / 'user/de').glob('*.md')) | set((DOCS / 'user/en').glob('*.md'))
    catalog = DOCS / 'nodes/catalog.json'
    if catalog.is_file():
        data = json.loads(catalog.read_text(encoding='utf-8'))
        validate_catalog(data)
        entries = data['nodes']
        for language in ('de', 'en'):
            sources.add(DOCS / 'nodes' / language / 'index.md')
            sources.update(DOCS / 'nodes' / language / (node['slug']+'.md') for node in entries)
    sources.update((ROOT / page).resolve() for page in args.page)
    names = [{p.name for p in (DOCS / 'user' / language).glob('*.md')} for language in ('de', 'en')]
    if names[0] != names[1]:
        raise ValueError(f'Foundation language coverage differs: {names[0] ^ names[1]}')
    destinations = {}
    for source in sources:
        if not source.is_file() or source.suffix != '.md':
            raise ValueError(f'Markdown page missing: {source}')
        destinations[source] = output / source.relative_to(DOCS).with_suffix('.html')

    count = 0
    for source in sorted(sources):
        def resolve(url):
            nonlocal count
            parsed = urlsplit(url)
            if parsed.scheme or parsed.netloc:
                return url
            target = (source.parent / unquote(parsed.path)).resolve() if parsed.path else source
            if not target.is_file():
                raise ValueError(f'Missing documentation target: {source} -> {url}')
            count += 1
            fragment = '#' + parsed.fragment if parsed.fragment else ''
            if target in destinations:
                return os.path.relpath(destinations[target], destinations[source].parent).replace(os.sep, '/') + fragment
            # Legacy pages remain Markdown sources until reviewed and migrated.
            return target.as_uri() + fragment
        render(source, destinations[source], resolve)

    parsed_pages = {}
    for destination in destinations.values():
        document = Links()
        document.feed(destination.read_text(encoding='utf-8'))
        if len(document.ids) != len(set(document.ids)):
            raise ValueError(f'Duplicate section IDs: {destination}')
        parsed_pages[destination] = document
    for destination, document in parsed_pages.items():
        for url in document.links:
            parsed = urlsplit(url)
            if parsed.scheme or parsed.netloc:
                continue
            target = (destination.parent / unquote(parsed.path)).resolve() if parsed.path else destination
            if target not in parsed_pages:
                raise ValueError(f'Missing generated page: {destination} -> {url}')
            if parsed.fragment and unquote(parsed.fragment) not in parsed_pages[target].ids:
                raise ValueError(f'Missing section: {destination} -> {url}')
    print(f'Built {len(destinations)} pages; checked {count} local links, section IDs and language coverage.')
    print(output / 'user/de/index.html')
    print(output / 'nodes/de/index.html')


if __name__ == '__main__':
    main()
