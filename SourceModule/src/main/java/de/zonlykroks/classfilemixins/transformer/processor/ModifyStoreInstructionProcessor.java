package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.ModifyStoreInstruction;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import java.lang.classfile.*;
import java.lang.classfile.instruction.*;

public class ModifyStoreInstructionProcessor extends AbstractAnnotationProcessor<ModifyStoreInstruction>{
    @Override
    public ClassModel processAnnotation(ModifyStoreInstruction annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(annotation.method(), new CodeTransform() {
            int currentIsnIndex = 0;
            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if(codeElement instanceof StoreInstruction storeInstruction) {
                    if(storeInstruction.opcode() == Opcode.valueOf(annotation.storeOpCode())) {
                        if(currentIsnIndex == annotation.staticIsnIndex()) {
                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, annotation.captureLocals() ? localVariables : null);

                            codeBuilder.storeLocal(storeInstruction.typeKind(), storeInstruction.slot());
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
