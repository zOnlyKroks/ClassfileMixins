package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyConstant;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;

public class ModifyConstantAnnotationProcessor extends AbstractAnnotationProcessor<ModifyConstant> {

    @Override
    public ClassModel processAnnotation(ModifyConstant injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        final String targetMethod = injectAnnotation.method();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(targetMethod), new CodeTransform() {
            int currentLvIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if (codeElement instanceof ConstantInstruction) {
                    if(currentLvIndex == injectAnnotation.lvIndex()) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                    }else {
                        codeBuilder.with(codeElement);
                    }
                    currentLvIndex++;
                }else {
                    codeBuilder.with(codeElement);
                }
            }
        }));

        return ClassFile.of().parse(modified);
    }
}
