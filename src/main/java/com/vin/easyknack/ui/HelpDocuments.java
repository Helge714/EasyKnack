package com.vin.easyknack.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/** Offline, escaped HTML for the small Markdown subset used by the handbook. */
final class HelpDocuments {
    static final List<String> PAGES = List.of("README", "workspace", "audio", "nodes", "groups", "analysis", "limitations");
    private HelpDocuments() { }

    static String resource(String name) throws IOException {
        return read("/META-INF/" + (name.equals("LICENSE") || name.equals("THIRD-PARTY.md") ? "easyknack/legal/" : "") + name);
    }
    private static String read(String name) throws IOException {
        if(name.startsWith(com.vin.easyknack.LegalDocuments.RESOURCE_PREFIX))
            return com.vin.easyknack.LegalDocuments.read(name.substring(com.vin.easyknack.LegalDocuments.RESOURCE_PREFIX.length()));
        try (var input = HelpDocuments.class.getResourceAsStream(name)) {
            if (input == null) throw new IOException("Bundled document missing: " + name);
            String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return text.startsWith("\uFEFF") ? text.substring(1) : text;
        }
    }

    static String licenses() throws IOException {
        var text = new StringBuilder(resource("THIRD-PARTY.md")).append("\n\n").append(resource("LICENSE"));
        for (String name : read("/easyknack/notices/index.txt").lines().filter(s -> !s.isBlank()).toList())
            text.append("\n\n----- ").append(name).append(" -----\n\n").append(read("/easyknack/notices/" + name));
        return text.toString();
    }

    record LicenseDocument(String title,String resource){
        @Override public String toString(){return title;}
        String text()throws IOException{return read(resource);}
    }
    static List<LicenseDocument> licenseDocuments()throws IOException{
        var documents=new java.util.ArrayList<LicenseDocument>();
        documents.add(new LicenseDocument(AppText.text("help.licenses.overview"),"/META-INF/easyknack/legal/THIRD-PARTY.md"));
        documents.add(new LicenseDocument("EasyKnack — GPL-3.0-only","/META-INF/easyknack/legal/LICENSE"));
        for(String name:read("/easyknack/notices/index.txt").lines().map(String::strip).filter(s->!s.isBlank()).toList())
            documents.add(new LicenseDocument(name,"/easyknack/notices/"+name));
        return List.copyOf(documents);
    }

    static Path manual() throws IOException {
        // Every page is regenerated, so a development run cannot show a stale manual.
        Path folder = Path.of(System.getProperty("user.home"), ".easyknack", "help");
        Files.createDirectories(folder);
        for (String page : PAGES)
            Files.writeString(folder.resolve(page + ".html"), html(resource("easyknack/manual/" + page + ".md")), StandardCharsets.UTF_8);
        return folder.resolve("README.html");
    }

    static String html(String markdown) {
        var result = new StringBuilder("<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width\"><title>EasyKnack user guide</title><style>body{font:17px/1.6 system-ui,sans-serif;max-width:960px;margin:40px auto;padding:0 24px;color:#20242a;background:#fafafa}a{color:#1558aa}table{border-collapse:collapse;width:100%;margin:20px 0}td,th{border:1px solid #ccd1d7;padding:8px;text-align:left}pre{overflow:auto;background:#e9edf1;padding:16px}code{font-family:monospace}h1,h2,h3{line-height:1.25;margin-top:1.5em}</style><body><nav><a href=\"README.html\">EasyKnack user guide</a></nav>");
        boolean code = false, table = false, list = false;
        var paragraph = new StringBuilder();
        for (String line : (markdown + "\n").split("\n", -1)) {
            line = line.stripTrailing();
            if (line.startsWith("```")) {
                flush(result, paragraph);
                if (table) { result.append("</table>"); table = false; }
                if (list) { result.append("</ul>"); list = false; }
                result.append(code ? "</code></pre>" : "<pre><code>"); code = !code; continue;
            }
            if (code) { result.append(escape(line)).append('\n'); continue; }
            boolean row = line.startsWith("|");
            boolean item = line.startsWith("- ") || line.startsWith("* ");
            if (!row && table) { result.append("</table>"); table = false; }
            if (!item && list) { result.append("</ul>"); list = false; }
            if (line.isBlank()) { flush(result, paragraph); continue; }
            if (row) {
                flush(result, paragraph);
                if (line.matches("[| :\\-]+")) continue;
                if (!table) { result.append("<table>"); table = true; }
                result.append("<tr>");
                for (String cell : line.substring(1).replaceFirst("\\|$", "").split("\\|", -1))
                    result.append("<td>").append(inline(cell.strip())).append("</td>");
                result.append("</tr>");
            } else if (item) {
                flush(result, paragraph);
                if (!list) { result.append("<ul>"); list = true; }
                result.append("<li>").append(inline(line.substring(2))).append("</li>");
            } else if (line.matches("#{1,6} .*")) {
                flush(result, paragraph);
                int level = line.indexOf(' '); String title = line.substring(level + 1);
                result.append("<h").append(level).append(" id=\"").append(title.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9 -]", "").replace(' ', '-')).append("\">").append(inline(title)).append("</h").append(level).append('>');
            } else { if (!paragraph.isEmpty()) paragraph.append(' '); paragraph.append(line); }
        }
        if (code) result.append("</code></pre>");
        return result.append("</body></html>").toString();
    }
    private static void flush(StringBuilder target, StringBuilder paragraph) {
        if (!paragraph.isEmpty()) { target.append("<p>").append(inline(paragraph.toString())).append("</p>"); paragraph.setLength(0); }
    }
    static String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    private static String inline(String value) {
        String escaped = escape(value);
        var links = Pattern.compile("\\[([^]]+)]\\(([A-Za-z]+)\\.md(#[a-z0-9-]+)?\\)").matcher(escaped);
        escaped = links.replaceAll(m -> java.util.regex.Matcher.quoteReplacement(PAGES.contains(m.group(2)) ? "<a href=\"" + m.group(2) + ".html" + (m.group(3) == null ? "" : m.group(3)) + "\">" + m.group(1) + "</a>" : m.group()));
        return escaped.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>").replaceAll("`([^`]+)`", "<code>$1</code>");
    }
}
