package de.zonlykroks.compiler.collector.injectors;

import de.zonlykroks.compiler.collector.util.MixinProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.nio.file.Path;

public class AnnotationDropperProcessor implements MixinProcessor {

    public static ClassModel handleAnnotationDrop(Class<?> mixinClass, Path targetPath, ClassModel targetModel) {
        byte[] modified = ClassFile.of().transform(targetModel, (cb, ce) -> {
            switch (ce) {
                case MethodModel m -> cb.transformMethod(m, dropMethodAnnos);
                case FieldModel f -> cb.transformField(f, dropFieldAnnos);
                default -> cb.with(ce);
            }
        });

        MixinProcessor.writeToClassFile(targetPath, modified);

        return ClassFile.of().parse(modified);
    }

    static MethodTransform dropMethodAnnos = (mb, me) -> {
        if (!(me instanceof RuntimeVisibleAnnotationsAttribute || me instanceof RuntimeInvisibleAnnotationsAttribute))
            mb.with(me);
    };

    static FieldTransform dropFieldAnnos = (fb, fe) -> {
        if (!(fe instanceof RuntimeVisibleAnnotationsAttribute || fe instanceof RuntimeInvisibleAnnotationsAttribute))
            fb.with(fe);
    };
}
