package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.StoreInstruction;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAnnotationProcessor<T extends Annotation> {

    private final ClassFile classFile = ClassFile.of();

    protected final Map<Integer, TypeKind> localVariables = new HashMap<>();

    public abstract ClassModel processAnnotation(T annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule);

    protected ClassTransform getTransformingMethodBodies(String annotationTargetMethodName, CodeTransform codeTransform) {
        localVariables.clear();
        return TransformerUtils.getTransformingMethodBodies(annotationTargetMethodName, codeTransform);
    }

    protected byte[] transform(ClassModel targetModel, ClassTransform classTransform) {
        return classFile.transform(targetModel,classTransform);
    }

    protected ClassModel parse(byte[] modified) {
        return classFile.parse(modified);
    }

    protected void checkIfLocalVariable(CodeElement codeElement) {
        if(codeElement instanceof LoadInstruction loadInstruction) {
            localVariables.put(loadInstruction.slot(), loadInstruction.typeKind());
        }else if(codeElement instanceof StoreInstruction storeInstruction) {
            localVariables.put(storeInstruction.slot(), storeInstruction.typeKind());
        }else if(codeElement instanceof ConstantInstruction constantInstruction) {
            localVariables.put(constantInstruction.opcode().slot(), constantInstruction.typeKind());
        }

        //Remove top of local variable stack
        localVariables.remove(-1);
    }
}
