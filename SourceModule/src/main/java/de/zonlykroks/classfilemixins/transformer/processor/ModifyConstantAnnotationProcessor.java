package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.ModifyConstant;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;

public class ModifyConstantAnnotationProcessor extends AbstractAnnotationProcessor<ModifyConstant> {

    @Override
    public ClassModel processAnnotation(ModifyConstant injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(injectAnnotation.method(), new CodeTransform() {
            int currentLvIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if (codeElement instanceof ConstantInstruction constantInstruction) {
                    if(currentLvIndex == injectAnnotation.lvIndex()) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, injectAnnotation.captureLocals() ? localVariables : null);
                    }else {
                        codeBuilder.with(codeElement);
                    }
                    currentLvIndex++;
                }else {
                    codeBuilder.with(codeElement);
                }
            }
        }));

        return parse(modified);
    }
}
