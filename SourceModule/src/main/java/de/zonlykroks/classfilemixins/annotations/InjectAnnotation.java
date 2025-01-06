package de.zonlykroks.classfilemixins.annotations;

import de.zonlykroks.classfilemixins.annotations.util.At;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectAnnotation {
    String method();

    At at();
    boolean captureLocals() default false;
}
