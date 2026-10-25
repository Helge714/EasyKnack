package com.vin.easyknack.ui;
import com.vin.easyknack.audio.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
class MidiDriverHostTest {
 @Test void driverTapWorksWhileStoppedAndDoesNotConsumeGraphMidi()throws Exception{
  try(var client=new AudioHostClient()){
   client.start(AudioHostClient.defaultExecutable(),true);
   assertEquals("OK",client.command("MIDI_DRIVERS 0 1 1"));
   int on=0x90|60<<8|100<<16,cc=0xb0|7<<8|64<<16,off=0x90|60<<8;
   assertEquals("OK",client.command("MIDI_KEYBOARD 3 "+on+" "+cc+" "+off));
   var events=MidiDriverInput.parse(client.command("MIDI_DRIVER_EVENTS"));assertEquals(3,events.size());assertTrue(events.get(0).on());assertEquals(64,events.get(1).value());assertTrue(events.get(2).off());
   assertTrue(MidiDriverInput.parse(client.command("MIDI_DRIVER_EVENTS")).isEmpty());
   assertEquals("OK",client.command("PLAY"));client.command("MIDI_KEYBOARD 1 "+on);
   assertEquals(1,MidiDriverInput.parse(client.command("MIDI_DRIVER_EVENTS")).size());
   client.command("STOP");client.command("MIDI_DRIVERS 0 0");client.command("MIDI_KEYBOARD 1 "+on);assertTrue(MidiDriverInput.parse(client.command("MIDI_DRIVER_EVENTS")).isEmpty());
  }
 }
}
