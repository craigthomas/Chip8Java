/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.runner;

import com.beust.jcommander.Parameter;
import ca.craigthomas.chip8java.emulator.components.CentralProcessingUnit;

/**
 * A data class that stores the arguments that may be passed to the emulator.
 */
public class Arguments
{
    @Parameter(description="ROM file")
    public String romFile;

    @Parameter(names={"--scale"}, description="scale factor")
    public Integer scale = 7;

    @Parameter(names={"--delay"}, description="delay factor")
    public Integer delay = (int) CentralProcessingUnit.DEFAULT_CPU_CYCLE_TIME;

    @Parameter(names={"--mem_size_4k"}, description="sets memory size to 4K (defaults to 64K)")
    public Boolean memSize4k = false;

    @Parameter(names={"--color_0"}, description="the hex color to use for the background (default=000000)", arity = 1)
    public String color0 = "000000";

    @Parameter(names={"--color_1"}, description="the hex color to use for bitplane 1 (default=FF33CC)", arity = 1)
    public String color1 = "FF33CC";

    @Parameter(names={"--color_2"}, description="the hex color to use for the bitplane 2 (default=33CCFF)", arity = 1)
    public String color2 = "33CCFF";

    @Parameter(names={"--color_3"}, description="the hex color to use for the bitplane 3 (default=FFFFFF)", arity = 1)
    public String color3 = "FFFFFF";
}