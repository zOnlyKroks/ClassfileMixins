package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.InjectAnnotation;
import de.zonlykroks.compiler.annotations.util.InjectSelector;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.lang.constant.ClassDesc;
import java.util.Optional;


public class InjectAnnotationProcessor extends AbstractAnnotationProcessor<InjectAnnotation>{

    private InjectSelector selector;
    private String targetToSelect;

    @Override
    public ClassModel processAnnotation(InjectAnnotation injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        System.out.println("Clazz: " + sourceClassModel.thisClass().name() +  " , Injecting into: " + injectAnnotation.method() + " , from: " + sourceMethodModule.methodName().stringValue());

        selector = injectAnnotation.at().selector();
        targetToSelect = injectAnnotation.at().target();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(injectAnnotation.method()), new CodeTransform() {
            boolean seenTarget = false;
            int currentReturnIsn = -1;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if(selector == InjectSelector.HEAD && !seenTarget) {
                    if(!injectAnnotation.method().equalsIgnoreCase("<init>")) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                    }else {
                        System.out.println("Injecting into <init> at HEAD not allowed! You should probably use TAIL!");
                    }

                    seenTarget = true;
                }
                if(codeElement instanceof InvokeInstruction invokeInstruction) {
                    handleInvokeInstruction(codeBuilder, invokeInstruction,targetModel,sourceMethodModule);
                    return;
                }

                if(codeElement instanceof ReturnInstruction && selector == InjectSelector.RETURN) {
                    if(injectAnnotation.at().index() < 0) {
                        codeBuilder.aload(0);

                        codeBuilder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());

                        codeBuilder.with(codeElement);
                    }else {
                        if(currentReturnIsn == injectAnnotation.at().index()) {
                            codeBuilder.aload(0);

                            codeBuilder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());

                            codeBuilder.with(codeElement);
                        }
                    }

                    currentReturnIsn++;

                    return;
                }

                //Elements with no explicit support yet / not needed;
                codeBuilder.with(codeElement);
            }
        }));

        return ClassFile.of().parse(modified);
    }

    private void handleInvokeInstruction(CodeBuilder builder, InvokeInstruction instruction, ClassModel targetModel, MethodModel sourceMethodModule) {
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
