/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.chip8java.emulator.Screen;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The main Emulator class for the Chip 8. The <code>main</code> method will
 * attempt to parse any command line options passed to the emulator.
 * 
 * @author Craig Thomas
 */
public class Emulator {

    // The font file for the Chip 8
    private static final String FONT_FILE = "src/resources/FONTS.chip8";
    // The flag for the delay option
    private static final String DELAY_OPTION = "d";
    // The flag for the scale option
    private static final String SCALE_OPTION = "s";
    // The flag for the trace option
    private static final String TRACE_OPTION = "t";
    // The flag for the help option
    private static final String HELP_OPTION = "h";

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
                                + "(default is 14)").create(SCALE_OPTION);

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
            System.err.println("Error: Command line parsing failed.");
            System.err.println("Reason: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("emulator", generateOptions());
            System.exit(1);
        }
        return null;
    }

    /**
     * Runs the emulator with the specified command line options.
     * 
     * @param argv
     *            The set of options passed to the emulator
     * @throws FileNotFoundException
     * @throws FontFormatException
     * @throws IOException
     */
    public static void main(String[] argv) throws FileNotFoundException,
            FontFormatException, IOException {

        Screen screen;
        Keyboard keyboard = new Keyboard();
        CommandLine commandLine = parseCommandLineOptions(argv);
        Memory memory = new Memory(Memory.MEMORY_4K);

        // Load the Chip 8 font file
        if (!memory.loadRomIntoMemory(FONT_FILE, 0)) {
            System.out.println("Error: could not load font file");
            return;
        }

        // Make sure a ROM filename was specified
        String[] args = commandLine.getArgs();
        if (args.length == 0) {
            System.out.println("Error: No rom file specified");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("emulator", generateOptions());
            return;
        }

        // Attempt to load ROM into memory
        if (!memory.loadRomIntoMemory(args[0],
                CentralProcessingUnit.PROGRAM_COUNTER_START)) {
            System.out.println("Error: could not load file [" + args[0] + "]");
            return;
        }

        // Check for the help switch
        if (commandLine.hasOption(HELP_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("emulator", generateOptions());
            return;
        }

        // Check for the scale switch
        if (commandLine.hasOption(SCALE_OPTION)) {
            Integer scale = Integer.parseInt(commandLine.getOptionValue("s"));
            screen = new Screen(scale);
        } else {
            screen = new Screen();
        }

        CentralProcessingUnit cpu = new CentralProcessingUnit(memory, keyboard,
                screen);

        // Check for CPU trace option
        if (commandLine.hasOption(TRACE_OPTION)) {
            cpu.setTrace(true);
        }

        // Begin the main execution loop
        try {
            cpu.execute();
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }
}
