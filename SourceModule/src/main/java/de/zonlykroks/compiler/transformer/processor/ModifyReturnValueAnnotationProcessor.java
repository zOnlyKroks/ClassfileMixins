package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyReturnValue;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.lang.constant.ClassDesc;

public class ModifyReturnValueAnnotationProcessor {

    public static ClassModel processModifyReturnValueAnnotation(ModifyReturnValue modifyReturnValueAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        final String targetMethod = modifyReturnValueAnnotation.method();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(targetMethod), new CodeTransform() {
            int currentReturnIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if (codeElement instanceof ReturnInstruction) {
                    if(currentReturnIndex == modifyReturnValueAnnotation.returnIndex()) {
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);

                        codeBuilder.with(codeElement);
                    } else {
                        codeBuilder.with(codeElement);
                    }

                    currentReturnIndex++;
                } else {
                    codeBuilder.with(codeElement);
                }
            }
        }));

        return ClassFile.of().parse(modified);
    }

}
