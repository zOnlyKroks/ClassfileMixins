package de.zonlykroks.compiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TerminateJVM {
    String method();
    String target();
    int methodIndex() default 0;
    int exitCode() default -1;
    boolean captureLocals() default false;
    boolean before() default false;
}
