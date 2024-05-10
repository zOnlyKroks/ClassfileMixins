package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.InjectAnnotation;
import de.zonlykroks.compiler.annotations.util.InjectSelector;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.*;

import java.util.HashMap;
import java.util.Map;

public class InjectAnnotationProcessor extends AbstractAnnotationProcessor<InjectAnnotation>{

    private InjectSelector selector;
    private String targetToSelect;
    private boolean captureLocals;

    private final Map<Integer, TypeKind> localVariables = new HashMap<>();

    @Override
    public ClassModel processAnnotation(InjectAnnotation injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        System.out.println("Clazz: " + sourceClassModel.thisClass().name() +  " , Injecting into: " + injectAnnotation.method() + " , from: " + sourceMethodModule.methodName().stringValue());

        selector = injectAnnotation.at().selector();
        targetToSelect = injectAnnotation.at().target();

        captureLocals = injectAnnotation.captureLocals();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(injectAnnotation.method()), new CodeTransform() {
            boolean seenTarget = false;
            int currentTargetIsn = -1;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if(codeElement instanceof LoadInstruction loadInstruction) {
                    localVariables.put(loadInstruction.slot(), loadInstruction.typeKind());
                }else if(codeElement instanceof StoreInstruction storeInstruction) {
                    localVariables.put(storeInstruction.slot(), storeInstruction.typeKind());
                }else if(codeElement instanceof ConstantInstruction constantInstruction) {
                    localVariables.put(constantInstruction.opcode().slot(), constantInstruction.typeKind());
                }


                if(selector == InjectSelector.HEAD && !seenTarget) {
                    if(!injectAnnotation.method().equalsIgnoreCase("<init>")) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
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
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

                        codeBuilder.with(codeElement);
                    }else {
                        if(currentTargetIsn == injectAnnotation.at().index()) {
                            codeBuilder.aload(0);

                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
                        }
                    }

                    currentTargetIsn++;

                    return;
                }

                if(codeElement instanceof LoadInstruction && selector == InjectSelector.LOAD_ISN) {
                    if(injectAnnotation.at().index() < 0) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

                        codeBuilder.with(codeElement);
                    }else {
                        if(currentTargetIsn == injectAnnotation.at().index()) {
                            codeBuilder.aload(0);

                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
                        }
                    }

                    currentTargetIsn++;
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
                TransformerUtils.invokeVirtualSourceMethod(builder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

                builder.with(instruction);

                return;
            }else if(selector == InjectSelector.INVOKE_AFTER) {
                builder.with(instruction);

                TransformerUtils.invokeVirtualSourceMethod(builder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

                return;
            }
        }

        builder.with(instruction);
    }
}
