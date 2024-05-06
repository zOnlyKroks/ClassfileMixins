package de.zonlykroks.compiler.collector.injectors;

import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationAdder;
import de.zonlykroks.compiler.collector.util.MixinProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.lang.constant.ClassDesc;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AnnotationAdderProcessor implements MixinProcessor {

    public static ClassModel handleAnnotationAdd(Class<?> mixinClass, Path targetPath, ClassModel targetModel) {
        AnnotationAdder anno = mixinClass.getAnnotation(AnnotationAdder.class);

        final String[] annotations = anno.annotationDescriptions();

        AtomicReference<ClassModel> currentModel = new AtomicReference<>(targetModel);

        for(String annotation : annotations) {
            currentModel.get().findAttribute(Attributes.RUNTIME_VISIBLE_ANNOTATIONS).ifPresentOrElse(
                    runtimeVisibleAnnotationsAttribute -> {
                        List<Annotation> modifiedAnnoList = appendOne(runtimeVisibleAnnotationsAttribute.annotations(), Annotation.of(ClassDesc.of(annotation)));

                        byte[] modified = ClassFile.of().transform(currentModel.get(), ClassTransform.endHandler(cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(modifiedAnnoList))));

                        MixinProcessor.writeToClassFile(targetPath, modified);

                        currentModel.set(ClassFile.of().parse(modified));
                    },
                    () -> {
                        // Create a new RuntimeVisibleAnnotationsAttribute and add the annotation
                        List<Annotation> annotationsList = new ArrayList<>();
                        annotationsList.add(Annotation.of(ClassDesc.of(annotation)));

                        byte[] modified = ClassFile.of().transform(currentModel.get(), ClassTransform.endHandler(cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(annotationsList))));

                        MixinProcessor.writeToClassFile(targetPath, modified);

                        currentModel.set(ClassFile.of().parse(modified));
                    }
            );
        }

        return currentModel.get();
    }

    public static <T> List<T> appendOne(List<T> al, T t) {
        List<T> bl = new ArrayList<>(al);
        bl.add(t);
        return Collections.unmodifiableList(bl);
    }
}
