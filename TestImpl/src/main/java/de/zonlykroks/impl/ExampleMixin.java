package de.zonlykroks.impl;

import de.zonlykroks.compiler.annotations.*;

import java.io.OutputStream;
import java.io.PrintStream;

@Mixin(classDescriptor = "Main")
public class ExampleMixin implements DummyInterface{

    @ModifyGetFieldReference(method = "injectIntoMe", field = "java/lang/System.out:Ljava/io/PrintStream;", staticIsnIndex = 0)
    public PrintStream modifyPrintStream() {
        return System.err;
    }

    @Redirect(method = "injectIntoMe", target = "java/io/PrintStream.println:(Ljava/lang/String;)V",methodIndex = 0)
    public void redirectPrintStream() {
        System.out.println("A + 1");
    }

    @ModifyLoadInstruction(method = "injectIntoMe", loadOpCode = "ALOAD_1", staticIsnIndex = 3)
    public PrintStream changeMethodHandle() {
        return new PrintStreamImpl(System.out);
    }

    @ModifyReturnValue(method = "testOutput", returnIndex = 0)
    public int modifyReturnValue2() {
        return 69420;
    }

    public static class PrintStreamImpl extends PrintStream{

        public PrintStreamImpl(OutputStream out) {
            super(out);

            System.out.println("A");
        }
    }

    @Override
    public void iWantToBeMergedPlease() {
        System.out.println("I am merged!");
    }
}
