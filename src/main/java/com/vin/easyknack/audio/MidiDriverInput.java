package com.vin.easyknack.audio;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** UI-driver taps share the host's MIDI handles with graph MIDI inputs. */
public final class MidiDriverInput {
    public record Event(String device,int message){
        public int type(){return message&0xf0;}public int channel(){return message&15;}
        public int number(){return (message>>8)&127;}public int value(){return (message>>16)&127;}
        public boolean on(){return type()==0x90&&value()>0;}
        public boolean off(){return type()==0x80||type()==0x90&&value()==0;}
    }
    private record Request(Set<String> devices,boolean learn){}
    private static final Map<Object,Request> requests=new IdentityHashMap<>();
    private static final CopyOnWriteArrayList<Consumer<List<Event>>> listeners=new CopyOnWriteArrayList<>();
    private MidiDriverInput(){}
    public static synchronized void request(Object owner,Set<String> devices,boolean learn){requests.put(owner,new Request(Set.copyOf(devices),learn));}
    public static synchronized void release(Object owner){requests.remove(owner);}
    public static synchronized String command(){
        var ids=new TreeSet<String>();boolean learn=false;for(var request:requests.values()){ids.addAll(request.devices);learn|=request.learn;}
        if(ids.size()>64)throw new IllegalStateException("Too many MIDI driver devices");
        return "MIDI_DRIVERS "+(learn?1:0)+" "+ids.size()+ids.stream().map(id->" "+id).collect(java.util.stream.Collectors.joining());
    }
    public static AutoCloseable listen(Consumer<List<Event>> listener){listeners.add(listener);return ()->listeners.remove(listener);}
    public static void publish(List<Event> events){if(!events.isEmpty())for(var listener:listeners)listener.accept(events);}
    public static List<Event> parse(String response){
        var words=response.split(" ");if(words.length<2||!words[0].equals("OK"))throw new IllegalArgumentException("Invalid MIDI driver response");
        int count=Integer.parseInt(words[1]);if(count<0||count>16640||words.length!=2+count*2)throw new IllegalArgumentException("Invalid MIDI driver event count");
        var result=new ArrayList<Event>();for(int i=0;i<count;i++){
            String id=words[2+i*2];Long.parseUnsignedLong(id);int message=Integer.parseInt(words[3+i*2]);
            if(message<0||message>0xffffff)throw new IllegalArgumentException("Invalid MIDI message");result.add(new Event(id,message));
        }return List.copyOf(result);
    }
}
