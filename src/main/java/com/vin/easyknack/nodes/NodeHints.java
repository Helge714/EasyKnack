package com.vin.easyknack.nodes;

import java.util.ArrayList;
import com.vin.easyknack.ui.AppText;

/** Current metadata shared by node controls, ports and inspector editors. */
public final class NodeHints {
    private NodeHints(){}
    public static String tooltip(NodeBuilder.Row row){
        var parts=new ArrayList<String>();
        parts.add(row.tooltip()==null||row.tooltip().isBlank()?row.label():row.tooltip());
        Editor.Slider slider=row.editor() instanceof Editor.Slider s?s:row.editor() instanceof UnitSlider u?u.spec():null;
        if(slider!=null){
            UnitSlider unit=row.editor() instanceof UnitSlider u?u:null;
            String suffix=unit==null?slider.unitSuffix():unit.selected().suffix();
            parts.add(AppText.format("node.hint.range",bound(slider.min(),unit,suffix),bound(slider.max(),unit,suffix)));
            if(unit!=null&&row.kind()==NodeBuilder.RowKind.INPUT)parts.add(AppText.text("node.hint.unitInput"));
        }
        if(row.kind()!=NodeBuilder.RowKind.EDITOR)parts.add(switch(row.dataType()){
            case HISTORY->AppText.text("node.hint.buffer");
            case SPECTRUM->AppText.text("node.hint.spectrum");
            case ANALYSIS->AppText.text("node.hint.analysis");
            case MIDI->AppText.text("node.hint.midi");
            case TRIGGER->ProcessingRate.EVENT.outputHint();
            default->row.kind()==NodeBuilder.RowKind.INPUT?row.rate().inputHint():row.rate().outputHint();
        });
        return String.join("\n",parts);
    }
    private static String bound(Double value,UnitSlider unit,String suffix){
        if(value==null)return AppText.text("node.hint.unbounded");
        double displayed=unit==null?value:unit.display(value);
        String number=Double.isFinite(displayed)?java.math.BigDecimal.valueOf(displayed).stripTrailingZeros().toPlainString():Double.toString(displayed);
        return number+(suffix.isBlank()?"":" "+suffix);
    }
}
