package de.zonlykroks.compiler.collector.util;

import java.nio.file.Files;
import java.nio.file.Path;

public interface MixinProcessor {

    static void writeToClassFile(Path targetPath, byte[] content) {
        try {
            Files.write(targetPath, content);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
