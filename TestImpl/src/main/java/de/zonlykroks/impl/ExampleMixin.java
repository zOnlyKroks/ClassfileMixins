package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;

import java.io.PrintStream;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    @ModifyGetFieldReference(method = "injectIntoMe", field = "java/lang/System.out:Ljava/io/PrintStream;")
    public PrintStream modifyPrintStream() {
        return System.err;
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }
}
