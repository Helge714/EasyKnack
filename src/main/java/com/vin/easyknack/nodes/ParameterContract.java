package com.vin.easyknack.nodes;

import java.util.*;
import java.util.function.*;
import com.vingame.glswing.node.model.GLNodeValue;

/** One declaration owns wire fields, input ordering and generated native parameter access. */
public final class ParameterContract {
    public enum Kind { SOURCE, VALUE, INTEGER, TOKEN, CURVE, MAPPED }
    public record Field(Kind kind, PropertyId id, String literal, Predicate<NodeState> enabled,
            Function<NodeState,String> mapping) { }
    private final List<Field> fields;
    private ParameterContract(List<Field> fields) {
        this.fields=List.copyOf(fields);
        if(inputs().stream().distinct().count()!=inputs().size())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Duplicate native input"));
        var names=fields.stream().filter(f->f.kind()!=Kind.TOKEN).map(NativeContractGenerator::fieldName).toList();
        if(names.stream().distinct().count()!=names.size())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Native parameter names collide"));
    }
    public static Builder builder(){return new Builder();}
    public List<Field> fields(){return fields;}
    public List<PropertyId> inputs(){return fields.stream().filter(f->f.kind()==Kind.SOURCE).map(Field::id).toList();}
    public String encode(BasicNode owner, NodeState state, ToIntFunction<PropertyId> input){
        var defaults=owner.defaults();
        var merged=new HashMap<>(defaults);merged.putAll(state.values());
        var resolved=new NodeState(merged,state.connectedInputs());
        // Validation is shared with document/editor validation, including bounds and choices.
        owner.layout(resolved);
        var out=new StringJoiner(" ");
        for(var f:fields){
            if(f.kind()==Kind.SOURCE){out.add(Integer.toString(f.enabled().test(resolved)?input.applyAsInt(f.id()):-1));continue;}
            if(f.kind()==Kind.TOKEN){out.add(f.literal());continue;}
            if(f.kind()==Kind.MAPPED){out.add(token(f.mapping().apply(resolved)));continue;}
            var value=merged.get(f.id().value());
            if(value==null)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Missing native parameter: ")+f.id());
            var raw=value.toJavaValue();
            if(f.kind()==Kind.CURVE){out.add(CurveData.nativePayload((String)raw));continue;}
            if(raw instanceof Boolean flag){out.add(flag?"1":"0");continue;}
            if(raw instanceof Number n){
                double number=n.doubleValue();
                if(!Double.isFinite(number))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Non-finite parameter: ")+f.id());
                if(f.kind()==Kind.INTEGER){
                    if(number!=Math.rint(number)||number<Integer.MIN_VALUE||number>Integer.MAX_VALUE)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Integer required: ")+f.id());
                    out.add(Integer.toString((int)number));
                }else out.add(Double.toString(number));
            }else out.add(token((String)raw));
        }
        return out.toString();
    }
    private static String token(String value){
        if(value==null||!value.matches("[A-Za-z0-9_.:+/-]+"))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid native token"));
        return value;
    }
    public static final class Builder {
        private final List<Field> fields=new ArrayList<>();
        public Builder source(PropertyId id){return source(id,s->true);}
        public Builder source(PropertyId id,Predicate<NodeState> enabled){fields.add(new Field(Kind.SOURCE,id,null,enabled,null));return this;}
        public Builder value(PropertyId id){fields.add(new Field(Kind.VALUE,id,null,null,null));return this;}
        public Builder integer(PropertyId id){fields.add(new Field(Kind.INTEGER,id,null,null,null));return this;}
        public Builder curve(PropertyId id){fields.add(new Field(Kind.CURVE,id,null,null,null));return this;}
        public Builder token(String value){fields.add(new Field(Kind.TOKEN,null,ParameterContract.token(value),null,null));return this;}
        public Builder mapped(PropertyId id,Function<NodeState,String> mapping){fields.add(new Field(Kind.MAPPED,id,null,null,mapping));return this;}
        public ParameterContract build(){return new ParameterContract(fields);}
    }
}
