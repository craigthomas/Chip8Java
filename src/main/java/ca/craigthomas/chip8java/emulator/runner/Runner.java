/*
 * Copyright (C) 2013-2025 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.runner;

import com.beust.jcommander.JCommander;
import ca.craigthomas.chip8java.emulator.components.Emulator;

/**
 * The main Emulator class for the Chip 8. The <code>main</code> method will
 * attempt to parse any command line options passed to the emulator.
 * 
 * @author Craig Thomas
 */
public class Runner
{
    /**
     * Runs the emulator with the specified command line options.
     * 
     * @param argv the set of options passed to the emulator
     */
    public static void main(String[] argv) {
        Arguments args = new Arguments();
        JCommander jCommander = JCommander.newBuilder().addObject(args).build();
        jCommander.setProgramName("yac8e");
        jCommander.parse(argv);

        /* Create the emulator and start it running */
        Emulator emulator = new Emulator(
                args.scale,
                args.maxTicks,
                args.romFile,
                args.memSize4k,
                args.color0,
                args.color1,
                args.color2,
                args.color3,
                args.shiftQuirks,
                args.logicQuirks,
                args.jumpQuirks,
                args.indexQuirks,
                args.clipQuirks
        );
        emulator.start();
    }
}
