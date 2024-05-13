package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.TerminateJVM;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.InvokeInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

public class TerminateJVMAnnotationProcessor extends AbstractAnnotationProcessor<TerminateJVM> {

    @Override
    public ClassModel processAnnotation(TerminateJVM annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(annotation.method(), new CodeTransform() {
            int currentInvokeTarget = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if(codeElement instanceof InvokeInstruction invokeInstruction) {
                    String fullyQuallifiedTarget = invokeInstruction.owner().name().stringValue() + "." + invokeInstruction.name().stringValue() + ":" + invokeInstruction.type().stringValue();

                    if(fullyQuallifiedTarget.equalsIgnoreCase(annotation.target())) {

                        if(currentInvokeTarget == annotation.methodIndex()) {
                            handleTerminationCondition(codeBuilder, targetModel,sourceClassModel, sourceMethodModule, codeElement, annotation);
                        }else {
                            codeBuilder.with(codeElement);
                        }

                        currentInvokeTarget++;
                        return;
                    }
                }

                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }

    private void handleTerminationCondition(CodeBuilder codeBuilder, ClassModel targetModel,ClassModel sourceClassModule, MethodModel sourceMethodModule, CodeElement codeElement, TerminateJVM annotation) {
        if(annotation.before()) {
            injectCode(codeBuilder, targetModel,sourceClassModule, sourceMethodModule, annotation);

            codeBuilder.with(codeElement);
        }else {
            codeBuilder.with(codeElement);

            injectCode(codeBuilder, targetModel,sourceClassModule, sourceMethodModule, annotation);
        }
    }

    private void injectCode(CodeBuilder codeBuilder, ClassModel targetModel,ClassModel sourceClassModule, MethodModel sourceMethodModule, TerminateJVM annotation) {
        Label stopLabel = codeBuilder.newLabel();

        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, annotation.captureLocals() ? localVariables : null);
        codeBuilder.ifeq(stopLabel);

        codeBuilder.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));

        codeBuilder.constantInstruction("Termination condition met! Terminating JVM with exit code: " + annotation.exitCode() + "! This was forced by the method: " + sourceMethodModule + " in class: " + sourceClassModule);

        codeBuilder.invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"));

        codeBuilder.constantInstruction(annotation.exitCode());
        codeBuilder.invokestatic(ClassDesc.of("java.lang.System"), "exit", MethodTypeDesc.ofDescriptor("(I)V"));

        codeBuilder.labelBinding(stopLabel);
    }
}
