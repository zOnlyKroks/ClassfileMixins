package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    @Overwrite(method = "injectIntoMe")
    public void overwriteMyInject() {
        System.out.println("I am the overwritten method");
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }
}
