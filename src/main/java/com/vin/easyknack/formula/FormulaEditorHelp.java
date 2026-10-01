package com.vin.easyknack.formula;

import java.util.*;
import com.vingame.glswing.ui.GLCodeSignature;

/** One description shared by completion, hover and argument help. */
final class FormulaEditorHelp {
    record Call(String name,List<String> parameters,String result,String help){
        String label(){return name+"("+String.join(", ",parameters)+") → "+result;}
        String description(){return label()+"\n"+help;}
        GLCodeSignature signature(int active){return new GLCodeSignature(label(),help,parameters,active<parameters.size()?active:-1);}
    }
    static Call function(String name,boolean typed){
        if(!FormulaCompiler.FUNCTIONS.containsKey(name))return null;
        List<String> parameters=switch(name){case "atan2"->List.of("y","x");case "pow"->List.of("base","exponent");case "min","max"->List.of("a","b");case "clamp"->List.of("value","minimum","maximum");case "mix"->List.of("a","b","factor");case "rand"->List.of("minimum","maximum","seed (optional)");case "sin","cos","tan"->List.of("angle");case "sineWave"->List.of("phase");default->List.of("value");};
        String help=switch(name){
            case "sineWave"->"Phase in cycles (1 = one period). Shared 2048-point sine table with linear interpolation; wraps negative/large phases. Nonfinite phase returns zero.";
            case "sin","cos","tan"->"Angle in radians. Standard mathematical function; integer arguments compute in double in typed formulas.";
            case "asin","acos"->"Returns radians; input range -1 to 1.";
            case "atan","atan2"->"Returns an angle in radians.";
            case "sqrt"->"Square root; negative values produce NaN.";
            case "log","log2","log10"->"Logarithm; positive input required for a finite result.";
            case "rand"->"Uniform reproducible random value per evaluation sample. Optional seed selects the sequence; default seed belongs to this node. Reversed bounds are exchanged.";
            case "mix"->"Linear interpolation: a + (b - a) * factor; factor is not clamped.";
            case "clamp"->"Limits value between minimum and maximum; supply minimum <= maximum.";
            case "floor"->"Rounds down toward negative infinity.";
            case "ceil"->"Rounds up toward positive infinity.";
            case "round"->"Rounds to the nearest integer value, halfway away from zero; result remains floating point.";
            case "abs"->"Absolute value. Minimum signed integer retains its value because its positive counterpart is out of range.";
            case "sign"->"Returns -1, 0 or 1 according to the sign.";
            case "pow"->"Raises base to exponent; unsupported real domains produce NaN.";
            case "exp"->"Exponential e raised to value.";
            case "min"->"Returns the smaller value.";
            case "max"->"Returns the larger value.";
            default->"Numeric function.";
        };
        String result=!typed||Set.of("sineWave","rand").contains(name)?"float":Set.of("min","max","clamp","abs","sign").contains(name)?"promoted numeric type":"float / double";
        return new Call(name,parameters,result,help);
    }
    static Call read(int version){return new Call("read",List.of("position","mode (optional)","interpolation (optional)"),"float",version>=2?"Absolute sample position. Modes: Clamp (default), Wrap, Zero. Interpolation: Linear (default), Nearest. Empty buffers return zero.":"Legacy physical index: read(index[, wrap=false[, interpolate=true]]). Named Clamp/Wrap/Zero and Linear/Nearest select absolute timeline semantics.");}
    static String type(String name){return switch(name){case "bool"->"bool — true/false; contributes 1/0 in typed arithmetic.";case "float"->"float — 32-bit floating point; about 7 significant decimal digits.";case "double"->"double — 64-bit floating point; about 16 significant decimal digits.";case "int"->"int — signed 32-bit integer.";case "long"->"long — signed 64-bit integer.";case "uint"->"uint — unsigned 32-bit integer; alias: unsigned int.";case "ulong"->"ulong — unsigned 64-bit integer; alias: unsigned long.";case "unsigned"->"unsigned int = uint; unsigned long = ulong.";default->null;};}
}
