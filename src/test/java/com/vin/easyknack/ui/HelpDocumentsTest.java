package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HelpDocumentsTest {
    @Test void licenseBrowserLoadsIndividualCompleteDocuments()throws Exception{
        var documents=HelpDocuments.licenseDocuments();
        assertTrue(documents.size()>2);
        assertEquals(AppText.text("help.licenses.overview"),documents.get(0).title());
        for(var document:documents)assertFalse(document.text().isBlank(),document.title());
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var panel=new HelpMenu.DocumentPanel("",documents,()->{});
            try{
                assertEquals(normalized(HelpDocuments.resource("THIRD-PARTY.md")),panel.content.getText());
                panel.documents.setSelectedItem(documents.get(1));
                assertEquals(normalized(HelpDocuments.resource("LICENSE")),panel.content.getText());
                panel.documents.setSelectedItem(documents.get(2));
                assertEquals(normalized(documents.get(2).text()),panel.content.getText());
                assertEquals(0,panel.content.getCaretPosition());
            }catch(java.io.IOException failure){throw new AssertionError(failure);}
        });
    }
    private static String normalized(String text){return text.replace("\r\n","\n").replace('\r','\n');}
    @Test void handbookAndLicenseAreAvailableWithoutAnInstallation() throws Exception {
        for (String page : HelpDocuments.PAGES)
            assertTrue(NodeDocumentation.read("user/" + page + ".md").startsWith("#"));
        assertTrue(HelpDocuments.resource("LICENSE").contains("GNU GENERAL PUBLIC LICENSE"));
        assertTrue(HelpDocuments.licenses().contains("JogAmp"));
    }
    @Test void conciseManualHasTheSameTwelveChaptersInBothLanguages()throws Exception{
        var chapters=java.util.List.of("start","signals","groups","instance-group","formula-group","spectral-group",
                "partial-group","spectral-synthesis","analysis","channels","midi","buffers");
        for(String language:java.util.List.of("de","en")){
            String index=NodeDocumentation.read("user/"+language+"/index.md");
            for(String chapter:chapters){
                assertTrue(index.contains("]("+chapter+".md)"),language+" "+chapter);
                String body=NodeDocumentation.read("user/"+language+"/"+chapter+".md");
                assertTrue(body.startsWith("#"));assertTrue(body.contains("](index.md)"));
                assertTrue(body.contains("](../"+(language.equals("de")?"en":"de")+"/"+chapter+".md)"));
            }
        }
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
