package de.zonlykroks.compiler.transformer.processor;

import de.zonlykroks.compiler.annotations.ModifyGetFieldReference;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.FieldInstruction;

public class ModifyGetFieldReferenceAnnotationProcessor extends AbstractAnnotationProcessor<ModifyGetFieldReference>{

    @Override
    public ClassModel processAnnotation(ModifyGetFieldReference modifyReturnValue, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        final String targetMethod = modifyReturnValue.method();
        final String targetField = modifyReturnValue.field();
        final int isnIndex = modifyReturnValue.staticIsnIndex();

        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.transformingMethodBodies(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(targetMethod), new CodeTransform() {
            int currentReturnIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
               if(codeElement instanceof FieldInstruction fieldInstruction) {
                   if(fieldInstruction.opcode() == Opcode.GETSTATIC || fieldInstruction.opcode() == Opcode.GETFIELD) {
                       String fullyQuallifiedIsnField = fieldInstruction.field().owner().name().stringValue() + "." + fieldInstruction.field().name().stringValue() + ":" + fieldInstruction.field().type().stringValue();

                       if(fullyQuallifiedIsnField.equals(targetField) && currentReturnIndex == isnIndex) {
                           TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                       }else {
                           codeBuilder.with(codeElement);
                       }

                       currentReturnIndex++;

                       return;
                   }
               }

               codeBuilder.with(codeElement);
            }
        }));

        return ClassFile.of().parse(modified);
    }
}
