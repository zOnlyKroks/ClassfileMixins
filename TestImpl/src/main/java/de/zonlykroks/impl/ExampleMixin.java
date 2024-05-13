package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;

import java.io.OutputStream;
import java.io.PrintStream;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    public ExampleMixin() {
        System.out.println("I think i dont belong here but eh!");
    }

    static {
        System.out.println("Static block!");
    }

    @ModifyGetFieldReference(method = "injectIntoMe", field = "java/lang/System.out:Ljava/io/PrintStream;", staticIsnIndex = 0)
    public PrintStream modifyPrintStream() {
        return new PrintStreamImpl(System.out);
    }

    @Redirect(method = "injectIntoMe", target = "java/io/PrintStream.println:(Ljava/lang/String;)V",methodIndex = 0)
    public void redirectPrintStream() {
        System.out.println("A + 1");
    }

    @ModifyReturnValue(method = "modifyMyReturnValue", returnIndex = 0)
    public int modifyReturnValue2() {
        return 69420;
    }

    @ModifyLoadInstruction(method = "injectIntoMe", loadOpCode = "ALOAD_2", staticIsnIndex = 1)
    public PrintStream modifyReceiver() {
        return new PrintStreamImpl(System.out);
    }

    @ModifyLoadInstruction(method = "injectIntoMe", loadOpCode = "ILOAD_1")
    public int changeInvokeParam() {
        return 69;
    }

    public static class PrintStreamImpl extends PrintStream{

        public PrintStreamImpl(OutputStream out) {
            super(out);
        }
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }
}
