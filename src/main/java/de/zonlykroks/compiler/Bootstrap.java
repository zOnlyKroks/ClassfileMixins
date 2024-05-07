package de.zonlykroks.compiler;

import de.zonlykroks.compiler.collector.MixinClassfileCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bootstrap {
    public static final Logger LOGGER = LogManager.getLogger("ClassfileMixin");

    private final List<File> mixinConfigFiles;
    public final List<MixinConfiguration> mixinConfigurations = new ArrayList<>();

    public static final List<String> transformedClasses = new ArrayList<>();

    /**
     * @param mixinConfigPaths the paths to the mixin configuration files
     */
    public Bootstrap(List<Path> mixinConfigPaths) throws IOException {
        this.mixinConfigFiles = mixinConfigPaths.stream().map(path -> {
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File does not exist: " + path);
            }
            return path.toFile();
        }).collect(Collectors.toList());

        parseConfigFiles();

        MixinClassfileCollector mixinClassfileCollector = new MixinClassfileCollector(mixinConfigurations);
        mixinClassfileCollector.populateMixinClassesList();
    }

    public void parseConfigFiles() throws IOException {
        for(File configFile : mixinConfigFiles) {
            LOGGER.info("Parsing mixin configuration file: {}", configFile);

            if(!configFile.getName().contains(".json5")) {
                LOGGER.error("Skipping file: {} (not a mixin configuration file)", configFile);
                continue;
            }

            final String fileContent = Files.readString(configFile.toPath());

            JSONObject jsonObject = new JSONObject(fileContent);

            LOGGER.info("Parsed mixin configuration file: {}, content is: \n{}", configFile, fileContent);

            MixinConfiguration mixinConfiguration = new MixinConfiguration(jsonObject);

            LOGGER.info("Parsed mixin configuration file: {}, scanDir is: {}", configFile, mixinConfiguration.scanDir);

            this.mixinConfigurations.add(mixinConfiguration);
        }
    }

}
