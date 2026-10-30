"""HTML preview renderer for the node-reference Markdown subset.

Supports headings, tables, paragraphs, bullet lists, fenced code, inline code,
strong emphasis, links and explicit anchor IDs. It is not a general CommonMark
engine and is not wired to the application's documentation UI.
"""
from pathlib import Path
from html import escape
import re
import unicodedata


def render(source: Path, destination: Path, link_resolver=None):
    lines = source.read_text(encoding='utf-8').splitlines()
    headings = []
    reserved_ids = set(re.findall(r'<a id="([a-z0-9-]+)"></a>', '\n'.join(lines)))

    def resource_url(url):
        if link_resolver is not None:
            return link_resolver(url)
        path, marker, fragment = url.partition('#')
        return (source.parent/path).resolve().as_uri() + ('#'+fragment if marker else '')

    def inline(text):
        tokens = []
        def preserve(value):
            tokens.append(value)
            return f'\x00{len(tokens)-1}\x00'
        text = re.sub(r'`([^`]+)`', lambda m: preserve('<code>'+escape(m[1])+'</code>'), text)
        text = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', lambda m: preserve('<a href="'+escape(resource_url(m[2]), quote=True)+'">'+escape(m[1])+'</a>'), text)
        text = re.sub(r'<a id="[a-z0-9-]+"></a>', lambda m: preserve(m[0]), text)
        text = escape(text)
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        return re.sub(r'\x00(\d+)\x00', lambda m: tokens[int(m[1])], text)

    blocks = []
    i = 0
    while i < len(lines):
        line = lines[i]
        if not line.strip():
            i += 1
            continue
        if line.startswith('```'):
            content = []
            i += 1
            while i < len(lines) and not lines[i].startswith('```'):
                content.append(lines[i]); i += 1
            blocks.append('<pre><code>'+escape('\n'.join(content))+'</code></pre>')
            i += 1
        elif line.startswith('#'):
            level = len(line)-len(line.lstrip('#'))
            title = line[level:].strip()
            slug = re.sub(r'[^a-z0-9]+', '-', unicodedata.normalize('NFKD', title).encode('ascii', 'ignore').decode().lower()).strip('-')
            base_slug = slug
            counter = 2
            while slug in reserved_ids or any(h[2] == slug for h in headings):
                slug = f'{base_slug}-{counter}'
                counter += 1
            headings.append((level, title, slug))
            blocks.append(f'<h{level} id="{slug}">{inline(title)}</h{level}>')
            i += 1
        elif line.startswith('|'):
            rows = []
            while i < len(lines) and lines[i].startswith('|'):
                row = [c.strip() for c in lines[i].strip().strip('|').split('|')]
                if not all(re.fullmatch(r':?-+:?', c) for c in row):
                    rows.append(row)
                i += 1
            head = '<thead><tr>'+''.join('<th scope="col">'+inline(c)+'</th>' for c in rows[0])+'</tr></thead>'
            body = '<tbody>'+''.join('<tr>'+''.join('<td>'+inline(c)+'</td>' for c in row)+'</tr>' for row in rows[1:])+'</tbody>'
            blocks.append('<div class="table-wrap" tabindex="0" role="region" aria-label="Tabelle"><table>'+head+body+'</table></div>')
        elif line.startswith('- '):
            items = []
            while i < len(lines) and lines[i].startswith('- '):
                text = lines[i][2:]; i += 1
                while i < len(lines) and lines[i].startswith('  '):
                    text += ' '+lines[i].strip(); i += 1
                items.append('<li>'+inline(text)+'</li>')
            blocks.append('<ul>'+''.join(items)+'</ul>')
        else:
            paragraph = [line]; i += 1
            while i < len(lines) and lines[i].strip() and not lines[i].startswith(('#', '|', '```', '- ')):
                paragraph.append(lines[i]); i += 1
            blocks.append('<p>'+inline(' '.join(paragraph))+'</p>')

    navigation = ''.join(f'<a href="#{slug}">{escape(title)}</a>' for level,title,slug in headings if level == 2)
    css = '''
    :root{color-scheme:light dark;--page:#f3f5f8;--paper:#fff;--ink:#192a3d;--muted:#526477;--line:#dce3eb;--accent:#1656a0;--soft:#edf3fa}
    *{box-sizing:border-box}html{scroll-behavior:smooth;scroll-padding-top:30px}
    body{margin:0;background:var(--page);color:var(--ink);font:20px/1.7 "Segoe UI",system-ui,sans-serif}
    .shell{max-width:1420px;margin:auto;padding:48px 36px;display:grid;grid-template-columns:240px minmax(0,1fr);gap:42px}
    aside{position:sticky;top:36px;align-self:start;font-size:16px;line-height:1.45}.brand{font-size:24px;font-weight:750;letter-spacing:-.7px}.subtitle{color:var(--muted);margin:4px 0 28px}
    nav a{display:block;text-decoration:none;color:var(--muted);padding:10px 12px;border-radius:7px;margin:3px 0}nav a:hover,nav a:focus-visible{background:var(--soft);color:var(--accent)}
    main{min-width:0;background:var(--paper);border:1px solid var(--line);border-radius:16px;padding:48px 54px;box-shadow:0 10px 40px #14283f08}
    .eyebrow{font-size:14px;letter-spacing:1.3px;text-transform:uppercase;color:var(--muted);font-weight:650}
    h1{font-size:clamp(32px,3.5vw,46px);line-height:1.18;letter-spacing:-1px;margin:12px 0 36px}h2{font-size:28px;line-height:1.3;margin:54px 0 18px;padding-top:12px;border-top:1px solid var(--line)}h3{font-size:22px;line-height:1.4;margin:32px 0 12px}
    p{margin:0 0 20px}a{color:var(--accent);text-underline-offset:3px}strong{font-weight:650}li{margin:10px 0}ul{padding-left:26px}
    code{font: .88em/1.6 Consolas,"Cascadia Code",monospace;background:var(--soft);padding:2px 5px;border-radius:4px}pre{background:var(--soft);padding:20px 24px;border:1px solid var(--line);border-radius:10px;overflow:auto}pre code{padding:0;background:none;font-size:18px;white-space:pre}
    .table-wrap{overflow:auto;margin:24px 0 30px;border:1px solid var(--line);border-radius:10px}.table-wrap:focus-visible{outline:3px solid var(--accent);outline-offset:3px}
    table{width:100%;border-collapse:collapse;font-size:17px;line-height:1.6;min-width:590px}th{background:var(--soft);text-align:left;font-size:15px;font-weight:700}td,th{padding:15px 16px;vertical-align:top;border-bottom:1px solid var(--line)}tr:last-child td{border-bottom:0}tbody tr:nth-child(even){background:color-mix(in srgb,var(--soft) 35%,var(--paper))}
    footer{margin-top:48px;padding-top:18px;border-top:1px solid var(--line);font-size:15px;color:var(--muted)}.skip{position:absolute;top:-100px}.skip:focus{top:8px;left:8px;background:var(--paper);padding:10px;z-index:5}
    @media(prefers-color-scheme:dark){:root{--page:#101821;--paper:#17222e;--ink:#e4edf6;--muted:#a7bacd;--line:#334457;--accent:#8ac1ff;--soft:#223345}}
    @media(max-width:1050px){.shell{grid-template-columns:1fr;padding:24px;gap:24px}aside{position:static}nav{display:flex;flex-wrap:wrap;gap:4px}.subtitle{margin-bottom:12px}nav a{padding:7px 10px}main{padding:32px}}
    @media(max-width:600px){body{font-size:18px}.shell{padding:12px}main{padding:24px 18px;border-radius:10px}h2{font-size:25px}table{font-size:16px}}
    @media print{body{background:white;color:black;font-size:11pt}.shell{display:block;padding:0}aside,.skip{display:none}main{border:0;padding:0;box-shadow:none}.table-wrap{overflow:visible}table{min-width:0;font-size:9pt}h2{break-after:avoid}tr,pre{break-inside:avoid}a{color:inherit}pre{white-space:pre-wrap}}
    '''
    page = '<!doctype html>\n<html lang="de"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Additiver Oszillator · EasyKnack</title><style>'+css+'</style></head><body><a class="skip" href="#inhalt">Zum Inhalt</a><div class="shell"><aside><div class="brand">EasyKnack</div><div class="subtitle">Node-Referenz · Deutsch</div><nav aria-label="Auf dieser Seite">'+navigation+'</nav></aside><main id="inhalt"><div class="eyebrow">Generatoren · Audio</div>'+''.join(blocks)+'<footer>EasyKnack · Node-Referenz</footer></main></div></body></html>'
    title = lines[0].removeprefix('# ') if lines else 'EasyKnack'
    english = source.parent.name == 'en'
    page = page.replace('Additiver Oszillator · EasyKnack', escape(title)+' · EasyKnack')
    page = page.replace('Generatoren · Audio', 'Node reference' if 'nodes' in source.parts else 'Documentation')
    page = page.replace('Node-Referenz · Deutsch', 'Documentation · English' if english else 'Dokumentation · Deutsch')
    if english:
        page=page.replace('lang="de"','lang="en"').replace('Zum Inhalt','Skip to content').replace('Auf dieser Seite','On this page').replace('aria-label="Tabelle"','aria-label="Table"')
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(page, encoding='utf-8')
    return page
