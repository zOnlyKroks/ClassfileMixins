package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyStoreInstruction;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.StoreInstruction;

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

                            codeBuilder.storeInstruction(storeInstruction.typeKind(), storeInstruction.slot());
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
