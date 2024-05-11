package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyReturnValue;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
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
