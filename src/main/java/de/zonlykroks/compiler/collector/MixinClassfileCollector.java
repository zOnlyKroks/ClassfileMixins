package de.zonlykroks.compiler.collector;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.MixinConfiguration;
import de.zonlykroks.compiler.collector.annotations.Mixin;
import de.zonlykroks.compiler.collector.util.MixinTransformerClass;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MixinClassfileCollector {

    private final List<String> scanDirs = new ArrayList<>();
    public final List<MixinTransformerClass> mixinClasses = new ArrayList<>();

    public MixinClassfileCollector(List<MixinConfiguration> scanDirs) {
        for (MixinConfiguration mixinConfiguration : scanDirs) {
            this.scanDirs.add(mixinConfiguration.scanDir);
        }
    }

    public void populateMixinClassesList() {
        for(String path : scanDirs) {
            path = "src/main/java/" + path.replace(".", "/");

            Bootstrap.LOGGER.info("Scanning path: {}", path);

            try {
                Files.walk(Path.of(path)).forEach(filePath -> {
                    if(filePath.toString().endsWith(".java")) {
                        try {
                            String sanitizedPath = filePath.toString().replace("\\", "/").replace("src/main/java/", "").replace("/", ".").replace(".java", "");

                            Class<?> mixinTransformerClazz = Class.forName(sanitizedPath, false , Thread.currentThread().getContextClassLoader());

                            if(mixinTransformerClazz.isAnnotationPresent(Mixin.class)) {
                                Bootstrap.LOGGER.info("Found mixin transformer class: {}", sanitizedPath);
                                Mixin annotation = mixinTransformerClazz.getAnnotation(Mixin.class);
                                mixinClasses.add(new MixinTransformerClass(mixinTransformerClazz, annotation));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
