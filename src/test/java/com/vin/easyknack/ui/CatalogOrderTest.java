package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class CatalogOrderTest {
    @Test void manualCategoryOrderSurvivesLanguageChangesAndUse() {
        String previous=System.getProperty("easyknack.language");
        try {
            for(String language:List.of("en","de")) {
                System.setProperty("easyknack.language",language);NodeText.reload();AppText.reload();
                var catalog=new GLNodeCatalog();new NodeRegistry(new GLNodeTypeRegistry(),catalog);
                var expected=NodeCategory.menuOrder();
                assertEquals(expected,catalog.search("").stream().map(GLNodeCatalogEntry::category).distinct().toList());
                var before=catalog.search("");
                catalog.markUsed(before.get(before.size()-1).type().id());
                assertEquals(before,catalog.search(""));
            }
        } finally {
            if(previous==null)System.clearProperty("easyknack.language");else System.setProperty("easyknack.language",previous);
            NodeText.reload();AppText.reload();
        }
    }
}
