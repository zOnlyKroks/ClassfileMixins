package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;
import de.zonlykroks.compiler.annotations.util.At;
import de.zonlykroks.compiler.annotations.util.InjectSelector;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    @InjectAnnotation(method = "<init>", at = @At(selector = InjectSelector.RETURN, target = "IGNORED!"))
    public void callMeInConstructorTail() {
        System.out.println("I got init TAIL!");
    }

    @InjectAnnotation(method = "<init>", at = @At(selector = InjectSelector.HEAD, target = "IGNORED!"))
    public void callMeInConstructorHEAD() {
        System.out.println("I got init HEAD!");
    }

    @InjectAnnotation(method = "lambda$lambdaTest$0", at = @At(selector = InjectSelector.HEAD, target = "IGNORED!"))
    public static void callMeInLambdaTestHEAD() {
        System.out.println("I got lambda HEAD!");
    }

    @ModifyConstant(method = "lambda$lambdaTest$0", lvIndex = 0)
    public static String modifyLambdaConstant() {
        return "I got the lambda modified!";
    }

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.HEAD, target = "IGNORED!"))
    public void callMeInInjectIntoMeHEAD() {
        System.out.println("I got head!");
    }

    @InjectAnnotation(method = "injectIntoMe", at = @At(selector = InjectSelector.RETURN, target = "IGNORED!"))
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
        return 69420;
    }

    @ModifyReturnValue(method = "modifyReturnValueMe", returnIndex = 0)
    public int callMeModifyReturnValue() {
        return 2;
    }

    @Redirect(method = "redirectMe", target = "println", methodIndex = 1)
    public void goodThingYouRedirectedMe() {
        System.out.println("I got redirected!");
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }

    @ModifyReturnValue(method = "returnTest")
    public int callMeModifyReturnValueReturnTest() {
        return 2;
    }
}
