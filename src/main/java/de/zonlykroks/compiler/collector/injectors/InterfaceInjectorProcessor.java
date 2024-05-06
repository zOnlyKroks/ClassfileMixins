package de.zonlykroks.compiler.collector.injectors;

import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;
import de.zonlykroks.compiler.collector.util.MixinProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.impl.ClassReaderImpl;
import org.glavo.classfile.impl.MethodInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class InterfaceInjectorProcessor implements MixinProcessor {

    public static ClassModel handleInterfaceInjection(Class<?> mixinClass, Path targetPath, ClassModel targetModel) {
        InterfaceInjector interfaceInjector = mixinClass.getAnnotation(InterfaceInjector.class);

        String[] interfaceDescriptors = interfaceInjector.interfaceDescriptor();

        List<ClassDesc> descList = new ArrayList<>();

        for (String interfaceDescriptor : interfaceDescriptors) {
            ClassDesc desc = ClassDesc.of(interfaceDescriptor);
            descList.add(desc);

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

        for (ClassDesc desc : descList) {
            targetModel = mergeMethods(desc, targetModel, targetPath);
        }

        return targetModel;
    }

    private static ClassModel mergeMethods(ClassDesc toMerge, ClassModel targetModel, Path targetPath) {
        ClassModel mergeModel = loadClassFromDescriptor(toMerge.descriptorString());

        List<MethodModel> mergableMethods = mergeModel.methods();

        for (MethodModel method : mergableMethods) {
            byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> (b, e) -> {
                b.withMethodBody(method.methodName(), method.methodType(), AccessFlag.PUBLIC.mask(), methodBuilder -> {
                    if(method.code().isEmpty()) {
                        methodBuilder.nop();
                        return;
                    }

                    for(CodeElement element : method.code().get().elementList()) {
                        methodBuilder.with(element);
                    }
                });
            }));

            MixinProcessor.writeToClassFile(targetPath, modified);

            targetModel = ClassFile.of().parse(modified);
        }


        return targetModel;
    }

    private static ClassModel loadClassFromDescriptor(String descriptor) {
        try {
            String className = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');

            // Load the class
            Class<?> clazz = Class.forName(className);

            // Get the class bytecode
            byte[] classBytes = getClassBytecode(clazz);

            return ClassFile.of().parse(new ByteArrayInputStream(classBytes).readAllBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load class from descriptor: " + descriptor, e);
        }
    }

    private static byte[] getClassBytecode(Class<?> clazz) throws Exception {
        // Get the path to the class file
        String classFilePath = clazz.getName().replace('.', '/') + ".class";
        Path path = Path.of(ClassLoader.getSystemResource(classFilePath).toURI());

        // Read the bytecode from the class file
        return Files.readAllBytes(path);
    }
}
