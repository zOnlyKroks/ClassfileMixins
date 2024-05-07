package de.zonlykroks.compiler.collector.injectors;

import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;
import de.zonlykroks.compiler.collector.annotations.modifiers.MethodcallInject;
import de.zonlykroks.compiler.collector.util.MixinProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.constantpool.MethodRefEntry;
import org.glavo.classfile.constantpool.Utf8Entry;
import org.glavo.classfile.impl.AbstractPoolEntry;
import org.glavo.classfile.impl.BlockCodeBuilderImpl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Path;
public class MethodCallInjectorProcessor  implements MixinProcessor {

    public static ClassModel handleMethodCallInjection(MethodcallInject inject, Path targetPath, ClassModel targetModel, ClassModel sourceModel, String callbackMethodModelName) {
        final CodeTransform transform = CodeTransform.ofStateful(() -> new CodeTransform() {
            boolean found = true;

            @Override
            public void accept(CodeBuilder codeB, CodeElement codeE) {
                if (found) {
                    codeB.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
                    codeB.ldc("Method call injected into the console");
                    codeB.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(ClassDesc.of("java.lang.String")));
                    found = false;
                }
                codeB.with(codeE);
            }
        });

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(transform));

        MixinProcessor.writeToClassFile(targetPath, modified);

        return ClassFile.of().parse(modified);
    }
}
