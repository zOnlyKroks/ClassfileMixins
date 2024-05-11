package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyLoadInstruction;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.LoadInstruction;


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

                            codeBuilder.storeInstruction(loadInstruction.typeKind(), slot);

                            codeBuilder.loadInstruction(loadInstruction.typeKind(), slot);
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
