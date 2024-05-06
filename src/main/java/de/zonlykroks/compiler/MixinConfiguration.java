package de.zonlykroks.compiler;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Path;

public class MixinConfiguration {

    public final String scanDir;

    public MixinConfiguration(JSONObject parsedConfig) {
        this.scanDir = parsedConfig.getString("package").replace("\\", "/");
    }
}
