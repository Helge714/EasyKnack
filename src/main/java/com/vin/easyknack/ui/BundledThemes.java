package com.vin.easyknack.ui;

import java.util.List;

/** Curated application themes, independent of stale files in incremental build output. */
final class BundledThemes {
    record Choice(String resource,String name){
        public String toString(){return name;}
        String id(){return "classpath:"+resource;}
    }
    static List<Choice> available(){
        return List.of(
            new Choice(ApplicationTheme.DEFAULT_RESOURCE,"EasyKnack Mint"),
            new Choice("/easyknack-slider-amber.gltheme","EasyKnack Amber"),
            new Choice("/easyknack-light-mint.gltheme","EasyKnack Light Mint"),
            new Choice("/easyknack-light-amber.gltheme","EasyKnack Light Amber"));
    }
}
