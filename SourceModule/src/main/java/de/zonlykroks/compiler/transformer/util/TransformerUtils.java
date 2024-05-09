package de.zonlykroks.compiler.transformer.util;

import org.glavo.classfile.*;

import java.lang.constant.ClassDesc;

public class TransformerUtils {

    public static void invokeVirtualSourceMethod(CodeBuilder builder, ClassModel targetModel, MethodModel sourceMethodModule) {
        boolean isStatic = sourceMethodModule.flags().has(AccessFlag.STATIC);

        if(isStatic) {
            builder.invokestatic(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());
        }else {
            builder.aload(0);

            builder.invokevirtual(ClassDesc.of(targetModel.thisClass().name().stringValue().replace("/", ".")), sourceMethodModule.methodName().stringValue(), sourceMethodModule.methodTypeSymbol());
        }
    }

}
