package de.zonlykroks.compiler.transformer.processor;

import org.glavo.classfile.*;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.StoreInstruction;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractAnnotationProcessor<T extends Annotation> {

    private final ClassFile classFile = ClassFile.of();

    protected final Map<Integer, TypeKind> localVariables = new HashMap<>();

    public abstract ClassModel processAnnotation(T annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule);

    public ClassTransform getTransformingMethodBodies(String annotationTargetMethodName, CodeTransform codeTransform) {
        localVariables.clear();
        return ClassTransform.transformingMethodBodies(methodModel -> getMethodModelPredicate(methodModel, annotationTargetMethodName), codeTransform);
    }

    private boolean getMethodModelPredicate(MethodModel targetMethodModel, String annotationTargetMethodName) {
        return targetMethodModel.methodName().stringValue().equalsIgnoreCase(annotationTargetMethodName);
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
