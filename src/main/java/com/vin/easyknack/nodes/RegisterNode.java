package com.vin.easyknack.nodes;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterNode {
    String id();
    String title();
    NodeCategory category();
    NodeKind kind();
    boolean experimental() default false;
}

