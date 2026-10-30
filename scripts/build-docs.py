#!/usr/bin/env python3
"""Build and validate the bilingual documentation preview using Python 3.10+."""
import argparse
import json
from html.parser import HTMLParser
import os
from pathlib import Path
from urllib.parse import unquote, urlsplit

from documentation_renderer import render

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / 'docs'


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
        entries = json.loads(catalog.read_text(encoding='utf-8'))['nodes']
        for language in ('de', 'en'):
            sources.add(DOCS / 'nodes' / language / 'index.md')
            sources.update(DOCS / 'nodes' / language / (node['slug']+'.md') for node in entries)
        if len({node['id'] for node in entries}) != len(entries) or len({node['slug'] for node in entries}) != len(entries):
            raise ValueError('Duplicate node documentation ID or slug')
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
