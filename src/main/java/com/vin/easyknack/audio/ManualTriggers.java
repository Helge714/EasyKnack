package com.vin.easyknack.audio;

/** Transient UI events: never model properties, undo entries or document data. */
public final class ManualTriggers {
    private ManualTriggers() { }
    private static java.util.function.Consumer<String> receiver=id->{};
    private static java.util.Map<String,java.util.List<String>> aliases=java.util.Map.of();
    public static void receiver(java.util.function.Consumer<String> value){receiver=value;}
    public static void aliases(java.util.Map<String,java.util.List<String>> value){aliases=java.util.Map.copyOf(value);}
    public static void fire(String id,String property){
        for(String target:new java.util.LinkedHashSet<>(aliases.getOrDefault(id,java.util.List.of(id))))receiver.accept(target+":"+property);
    }
}
