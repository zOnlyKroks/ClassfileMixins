package de.zonlykroks.compiler;

import de.zonlykroks.compiler.scanner.JarFileScanner;
import de.zonlykroks.compiler.scanner.util.MixinAnnotatedClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

public class Bootstrap {

    public static final Logger LOGGER = LogManager.getLogger("ClassfileMixin");

    private final URLClassLoader mixinClassLoader;

    public static final List<MixinAnnotatedClass> mixinAnnotatedClasses = new ArrayList<>();

    public Bootstrap(Path bootstrapJarFolder) throws Exception {
        LOGGER.info("Bootstrapping...");

        List<URL> bootstrapJars = new ArrayList<>();
        List<JarFile> jarFiles = new ArrayList<>();

        Files.walk(bootstrapJarFolder)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".jar"))
                .forEach(path -> {
                    try {
                        LOGGER.info("Adding {} to classpath", path.getFileName());
                        bootstrapJars.add(path.toUri().toURL());
                        jarFiles.add(new JarFile(path.toFile()));
                    } catch (Exception e) {
                        LOGGER.error("Failed to load class", e);
                    }
                });

        this.mixinClassLoader = new URLClassLoader(
                bootstrapJars.toArray(new URL[0]),
                getClass().getClassLoader()
        );

        JarFileScanner jarFileScanner = new JarFileScanner(jarFiles,this.mixinClassLoader);

        mixinAnnotatedClasses.addAll(jarFileScanner.getTransformerFileNames());

        LOGGER.info("Collected Mixin Classes: {}", Arrays.toString(mixinAnnotatedClasses.toArray()));
    }

    public void finalizeLaunch(String mainClass, String[] args) {
        try {
            LOGGER.info("Searching for bootstrap class {} in URLs: {}", mainClass, Arrays.toString(mixinClassLoader.getURLs()));

            URL resourceURL = mixinClassLoader.findResource(mainClass);

            Class<?> entrypointClass = URLClassLoader.newInstance(new URL[]{resourceURL}, mixinClassLoader).loadClass(mainClass);
            entrypointClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            LOGGER.error("Failed to bootstrap", e);
        }
    }
}
