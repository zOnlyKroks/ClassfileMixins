package de.zonlykroks.classfilemixins.annotations.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface At {
    InjectSelector selector();
    String target();
    int index() default -1;
}
