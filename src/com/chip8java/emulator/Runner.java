/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import org.apache.commons.cli.*;
import java.util.logging.Logger;

/**
 * The main Emulator class for the Chip 8. The <code>main</code> method will
 * attempt to parse any command line options passed to the emulator.
 * 
 * @author Craig Thomas
 */
public class Runner {

    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Runner.class.getName());
    // The flag for the delay option
    private static final String DELAY_OPTION = "d";
    // The flag for the scale option
    private static final String SCALE_OPTION = "s";
    // The flag for the trace option
    private static final String TRACE_OPTION = "t";
    // The flag for the help option
    private static final String HELP_OPTION = "h";
    // The default scaling option for the emulator
    public static final int SCALE_DEFAULT = 14;

    /**
     * Generates the set of options for the command line option parser.
     * 
     * @return The options for the emulator
     */
    public static Options generateOptions() {
        Options options = new Options();

        @SuppressWarnings("static-access")
        Option delay = OptionBuilder
                .withArgName("delay")
                .hasArg()
                .withDescription(
                        "sets the CPU operation to take at least "
                                + "the specified number of milliseconds to execute "
                                + "(default is 1)").create(DELAY_OPTION);

        @SuppressWarnings("static-access")
        Option scale = OptionBuilder
                .withArgName("scale")
                .hasArg()
                .withDescription(
                        "the scale factor to apply to the display "
                                + "(default is " + SCALE_DEFAULT + ")").create(SCALE_OPTION);

        @SuppressWarnings("static-access")
        Option trace = OptionBuilder.withDescription(
                "starts the CPU in trace mode").create(TRACE_OPTION);

        @SuppressWarnings("static-access")
        Option help = OptionBuilder.withDescription(
                "show this help message and exit").create(HELP_OPTION);

        options.addOption(help);
        options.addOption(delay);
        options.addOption(scale);
        options.addOption(trace);
        return options;
    }

    /**
     * Attempts to parse the command line options.
     * 
     * @param args
     *            The set of arguments provided to the program
     * @return A CommandLine object containing the parsed options
     */
    public static CommandLine parseCommandLineOptions(String[] args) {
        CommandLineParser parser = new BasicParser();
        try {
            return parser.parse(generateOptions(), args);
        } catch (ParseException e) {
            LOGGER.severe("Command line parsing failed.");
            LOGGER.severe(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * Runs the emulator with the specified command line options.
     * 
     * @param argv
     *            The set of options passed to the emulator
     */
    public static void main(String[] argv) {
        CommandLine commandLine = parseCommandLineOptions(argv);
        Emulator.Builder emulatorBuilder = new Emulator.Builder();

        if (commandLine.hasOption(HELP_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("emulator", generateOptions());
            System.exit(0);
        }

        if (commandLine.hasOption(SCALE_OPTION)) {
            int scale = Integer.parseInt(commandLine.getOptionValue("s"));
            emulatorBuilder.setScale(scale);
        }

        String[] args = commandLine.getArgs();
        if (args.length != 0) {
            emulatorBuilder.setRom(args[0]);
        }

        if (commandLine.hasOption(TRACE_OPTION)) {
            emulatorBuilder.setTrace();
        }

        Emulator emulator = emulatorBuilder.build();
        emulator.start();
    }
}
