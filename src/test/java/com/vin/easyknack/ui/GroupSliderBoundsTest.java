package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.model.GLNodeValue;
class GroupSliderBoundsTest {
    private final Map<String,GLNodeValue> initial=Map.of("minimum",GLNodeValue.of(0.),"maximum",GLNodeValue.of(1.),"default",GLNodeValue.of(0.));
    @Test void limitsPullDefaultAndOppositeLimit(){
        var s=new NodeState(GroupSliderBounds.adjust(initial,"minimum",1.));
        assertEquals(1,s.number("minimum",-1));assertEquals(1,s.number("default",-1));assertTrue(s.number("maximum",0)>1);
        s=new NodeState(GroupSliderBounds.adjust(initial,"maximum",-2.));
        assertEquals(-2,s.number("default",0));assertTrue(s.number("minimum",0)<-2);
    }
    @Test void defaultExtendsLimitsAndEmptyLimitStaysUnbounded(){
        var s=new NodeState(GroupSliderBounds.adjust(initial,"default",5.));assertEquals(5,s.number("maximum",0));assertEquals(5,s.number("default",0));
        var options=GroupSliderBounds.adjust(initial,"minimum",null);options=GroupSliderBounds.adjust(options,"default",-10.);
        s=new NodeState(options);assertFalse(s.flag("minimumEnabled",true));assertEquals(-10,s.number("default",0));
    }
}
