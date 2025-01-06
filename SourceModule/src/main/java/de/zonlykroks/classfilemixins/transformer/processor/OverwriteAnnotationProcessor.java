package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.Overwrite;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ReturnInstruction;

public class OverwriteAnnotationProcessor extends AbstractAnnotationProcessor<Overwrite>{

    @Override
    public ClassModel processAnnotation(Overwrite injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        System.out.println("Clazz: " + sourceClassModel.thisClass().name() +  " , Overwriting: " + injectAnnotation.method() + " , with: " + sourceMethodModule.methodName().stringValue());

        byte[] modified = transform(targetModel, getTransformingMethodBodies(injectAnnotation.method(), new CodeTransform() {
            public TypeKind returnKind;
            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if(codeElement instanceof ReturnInstruction returnInstruction) {
                    returnKind = returnInstruction.typeKind();
                }
            }

            @Override
            public void atEnd(CodeBuilder builder) {
                TransformerUtils.invokeVirtualSourceMethod(builder, targetModel, sourceMethodModule);

                builder.returnInstruction(returnKind);
            }
        }));

        return parse(modified);
    }
}
