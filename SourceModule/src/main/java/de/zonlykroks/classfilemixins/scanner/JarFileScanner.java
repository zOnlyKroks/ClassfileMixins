package de.zonlykroks.classfilemixins.scanner;

import de.zonlykroks.classfilemixins.scanner.util.MixinAnnotatedClass;
import org.glavo.classfile.Attributes;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileScanner {
    private final List<JarFile> jarFiles;
    private final URLClassLoader delegatedClassLoader;

    public JarFileScanner(List<JarFile> jarFiles, URLClassLoader delegatedClassLoader) {
        this.jarFiles = jarFiles;
        this.delegatedClassLoader = delegatedClassLoader;
    }

    public List<MixinAnnotatedClass> getTransformerFileNames() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<List<String>>> futures = new ArrayList<>();

        // Submit tasks to process each JAR file
        for (final JarFile jarFile : jarFiles) {
            Callable<List<String>> task = () -> scanJarForMixinAnnotation(jarFile);
            Future<List<String>> future = executor.submit(task);
            futures.add(future);
        }

        List<String> mixinFiles = new ArrayList<>();

        // Retrieve results from futures
        for (Future<List<String>> future : futures) {
            mixinFiles.addAll(future.get());
        }

        executor.shutdown();

        List<MixinAnnotatedClass> mixinAnnotatedClasses = new ArrayList<>();

        for (String mixinFile : mixinFiles) {
            Class<?> mixinFileClass = this.delegatedClassLoader.loadClass(mixinFile.replace(".class", "").replace("/", "."));

            mixinAnnotatedClasses.add(new MixinAnnotatedClass(mixinFileClass));
        }

        return mixinAnnotatedClasses;
    }

    private List<String> scanJarForMixinAnnotation(JarFile jarFile) throws IOException {
        List<String> mixinFiles = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                // Process the class file
                if (containsMixinAnnotation(jarFile.getInputStream(entry))) {
                    mixinFiles.add(entry.getName());
                }
            }
        }

        return mixinFiles;
    }

    private boolean containsMixinAnnotation(InputStream inputStream) throws IOException {
        byte[] classBytes = inputStream.readAllBytes();

        ClassModel classModel = ClassFile.of().parse(classBytes);

        if(classModel.findAttribute(Attributes.RUNTIME_VISIBLE_ANNOTATIONS).isPresent()) {
            RuntimeVisibleAnnotationsAttribute attribute = classModel.findAttribute(Attributes.RUNTIME_VISIBLE_ANNOTATIONS).get();

            return attribute.annotations().stream().anyMatch(annotation -> annotation.className().stringValue().equalsIgnoreCase("Lde/zonlykroks/classfilemixins/annotations/Mixin;"));
        }

        return false;
    }
}

