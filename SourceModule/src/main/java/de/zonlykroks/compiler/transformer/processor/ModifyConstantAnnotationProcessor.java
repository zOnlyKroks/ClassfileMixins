package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyConstant;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;

import java.lang.constant.ClassDesc;
import java.util.concurrent.atomic.AtomicInteger;

public class ModifyConstantAnnotationProcessor {

    public static ClassModel processModifyConstantAnnotation(ModifyConstant injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        final String targetMethod = injectAnnotation.method();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(targetMethod), new CodeTransform() {
            int currentLvIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if (codeElement instanceof ConstantInstruction constantInstruction) {
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
