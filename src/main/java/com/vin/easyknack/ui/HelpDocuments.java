package com.vin.easyknack.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    static String manualPage() { return NodeDocumentation.localizedPage("user/en/index.md",ApplicationSettings.documentationLocale()); }
    static Path manual() throws IOException { return NodeDocumentation.page(manualPage()); }

    static String html(String markdown) {
        return html(markdown,"en",link -> {
            var match=Pattern.compile("([A-Za-z]+)\\.md(#[a-z0-9-]+)?").matcher(link);
            return match.matches()&&PAGES.contains(match.group(1))?match.group(1)+".html"+(match.group(2)==null?"":match.group(2)):null;
        });
    }
    static String html(String markdown,String language,java.util.function.Function<String,String> resolve) {
        return DocumentationMarkdown.render(markdown,language,resolve);
    }
    static String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
}
