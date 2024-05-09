package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;
import de.zonlykroks.compiler.annotations.util.At;
import de.zonlykroks.compiler.annotations.util.InjectSelector;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    private final int pleaseMergeThisField = 69420;

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.HEAD, target = "IGNORED!"))
    public void callMeInInjectIntoMeHEAD() {
        System.out.println("I got head!");
    }

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.TAIL, target = "IGNORED!"))
    public void callMeInInjectIntoMeTail() {
        System.out.println("I got tail!");
    }

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.INVOKE_BEFORE, target = "println"))
    public void callMeInInjectIntoMeBefore() {
        System.out.println("I got before invoke!");
    }

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.INVOKE_AFTER, target = "println"))
    public void callMeInInjectIntoMeAfter() {
        System.out.println("I got after invoke!");
    }

    @Overwrite(method = "overwriteMe")
    public void callMeInOverwriteMe() {
        System.out.println("I got overwritten!");
    }

    @ModifyConstant(method = "modifyConstantMe", lvIndex = 1)
    public int callMeModifyConstant() {
        return 12;
    }

    @ModifyReturnValue(method = "modifyReturnValueMe", returnIndex = 0)
    public int callMeModifyReturnValue() {
        return 2;
    }

    public boolean condition() {
        return false;
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }
}
