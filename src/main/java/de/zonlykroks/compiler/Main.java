package de.zonlykroks.compiler;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        new Bootstrap(List.of(
            Path.of("src/main/resources/example_mixin_conf.json5")
        ));
    }

}
