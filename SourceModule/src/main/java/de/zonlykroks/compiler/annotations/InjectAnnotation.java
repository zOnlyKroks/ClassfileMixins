package de.zonlykroks.compiler.annotations;

import de.zonlykroks.compiler.annotations.util.At;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectAnnotation {
    String method();

    At at();
}
