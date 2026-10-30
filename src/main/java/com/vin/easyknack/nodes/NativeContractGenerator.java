package com.vin.easyknack.nodes;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Build-time C++ structs/readers generated from the same declarations as the Java encoder. */
public final class NativeContractGenerator {
    private NativeContractGenerator() { }
    public static String name(BasicNode node){return node.nativeParameterType();}
    public static String fieldName(ParameterContract.Field f){
        String name=f.id().value().replaceAll("[^A-Za-z0-9_]","_");
        return (Character.isDigit(name.charAt(0))?"p_":"")+name+(f.kind()==ParameterContract.Kind.SOURCE?"_source":"_value");
    }
    private static String type(BasicNode node,ParameterContract.Field f){
        return switch(f.kind()){
            case SOURCE,INTEGER -> "int";
            case MAPPED -> "std::string";
            case CURVE -> "FunctionCurve";
            case VALUE -> node.defaults().get(f.id().value()).toJavaValue() instanceof Number ? "double"
                    : node.defaults().get(f.id().value()).toJavaValue() instanceof Boolean ? "int":"std::string";
            case TOKEN -> "";
        };
    }
    private static final ClassValue<java.util.concurrent.ConcurrentMap<String,String>> fingerprints=new ClassValue<>() {
        protected java.util.concurrent.ConcurrentMap<String,String> computeValue(Class<?> type){return new java.util.concurrent.ConcurrentHashMap<>();}
    };
    public static String fingerprint(BasicNode node){return fingerprints.get(node.getClass()).computeIfAbsent(node.nativeParameterType(),key->calculateFingerprint(node));}
    private static String calculateFingerprint(BasicNode node){
        try{
            var schema=new StringBuilder("easyknack-parameters-1/").append(node.processor().id());
            for(var f:node.processor().contract().fields())schema.append('/').append(f.kind()).append(':')
                    .append(f.id()==null?f.literal():f.id().value()).append(':').append(type(node,f));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(schema.toString().getBytes(StandardCharsets.UTF_8))).substring(0,16);
        }catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
    public static String generate(Collection<BasicNode> nodes){
        var out=new StringBuilder("// Generated from Java ParameterContract. Do not edit.\n#pragma once\n#include \"FunctionCurve.hpp\"\n#include <string>\n#include <vector>\n#include <cmath>\nnamespace easyknack { namespace parameters {\n");
        for(var node:nodes){
            out.append("struct ").append(name(node)).append(" {\n");
            for(var f:node.processor().contract().fields())if(f.kind()!=ParameterContract.Kind.TOKEN)
                out.append("    ").append(type(node,f)).append(' ').append(fieldName(f)).append("{};\n");
            out.append("    std::vector<int> sources() const { return {");
            out.append(String.join(",",node.processor().contract().fields().stream().filter(f->f.kind()==ParameterContract.Kind.SOURCE).map(NativeContractGenerator::fieldName).toList()));
            out.append("}; }\n    static ").append(name(node)).append(" read(std::istream& in) {\n        ").append(name(node)).append(" p; std::string marker;\n")
                .append("        if(!(in>>marker)||marker!=\"!").append(fingerprint(node)).append("\") throw std::runtime_error(\"Incompatible parameter contract: ").append(node.processor().id()).append("; rebuild Java and audio host together\");\n");
            for(var f:node.processor().contract().fields()){
                if(f.kind()==ParameterContract.Kind.TOKEN){out.append("        if(!(in>>marker)||marker!=\"").append(f.literal()).append("\") throw std::runtime_error(\"Invalid parameter marker\");\n");continue;}
                String field="p."+fieldName(f);
                if(f.kind()==ParameterContract.Kind.CURVE){out.append("        ").append(field).append(".read(in);\n");continue;}
                out.append("        if(!(in>>").append(field).append(")) throw std::runtime_error(\"Missing parameter: ").append(f.id().value()).append("\");\n");
                if(type(node,f).equals("double"))out.append("        if(!std::isfinite(").append(field).append(")) throw std::runtime_error(\"Non-finite parameter\");\n");
                if(f.kind()==ParameterContract.Kind.SOURCE)out.append("        if(").append(field).append(" < -1) throw std::runtime_error(\"Invalid source reference\");\n");
                if(f.kind()==ParameterContract.Kind.VALUE||f.kind()==ParameterContract.Kind.INTEGER){
                    var row=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.key().equals(f.id())).findFirst().orElse(null);
                    if(row!=null){
                        Editor editor=row.editor(); if(editor instanceof UnitSlider u)editor=u.spec();
                        if(editor instanceof Editor.CheckBox || editor instanceof Editor.Toggle)out.append("        if(").append(field).append("!=0 && ").append(field).append("!=1) throw std::runtime_error(\"Boolean must be 0 or 1\");\n");
                        if(editor instanceof Editor.ComboBox<?> combo)out.append("        if(").append(String.join(" && ",(f.id().value().equals("rate")?java.util.stream.Stream.concat(choiceIds(combo).stream(),java.util.stream.Stream.of("event")):choiceIds(combo).stream()).map(id->field+"!=\""+id+"\"").toList())).append(") throw std::runtime_error(\"Unknown enum parameter\");\n");
                        if(editor instanceof Editor.Slider slider){
                            if(slider.min()!=null)out.append("        if(").append(field).append('<').append(slider.min()).append(") throw std::runtime_error(\"Parameter below minimum\");\n");
                            if(slider.max()!=null)out.append("        if(").append(field).append('>').append(slider.max()).append(") throw std::runtime_error(\"Parameter above maximum\");\n");
                        }
                    }
                }
            }
            out.append("        return p;\n    }\n};\n");
        }
        return out.append("}}\n").toString();
    }
    private static <T> List<String> choiceIds(Editor.ComboBox<T> combo){return combo.items().stream().map(combo.id()).toList();}
    public static void main(String[] args)throws Exception{
        var path=Path.of(args.length==0?"target/native/generated/ParameterContracts.hpp":args[0]);Files.createDirectories(path.toAbsolutePath().getParent());
        var source=generate(NodeRegistry.definitionsList());
        if(!Files.exists(path)||!Files.readString(path).equals(source))Files.writeString(path,source,StandardCharsets.UTF_8);
        com.vin.easyknack.plugins.PluginSdk.packageSdk(Path.of("native/audio-host"),Path.of("target/classes/easyknack/plugin-sdk.zip"));
    }
}
