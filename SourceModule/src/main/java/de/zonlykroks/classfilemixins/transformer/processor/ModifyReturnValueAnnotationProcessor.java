package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.ModifyReturnValue;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ReturnInstruction;

public class ModifyReturnValueAnnotationProcessor extends AbstractAnnotationProcessor<ModifyReturnValue>{

    @Override
    public ClassModel processAnnotation(ModifyReturnValue modifyReturnValue, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(modifyReturnValue.method(), new CodeTransform() {
            int currentReturnIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if (codeElement instanceof ReturnInstruction returnInstruction) {
                    if(currentReturnIndex == modifyReturnValue.returnIndex()) {
                        codeBuilder.pop();

                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, modifyReturnValue.captureLocals() ? localVariables : null);

                        codeBuilder.returnInstruction(returnInstruction.typeKind());
                    } else {
                        codeBuilder.with(codeElement);
                    }

                    currentReturnIndex++;
                    return;
                }

                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }
}
