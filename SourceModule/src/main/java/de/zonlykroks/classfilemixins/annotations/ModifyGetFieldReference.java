package de.zonlykroks.classfilemixins.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModifyGetFieldReference {
    String method();
    String field();
    int staticIsnIndex() default 0;
    boolean captureLocal() default false;
}
