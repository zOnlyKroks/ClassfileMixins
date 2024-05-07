package de.zonlykroks.compiler;

import de.zonlykroks.compiler.lookhere.TransformMe;

import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        new Bootstrap(List.of(
            Path.of("src/main/resources/example_mixin_conf.json5")
        ));

        new TransformMe();
    }

}
