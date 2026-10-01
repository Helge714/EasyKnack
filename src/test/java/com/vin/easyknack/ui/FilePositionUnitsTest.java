package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.FilePositionEditor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FilePositionUnitsTest {
 @Test void unitsConvertRelativeToFileDuration(){
  var formats=new FilePositionEditor(0,2).formats();
  assertEquals(1,formats.stored().convertTo(.5,formats.choices().get(1)),1e-9);
  assertEquals(1000,formats.stored().convertTo(.5,formats.choices().get(2)),1e-9);
  assertEquals(48000,formats.stored().convertTo(.5,formats.choices().get(3)),1e-9);
  assertEquals(.5,formats.choices().get(3).convertTo(48000,formats.stored()),1e-9);
 }
}
