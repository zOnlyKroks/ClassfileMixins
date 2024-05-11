package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.Redirect;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.InvokeInstruction;

public class RedirectAnnotationProcessor extends AbstractAnnotationProcessor<Redirect> {
    @Override
    public ClassModel processAnnotation(Redirect redirectAnnotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(redirectAnnotation.method(), new CodeTransform() {
            int currentInvokeTarget = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                checkIfLocalVariable(codeElement);

                if(codeElement instanceof InvokeInstruction invokeInstruction) {
                    String fullyQuallifiedTarget = invokeInstruction.owner().name().stringValue() + "." + invokeInstruction.name().stringValue() + ":" + invokeInstruction.type().stringValue();

                    if(fullyQuallifiedTarget.equalsIgnoreCase(redirectAnnotation.target())) {

                        if(currentInvokeTarget == redirectAnnotation.methodIndex()) {
                            TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule, redirectAnnotation.captureLocals() ? localVariables : null);
                        }else {
                            codeBuilder.with(codeElement);
                        }

                        currentInvokeTarget++;
                        return;
                    }
                }

                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }
}
