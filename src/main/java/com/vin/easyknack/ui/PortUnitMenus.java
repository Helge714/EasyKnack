package com.vin.easyknack.ui;

import java.util.List;
import java.util.function.Consumer;
import com.vin.easyknack.nodes.UnitFormat;
import com.vingame.glswing.ui.GLPopupMenu;

/** One port setting, accessible even when a connection hides its editor. */
final class PortUnitMenus {
    static GLPopupMenu create(List<UnitFormat> choices,UnitFormat selected,Consumer<UnitFormat> changed){
        var menu=new GLPopupMenu();var units=new GLPopupMenu();
        for(var unit:choices)units.addItem(unit.toString(),e->changed.accept(unit)).setEnabled(unit!=selected);
        menu.addSubMenu(AppText.text("port.unit"),units);return menu;
    }
}
