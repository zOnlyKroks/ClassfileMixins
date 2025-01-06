package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.WrapWithCondition;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.StoreInstruction;

import java.util.ArrayList;
import java.util.List;

public class WrapWithConditionAnnotationProcessor extends AbstractAnnotationProcessor<WrapWithCondition> {
    @Override
    public ClassModel processAnnotation(WrapWithCondition annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(annotation.method(), new CodeTransform() {
            final List<Instruction> instructionListSinceLastInvoke = new ArrayList<>();
            int currentInvokeIndex = 0;
            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
               if(codeElement instanceof InvokeInstruction invokeInstruction) {
                   if(currentInvokeIndex == annotation.isnIndex()) {
                       if(invokeInstruction.name().stringValue().equalsIgnoreCase(annotation.invokeIsn())) {
                           Label stopLabel = codeBuilder.newLabel();

                           TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                           codeBuilder.ifeq(stopLabel);

                           for (Instruction instruction : instructionListSinceLastInvoke) {
                               codeBuilder.with(instruction);
                           }

                           codeBuilder.invokevirtual(invokeInstruction.owner().asSymbol(),invokeInstruction.name().stringValue(), invokeInstruction.typeSymbol());

                           codeBuilder.labelBinding(stopLabel);
                       }else {
                           codeBuilder.with(codeElement);
                       }
                   }else {
                          codeBuilder.with(codeElement);
                   }

                   instructionListSinceLastInvoke.clear();
                   currentInvokeIndex++;
                   return;
               }

               if(codeElement instanceof Instruction instruction && currentInvokeIndex == annotation.isnIndex()) {
                   if(instruction instanceof LoadInstruction || instruction instanceof ConstantInstruction || instruction instanceof StoreInstruction) {
                       instructionListSinceLastInvoke.add(instruction);
                   }
               }

               codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }
}
