package de.zonlykroks.compiler.collector.injectors;

import org.glavo.classfile.ClassModel;

import java.nio.file.Path;

public class AnnotationDropperProcessor {

    public static ClassModel handleAnnotationDrop(Class<?> mixinClass, Path targetPath, ClassModel targetModel) {
        return targetModel;
    }

    /*
    public byte[] deleteAnnotations(ClassModel cm) {
            return ClassFile.of().transform(cm, (cb, ce) -> {
                switch (ce) {
                    case MethodModel m -> cb.transformMethod(m, dropMethodAnnos);
                    case FieldModel f -> cb.transformField(f, dropFieldAnnos);
                    default -> cb.with(ce);
                }
            });
        }
     */

}
