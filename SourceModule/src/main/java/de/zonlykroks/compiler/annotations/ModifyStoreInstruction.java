package de.zonlykroks.compiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModifyStoreInstruction {
    String method();
    String storeOpCode();
    int staticIsnIndex() default 0;
    boolean captureLocals() default false;
}
