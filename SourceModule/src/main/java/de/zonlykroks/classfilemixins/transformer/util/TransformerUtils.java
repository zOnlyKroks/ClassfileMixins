package de.zonlykroks.classfilemixins.transformer.util;

import org.glavo.classfile.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.constant.ClassDesc;
import java.util.Map;

public class TransformerUtils {

    public static void invokeVirtualSourceMethod(@Nonnull CodeBuilder builder, @Nonnull ClassModel targetModel, @Nonnull MethodModel sourceMethodModule) {
        boolean isStatic = sourceMethodModule.flags().has(AccessFlag.STATIC);

        if (!isStatic) {
            builder.aload(0);
        }

        builder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());
    }

    public static void invokeVirtualSourceMethod(@Nonnull CodeBuilder builder, @Nonnull ClassModel targetModel, @Nonnull MethodModel sourceMethodModule, @Nullable Map<Integer, TypeKind> localVariables) {
        if(localVariables == null) {
            invokeVirtualSourceMethod(builder, targetModel, sourceMethodModule);
            return;
        }

        boolean isStatic = sourceMethodModule.flags().has(AccessFlag.STATIC);

        if(!isStatic) {
            builder.aload(0);
        }

        for (Map.Entry<Integer, TypeKind> entry : localVariables.entrySet()) {
            switch (entry.getValue()) {
                case IntType -> builder.iload(entry.getKey());
                case LongType -> builder.lload(entry.getKey());
                case FloatType -> builder.fload(entry.getKey());
                case DoubleType -> builder.dload(entry.getKey());
                case ReferenceType -> builder.aload(entry.getKey());
                default -> builder.loadInstruction(entry.getValue(), entry.getKey());
            }
        }

        builder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());
    }

    public static ClassTransform getTransformingMethodBodies(String annotationTargetMethodName, CodeTransform codeTransform) {
        return ClassTransform.transformingMethodBodies(methodModel -> getMethodModelPredicate(methodModel, annotationTargetMethodName), codeTransform);
    }

    private static boolean getMethodModelPredicate(MethodModel targetMethodModel, String annotationTargetMethodName) {
        return targetMethodModel.methodName().stringValue().equalsIgnoreCase(annotationTargetMethodName);
    }
}
