package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class DocumentationLanguageTest {
    private static void await(java.util.function.Supplier<CompletableFuture<Void>> action)throws Exception{
        var pending=new AtomicReference<CompletableFuture<Void>>();SwingUtilities.invokeAndWait(()->pending.set(action.get()));pending.get().get(30,TimeUnit.SECONDS);
    }
    @Test void choicePersistsForReopenedHelpWithoutChangingTheApplicationLanguage()throws Exception{
        String override=System.getProperty("easyknack.documentation.language");System.clearProperty("easyknack.documentation.language");
        String previous=ApplicationSettings.documentationLanguage();
        try{
            ApplicationSettings.previewLocale(Locale.GERMAN);ApplicationSettings.saveDocumentationLanguage("application");
            var ref=new AtomicReference<DocumentationViewer>();SwingUtilities.invokeAndWait(()->ref.set(new DocumentationViewer(()->{})));var panel=ref.get();
            await(()->panel.openPage("nodes/en/oscillator.md"));
            SwingUtilities.invokeAndWait(()->assertEquals("nodes/de/oscillator.md",panel.position().page()));
            await(()->panel.chooseLanguage(DocumentationViewer.Language.ENGLISH));
            SwingUtilities.invokeAndWait(()->{assertEquals("nodes/en/oscillator.md",panel.position().page());assertEquals("de",ApplicationSettings.locale().getLanguage());});
            assertEquals("en",ApplicationSettings.documentationLanguage());
            SwingUtilities.invokeAndWait(()->ref.set(new DocumentationViewer(()->{})));var reopened=ref.get();
            await(()->reopened.openPage("nodes/de/index.md"));
            SwingUtilities.invokeAndWait(()->{assertEquals(DocumentationViewer.Language.ENGLISH,reopened.language.getSelectedItem());assertEquals("nodes/en/index.md",reopened.position().page());});
            assertTrue(NodeDocumentation.reference("easyknack.audio.oscillator",ApplicationSettings.documentationLocale()).startsWith("nodes/en/"));
            await(()->reopened.chooseLanguage(DocumentationViewer.Language.GERMAN));
            assertEquals("user/de/index.md",HelpDocuments.manualPage());
            await(()->reopened.openPage(HelpDocuments.manualPage()));
            await(()->reopened.follow("groups.md"));
            SwingUtilities.invokeAndWait(()->assertEquals("user/de/groups.md",reopened.position().page()));
            await(()->reopened.chooseLanguage(DocumentationViewer.Language.ENGLISH));
            SwingUtilities.invokeAndWait(()->assertEquals("user/en/groups.md",reopened.position().page()));
            await(()->reopened.chooseLanguage(DocumentationViewer.Language.GERMAN));
            await(()->reopened.openPage("user/README.md"));
            SwingUtilities.invokeAndWait(()->{assertEquals("user/README.md",reopened.position().page());assertTrue(reopened.status.getText().contains(AppText.text("help.englishFallback")));assertEquals(DocumentationViewer.Language.GERMAN,reopened.language.getSelectedItem());});
            assertEquals("de",ApplicationSettings.documentationLanguage());
            await(()->reopened.openPage("nodes/en/oscillator.md"));await(()->reopened.follow("../../user/en/rates.md#auto"));
            SwingUtilities.invokeAndWait(()->{assertEquals("user/de/rates.md",reopened.position().page());});
            await(reopened::back);SwingUtilities.invokeAndWait(()->assertEquals("nodes/de/oscillator.md",reopened.position().page()));
            await(()->reopened.follow("../en/oscillator.md"));assertEquals("en",ApplicationSettings.documentationLanguage());
            SwingUtilities.invokeAndWait(()->assertEquals("nodes/en/oscillator.md",reopened.position().page()));
            await(()->reopened.chooseLanguage(DocumentationViewer.Language.APPLICATION));assertEquals("de",ApplicationSettings.documentationLocale().getLanguage());
        }finally{ApplicationSettings.saveDocumentationLanguage(previous);ApplicationSettings.previewLocale(null);if(override!=null)System.setProperty("easyknack.documentation.language",override);}
    }
    @Test void missingTranslationFallsBackAndNarrowToolbarRemainsUsable()throws Exception{
        assertEquals("nodes/en/only.md",NodeDocumentation.localizedPage("nodes/de/only.md",Locale.GERMAN,p->p.equals("nodes/en/only.md")));
        assertEquals("user/README.md",NodeDocumentation.localizedPage("user/README.md",Locale.GERMAN,p->true));
        assertThrows(IllegalArgumentException.class,()->ApplicationSettings.saveDocumentationLanguage("unsupported"));
        SwingUtilities.invokeAndWait(()->{var panel=new DocumentationViewer(()->{});for(int width:new int[]{320,800,1200}){
            panel.setBounds(0,0,width,420);
            assertTrue(panel.language.getX()>=0);assertTrue(panel.language.getX()+panel.language.getWidth()<=width);
            assertTrue(panel.language.getY()+panel.language.getHeight()<=panel.scroll.getY());
        }});
    }
}
