package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
class GroupResourceRateTest {
 @Test void copiedSpectrumPortShowsInheritedRateWithoutInvalidSelection() throws Exception {
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   var a=SpectralGroups.create();var d=a.compositeDescriptor();var ports=d.portMappings().stream().map(p->{
    if(p.publicPort().direction()!=com.vingame.glswing.node.model.GLPortDirection.INPUT)return p;
    var options=new HashMap<>(p.presentation().editorOptions());options.put(GroupInputSettings.MODE,com.vingame.glswing.node.model.GLNodeValue.of("COPY"));
    return new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(p.presentation().inputEditorId(),true,options),p.additionalEndpoints());
   }).toList();
   a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
   c.openDocument("Spectrum",GroupAuthoringTest.document(a,com.vingame.glswing.node.model.GLNodeValue.of(0.)));assertTrue(c.workspace().openComposite(a.id()));
   var settings=GroupEditingTest.descendants(c.workspace().interfaceEditor()).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
   settings.refresh();assertEquals(ProcessingRate.CONTROL,settings.rate.getSelectedItem());assertFalse(settings.rate.isEnabled());
   settings.kind.setSelectedItem(GroupInputSettings.Kind.NONE);settings.refresh();
   assertFalse(settings.kind.getItems().contains(GroupInputSettings.Kind.COPY),"A resource port without a copyable editor must not offer Copy Editor");
   assertEquals(ProcessingRate.CONTROL,settings.rate.getSelectedItem());assertFalse(settings.rate.isEnabled());
  }});
 }
}
