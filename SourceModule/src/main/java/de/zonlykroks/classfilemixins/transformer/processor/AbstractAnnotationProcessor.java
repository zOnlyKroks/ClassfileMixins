package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;

import java.lang.annotation.Annotation;
import java.lang.classfile.*;
import java.lang.classfile.instruction.*;
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
