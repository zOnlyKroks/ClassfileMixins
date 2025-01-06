package de.zonlykroks.classfilemixins;

import org.apache.commons.cli.*;

import java.nio.file.Path;

public class Entrypoint {

    public static void main(String[] args) throws Exception{
        Options options = new Options();

        Option input = new Option("j", "jarPath", true, "Path to the jar files to discombobulate");
        input.setRequired(true);
        options.addOption(input);

        Option mainClass = new Option("m", "mainClass", true, "Path to the main class to discombobulate");
        mainClass.setRequired(true);
        options.addOption(mainClass);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("utility-name", options);
            return;
        }

        String inputFilePath = cmd.getOptionValue("jarPath");
        String mainClassPath = cmd.getOptionValue("mainClass");

        Bootstrap bootstrap = new Bootstrap(Path.of(inputFilePath.replace(" ", "")));

        bootstrap.finalizeLaunch(mainClassPath, args);
    }

}
