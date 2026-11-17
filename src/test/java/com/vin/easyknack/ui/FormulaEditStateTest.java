package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.history.GLHistoryManager;
class FormulaEditStateTest {
 @Test void draftValidityAndAppliedStateTrackEditsAndReverts()throws Exception {
  java.awt.EventQueue.invokeAndWait(()->{
   var accepted=FormulaGroups.create();assertTrue(FormulaGroups.editState(accepted).applied());
   var meta=new HashMap<>(accepted.metadata());meta.put(FormulaGroups.DRAFT,GLNodeValue.of("result=signal*2;"));
   var pending=FormulaGroups.metadata(accepted,meta);assertTrue(FormulaGroups.editState(pending).valid());assertFalse(FormulaGroups.editState(pending).applied());
   meta.put(FormulaGroups.DRAFT,GLNodeValue.of("result=missing;"));var invalid=FormulaGroups.metadata(accepted,meta);
   assertFalse(FormulaGroups.editState(invalid).valid());assertEquals(FormulaGroups.source(accepted),FormulaGroups.source(invalid));
   var registry=new GLCompositeGraphRegistry();registry.registerAsset(invalid);var d=invalid.compositeDescriptor();
   var factory=new GroupViewFactory(registry,new GLNodeCatalog(),(id,key,value)->{},new GLHistoryManager(),()->{});
   var node=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());var view=(GroupViewFactory.GroupView)factory.createNode(node,d.nodeType());
   assertEquals("!",view.getTitleBadge());assertFalse(view.getTitle().contains("!"));registry.replaceAsset(accepted);view.refreshFormulaState();assertEquals("",view.getTitleBadge());
   assertTrue(FormulaGroups.editState(FormulaGroups.accept(pending,FormulaGroups.draft(pending))).applied());
  });
 }
}
