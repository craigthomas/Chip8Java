/*
 * Copyright (C) 2013-2018 Craig Thomas
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
}