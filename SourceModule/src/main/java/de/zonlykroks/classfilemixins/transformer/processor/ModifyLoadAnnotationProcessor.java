package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.ModifyLoadInstruction;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import java.lang.classfile.*;
import java.lang.classfile.instruction.*;


public class ModifyLoadAnnotationProcessor extends AbstractAnnotationProcessor<ModifyLoadInstruction> {

    @Override
    public ClassModel processAnnotation(ModifyLoadInstruction annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(annotation.method(), new CodeTransform() {
            int currentIsnIndex = 0;
            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if(codeElement instanceof LoadInstruction loadInstruction) {
                    if(loadInstruction.opcode() == Opcode.valueOf(annotation.loadOpCode())) {
                        if(currentIsnIndex == annotation.staticIsnIndex()) {
                            int slot = codeBuilder.allocateLocal(loadInstruction.typeKind());

                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, annotation.captureLocals() ? localVariables : null);

                            codeBuilder.storeLocal(loadInstruction.typeKind(), slot);

                            codeBuilder.loadLocal(loadInstruction.typeKind(), slot);
                        }else {
                            codeBuilder.with(codeElement);
                        }

                        currentIsnIndex++;
                        return;
                    }
                }

                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }
}
