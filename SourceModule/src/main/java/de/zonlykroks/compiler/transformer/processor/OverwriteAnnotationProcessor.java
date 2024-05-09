package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.Overwrite;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;

public class OverwriteAnnotationProcessor extends AbstractAnnotationProcessor<Overwrite>{

    @Override
    public ClassModel processAnnotation(Overwrite injectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        System.out.println("Clazz: " + sourceClassModel.thisClass().name() +  " , Overwriting: " + injectAnnotation.method() + " , with: " + sourceMethodModule.methodName().stringValue());

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(injectAnnotation.method()), new CodeTransform() {
            boolean seen = false;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                if(!seen) {
                    TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);

                    codeBuilder.return_();

                    seen = true;
                }
            }
        }));

        return ClassFile.of().parse(modified);
    }
}
