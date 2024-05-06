package de.zonlykroks.compiler.collector.util;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.collector.annotations.Mixin;
import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationDropper;
import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;
import de.zonlykroks.compiler.collector.injectors.AnnotationDropperProcessor;
import de.zonlykroks.compiler.collector.injectors.InterfaceInjectorProcessor;
import org.glavo.classfile.*;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MixinTransformerClass {

    private final Class<?> clazz;
    private final Mixin annotation;
    private ClassModel targetModel;
    private Path targetPath;

    public MixinTransformerClass(Class<?> clazz, Mixin annotation){
        this.clazz = clazz;
        this.annotation = annotation;

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

    private void performTopLevelModifications() throws Exception {
        Annotation[] annotations = this.clazz.getAnnotations();

        for (Annotation annotation : annotations) {
            if(annotation instanceof InterfaceInjector) {
                this.targetModel = InterfaceInjectorProcessor.handleInterfaceInjection(clazz,this.targetPath,this.targetModel);
            }else if(annotation instanceof AnnotationDropper) {
                this.targetModel = AnnotationDropperProcessor.handleAnnotationDrop(clazz,this.targetPath,this.targetModel);
            }
        }
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
