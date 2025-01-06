package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.InjectAnnotation;
import de.zonlykroks.classfilemixins.annotations.util.InjectSelector;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.*;

public class InjectAnnotationProcessor extends AbstractAnnotationProcessor<InjectAnnotation>{

    private InjectSelector selector;
    private String targetToSelect;
    private boolean captureLocals;
    private boolean modifyAll;

    @Override
    public ClassModel processAnnotation(InjectAnnotation injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        selector = injectAnnotation.at().selector();
        targetToSelect = injectAnnotation.at().target();

        captureLocals = injectAnnotation.captureLocals();

        modifyAll = injectAnnotation.at().index() < 0;

        byte[] modified = transform(targetModel, getTransformingMethodBodies(injectAnnotation.method(), new CodeTransform() {
            boolean seenTarget = false;
            int currentTargetIsn = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if(selector == InjectSelector.HEAD && !seenTarget) {
                    if(!injectAnnotation.method().equalsIgnoreCase("<init>")) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
                    }else {
                        System.out.println("Injecting into <init> at HEAD not allowed! You should probably use TAIL!");
                    }

                    seenTarget = true;
                }

                if(codeElement instanceof InvokeInstruction invokeInstruction) {
                    String fullyQuallifiedTarget = invokeInstruction.owner().name().stringValue() + "." + invokeInstruction.name().stringValue() + ":" + invokeInstruction.type().stringValue();

                    if(fullyQuallifiedTarget.equalsIgnoreCase(targetToSelect) && currentTargetIsn == injectAnnotation.at().index()) {
                        if(selector == InjectSelector.INVOKE_BEFORE) {
                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

                            codeBuilder.with(codeElement);
                        }else if(selector == InjectSelector.INVOKE_AFTER) {
                            codeBuilder.with(codeElement);

                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
                        }
                    }else {
                        codeBuilder.with(codeElement);
                    }

                    currentTargetIsn++;
                    return;
                }

                if(codeElement instanceof ReturnInstruction && selector == InjectSelector.RETURN) {
                    transformCheckModifyAll(codeBuilder, targetModel,sourceMethodModule, codeElement, currentTargetIsn == injectAnnotation.at().index());

                    currentTargetIsn++;
                    return;
                }

                if(codeElement instanceof LoadInstruction && selector == InjectSelector.LOAD_ISN) {
                    transformCheckModifyAll(codeBuilder, targetModel,sourceMethodModule, codeElement, currentTargetIsn == injectAnnotation.at().index());

                    currentTargetIsn++;
                    return;
                }

                //Elements with no explicit support yet / not needed;
                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }

    public void transformCheckModifyAll(CodeBuilder codeBuilder, ClassModel targetModel, MethodModel sourceMethodModule, CodeElement codeElement, boolean condition) {
        if(modifyAll) {
            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);

            codeBuilder.with(codeElement);
        }else {
            if(condition) {
                codeBuilder.aload(0);

                TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, captureLocals ? localVariables : null);
            }
        }
    }
}
