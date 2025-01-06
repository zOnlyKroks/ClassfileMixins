package de.zonlykroks.classfilemixins.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LookupSwitchCaseAddition {
    String method();
    int caseValue();
    int switchIndex() default 0;
}
