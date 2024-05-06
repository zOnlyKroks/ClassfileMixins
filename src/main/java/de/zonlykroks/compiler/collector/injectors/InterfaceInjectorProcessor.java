package de.zonlykroks.compiler.collector.injectors;

import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;
import de.zonlykroks.compiler.collector.util.MixinProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;

import java.io.ByteArrayInputStream;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class InterfaceInjectorProcessor implements MixinProcessor {

    public static ClassModel handleInterfaceInjection(Class<?> mixinClass, Path targetPath, ClassModel targetModel) {
        InterfaceInjector interfaceInjector = mixinClass.getAnnotation(InterfaceInjector.class);

        String[] interfaceDescriptors = interfaceInjector.interfaceDescriptor();

        for (String interfaceDescriptor : interfaceDescriptors) {
            ClassDesc desc = ClassDesc.of(interfaceDescriptor);

            byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
                boolean seen = false;

                @Override
                public void accept(ClassBuilder builder, ClassElement element) {
                    if (element instanceof Interfaces i) {
                        List<ClassEntry> interfaces = Stream.concat(i.interfaces().stream(),
                                        Stream.of(builder.constantPool().classEntry(desc)))
                                .distinct()
                                .toList();
                        builder.withInterfaces(interfaces);

                        seen = true;
                    } else {
                        builder.with(element);
                    }
                }

                @Override
                public void atEnd(ClassBuilder builder) {
                    if (!seen)
                        builder.withInterfaceSymbols(desc);
                }
            }));

            MixinProcessor.writeToClassFile(targetPath,modified);

            targetModel = ClassFile.of().parse(modified);
        }

        return targetModel;
    }
}
