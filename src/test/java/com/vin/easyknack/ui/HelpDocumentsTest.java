package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HelpDocumentsTest {
    @Test void handbookAndLicenseAreAvailableWithoutAnInstallation() throws Exception {
        for (String page : HelpDocuments.PAGES)
            assertTrue(HelpDocuments.resource("easyknack/manual/" + page + ".md").startsWith("#"));
        assertTrue(HelpDocuments.resource("LICENSE").contains("GNU GENERAL PUBLIC LICENSE"));
        assertTrue(HelpDocuments.licenses().contains("JogAmp"));
    }
    @Test void localLinksAndAnchorsWorkAndHtmlIsEscaped() {
        String html = HelpDocuments.html("# Formula Groups\n\n[Read](groups.md#formula-groups) <script>alert(1)</script>\n\n| A | B |\n|---|---|\n| 1 | 2 |\n");
        assertTrue(html.contains("href=\"groups.html#formula-groups\""));
        assertTrue(html.contains("id=\"formula-groups\""));
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("<td>1</td>"));
        assertFalse(html.contains("<td>---</td>"));
    }
    @Test void noUntrustedLinksAreActivated() {
        String html = HelpDocuments.html("[Outside](../private.md) [Code](javascript:alert) [Unknown](secret.md)");
        assertFalse(html.contains("href=\"../"));
        assertFalse(html.contains("href=\"javascript:"));
        assertFalse(html.contains("href=\"secret"));
    }
}
