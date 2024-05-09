package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.InjectAnnotation;
import de.zonlykroks.compiler.annotations.util.InjectSelector;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.lang.constant.ClassDesc;


public class InjectAnnotationProcessor {

    private static InjectSelector selector;
    private static String targetToSelect;

    public static ClassModel processInjectAnnotation(InjectAnnotation injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        System.out.println("Clazz: " + sourceClassModel.thisClass().name() +  " , Injecting into: " + injectAnnotation.method() + " , from: " + sourceMethodModule.methodName().stringValue());

        selector = injectAnnotation.at().selector();
        targetToSelect = injectAnnotation.at().target();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(injectAnnotation.method()), new CodeTransform() {
            boolean seenTarget = false;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if(selector == InjectSelector.HEAD && !seenTarget) {
                    TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                    seenTarget = true;
                }

                if(codeElement instanceof InvokeInstruction invokeInstruction) {
                    handleInvokeInstruction(codeBuilder, invokeInstruction,targetModel,sourceMethodModule);
                    return;
                }

                if(codeElement instanceof ReturnInstruction && selector == InjectSelector.TAIL) {
                    codeBuilder.aload(0);

                    codeBuilder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());
                }

                codeBuilder.with(codeElement);
            }
        }));

        return ClassFile.of().parse(modified);
    }

    private static void handleInvokeInstruction(CodeBuilder builder, InvokeInstruction instruction, ClassModel targetModel, MethodModel sourceMethodModule) {
        if(instruction.name().stringValue().equalsIgnoreCase(targetToSelect)) {
            if(selector == InjectSelector.INVOKE_BEFORE) {
                builder.aload(0);

                builder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());

                builder.with(instruction);

                return;
            }else if(selector == InjectSelector.INVOKE_AFTER) {
                builder.with(instruction);

                builder.aload(0);

                builder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());

                return;
            }
        }

        builder.with(instruction);
    }

}
