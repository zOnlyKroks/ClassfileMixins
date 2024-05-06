package de.zonlykroks.compiler.collector.util;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.collector.annotations.Mixin;
import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationAdder;
import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationDropper;
import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;
import de.zonlykroks.compiler.collector.injectors.AnnotationAdderProcessor;
import de.zonlykroks.compiler.collector.injectors.AnnotationDropperProcessor;
import de.zonlykroks.compiler.collector.injectors.InterfaceInjectorProcessor;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MixinTransformerClass {

    private final Class<?> clazz;
    private final Mixin annotation;
    private ClassModel mixinModel;
    private ClassModel targetModel;
    private Path targetPath;

    public MixinTransformerClass(Class<?> clazz, Mixin annotation){
        this.clazz = clazz;
        this.annotation = annotation;

        try {
            this.mixinModel = ClassFile.of().parse(new ByteArrayInputStream(getClassBytecode(clazz)).readAllBytes());
        }catch (Exception e) {
            e.printStackTrace();
        }

        collectTarget();
    }

    private void collectTarget(){
        this.targetPath = Path.of(annotation.target());

        try {
            loadClassFile();
        }catch (Exception e) {
            Bootstrap.LOGGER.error("Failed to load target class file: {}", this.targetPath);
            e.printStackTrace();
        }

        try {
            performTopLevelModifications();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performTopLevelModifications() {
        Annotation[] annotations = this.clazz.getAnnotations();

        for(ClassEntry interfaceEntry : this.mixinModel.interfaces()) {
            Bootstrap.LOGGER.info("Interface: {}", interfaceEntry);

            this.targetModel = mergeInterfaceMethods(ClassDesc.of(interfaceEntry.name().stringValue().replace("/", ".")), this.targetModel, this.targetPath);
        }

        Bootstrap.LOGGER.info("Merged all interface methods!");

        for(MethodModel model : this.mixinModel.methods()) {
            Bootstrap.LOGGER.info("Method: {}", model.methodName());

            //TODO: Check if init and duplicate methods are present, dont merge those

            this.targetModel = mergeMethods(model, this.targetModel, this.targetPath);
        }

        for (Annotation annotation : annotations) {
            if(annotation instanceof InterfaceInjector) {
                this.targetModel = InterfaceInjectorProcessor.handleInterfaceInjection(clazz,this.targetPath,this.targetModel);
            }else if(annotation instanceof AnnotationDropper) {
                this.targetModel = AnnotationDropperProcessor.handleAnnotationDrop(clazz,this.targetPath,this.targetModel);
            }else if(annotation instanceof AnnotationAdder) {
                this.targetModel = AnnotationAdderProcessor.handleAnnotationAdd(clazz,this.targetPath,this.targetModel);
            }
        }
    }

    private ClassModel mergeMethods(MethodModel model, ClassModel targetModel, Path targetPath) {
        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> ClassTransform.endHandler( (b) -> {
            b.withMethodBody(model.methodName(), model.methodType(), AccessFlag.PUBLIC.mask(), methodBuilder -> {
                if(model.code().isEmpty()) {
                    methodBuilder.nop();
                    return;
                }

                for(CodeElement element : model.code().get().elementList()) {
                    methodBuilder.with(element);
                }
            });
        })));

        MixinProcessor.writeToClassFile(targetPath, modified);

        return ClassFile.of().parse(modified);
    }

    private ClassModel mergeInterfaceMethods(ClassDesc toMerge, ClassModel targetModel, Path targetPath) {
        ClassModel mergeModel = loadClassFromDescriptor(toMerge.descriptorString());

        List<MethodModel> mergableMethods = mergeModel.methods();

        for (MethodModel method : mergableMethods) {
            byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> ClassTransform.endHandler( (b) -> {
                b.withMethodBody(method.methodName(), method.methodType(), AccessFlag.PUBLIC.mask(), methodBuilder -> {
                    if(method.code().isEmpty()) {
                        methodBuilder.nop();
                        return;
                    }

                    for(CodeElement element : method.code().get().elementList()) {
                        methodBuilder.with(element);
                    }
                });
            })));

            MixinProcessor.writeToClassFile(targetPath, modified);

            targetModel = ClassFile.of().parse(modified);
        }

        return targetModel;
    }

    private static ClassModel loadClassFromDescriptor(String descriptor) {
        try {
            String className = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');

            // Load the class
            Class<?> clazz = Class.forName(className, false, ClassLoader.getSystemClassLoader());

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

    private void loadClassFile() throws Exception {
        String sanitizedPath = this.targetPath.toString().replace("\\", "/").replace("src/main/java/", "").replace("/", ".").replace(".java", "").replaceFirst(".", "");

        Bootstrap.LOGGER.info("Found mixin transformer target class: {}", sanitizedPath);

        Class<?> mixinTransformerClazz = Class.forName(sanitizedPath, false, Thread.currentThread().getContextClassLoader());

        ClassLoader classLoader = mixinTransformerClazz.getClassLoader();

        String className = mixinTransformerClazz.getName().replace('.', '/') + ".class";
        URL resource = classLoader.getResource(className);

        if (resource != null) {
            Path fullPath = Paths.get(resource.toURI());
            Bootstrap.LOGGER.info("Full path of the target class file: {}", fullPath);

            this.targetPath = fullPath;
            this.targetModel = ClassFile.of().parse(this.targetPath);
        } else {
            Bootstrap.LOGGER.error("Could not determine the path of the target class file.");
        }
    }
}
