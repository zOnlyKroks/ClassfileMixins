package de.zonlykroks.compiler;

import de.zonlykroks.compiler.delegator.DelegatingClassLoader;
import de.zonlykroks.compiler.scanner.JarFileScanner;
import de.zonlykroks.compiler.scanner.util.MixinAnnotatedClass;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

public class Bootstrap {

    public static URLClassLoader mixinClassLoader;

    public static final List<MixinAnnotatedClass> mixinAnnotatedClasses = new ArrayList<>();

    public Bootstrap(Path bootstrapJarFolder) throws Exception {
        System.out.println("Bootstrapping...");

        List<URL> bootstrapJars = new ArrayList<>();
        List<JarFile> jarFiles = new ArrayList<>();

        Files.walk(bootstrapJarFolder)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".jar"))
                .forEach(path -> {
                    try {
                        System.out.println("Adding " + path.getFileName() + " to classpath");
                        File file = new File(String.valueOf(path));
                        bootstrapJars.add(file.toURL());
                        jarFiles.add(new JarFile(file));
                    } catch (Exception e) {
                        System.out.println("Failed to load class: " + e);
                    }
                });

        mixinClassLoader = new URLClassLoader(bootstrapJars.toArray(new URL[0]), new DelegatingClassLoader());

        JarFileScanner jarFileScanner = new JarFileScanner(jarFiles,this.mixinClassLoader);

        mixinAnnotatedClasses.addAll(jarFileScanner.getTransformerFileNames());

        System.out.println("Collected Mixin Classes: " + Arrays.toString(mixinAnnotatedClasses.toArray()));
    }

    public void finalizeLaunch(String mainClass, String[] args) {
        try {
            System.out.println("Searching for bootstrap class" + mainClass + " in URLs: " + Arrays.toString(mixinClassLoader.getURLs()));

            Class<?> entrypointClass = Class.forName(mainClass.replace(" ", ""), true, this.mixinClassLoader);

            entrypointClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
